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

public class TR6Stage extends CommonStage {

	public TR6Stage() {
		super(Stage.TR6);
	}

	@Override
	protected void populateDevicesMap() {
		addToDevicesMap(StageDevices.MOTOR_STAGE_X, "tomography.tr6.motor.linear.y");
		addToDevicesMap(StageDevices.MOTOR_STAGE_ROT_Y, "tomography.tr6.motor.rot.y");
		addToDevicesMap(StageDevices.MALCOLM_TOMO, "tomography.malcolm.device.tomo");
	}

	@Override
	protected TabCompositeFactory[] getTabsFactories() {
		TabCompositionBuilder builder = new TabCompositionBuilder();
		builder.assemble(createMotorAxesComposite(), ClientMessages.STAGE);
		return builder.build();
	}

	private StageCompositeDefinition[] createMotorAxesComposite() {
		StageCompositeDefinitionBuilder builder = new StageCompositeDefinitionBuilder();
		builder.assemble(StageDevices.MOTOR_STAGE_Y, ClientMessages.AXIS_Y);
		builder.assemble(StageDevices.MOTOR_STAGE_ROT_Y, ClientMessages.THETA);
		return builder.build();
	}
}
