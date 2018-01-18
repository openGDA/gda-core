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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gda.factory.Findable;

/**
 * This class stores configuration of which detectors to use for different experiment types,
 * and which configuration template files to use for each type of detector.
 * This is used in {@link FluorescenceComposite} (i.e. ' Detector Parameters' view) to set the list of available detectors for
 * transmission, fluorescence, XES etc. experiment types. and to get the config file for each detector.
 */
public class DetectorGroupTemplateConfiguration implements Findable {

	public static final String NAME = "detectorGroupTemplateConfiguration";

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
	 *
	 */
	/**
	 * Set map from name of experiment type, to list of detectors groups that can be used for that type:
	 * {@link #addDetectorGroups(String, List)}.	 *
	 * @param detectorGroupsMap
	 */
	public void setDetectorGroupsMap(Map<String, List<String>> detectorGroupsMap) {
		this.detectorGroupsMap = detectorGroupsMap;
	}

	/** Add list of detector groups to map.
	 * @param experimentType - one of {@link DetectorParameters#TRANSMISSION_TYPE}, {@link DetectorParameters.FLUORESCENCE_TYPE} etc.
	 * @param detectorGroups list of names of detector groups for the experiment type (i.e. one of named detector groups in Detector_parameters.xml file)
	 */
	public void addDetectorGroups(String experimentType, List<String> detectorGroups) {
		detectorGroupsMap.put(experimentType, detectorGroups);
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public String getName() {
		return NAME;
	}
}
