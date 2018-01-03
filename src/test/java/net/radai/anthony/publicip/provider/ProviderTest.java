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

import net.radai.anthony.OnlineTest;
import net.radai.anthony.publicip.PublicIp;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public abstract class ProviderTest<P extends PublicIpServiceProvider> extends OnlineTest {
    protected AsyncHttpClient client;
    protected P provider;
    
    protected abstract P buildProvider() throws Exception;
    
    @Before
    public void setup() throws Exception {
        DefaultAsyncHttpClientConfig.Builder configBuilder = new DefaultAsyncHttpClientConfig.Builder();
        AsyncHttpClientConfig config = configBuilder.build();
        client = new DefaultAsyncHttpClient(config);
        provider = buildProvider();
    }
    
    @After
    public void teardown() throws Exception {
        if (provider != null) {
            if (provider instanceof AutoCloseable) {
                ((AutoCloseable)provider).close();
            }
            provider = null;
        }
        if (client != null) {
            client.close();
            client = null;
        }
    }
    
    @Test
    public void testSimpleScenario() throws Exception {
        PublicIp publicIp = getPublicIp();
        Assert.assertNotNull(publicIp);
        Assert.assertTrue(publicIp.getV4() != null || publicIp.getV6() != null);
    }
    
    protected PublicIp getPublicIp() throws Exception {
        Request request = provider.buildRequest();
        Response response = run(request);
        return provider.parse(response);
    } 
    
    protected Response run(Request req) throws Exception {
        return client.executeRequest(req).get();
    }
}
