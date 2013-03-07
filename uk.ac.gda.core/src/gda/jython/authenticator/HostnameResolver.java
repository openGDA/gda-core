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
import java.util.List;

/**
 * Resolves a hostname to the corresponding IP addresses.
 */
public interface HostnameResolver {

	/**
	 * Resolves a hostname.
	 * 
	 * @param hostname hostname
	 * @return list of IP addresses
	 */
	public List<String> resolveHostname(String hostname) throws UnknownHostException;

}
