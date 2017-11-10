/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.aop;

import gda.device.DeviceException;
import gda.device.enumpositioner.EpicsSimpleMbbinary;

public class DeviceAccess {
	/**
	 * access to EPICS CONTROLLER PV
	 */
	private EpicsSimpleMbbinary controller;

	private String endStation;

	public boolean hasAccess() throws DeviceException {
		String trim = ((String)controller.getPosition()).trim();
		return trim.equals(endStation);
	}

	public void init() throws Exception {
		if (endStation == null || endStation.isEmpty()) {
			throw new IllegalStateException("endStation is not set.");
		}
		if (controller == null) {
			throw new IllegalStateException("Epics Controller scannable is not set.");
		}

	}

	public EpicsSimpleMbbinary getController() {
		return controller;
	}

	public void setController(EpicsSimpleMbbinary controller) {
		this.controller = controller;
	}

	public String getEndStation() {
		return endStation;
	}

	public void setEndStation(String endStation) {
		this.endStation = endStation;
	}

}
