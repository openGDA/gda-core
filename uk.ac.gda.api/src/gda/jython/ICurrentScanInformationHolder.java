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

import gda.scan.Scan;
import gda.scan.ScanInformation;

/**
 *
 * Interface used by some classes to set the current scan
 * Provided to ensure loose coupling between callers and command runner implementation.
 * 
 * The methods in this interface should be not distributed over Corba, but should only be used by objects local to the
 * object implementing this interface, i.e. the methods should only be used in scans or during testing.
 * 
 */
public interface ICurrentScanInformationHolder {
	/**
	 * Registers the supplied object as the scan currently running. This is the scan which will be restarted by
	 * resumeCurrentScan if the status is HALTED.
	 * 
	 * @param newScan
	 */
	public void setCurrentScan(Scan newScan);
	
	/**
	 * Return some information about the currently running scan
	 * 
	 * @return ScanInformation
	 */
	public ScanInformation getCurrentScanInformation();
	
	
}