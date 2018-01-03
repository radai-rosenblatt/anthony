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

public class ICanHazIp implements PublicIpServiceProvider {
    private static final Request REQUEST = new RequestBuilder().setUrl("http://ipv4.icanhazip.com/").build(); 
    
    @Override
    public Request buildRequest() {
        return REQUEST;
    }

    @Override
    public PublicIp parse(Response response) throws Exception {
        String v4 = response.getResponseBody().trim();
        return new PublicIp((Inet4Address) InetAddress.getByName(v4));
    }
}
