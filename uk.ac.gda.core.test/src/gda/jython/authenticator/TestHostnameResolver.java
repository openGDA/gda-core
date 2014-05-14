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

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link HostnameResolver} for testing.
 */
public class TestHostnameResolver implements HostnameResolver {

	private Map<String, List<String>> entries;
	
	/**
	 * Creates a new resolver that can resolve "localhost".
	 */
	public TestHostnameResolver() {
		entries = new HashMap<String, List<String>>();
		addEntry("localhost", "127.0.0.1", "[::1]");
	}
	
	/**
	 * Adds a new entry to this resolver.
	 * 
	 * @param hostname the hostname
	 * @param addresses IP addresses for the hostname
	 */
	public void addEntry(String hostname, String... addresses) {
		final List<String> addressList = Collections.unmodifiableList(Arrays.asList(addresses));
		entries.put(hostname, addressList);
	}
	
	@Override
	public List<String> resolveHostname(String hostname) throws UnknownHostException {
		if (!entries.containsKey(hostname)) {
			throw new UnknownHostException("Unknown host '" + hostname + "'");
		}
		return entries.get(hostname);
	}

}
