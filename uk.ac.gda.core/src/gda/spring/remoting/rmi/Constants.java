/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.spring.remoting.rmi;

public final class Constants {
	
	// Prevent instantiation
	private Constants() {}

	/**
	 * Prefix to use for the names used when binding objects in the RMI registry.
	 */
	public static final String RMI_NAME_PREFIX = "gda/";
	
	/**
	 * RMI name for the remote object lister object.
	 */
	public static final String REMOTE_OBJECT_LISTER_RMI_NAME = "_catalogue";

}
