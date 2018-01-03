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

import net.radai.anthony.publicip.PublicIp;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DnsJavaResolver implements DnsResolver {
    
    private final List<String> dnsServers;

    public DnsJavaResolver(List<String> dnsServers) throws IllegalStateException {
        this.dnsServers = new ArrayList<>(dnsServers);
    }

    @Override
    public PublicIp resolve(String hostname) throws UnknownHostException {
        Name name;
        try {
            name = Name.fromString(hostname);
        } catch (TextParseException e) {
            throw new IllegalArgumentException("bad argument " + hostname, e);
        }
        
        //the following will throw UnknownHostException if we're offline
        //or the dns servers arent resolvable.
        Resolver[] resolvers = new Resolver[dnsServers.size()];
        for (int i = 0; i < dnsServers.size(); i++) {
            resolvers[i] = new SimpleResolver(dnsServers.get(i));
        }
        ExtendedResolver resolver = new ExtendedResolver(resolvers);

        Inet4Address v4Addr = null;
        Inet6Address v6Addr = null;
        
        Lookup v4Lookup = new Lookup(name, Type.A);
        v4Lookup.setResolver(resolver);
        Record[] v4Records = v4Lookup.run();
        if (v4Records != null) {
            for (Record v4Rec : v4Records) {
                ARecord rec = (ARecord) v4Rec;
                v4Addr = (Inet4Address) rec.getAddress();
            }
        }
        
        Lookup v6Lookup = new Lookup(name, Type.AAAA);
        v6Lookup.setResolver(resolver);
        Record[] v6Records = v6Lookup.run();
        if (v6Records != null) {
            for (Record v6Rec : v6Records) {
                AAAARecord rec = (AAAARecord) v6Rec;
                v6Addr = (Inet6Address) rec.getAddress();
            }
        }
        
        if (v4Addr != null || v6Addr != null) {
            return new PublicIp(v4Addr, v6Addr);
        }
        return null;
    }
}
