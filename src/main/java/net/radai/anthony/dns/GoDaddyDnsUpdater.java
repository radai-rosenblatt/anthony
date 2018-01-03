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

package net.radai.anthony.dns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.radai.anthony.publicip.PublicIp;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * see https://developer.godaddy.com/doc for api docs
 */
public class GoDaddyDnsUpdater implements DnsUpdater {
    private final String key;
    private final String secret;
    private final AsyncHttpClient client;

    public GoDaddyDnsUpdater(String key, String secret) {
        this.key = key;
        this.secret = secret;
        DefaultAsyncHttpClientConfig.Builder configBuilder = new DefaultAsyncHttpClientConfig.Builder();
        AsyncHttpClientConfig config = configBuilder.build();
        this.client = new DefaultAsyncHttpClient(config);
    }

    @Override
    public boolean update(String domain, PublicIp addr, boolean updateWildcardAsWell) throws Exception {
        List<Record> beforeRecords = fetchAllRecords(domain);
        List<Record> sansRoots = filterOutRoots(beforeRecords, updateWildcardAsWell);
        List<Record> newRecords = buildRecords(addr, updateWildcardAsWell);
        List<Record> afterRecords = new ArrayList<>();
        
        afterRecords.addAll(sansRoots);
        afterRecords.addAll(newRecords);
        
        setRecords(domain, afterRecords);
        
        //plain equals() would also care about order
        return beforeRecords.size() == afterRecords.size() && beforeRecords.containsAll(afterRecords);
    }
    
    private List<Record> fetchAllRecords(String domain) throws Exception {
        Request request = new RequestBuilder()
                .setMethod("GET")
                .setUrl("https://api.godaddy.com/v1/domains/" + domain + "/records")
                .addHeader("Authorization", "sso-key " + key + ":" + secret)
                .build();
        Response response = client.executeRequest(request).get();
        String body = response.getResponseBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Record[] records = objectMapper.readValue(body, Record[].class);
        if (records == null || records.length < 1) {
            return Collections.emptyList();
        }
        return Arrays.asList(records);
    }
    
    private void setRecords(String domain, List<Record> records) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String recordsJson;
        try {
            recordsJson = objectMapper.writeValueAsString(records);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        Request request = new RequestBuilder()
                .setMethod("PUT")
                .setUrl("https://api.godaddy.com/v1/domains/" + domain + "/records")
                .addHeader("Authorization", "sso-key " + key + ":" + secret)
                .addHeader("Content-Type", "application/json")
                .setBody(recordsJson)
                .build();
        Response response;
        response = client.executeRequest(request).get();
        int code = response.getStatusCode();
        if (code != 200) {
            throw new IllegalStateException("attempt tp update DNS records returned " + code + ": " + response.getStatusText());
        }
    }

    private List<Record> buildRecords(PublicIp addr, boolean alsoBuildWildcards) {
        Inet4Address v4 = addr.getV4();
        Inet6Address v6 = addr.getV6();
        List<Record> records = new ArrayList<>();
        if (v4 != null) {
            records.add(Record.A("@", v4.getHostAddress(), 3600));
            if (alsoBuildWildcards) {
                records.add(Record.A("*", v4.getHostAddress(), 3600));
            }
        }
        if (v6 != null) {
            records.add(Record.AAAA("@", v6.getHostAddress(), 3600));
            if (alsoBuildWildcards) {
                records.add(Record.AAAA("*", v6.getHostAddress(), 3600));
            }
        }
        return records;
    }
    
    private List<Record> filterOutRoots(List<Record> input, boolean includeWildcards) {
        List<Record> output = new ArrayList<>();
        for (Record record : input) {
            String type = record.type;
            if (!("A".equals(type) || "AAAA".equals(type))) {
                output.add(record);
                continue;
            }
            String name = record.name;
            if (!(("*".equals(name) && includeWildcards) || "@".equals(name))) {
                output.add(record);
                continue;
            }
            //filter it out
        }
        return output;
    }

    @SuppressWarnings("unused")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private static class Record {
        public String type = null;
        public String name = null;
        public String data = null;
        public long ttl = -1;
        public long priority = -1;
        public String service = null;
        public String protocol = null;
        public int port = -1;
        public long weight = -1;

        public Record() {
        }

        @Override
        public String toString() {
            return type + " " + name + " = " + data;
        }

        public static Record A(String subdomain, String ipv4, long ttl) {
            Record rec = new Record();
            rec.type = "A";
            rec.name = subdomain;
            rec.data = ipv4;
            rec.ttl = ttl;
            return rec;
        }

        public static Record AAAA(String subdomain, String ipv6, long ttl) {
            Record rec = new Record();
            rec.type = "AAAA";
            rec.name = subdomain;
            rec.data = ipv6;
            rec.ttl = ttl;
            return rec;
        }
    }
}
