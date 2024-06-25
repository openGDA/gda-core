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

package uk.ac.gda.core.sampletransfer;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraState;

public class CameraControlAction extends AbstractStepAction {
	private static Logger logger = LoggerFactory.getLogger(CameraControlAction.class);

	private boolean startAcquiring;
	private List<CameraControl> cameraControls;
	private Scannable scannable;

	public CameraControlAction(String description, Scannable scannable, List<CameraControl> cameraControls, boolean startAcquiring) {
		super(description);
		Objects.requireNonNull(cameraControls, "Camera control must not be null");
		this.scannable = scannable;
		this.cameraControls = cameraControls;
		this.startAcquiring = startAcquiring;
	}

	private void startAcquiring() throws DeviceException {
		for (var cameraControl : cameraControls) {
			if (cameraControl.getAcquireState() == CameraState.IDLE ) {
				cameraControl.startAcquiring();
			} else {
				logger.debug("Detector is not idle - not starting it!");
			}
		}
	}

	private void stopAcquiring() throws DeviceException {
		for (var cameraControl : cameraControls) {
			cameraControl.stopAcquiring();
		}
	}

	@Override
	public void execute(StepProperties properties) throws DeviceException, InterruptedException {
		if (startAcquiring) {
			scannable.moveTo(1);
			startAcquiring();
		} else {
			scannable.moveTo(0);
			stopAcquiring();
		}
	}

	// FIXME incomplete implementation
	@Override
	public void terminate() throws DeviceException {
		// do nothing
	}

}
