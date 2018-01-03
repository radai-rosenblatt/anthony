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

import java.util.Properties;

public class Config {
    //general configs
    private final long pollIntervalMs; //in millis
    private final String domain;
    //private final List<String> dnsServers;
    
    //godaddy configs
    private final String godaddyKey;
    private final String godaddySecret;
    
    //mailgun configs
    private final String notificationDomain;
    private final String notificationFrom;
    private final String notificationTo;
    private final String mailgunApiKey;

    public Config(
            long pollIntervalMs,
            String domain,
            //List<String> dnsServers,
            String godaddyKey,
            String godaddySecret,
            String notificationDomain,
            String notificationFrom,
            String notificationTo,
            String mailgunApiKey
    ) {
        //validate required configs
        if (pollIntervalMs <= 0 || domain == null || domain.isEmpty()) {
            throw new IllegalArgumentException();
        }
        //validate godaddy configs
        if (godaddyKey == null || godaddyKey.isEmpty() || godaddySecret == null || godaddySecret.isEmpty()) {
            throw new IllegalArgumentException();
        }
        //validate (optional) notification configs
        if (notificationDomain != null || notificationFrom != null || notificationTo != null || mailgunApiKey != null) {
            if (notificationDomain == null || notificationDomain.isEmpty() || notificationFrom == null
                    || notificationFrom.isEmpty() || notificationTo == null || notificationTo.isEmpty()
                    || mailgunApiKey == null || mailgunApiKey.isEmpty()) {
                throw new IllegalArgumentException("must provide complete set of notification configs");
            }
        }
        //for (String server : dnsServers) {
        //    if (server == null || server.isEmpty()) {
        //        throw new IllegalArgumentException();
        //    }
        //}
        this.pollIntervalMs = pollIntervalMs;
        this.domain = domain;
        //this.dnsServers = new ArrayList<>(dnsServers);
        this.godaddyKey = godaddyKey;
        this.godaddySecret = godaddySecret;
        this.notificationDomain = notificationDomain;
        this.notificationFrom = notificationFrom;
        this.notificationTo = notificationTo;
        this.mailgunApiKey = mailgunApiKey;
    }
    
    public static Config deserailize(Properties from) {
        long pollIntervalMs;
        try {
            pollIntervalMs = Long.parseLong(from.getProperty("pollIntervalMs"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
        //String str = from.getProperty("dnsServers");
        //if (str == null || str.isEmpty()) {
        //    throw new IllegalArgumentException();
        //}
        //List<String> dnsServers = new ArrayList<>();
        //String[] parts = str.split("\\s*,\\s*");
        //for (String part : parts) {
        //    dnsServers.add(part.trim());
        //}
        return new Config(
                pollIntervalMs,
                from.getProperty("domain"),
                //dnsServers,
                from.getProperty("godaddyKey"),
                from.getProperty("godaddySecret"),
                from.getProperty("notificationDomain"),
                from.getProperty("notificationFrom"),
                from.getProperty("notificationTo"),
                from.getProperty("mailgunApiKey")
        );
    }

    public Properties serialize() {
        Properties output = new Properties();
        output.setProperty("pollIntervalMs", Long.toString(pollIntervalMs));
        output.setProperty("domain", domain);
        //StringJoiner csv = new StringJoiner(",");
        //for (String dnsServer : dnsServers) {
        //    csv.add(dnsServer);
        //}
        //String value = csv.toString();
        //output.setProperty("dnsServers", value);
        output.setProperty("godaddyKey", godaddyKey);
        output.setProperty("godaddySecret", godaddySecret);
        output.setProperty("notificationDomain", notificationDomain);
        output.setProperty("notificationFrom", notificationFrom);
        output.setProperty("notificationTo", notificationTo);
        output.setProperty("mailgunApiKey", mailgunApiKey);
        return output;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public String getDomain() {
        return domain;
    }

    public String getGodaddyKey() {
        return godaddyKey;
    }

    public String getGodaddySecret() {
        return godaddySecret;
    }

    public String getNotificationDomain() {
        return notificationDomain;
    }

    public String getNotificationFrom() {
        return notificationFrom;
    }

    public String getNotificationTo() {
        return notificationTo;
    }

    public String getMailgunApiKey() {
        return mailgunApiKey;
    }
}
