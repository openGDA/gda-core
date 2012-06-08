/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
 * Interface for the local JythonServer object.
 * <p>
 * It supplies extra functionality to the distributed Jython interface so it may
 * pass object references to server-side objects.
 * <p>
 * Examples of this would be objects in Jython scripts which would like access
 * to the scan object as it is running.
 * 
 * 
 * @author rjw82
 * 
 */
public interface LocalJython extends Jython, ICurrentScanInformationHolder, IJythonServerNotifer,
		IDefaultScannableProvider, IJythonServerStatusProvider {

}
