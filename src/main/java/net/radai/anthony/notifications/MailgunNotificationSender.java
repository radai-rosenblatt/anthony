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

package net.radai.anthony.notifications;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * see https://documentation.mailgun.com/en/latest/api_reference.html for api doc
 */
public class MailgunNotificationSender implements NotificationSender {
    private final AsyncHttpClient client;
    private final String domain;
    private final String from;
    private final String to;
    private final String apiKey;
    
    private final Realm realm;
    private final String fromParam;
    private final String toParam;

    MailgunNotificationSender(
            AsyncHttpClient client,
            String domain,
            String from,
            String to,
            String apiKey
    ) {
        this.client = client;
        this.domain = domain;
        this.from = from;
        this.to = to;
        this.apiKey = apiKey;
        this.realm = new Realm.Builder("api", apiKey)
                .setUsePreemptiveAuth(true)
                .setScheme(Realm.AuthScheme.BASIC)
                .build();
        this.fromParam = from + " <" + from + "@" + domain + ">";
        this.toParam = "<" + to + ">";
    }

    public MailgunNotificationSender(String domain, String from, String to, String apiKey) {
        this(new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                .build())
        ,domain, from, to, apiKey);
    }

    @Override
    public void send(List<Notification> notifications) throws Exception {
        Request req = new RequestBuilder()
                .setUrl("https://api.mailgun.net/v3/" + domain + "/messages")
                .setMethod("POST")
                .addFormParam("from",  fromParam)
                .addFormParam("to", toParam)
                .addFormParam("subject", "notifications")
                .setRealm(realm)
                .addFormParam("text", compose(notifications))
                .build();
        Response response = client.executeRequest(req).get();
        int code = response.getStatusCode();
        if (code != 200) {
            throw new IllegalStateException("got " + code + ": " + response.getStatusText() + " while trying to send notifications");
        }
    }
    
    private String compose(List<Notification> notifications) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter timeFormat = DateTimeFormatter.ISO_INSTANT; 
        for (Notification notification : notifications) {
            Instant timestamp = notification.getTimestamp();
            String text = notification.getText();
            sb.append(timeFormat.format(timestamp)).append(" - ").append(text).append("\n");
        }
        return sb.toString().trim();
    }
}
