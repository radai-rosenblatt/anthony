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

import org.junit.Assume;
import org.junit.BeforeClass;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public abstract class OnlineTest {
    @BeforeClass
    public static void skipIfNoInternet() throws Exception {
        URL likelyExists = new URL("http://google.com");
        HttpURLConnection conn = (HttpURLConnection) likelyExists.openConnection();
        try {
            conn.connect();
        } catch (UnknownHostException e) {
            Assume.assumeNoException("no internet connection", e);
        }
        conn.disconnect();
    }
}
