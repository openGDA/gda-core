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

package gda.jython.scriptcontroller.event;

import java.io.Serializable;

/**
 * Event for broadcast by Scriptcontrollers to inform IObervers of the script that a scan has been created by the
 * script, so ScanDataPoints with a matching scan name may be expected.
 * <p>
 * Observers of the script, via the Scriptcontroller, should also observer the JythonServer to receive the relevant
 * ScanDataPoints.
 * <p>
 * Example usage in a Jython script: 
 * <pre>
 * {@code
 * controller = Finder.getInstance().find("MyScriptObserver") 
 * controller.update(None,ScriptProgressEvent("Running scan")) 
 * thisscan = ConcurrentScan(scan_args)
 * controller.update(None,ScanCreationEvent(thisscan.getName())) 
 * thisscan.runScan()
 * controller.update(None,ScanFinishEvent(thisscan.getName(),ScanFinishEvent.FinishType.OK));
 * }
 * </pre>
 */
public class ScanCreationEvent implements Serializable {

	private String scanName = "";

	public ScanCreationEvent(String scanName) {
		this.scanName = scanName;
	}

	public String getScanName() {
		return scanName;
	}

	public void setScanName(String scanName) {
		this.scanName = scanName;
	}
}
