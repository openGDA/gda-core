/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.jython.authenticator;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class InetAddressHostnameResolver implements HostnameResolver {

	@Override
	public List<String> resolveHostname(String hostname) throws UnknownHostException {
		
		InetAddress[] addresses = InetAddress.getAllByName(hostname);
		
		List<String> ips = new ArrayList<String>();
		
		for (InetAddress addr : addresses) {
			
			if (addr instanceof Inet4Address) {
				String ip = addr.getHostAddress();
				ips.add(ip);
			}
			
			else if (addr instanceof Inet6Address) {
				String ip = addr.getHostAddress();
				ip = "[" + ip + "]"; // RFC 3986
				ips.add(ip);
			}
		}
		
		return ips;
	}

}
