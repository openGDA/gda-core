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

package gda.jython;
/**
 *
 * Interface used by some classes to notify the jython server
 * Provided to ensure loose coupling between callers and command runner implementation
 * 
 * The methods in this interface should be not distributed over Corba, but should only be used by objects local to the
 * object implementing this interface, i.e. the methods should only be using in scans or during testing.
 */
public interface IJythonServerNotifer {
	/**
	 * This method passes information from a scan to the local facade objects. These in turn pass the data to the local
	 * version of the GUI panel which created the scan.
	 * 
	 * @param source
	 *            The serialized scan object which has created the data.
	 * @param data
	 *            A vector carrying the latest data collected by the scan
	 */
	public void notifyServer(Object source, Object data);

}