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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.core.tool.spring.properties.AcquisitionFileContextProperties;
import uk.ac.gda.core.tool.spring.properties.ExperimentProperties;

/**
 * Defines the user experiment files structure
 *
 * @author Maurizio Nagni
 */
@Component
public class ExperimentFileContext extends AcquisitionFileBaseContext<ExperimentContextFile> {

	@Autowired
	private ServerSpringProperties serverProperties;

	public static final String EXPERIMENTS_OPERATIONAL_DIRECTORY_PROPERTY_DEFAULT = "experiments";
	public static final String EXPERIMENTS_PROCESSED_DIRECTORY_PROPERTY_DEFAULT = "processed";

	private void initializeOperationalDir() {
		var directoryPath = Optional.ofNullable(getExperimentProperties())
				.map(ExperimentProperties::getDirectory)
				.orElse(EXPERIMENTS_OPERATIONAL_DIRECTORY_PROPERTY_DEFAULT);

		initializeDirectoryInVisitDir(directoryPath, ExperimentContextFile.EXPERIMENTS_DIRECTORY);
	}

	private void initializeProcessedDir() {
		var directoryPath = Optional.ofNullable(getExperimentProperties())
				.map(ExperimentProperties::getProcessed)
				.orElse(EXPERIMENTS_PROCESSED_DIRECTORY_PROPERTY_DEFAULT);

		initializeDirectory(getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY),
				directoryPath,
				ExperimentContextFile.EXPERIMENTS_PROCESSED_DIRECTORY);
	}

	@Override
	protected void initializeFolderStructure() {
		initializeOperationalDir();
		initializeProcessedDir();
	}

	private ExperimentProperties getExperimentProperties() {
		return Optional.ofNullable(serverProperties)
				.map(ServerSpringProperties::getFileContexts)
				.map(AcquisitionFileContextProperties::getExperiment)
				.orElse(null);
	}
}