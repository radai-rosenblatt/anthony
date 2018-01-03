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

package net.radai.anthony.publicip.provider;

import net.radai.anthony.publicip.PublicIp;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhatIp implements PublicIpServiceProvider {
    private static final Request REQUEST = new RequestBuilder().setUrl("http://ipv4.whatip.me/").build();
    private static final Pattern PATTERN = Pattern.compile("<body>([^<]*)</body>");

    @Override
    public Request buildRequest() {
        return REQUEST;
    }

    @Override
    public PublicIp parse(Response response) throws Exception {
        String body = response.getResponseBody().trim();
        Matcher matcher = PATTERN.matcher(body);
        if (!matcher.find()) {
            throw new IllegalArgumentException("unable to parse result out of " + response);
        }
        String v4 = matcher.group(1).trim();
        return new PublicIp((Inet4Address) InetAddress.getByName(v4));
    }
}
