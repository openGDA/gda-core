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

package uk.ac.diamond.daq.mapping.ui.stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import gda.rcp.views.StageCompositeDefinition;
import gda.rcp.views.TabCompositeFactory;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Stage;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.client.properties.stage.ScannableGroupProperties;
import uk.ac.gda.client.properties.stage.ScannablesPropertiesHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Describes a General Tomography Stage
 *
 * @author Maurizio Nagni
 */
public class GTSStage extends CommonStage {

	public static final String GTS_GROUP_ID = "GTS";
	public static final String GTS_SCANNABLE_X_ID = "X";
	public static final String GTS_SCANNABLE_Y_ID = "Y";
	public static final String GTS_SCANNABLE_Z_ID = "Z";
	public static final String GTS_SCANNABLE_THETA_ID = "THETA";

	private static final Map<String, StageDevice> tempNewToOldMapping = new HashMap<>();

	public GTSStage() {
		super(Stage.GTS);

		// Temporary map until is possible to rewrite populateDevicesMap in term of ScannablesPropertiesHelper elements
		tempNewToOldMapping.put(GTS_SCANNABLE_X_ID, StageDevice.MOTOR_STAGE_X);
		tempNewToOldMapping.put(GTS_SCANNABLE_Y_ID, StageDevice.MOTOR_STAGE_Y);
		tempNewToOldMapping.put(GTS_SCANNABLE_Z_ID, StageDevice.MOTOR_STAGE_Z);
		tempNewToOldMapping.put(GTS_SCANNABLE_THETA_ID, StageDevice.MOTOR_STAGE_ROT_Y);
	}

	@Override
	protected void populateDevicesMap() {
		addToDevicesMap(StageDevice.MOTOR_STAGE_X, "tomography.main.motor.linear.x");
		addToDevicesMap(StageDevice.MOTOR_STAGE_Y, "tomography.main.motor.linear.y");
		addToDevicesMap(StageDevice.MOTOR_STAGE_Z, "tomography.main.motor.linear.z");
		addToDevicesMap(StageDevice.MOTOR_STAGE_ROT_Y, "tomography.main.motor.rot.y");
	}

	@Override
	protected TabCompositeFactory[] getTabsFactories() {
		TabCompositionBuilder builder = new TabCompositionBuilder();
		builder.assemble(createMotorAxesComposite(), ClientMessages.STAGE);
		return builder.build();
	}

	private StageCompositeDefinition[] createMotorAxesComposite() {
		return Optional.ofNullable(getScannableHelper().getScannableGroupPropertiesDocument(GTS_GROUP_ID))
				.map(this::createMotorAxesComposite)
				.orElse(null);
	}

	private StageCompositeDefinition[] createMotorAxesComposite(ScannableGroupProperties groupDocument) {
		StageCompositeDefinitionBuilder builder = new StageCompositeDefinitionBuilder();
		groupDocument.getScannables().forEach(s ->
			builder.assemble(tempNewToOldMapping.get(s.getId()), ClientMessagesUtility.getClientMessageByString(s.getLabel()))
		);
		return builder.build();
	}

	private ScannablesPropertiesHelper getScannableHelper() {
		return SpringApplicationContextFacade.getBean(ScannablesPropertiesHelper.class);
	}
}
