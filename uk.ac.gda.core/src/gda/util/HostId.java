/*-
 * Copyright © 2012 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.UUID;

/**
 * some string that is likely to be unique across machines 
 * 
 * mainly used in the PathConstructor to generate a workspace path on a shared drive that is still unique to the machine
 */
public class HostId {
	
	private String id;
	private static HostId instance;
	
	public static String getId() {
		if (instance == null)
			instance = new HostId();
		return instance.toString();
	}
	
	public HostId() {
		id = generateId();
	}

	private String generateId() {
		
		try {
			for (NetworkInterface ni: Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (!ni.isLoopback()) {
					   byte[] mac = ni.getHardwareAddress();
					    if (mac != null) {

					    StringBuilder sb = new StringBuilder(18);
					    for (byte b : mac) {
					        if (sb.length() > 0)
					            sb.append(':');
					        sb.append(String.format("%02x", b));
					    }
					    return sb.toString();
					    }
				}
			}
		} catch (Exception e) {

		}
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {

		}
		/* at least make something unique for the session */
		return UUID.randomUUID().toString();
	}
	
	@Override
	public String toString() {
		return id;
	}
}
