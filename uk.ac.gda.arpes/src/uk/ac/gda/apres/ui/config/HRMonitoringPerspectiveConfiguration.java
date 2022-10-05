/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.apres.ui.config;

import gda.factory.Findable;
import uk.ac.gda.client.livecontrol.ControlSet;

public class HRMonitoringPerspectiveConfiguration implements Findable {

	private String name;

	private String camera1Name;
	private String camera2Name;
	private String camera3Name;

	private ControlSet temperatureControls;
	private ControlSet pressureControls;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getCamera1Name() {
		return camera1Name;
	}

	public void setCamera1Name(String camera1Name) {
		this.camera1Name = camera1Name;
	}

	public String getCamera2Name() {
		return camera2Name;
	}

	public void setCamera2Name(String camera2Name) {
		this.camera2Name = camera2Name;
	}

	public String getCamera3Name() {
		return camera3Name;
	}

	public void setCamera3Name(String camera3Name) {
		this.camera3Name = camera3Name;
	}

	public ControlSet getTemperatureControls() {
		return temperatureControls;
	}

	public void setTemperatureControls(ControlSet temperatureControls) {
		this.temperatureControls = temperatureControls;
	}

	public ControlSet getPressureControls() {
		return pressureControls;
	}

	public void setPressureControls(ControlSet pressureControls) {
		this.pressureControls = pressureControls;
	}
}
