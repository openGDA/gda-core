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
 * Event for broadcast by Scriptcontrollers to inform IObervers of the script that a scan created by the
 * script has finished.
 */
public class ScanFinishEvent implements Serializable{

	public static enum FinishType {OK,INTERRUPTED,ERROR}
	
	String scanName;
	FinishType finishType;
	
	public ScanFinishEvent(String scanName, FinishType finishType) {
		this.scanName = scanName;
		this.finishType = finishType;
	}
	
	public String getScanName() {
		return scanName;
	}
	public void setScanName(String scanName) {
		this.scanName = scanName;
	}
	public FinishType getFinishType() {
		return finishType;
	}
	public void setFinishType(FinishType finishType) {
		this.finishType = finishType;
	}
	
}