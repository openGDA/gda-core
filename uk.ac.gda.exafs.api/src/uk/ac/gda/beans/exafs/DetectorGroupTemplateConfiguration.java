/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;
import uk.ac.gda.util.io.FileUtils;

/**
 * This class stores configuration of which detectors to use for different experiment types,
 * and which configuration template files to use for each type of detector.
 * This is used in {@link FluorescenceComposite} (i.e. ' Detector Parameters' view) to set the list of available detectors for
 * transmission, fluorescence, XES etc. experiment types. and to get the config file for each detector.
 */
public class DetectorGroupTemplateConfiguration extends FindableBase {
	private static final Logger logger = LoggerFactory.getLogger(DetectorGroupTemplateConfiguration.class);

	private Map<String, List<String>> detectorGroupsMap = new HashMap<>(); // <experimentType, List of names detectorGroups
	private Map<String, String> detectorTemplateMap = new HashMap<>(); // <detector name, template file with default parameters

	/**Return map : name of detector scannable object to name of template configuration file
	 * that can be used to configure it.
	 *
	 * @return Map
	 */
	public Map<String, String> getDetectorTemplateMap() {
		return detectorTemplateMap;
	}

	/**
	 * Set map to configure which configuration file to use.for each detector.
	 * <li>key = name of detector object
	 * <li>value = full path to template configuration file
	 */
	public void setDetectorTemplateMap(Map<String, String> detectorTemplateMap) {
		this.detectorTemplateMap = detectorTemplateMap;
	}

	/**
	 * Return map  : name of group, to list of detectors to use for group
	 * @return Map
	 */
	public Map<String, List<String>> getDetectorGroupsMap() {
		return detectorGroupsMap;
	}

	/**
	 * Set map from name of experiment type, to list of detectors groups that can be used for that type:
	 * {@link #addDetectorGroups(String, List)}.	 *
	 * @param detectorGroupsMap
	 */
	public void setDetectorGroupsMap(Map<String, List<String>> detectorGroupsMap) {
		this.detectorGroupsMap = detectorGroupsMap;
	}

	/** Add list of detector groups to map.
	 * @param experimentType - one of {@link DetectorParameters#TRANSMISSION_TYPE}, {@link DetectorParameters#FLUORESCENCE_TYPE} etc.
	 * @param detectorGroups list of names of detector groups for the experiment type (i.e. one of named detector groups in Detector_parameters.xml file)
	 */
	public void addDetectorGroups(String experimentType, List<String> detectorGroups) {
		detectorGroupsMap.put(experimentType, detectorGroups);
	}

	/**
	 * Copy template XML file for a detector. If destination file already exists,
	 * new unique name is generated by appending an integer (using {@link FileUtils#getUnique(File, String, String)}).
	 *
	 * @param detectorName name of the detector object (i.e. one of the detectors in the detector template map - set by {@link #setDetectorTemplateMap(Map)})
	 * @param outputFolder destination folder for the copied file
	 * @param fileName name of new file (if empty, the new name will be same as the template name).
	 * @return Full path to the newly created file
	 * @throws FileNotFoundException
	 */
	public String copyConfigFromTemplate(String detectorName, String outputFolder, String fileName) throws IOException {
		if (!detectorTemplateMap.containsKey(detectorName)) {
			throw new FileNotFoundException("No template found for "+detectorName+" detector.");
		}

		// Check if template file exists
		File templateFile = Paths.get(detectorTemplateMap.get(detectorName)).toFile(); // full path to file
		if (!templateFile.isFile() || !templateFile.canRead()){
			throw new FileNotFoundException("Could not read template for "+detectorName+" from file "+templateFile);
		}

		// Set the default filename to match template name if fileName has not been given
		String templateFileName = templateFile.getName();
		if (StringUtils.isEmpty(fileName)) {
			fileName = templateFileName;
		}

		String newFileName = fileName;
		Path outputPath = Paths.get(outputFolder);
		// If file already exists, make a new one with unique name
		if (Files.isRegularFile(outputPath.resolve(fileName))) {
			newFileName = FileUtils.getUnique(outputPath.toFile(), FilenameUtils.getBaseName(fileName), "xml").getName();
		}

		Path newFilePath = outputPath.resolve(newFileName);
		logger.info("Copying detector parameters file from {} to {}", templateFile.getAbsolutePath(), newFilePath.toAbsolutePath());
		FileUtils.copy(templateFile, newFilePath.toFile());

		return newFilePath.toAbsolutePath().toString();
	}
}