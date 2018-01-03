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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Objects;

public class PublicIp {
    private final Inet4Address v4;
    private final Inet6Address v6;

    public PublicIp(Inet4Address v4, Inet6Address v6) {
        if (v4 == null && v6 == null) {
            throw new IllegalArgumentException();
        }
        this.v4 = v4;
        this.v6 = v6;
    }

    public PublicIp(Inet4Address v4) {
        this(v4, null);
    }

    public PublicIp(Inet6Address v6) {
        this(null, v6);
    }

    public Inet4Address getV4() {
        return v4;
    }

    public Inet6Address getV6() {
        return v6;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (v4 != null) {
            sb.append("v4: ").append(v4.getHostAddress());
        }
        if (v6 != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("v6: ").append(v6.getHostAddress());
        }
        if (sb.length() == 0) {
            return "unknown";
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PublicIp publicIp = (PublicIp) o;
        return Objects.equals(v4, publicIp.v4) &&
                Objects.equals(v6, publicIp.v6);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v4, v6);
    }
}
