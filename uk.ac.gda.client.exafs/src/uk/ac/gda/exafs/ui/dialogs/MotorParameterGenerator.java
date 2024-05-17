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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;

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

			// Parameter for moveTo (true/false) flag
			ParameterConfig paramConfig = new ParameterConfig();
			paramConfig.setFullPathToGetter(String.format(activeGetterFormat, motorPos.getScannableName()));
			paramConfig.setBeanType(beanTypeString);
			paramConfig.setDescription("Move " + motorPos.getDescription());
			paramConfig.setAllowedValuesFromBoolean(true);

			// Parameter with demandPosition is set as additional config
			ParameterConfig positionParam = new ParameterConfig();
			positionParam.setFullPathToGetter(String.format(positionGetterFormat, motorPos.getScannableName()));
			positionParam.setBeanType(beanTypeString);
			positionParam.setDescription(motorPos.getDescription());

			paramConfig.setAdditionalConfig(List.of(positionParam));

			paramConfigs.add(paramConfig);
		}
		return paramConfigs;
	}
}
