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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Defines the tomography operational file structure.
 * Such structure defines a <i>tomography</i> folder under the {@code $xml} folder and inside this, another two folders for
 * <i>configurations</i> and <i>savu processing files</i>
 *
 * @author Maurizio Nagni
 */
@Component
class TomographyFileContext extends AcquisitionFileBaseContext<TomographyContextFile>{

	private static final Logger logger = LoggerFactory.getLogger(TomographyFileContext.class);

	public static final String TOMOGRAPHY_OPERATIONAL_DIRECTORY_PROPERTY = "experiments.tomography";
	public static final String TOMOGRAPHY_OPERATIONAL_DIRECTORY_PROPERTY_DEFAULT = "tomography";

	public static final String TOMOGRAPHY_CONFIGURATION_DIRECTORY_PROPERTY =	"experiments.tomography.configurations";
	public static final String TOMOGRAPHY_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT =	"configurations";

	public static final String TOMOGRAPHY_SAVU_DIRECTORY_PROPERTY =	"experiments.tomography.savu";
	public static final String TOMOGRAPHY_SAVU_DIRECTORY_PROPERTY_DEFAULT =	"savu";

	private void initializeOperationalDir() {
		initializeDirectoryInConfigDir(TOMOGRAPHY_OPERATIONAL_DIRECTORY_PROPERTY,
				TOMOGRAPHY_OPERATIONAL_DIRECTORY_PROPERTY_DEFAULT,
				TomographyContextFile.TOMOGRAPHY_OPERATIONAL_DIRECTORY);
	}

	private void initializeConfigurationDir() {
		initializeDirectory(getContextFile(TomographyContextFile.TOMOGRAPHY_OPERATIONAL_DIRECTORY),
				TOMOGRAPHY_CONFIGURATION_DIRECTORY_PROPERTY,
				TOMOGRAPHY_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT,
				TomographyContextFile.TOMOGRAPHY_CONFIGURATION_DIRECTORY);
	}

	private void initializeCalibrationDir() {
		initializeDirectory(getContextFile(TomographyContextFile.TOMOGRAPHY_OPERATIONAL_DIRECTORY),
				TOMOGRAPHY_SAVU_DIRECTORY_PROPERTY,
				TOMOGRAPHY_SAVU_DIRECTORY_PROPERTY_DEFAULT,
				TomographyContextFile.TOMOGRAPHY_SAVU_DIRECTORY);
	}

	@Override
	protected void initializeFolderStructure() {
		initializeOperationalDir();
		initializeConfigurationDir();
		initializeCalibrationDir();
	}

	/**
	 * Set a {@code URL} to be used as default reconstruction processing file for any tomography.
	 * The file can then be retrieved using {@code getContextFile(TomographyContextFile.TOOGRAPHY_DEFAULT_PROCESSING_FILE)};
	 * @param processingFile
	 * @return {@code true} if the file exists and the operation succeeds, otherwise {@code false}
	 */
	public boolean putProcessingFileInContext(URL processingFile) {
		return putFileInContext(TomographyContextFile.TOMOGRAPHY_DEFAULT_PROCESSING_FILE, processingFile);
	}
}