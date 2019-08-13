/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.ui.mode;

import gda.rcp.views.StageCompositeDefinition;
import gda.rcp.views.TabCompositeFactory;
import uk.ac.gda.tomography.scan.editor.view.TomographyMessages;

public class TomographyTR6Mode extends TomographyBaseMode {

	@Override
	protected void populateDevicesMap() {
		addToDevicesMap(ModeDevices.STAGE_MOTOR_Y, "tomography.tr6.motor.linear.y");
		addToDevicesMap(ModeDevices.STAGE_ROT_Y, "tomography.tr6.motor.rot.y");
		addToDevicesMap(ModeDevices.CAMERA_Z, "tomography.main.motor.camera.z");
	}

	@Override
	protected TabCompositeFactory[] getTabsFactories() {
		return new TabCompositeFactory[] { createStageMotorsCompositeFactory(createMotorAxesComposite(), TomographyMessages.STAGE),
				createStageMotorsCompositeFactory(createCameraMotorsComposite(), TomographyMessages.CAMERA) };
	}

	private StageCompositeDefinition[] createMotorAxesComposite() {
		return new StageCompositeDefinition[] {
				doMotor(ModeDevices.STAGE_MOTOR_Y, TomographyMessages.AXIS_Y),
				doMotor(ModeDevices.STAGE_ROT_Y, TomographyMessages.THETA) };
	}

	private StageCompositeDefinition[] createCameraMotorsComposite() {
		return new StageCompositeDefinition[] { doMotor(ModeDevices.CAMERA_Z, TomographyMessages.AXIS_Z) };
	}
}
