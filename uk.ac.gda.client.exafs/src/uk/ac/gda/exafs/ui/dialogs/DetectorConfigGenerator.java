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

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;

public class DetectorConfigGenerator extends ParameterConfigGenerator {
	private static final Logger logger = LoggerFactory.getLogger(MotorParameterGenerator.class);

	private static final String GET_DETECTOR_CONFIG_NAME = "getDetectorConfiguration";
	private static final String USE_DETECTOR_IN_SCAN_NAME = "isUseDetectorInScan";
	private static final String GET_CONFIG_FILE_NAME = "getConfigFileName";

	private String useDetectorGetter(int i) {
		return String.format("%s(%d).%s", GET_DETECTOR_CONFIG_NAME, i, USE_DETECTOR_IN_SCAN_NAME);
	}

	private String detectorFilenameGetter(int i) {
		return String.format("%s(%d).%s", GET_DETECTOR_CONFIG_NAME, i, GET_CONFIG_FILE_NAME);
	}

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
			String detectorDescription = detectorParams.getDetectorConfigurations().get(i).getDescription();
			logger.debug("{}", detectorDescription);

			// Detector configuration filename
			ParameterConfig paramConfig = new ParameterConfig();
			paramConfig.setDescription(detectorDescription+" config file name");
			paramConfig.setFullPathToGetter(detectorFilenameGetter(i));
			paramConfig.setBeanType(beanTypeString);
//			paramConfig.setUseDetectorFileNames(true);
			paramConfig.setShowInParameterSelectionDialog(false);
			paramConfigs.add(paramConfig);

			// Use detector in scan (boolean)
			paramConfig = new ParameterConfig();
			paramConfig.setDescription("Use "+detectorDescription);
			paramConfig.setFullPathToGetter(useDetectorGetter(i));
			paramConfig.setBeanType(beanTypeString);
			paramConfig.setAllowedValuesFromBoolean(true);
			paramConfig.setShowInParameterSelectionDialog(true);
			paramConfigs.add(paramConfig);
		}
		return paramConfigs;
	}

	@Override
	public void addParameterValues(List<ParameterValuesForBean> paramValuesForBeans) {

		Optional<ParameterValuesForBean> result = getParameterOfType(paramValuesForBeans, DetectorParameters.class);
		if (!result.isPresent()) {
			logger.warn("No bean found that has detector parameters");
			return;
		}

		// Make new method path for the detector configuration filename from the 'use detector' path by
		// converting :
		//    getDetectorConfiguration(n).isUseDetectorInScan to getDetectorConfiguration(n).getConfigFileName()
		var iter = result.get().getParameterValues().listIterator();
		while(iter.hasNext()) {
			ParameterValue paramValue = iter.next();
			String pathToGetter = paramValue.getFullPathToGetter();
			if (pathToGetter.startsWith(GET_DETECTOR_CONFIG_NAME)) {
				// Extract the index from the string:
				String[] splitStr = pathToGetter.split("[()]");
				int configNumber = Integer.parseInt(splitStr[1]);

				// Make the new path :
				String pathToDoMove = detectorFilenameGetter(configNumber);
				// Insert to the list immediately after the current item
				iter.add(new ParameterValue(pathToDoMove, ""));
			}
		}
	}

}
