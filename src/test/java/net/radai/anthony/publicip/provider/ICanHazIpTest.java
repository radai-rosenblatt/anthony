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
import org.junit.Assert;
import org.junit.Test;

public class ICanHazIpTest extends ProviderTest<ICanHazIp> {

    @Override
    protected ICanHazIp buildProvider() {
        return new ICanHazIp();
    }

    @Test
    public void testRequestReusable() throws Exception {
        PublicIp ip1 = getPublicIp();
        PublicIp ip2 = getPublicIp();
        Assert.assertEquals(ip1, ip2);
    }
}
