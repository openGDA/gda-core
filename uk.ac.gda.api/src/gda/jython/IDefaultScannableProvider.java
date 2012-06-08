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

import java.util.Vector;

import gda.device.Scannable;

/**
 * Interface used by some classes to get the default scannable Provided to ensure loose coupling between callers and
 * command runner implementation The methods in this interface should be not distributed over Corba, but should only be
 * used by objects local to the object implementing this interface, i.e. the methods should only be using in scans or
 * during testing.
 */
public interface IDefaultScannableProvider {
	/**
	 * Returns a list of scannable objects which must be called in every scan. Used by scans to construct the list of
	 * objects whose positions are reported at every node of a scan.
	 * <P>
	 * This returns a vector of object references of all the objects in the Jython namepsace listed in the
	 * DefaultScannablesList object named "scannablesList" in the configuration information.
	 * <P>
	 * As this method returns object references, this method must only be called by objects local to this object. This
	 * method must never be available remotely.
	 * 
	 * @return Vector
	 */
	public Vector<Scannable> getDefaultScannables();
}