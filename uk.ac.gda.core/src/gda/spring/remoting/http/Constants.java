/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring.remoting.http;

/**
 * Constants used by the {@code gda.spring.remoting.http} package.
 */
public final class Constants {
	
	// Prevent instantiation
	private Constants() {}
	
	/**
	 * Context path under which the objects (and the object lister service)
	 * will be available.
	 */
	public static final String CONTEXT_PATH = "/gda";
	
	/**
	 * Servlet path of the object lister service.
	 */
	public static final String REMOTE_OBJECT_LISTER_PATH = "/objects";
	
}
