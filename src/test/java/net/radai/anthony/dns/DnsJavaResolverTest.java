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

import net.radai.anthony.OnlineTest;
import net.radai.anthony.publicip.PublicIp;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DnsJavaResolverTest extends OnlineTest {
    
    @Test
    public void testResolveWellKnownDomain() throws Exception {
        DnsJavaResolver resolver = new DnsJavaResolver(Arrays.asList(
                "209.244.0.3", "209.244.0.4", //level3
                "8.8.8.8", "8.8.4.4"          //google
        ));
        PublicIp ip = resolver.resolve("yahoo.com");
        Assert.assertNotNull(ip);
        Assert.assertNotNull(ip.getV4());
        Assert.assertNotNull(ip.getV6());
    }
}
