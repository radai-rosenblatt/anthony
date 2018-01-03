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

public class PublicIpResult {
    private final PublicIpServiceProvider provider;
    private final PublicIp ip;
    private final Throwable issue;

    private PublicIpResult(PublicIpServiceProvider provider, PublicIp ip, Throwable issue) {
        this.provider = provider;
        this.ip = ip;
        this.issue = issue;
    }

    public PublicIpResult(PublicIpServiceProvider provider, PublicIp ip) {
        this(provider, ip, null);
    }

    public PublicIpResult(PublicIpServiceProvider provider, Throwable issue) {
        this(provider, null, issue);
    }

    public PublicIpServiceProvider getProvider() {
        return provider;
    }

    public PublicIp getIp() {
        return ip;
    }

    public Throwable getIssue() {
        return issue;
    }
}
