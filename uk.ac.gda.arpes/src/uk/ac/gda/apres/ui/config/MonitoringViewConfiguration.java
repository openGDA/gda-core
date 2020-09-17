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
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.livecontrol.ControlSet;
import uk.ac.gda.client.livecontrol.LiveControl;

public class MonitoringViewConfiguration implements Findable {

	private String name;
	private LiveControl overExposureControl;
	private ControlSet temperatureControls;
	private CameraConfiguration analyserLiveStreamConfiguration;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public LiveControl getOverExposureControl() {
		return overExposureControl;
	}

	public void setOverExposureControl(LiveControl overExposureControl) {
		this.overExposureControl = overExposureControl;
	}

	public ControlSet getTemperatureControls() {
		return temperatureControls;
	}

	public void setTemperatureControls(ControlSet temperatureControls) {
		this.temperatureControls = temperatureControls;
	}

	public CameraConfiguration getAnalyserLiveStreamConfiguration() {
		return analyserLiveStreamConfiguration;
	}

	public void setAnalyserLiveStreamConfiguration(CameraConfiguration analyserLiveStreamConfiguration) {
		this.analyserLiveStreamConfiguration = analyserLiveStreamConfiguration;
	}
}
