/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.IScannable;

/**
 * An instance of this class represents a monitor (an {@link IScannable}) in the UI.
 */
public class MonitorScanUIElement extends ScanUIElement {

	/**
	 * For devices that are monitored they may be monitored per scan or per point.
	 * In the former case their values will be read and written to the scan file
	 * once at the configure stage of the scan. For the per point type, the monitor
	 * will run with the scans IPositioner and read/write value during the same
	 * task which writes the motors.
	 * <p>
	 * All types of monitor are added to the annotation pool and will have annotations
	 * processed with the scan.
	 *
	 */
	public enum MonitorScanRole {

		/**
		 * Write the device value at each point in the scan.
		 */
		PER_POINT("Every point"),

		/**
		 * Write the device value once, at scan start.
		 */
		PER_SCAN("At scan start");

		private final String label;

		private MonitorScanRole(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

	private MonitorScanRole monitorScanRole;

	public MonitorScanUIElement() {
		super();
	}

	public MonitorScanUIElement(String monitorName) {
		super(monitorName, null);
		monitorScanRole = MonitorScanRole.PER_POINT;
	}

	public MonitorScanRole getMonitorScanRole() {
		return monitorScanRole;
	}

	public void setMonitorScanRole(MonitorScanRole monitorScanRole) {
		this.monitorScanRole = monitorScanRole;
	}

	@Override
	public String toString() {
		return "MonitorScanViewItem [name=" + getName() + ", isEnabled=" + isEnabled() +
				", monitorScanRole=" + monitorScanRole + "]";
	}

}
