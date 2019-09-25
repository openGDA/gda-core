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
import uk.ac.gda.ui.tool.ClientMessages;

public class TomographyTR6Mode extends TomographyBaseMode {

	public TomographyTR6Mode(Stage stage) {
		super(stage);
	}

	@Override
	protected void populateDevicesMap() {
		addToDevicesMap(TomographyDevices.MOTOR_STAGE_X, "tomography.tr6.motor.linear.y");
		addToDevicesMap(TomographyDevices.MOTOR_STAGE_ROT_Y, "tomography.tr6.motor.rot.y");
		addToDevicesMap(TomographyDevices.MOTOR_CAMERA_Z, "tomography.main.motor.camera.z");
		addToDevicesMap(TomographyDevices.MALCOLM_TOMO, "tomography.malcolm.device.tomo");
	}

	@Override
	protected TabCompositeFactory[] getTabsFactories() {
		TabCompositionBuilder builder = new TabCompositionBuilder();
		builder.assemble(createMotorAxesComposite(), ClientMessages.STAGE);
		builder.assemble(createCameraMotorsComposite(), ClientMessages.CAMERA);
		return builder.build();
	}

	private StageCompositeDefinition[] createMotorAxesComposite() {
		StageCompositeDefinitionBuilder builder = new StageCompositeDefinitionBuilder();
		builder.assemble(TomographyDevices.MOTOR_STAGE_Y, ClientMessages.AXIS_Y);
		builder.assemble(TomographyDevices.MOTOR_STAGE_ROT_Y, ClientMessages.THETA);
		return builder.build();
	}

	private StageCompositeDefinition[] createCameraMotorsComposite() {
		StageCompositeDefinitionBuilder builder = new StageCompositeDefinitionBuilder();
		builder.assemble(TomographyDevices.MOTOR_CAMERA_Z, ClientMessages.CAMERA);
		return builder.build();
	}
}
