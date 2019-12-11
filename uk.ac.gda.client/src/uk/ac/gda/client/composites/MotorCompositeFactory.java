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

package uk.ac.gda.client.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.rcp.views.CompositeFactory;
import gda.rcp.views.StageCompositeDefinition;
import gda.rcp.views.StageCompositeFactory;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.observablemodels.ScannableWrapper;
import uk.ac.gda.client.properties.MotorProperties;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Displays a motor position.
 *
 * @author Maurizio Nagni
 */
public class MotorCompositeFactory implements CompositeFactory {

	private final MotorProperties motorProperties;
	private StageCompositeDefinition stageCompositeDefinition;

	protected static final Logger logger = LoggerFactory.getLogger(MotorCompositeFactory.class);

	/**
	 * Creates a component based on text properties delegating to this component to find the motor associated
	 * {@link Scannable}
	 *
	 * @param motorProperties
	 */
	public MotorCompositeFactory(MotorProperties motorProperties) {
		this.motorProperties = motorProperties;
		this.stageCompositeDefinition = null;
	}

	/**
	 * Creates a component based on previous {@link StageCompositeDefinition}, making this class suitable also for
	 * {@link StageCompositeFactory}
	 *
	 * @param stageCompositeDefinition
	 */
	public MotorCompositeFactory(StageCompositeDefinition stageCompositeDefinition) {
		this.motorProperties = null;
		this.stageCompositeDefinition = stageCompositeDefinition;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		if (stageCompositeDefinition == null) {
			try {
				stageCompositeDefinition = assembleStageComposite();
			} catch (GDAClientException e) {
				logger.error("Error", e);
				return null;
			}
		}

		Composite motorComp = ClientSWTElements.createComposite(parent, style, 2);
		ClientSWTElements.createLabel(motorComp, SWT.NONE,
				stageCompositeDefinition.getLabel() != null ? stageCompositeDefinition.getLabel()
						: stageCompositeDefinition.getScannable().getName());
		MotorPositionEditorControl motorPosControl;
		try {
			motorPosControl = new MotorPositionEditorControl(motorComp, SWT.NONE,
					new ScannableWrapper(stageCompositeDefinition.getScannable()), true, false);
		} catch (Exception e) {
			UIHelper.showError("Motor creation error", "Cannot create motor MotorPositionEditorControl");
			return null;
		}
		double d = stageCompositeDefinition.getStepSize() * Math.pow(10, stageCompositeDefinition.getDecimalPlaces());
		motorPosControl.setDigits(stageCompositeDefinition.getDecimalPlaces());
		try {
			motorPosControl.setIncrement((int) d);
		} catch (Exception e) {
			UIHelper.showError("Motor update error", "Cannot set motor increment");
			return null;
		}
		return motorComp;
	}

	private StageCompositeDefinition assembleStageComposite() throws GDAClientException {
		StageCompositeDefinition scd = new StageCompositeDefinition();
		scd.setScannable(
				FinderHelper.getScannable(motorProperties.getController()).orElseThrow(GDAClientException::new));
		scd.setStepSize(1);
		scd.setDecimalPlaces(0);
		scd.setLabel(motorProperties.getName());
		return scd;
	}
}
