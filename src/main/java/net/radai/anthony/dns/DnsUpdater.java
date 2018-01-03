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

public interface DnsUpdater {
    /**
     * points the given domain to the give public address.
     * if both ipv4 and v6 are != null DNS records (A and AAAA respectively)
     * are created and/or updated for both.
     * @param domain the domain to update
     * @param addr the new public IP addr(s) to point the domain to
     * @param updateWildcardAsWell true to update '*' entries, otherwise only '@'
     * @return true if registration actually changed upstream as a result of this call
     * @throws Exception if anything goes wrong
     */
    boolean update(String domain, PublicIp addr, boolean updateWildcardAsWell) throws Exception;
}
