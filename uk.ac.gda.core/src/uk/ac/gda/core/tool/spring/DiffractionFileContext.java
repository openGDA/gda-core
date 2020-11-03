/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.core.tool.spring;

import java.net.URL;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;

/**
 * Defines the diffraction operational file structure.
 * Such structure defines a <i>diffraction</i> folder under the {@code $xml} folder and inside this, another two folders for
 * <i>configurations</i> and <i>calibrations</i>
 *
 * @author Maurizio Nagni
 */
@Component
public class DiffractionFileContext extends AcquisitionFileBaseContext<DiffractionContextFile>{

	public static final String DIFFRACTION_OPERATIONAL_DIRECTORY_PROPERTY = "experiments.diffraction";
	public static final String DIFFRACTION_OPERATIONAL_DIRECTORY_PROPERTY_DEFAULT = "diffraction";

	public static final String DIFFRACTION_CONFIGURATION_DIRECTORY_PROPERTY =	"experiments.diffraction.configurations";
	public static final String DIFFRACTION_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT =	"configurations";

	public static final String DIFFRACTION_CALIBRATION_DIRECTORY_PROPERTY =	"experiments.diffraction.calibrations";
	public static final String DIFFRACTION_CALIBRATION_DIRECTORY_PROPERTY_DEFAULT =	"calibrations";

	public static final String DIFFRACTION_CALIBRATION_DIRECTORY_PERMISSIONS_PROPERTY =	"experiments.diffraction.calibrations.permissions";

	private void initializeOperationalDir() {
		initializeDirectoryInConfigDir(DIFFRACTION_OPERATIONAL_DIRECTORY_PROPERTY,
				DIFFRACTION_OPERATIONAL_DIRECTORY_PROPERTY_DEFAULT,
				DiffractionContextFile.DIFFRACTION_OPERATIONAL_DIRECTORY);
	}

	private void initializeConfigurationDir() {
		initializeDirectory(getContextFile(DiffractionContextFile.DIFFRACTION_OPERATIONAL_DIRECTORY),
				DIFFRACTION_CONFIGURATION_DIRECTORY_PROPERTY,
				DIFFRACTION_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT,
				DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY);
	}

	private void initializeCalibrationDir() {
		URL url = initializeDirectory(getContextFile(DiffractionContextFile.DIFFRACTION_OPERATIONAL_DIRECTORY),
				DIFFRACTION_CALIBRATION_DIRECTORY_PROPERTY,
				DIFFRACTION_CALIBRATION_DIRECTORY_PROPERTY_DEFAULT,
				DiffractionContextFile.DIFFRACTION_CALIBRATION_DIRECTORY);
		Optional.ofNullable(LocalProperties.get(DIFFRACTION_CALIBRATION_DIRECTORY_PERMISSIONS_PROPERTY, null))
			.ifPresent(permissions -> changeDirectoryPermissions(permissions, url));
	}

	@Override
	protected void initializeFolderStructure() {
		initializeOperationalDir();
		initializeConfigurationDir();
		initializeCalibrationDir();
	}

	/**
	 * Set a {@code URL} to be used as default calibration file for any diffraction.
	 * The file can then be retrieved using {@code getContextFile(DiffractionContextFile.DIFFRACTION_DEFAULT_CALIBRATION)};
	 * @param calibrationUrl
	 * @return {@code true} if the file exists and the operation succeeds, otherwise {@code false}
	 */
	public boolean putCalibrationInContext(URL calibrationUrl) {
		return putFileInContext(DiffractionContextFile.DIFFRACTION_DEFAULT_CALIBRATION, calibrationUrl);
	}
}