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

import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorParameters;

public class DetectorConfigGenerator extends ParameterConfigGenerator {
	private static final Logger logger = LoggerFactory.getLogger(DetectorConfigGenerator.class);

	private static final String GET_DETECTOR_CONFIG_NAME = "getDetectorConfiguration";
	private static final String USE_DETECTOR_IN_SCAN_NAME = "isUseDetectorInScan";
	private static final String GET_CONFIG_FILE_NAME = "getConfigFileName";
	private static final String GET_SCRIPT_COMMAND_NAME = "getScriptCommand";

	@Override
	public List<ParameterConfig> createParameterConfigs(List<ParameterValuesForBean> o) {

		DetectorParameters detectorParams = getBeanOfType(o, DetectorParameters.class);
		if (detectorParams == null) {
			return Collections.emptyList();
		}
		if (detectorParams.getDetectorConfigurations().isEmpty()) {
			logger.debug("No generic detector parameters in detector parameters - nothing to be added");
			return Collections.emptyList();
		}

		String beanTypeString = detectorParams.getClass().getCanonicalName();

		logger.debug("Creating ParameterConfigs for DetectorConfig objects : ");
		List<ParameterConfig> paramConfigs = new ArrayList<>();
		for (int i=0; i<detectorParams.getDetectorConfigurations().size(); i++) {
			DetectorConfig detConfig = detectorParams.getDetectorConfiguration(i);

			String detectorDescription = detConfig.getDescription();
			logger.debug("{}", detectorDescription);

			// Use detector in scan (boolean)
			ParameterConfig paramConfig = new ParameterConfig();
			paramConfig.setDescription("Use "+detectorDescription);
			paramConfig.setFullPathToGetter(generateConfigGetter(i, USE_DETECTOR_IN_SCAN_NAME));
			paramConfig.setBeanType(beanTypeString);
			paramConfig.setAllowedValuesFromBoolean(true);

			// Path to script and detector configuration file name are set as additional config
			List<ParameterConfig> extraConfigs = new ArrayList<>();
			if (Boolean.TRUE.equals(detConfig.isUseScriptCommand())) {
				ParameterConfig extraConfig = new ParameterConfig();
				extraConfig.setDescription(detectorDescription+" script path");
				extraConfig.setFullPathToGetter(generateConfigGetter(i, GET_SCRIPT_COMMAND_NAME));
				extraConfig.setBeanType(beanTypeString);
				extraConfigs.add(extraConfig);
			}
			if (Boolean.TRUE.equals(detConfig.isUseConfigFile())) {
				ParameterConfig extraConfig = new ParameterConfig();
				extraConfig.setDescription(detectorDescription+" config file");
				extraConfig.setFullPathToGetter(generateConfigGetter(i, GET_CONFIG_FILE_NAME));
				extraConfig.setBeanType(beanTypeString);
				extraConfigs.add(extraConfig);
			}
			if (!extraConfigs.isEmpty()) {
				paramConfig.setAdditionalConfig(extraConfigs);
			}
			paramConfigs.add(paramConfig);
		}
		return paramConfigs;
	}

	private String generateConfigGetter(int index, String childMethod) {
		return String.format("%s(%d).%s", GET_DETECTOR_CONFIG_NAME, index, childMethod);
	}
}
