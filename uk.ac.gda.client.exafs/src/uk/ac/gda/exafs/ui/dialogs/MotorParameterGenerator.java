/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;

public class MotorParameterGenerator extends ParameterConfigGenerator {

	private static final Logger logger = LoggerFactory.getLogger(MotorParameterGenerator.class);

	/**
	 * Get a list of ParameterConfigs for generic scannable positions (i.e. List<{@link SampleParameterMotorPosition}>) in the B18 sample parameter bean.
	 * Makes list of ParameterConfig objects with 'demand position' and 'do move' parameter for each one.
	 * @param o List of ParameterValuesForBean
	 * @return list of ParameterConfigs
	 */
	@Override
	public List<ParameterConfig> createParameterConfigs(List<ParameterValuesForBean> o) {
		ISampleParametersWithMotorPositions sampleParams = getBeanOfType(o, ISampleParametersWithMotorPositions.class);

		if (sampleParams == null) {
			return Collections.emptyList();
		}

		String positionGetterFormat = ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME + "(%s)."+ SampleParameterMotorPosition.DEMAND_POSITION_GETTER_NAME;
		String activeGetterFormat = ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME + "(%s)."+ SampleParameterMotorPosition.DO_MOVE_GETTER_NAME;
		String beanTypeString = sampleParams.getClass().getCanonicalName();

		// Make list of new ParameterConfigs from sample parameter motor positions.
		// Add two ParameterConfigs per motor : one to control demand position another to control 'doMove' flag
		logger.debug("Creating ParameterConfigs for Sample parameter motor positions :");
		List<ParameterConfig> paramConfigs = new ArrayList<>();
		for (SampleParameterMotorPosition motorPos : sampleParams.getSampleParameterMotorPositions()) {
			logger.debug(motorPos.getDescription());

			// Parameter with demandPosition
			ParameterConfig paramConfig = new ParameterConfig();
			paramConfig.setFullPathToGetter(String.format(positionGetterFormat, motorPos.getScannableName()));
			paramConfig.setBeanType(beanTypeString);
			paramConfig.setDescription(motorPos.getDescription());
			paramConfigs.add(paramConfig);

			// Parameter for moveTo (true/false) flag
			// This is not shown in measurement conditions dialog - it is added to table if corresponding
			// demandPosition has been selected by user.
			paramConfig = new ParameterConfig();
			paramConfig.setFullPathToGetter(String.format(activeGetterFormat, motorPos.getScannableName()));
			paramConfig.setBeanType(beanTypeString);
			paramConfig.setDescription("Move " + motorPos.getDescription());
			paramConfig.setAllowedValuesFromBoolean(true);
			paramConfig.setShowInParameterSelectionDialog(false);
			paramConfigs.add(paramConfig);
		}
		return paramConfigs;
	}

	/**
	 * Add 'do move' ParameterValue for sample parameter motor positions (true = move at scan start, false = don't move)
	 * for selected SampleParameterMotors. <p>
	 * i.e. if the parameter values contains demand position call {@code getSampleParameterMotorPosition(user1).getDemandPosition}
	 * then insert new parameterValue for 'do move' function, {@code getSampleParameterMotorPosition(user1).getDoMove},
	 * before demand position item in list.
	 *
	 *  @param paramValuesForBeans
	 */
	@Override
	public void addParameterValues(List<ParameterValuesForBean> paramValuesForBeans) {

		Optional<ParameterValuesForBean> result = getParameterOfType(paramValuesForBeans, ISampleParametersWithMotorPositions.class);
		if (!result.isPresent()) {
			logger.warn("No bean found that has motor parameters");
			return;
		}

		// Add the ParameterValue to control the 'doMove' flag for each motor.
		var iter = result.get().getParameterValues().listIterator();
		while(iter.hasNext()) {
			ParameterValue paramValue = iter.next();
			String pathToGetter = paramValue.getFullPathToGetter();
			// Create new ParameterValue with getter for 'do move' function:
			if (pathToGetter.startsWith(ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME)) {
				String[] splitStr = pathToGetter.split("[.]");
				String pathToDoMove = splitStr[0] + "." + SampleParameterMotorPosition.DO_MOVE_GETTER_NAME;
				// Insert to the queue immediately after the current item
				iter.add(new ParameterValue(pathToDoMove, "true"));
			}
		}
	}

}
