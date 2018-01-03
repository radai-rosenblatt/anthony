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

package net.radai.anthony.publicip;

import net.radai.anthony.publicip.provider.PublicIpServiceProvider;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

public class MajorityVoteIpFinder implements PublicIpFinder, AutoCloseable {
    private final AsyncHttpClient client;
    private final List<PublicIpServiceProvider> providers;

    MajorityVoteIpFinder(AsyncHttpClient client, PublicIpServiceProvider... providers) {
        if (providers == null || providers.length < 1) {
            throw new IllegalArgumentException();
        }
        for (PublicIpServiceProvider provider : providers) {
            if (provider == null) {
                throw new IllegalArgumentException();
            }
        }
        this.client = client;
        this.providers = List.of(providers);
    }

    public MajorityVoteIpFinder(PublicIpServiceProvider... providers) {
        this(new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                        .build()
                ),
                providers);
    }

    @Override
    public PublicIp get() throws InterruptedException {
        List<CompletableFuture<PublicIpResult>> futures = new ArrayList<>(providers.size());
        for (PublicIpServiceProvider provider : providers) {
            Request req = provider.buildRequest();
            ListenableFuture<Response> future = client.executeRequest(req);
            CompletableFuture<PublicIpResult> resultFuture = future.toCompletableFuture()
                    .handle((response, throwable) -> {
                        if (throwable != null) {
                            return new PublicIpResult(provider, throwable);
                        }
                        try {
                            return new PublicIpResult(provider, provider.parse(response));
                        } catch (Exception e) {
                            return new PublicIpResult(provider, e);
                        }
            });
            futures.add(resultFuture);
        }
        List<PublicIpResult> votes = new ArrayList<>(futures.size());
        for (Future<PublicIpResult> future : futures) {
            try {
                votes.add(future.get());
            } catch (ExecutionException e) {
                throw new IllegalStateException(e); //should never happen
            }
        }
        return summarize(votes);
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
    
    private PublicIp summarize(List<PublicIpResult> results) {
        Map<Inet4Address, Integer> v4Votes = new HashMap<>();
        Map<Inet6Address, Integer> v6Votes = new HashMap<>();
        for (PublicIpResult vote : results) {
            if (vote.getIssue() != null) {
                continue;
            }
            PublicIp ip = vote.getIp();
            Inet4Address v4 = ip.getV4();
            if (v4 != null) {
                v4Votes.compute(v4, COUNTER);
            }
            Inet6Address v6 = ip.getV6();
            if (v6 != null) {
                v6Votes.compute(v6, COUNTER);
            }
        }
        Inet4Address v4 = findMajority(v4Votes);
        Inet6Address v6 = findMajority(v6Votes);
        if (v4 == null && v6 == null) {
            return null;
        }
        return new PublicIp(v4, v6);
    }
    
    private final static BiFunction<InetAddress, Integer, Integer> COUNTER = (addr, count) -> {
        if (count == null) {
            return 1;
        }
        return count + 1;
    };
    
    private static <T> T findMajority(Map<T, Integer> votes) {
        T leader = null;
        int leaderVotes = Integer.MIN_VALUE;
        for (Map.Entry<T, Integer> entry : votes.entrySet()) {
            T candidate = entry.getKey();
            int candidateVotes = entry.getValue();
            if (candidateVotes > leaderVotes) {
                leader = candidate;
                leaderVotes = candidateVotes;
            }
        }
        return leader;
    }
}
