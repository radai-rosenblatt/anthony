/*
 *     Copyright (C) 2018 Radai Rosenblatt (radai.rosenblatt@gmail.com)
 *     
 *     This file is part of Anthony.
 *
 *     Anthony is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 */

package net.radai.anthony;

import net.radai.anthony.dns.DnsUpdater;
import net.radai.anthony.dns.GoDaddyDnsUpdater;
import net.radai.anthony.notifications.MailgunNotificationSender;
import net.radai.anthony.notifications.NopNotificationSender;
import net.radai.anthony.notifications.Notification;
import net.radai.anthony.notifications.NotificationSender;
import net.radai.anthony.publicip.MajorityVoteIpFinder;
import net.radai.anthony.publicip.PublicIp;
import net.radai.anthony.publicip.PublicIpFinder;
import net.radai.anthony.publicip.provider.ICanHazIp;
import net.radai.anthony.publicip.provider.WhatIp;
import net.radai.anthony.publicip.provider.WhatIsMyIp4;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Main {
    private final static Logger LOG = LogManager.getLogger(Main.class);
    
    public static void main(String[] args) throws Exception {
        LOG.info("starting up");
        Clock clock = Clock.systemUTC();
        Instant bootTime = clock.instant();
        Config config = findConfig();
        PublicIpFinder ipFinder = new MajorityVoteIpFinder(
                new ICanHazIp(),
                new WhatIp(),
                new WhatIsMyIp4()
        );
        DnsUpdater updater = new GoDaddyDnsUpdater(config.getGodaddyKey(), config.getGodaddySecret());
        NotificationSender notifier = new NopNotificationSender();
        if (config.getNotificationDomain() != null) {
            notifier = new MailgunNotificationSender(
                    config.getNotificationDomain(),
                    config.getNotificationFrom(),
                    config.getNotificationTo(),
                    config.getMailgunApiKey()
            );
        }
        
        Duration interval = Duration.ofMillis(config.getPollIntervalMs());
        
        boolean alive = true;
        PublicIp currentIp = null;
        PublicIp previousIp = currentIp;
        PublicIp lastGoodIp = null;
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification(bootTime, "booted"));
        boolean complainedAboutUpdate = false;
        boolean complainedAboutOffline = false;
        boolean complainedAboutNotifications = false;
        Instant cycleStart;
        Instant nextCycleStart;
        
        while (alive) {
            cycleStart = clock.instant();
            nextCycleStart = cycleStart.plus(interval);
            
            try {
                // handle any IP/connectivity changes
                currentIp = ipFinder.get();
                LOG.debug("public ip is {}", currentIp);
                if (!Objects.equals(currentIp, lastGoodIp) || !Objects.equals(currentIp, previousIp)) {
                    if (currentIp == null) {
                        //we're offline
                        if (!complainedAboutOffline) {
                            LOG.info("went offline");
                            notifications.add(new Notification(cycleStart, "went offline"));
                            complainedAboutOffline = true;
                        }
                    } else {
                        //we're online
                        if (complainedAboutOffline) {
                            LOG.info("came back online as {}", currentIp);
                            complainedAboutOffline = false;
                        }
                        if (currentIp.equals(lastGoodIp)) {
                            notifications.add(new Notification(cycleStart, "came back online (ip unchanged - " + currentIp + ")"));
                        } else {
                            try {
                                boolean changed = updater.update(config.getDomain(), currentIp, true);
                                LOG.info("{} {} as {}", (changed ? "set" : "refreshed"), config.getDomain(), currentIp);
                                String notificationText;
                                if (lastGoodIp == null) {
                                    //first time we're online since boot
                                    if (changed) {
                                        notificationText = "connected as " + currentIp + ". DNS updated";
                                    } else {
                                        notificationText = "connected as " + currentIp + ". DNS refreshed";
                                    }
                                } else {
                                    notificationText = "ip changed from " + lastGoodIp + " to " + currentIp + ". DNS updated";
                                }
                                notifications.add(new Notification(cycleStart, notificationText));
                                complainedAboutUpdate = false;
                                lastGoodIp = currentIp;
                            } catch (Exception e) {
                                LOG.error("while updating DNS", e);
                                if (!complainedAboutUpdate) {
                                    complainedAboutUpdate = true;
                                    notifications.add(new Notification(cycleStart, "unable to update DNS: " + e.getMessage()));
                                }
                            }
                        }
                    }
                    
                }
                previousIp = currentIp;

                //send out any pending notifications (if online)
                if (currentIp != null && !notifications.isEmpty()) {
                    try {
                        notifier.send(notifications);
                        LOG.info("sent {} notifications", notifications.size());
                        notifications.clear();
                        complainedAboutNotifications = false;
                    } catch (Exception e) {
                        LOG.error("while sending notifications", e);
                        if (!complainedAboutNotifications) {
                            notifications.add(new Notification(cycleStart, "unable to send notifications: " + e.getMessage()));
                            complainedAboutNotifications = true;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("during poll cycle", e);
            }
            
            try {
                long remaining = Duration.between(clock.instant(), nextCycleStart).toMillis();
                if (remaining > 0) {
                    LOG.debug("waiting {} millis for next cycle", remaining);
                    Thread.sleep(remaining);
                }
            } catch (InterruptedException e) {
                if (!alive) {
                    LOG.error("interrupted waiting for next poll", e);
                } else {
                    LOG.debug("interrupted waiting for next poll", e);
                }
            }
        }
        
        LOG.info("terminating");
    }
    
    private static Config findConfig() throws Exception {
        //try CWD
        File configFile = new File(new File("."), "anthony.properties");
        if (!configFile.exists()) {
            File appLocation = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            configFile = new File(appLocation, "anthony.properties");
        }
        if (!configFile.exists()) {
            throw new IllegalStateException("unable to locate anthony.properties");
        }
        LOG.info("loading configuration from {}", configFile.getCanonicalPath());
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(configFile)) {
            props.load(is);
        }
        return Config.deserailize(props);
    }
}
