/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import java.util.List;

import gda.factory.FindableConfigurableBase;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

public class SampleTransferCameras extends FindableConfigurableBase {

	private List<CameraConfiguration> cameras;

	public List<CameraConfiguration> getCameras() {
		return cameras;
	}

	public void setCameras(List<CameraConfiguration> cameras) {
		this.cameras = cameras;
	}

}
