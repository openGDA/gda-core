/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties.acquisition;

import java.util.List;
import java.util.Set;

import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionSubType;
import uk.ac.gda.client.properties.acquisition.processing.ProcessingRequestProperties;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;

public class AcquisitionTemplate {

	private AcquisitionPropertyType type;
	private AcquisitionSubType subType;

	private AcquisitionEngineDocument engine;
	private Set<String> detectors;

	private List<Trajectory> defaultTrajectories;

	private ProcessingRequestProperties processingProperties;

	private List<ScannablePropertiesValue> startPosition;
	private List<ScannablePropertiesValue> endPosition;

	public AcquisitionPropertyType getType() {
		return type;
	}
	public void setType(AcquisitionPropertyType type) {
		this.type = type;
	}
	public AcquisitionSubType getSubType() {
		return subType;
	}
	public void setSubType(AcquisitionSubType subType) {
		this.subType = subType;
	}
	public AcquisitionEngineDocument getEngine() {
		return engine;
	}
	public void setEngine(AcquisitionEngineDocument engine) {
		this.engine = engine;
	}
	public Set<String> getDetectors() {
		return detectors;
	}
	public void setDetectors(Set<String> detectors) {
		this.detectors = detectors;
	}
	public List<Trajectory> getDefaultTrajectories() {
		return defaultTrajectories;
	}
	public void setDefaultTrajectories(List<Trajectory> defaultTrajectories) {
		this.defaultTrajectories = defaultTrajectories;
	}
	public ProcessingRequestProperties getProcessingProperties() {
		return processingProperties;
	}
	public void setProcessingProperties(ProcessingRequestProperties processingProperties) {
		this.processingProperties = processingProperties;
	}
	public List<ScannablePropertiesValue> getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(List<ScannablePropertiesValue> startPosition) {
		this.startPosition = startPosition;
	}
	public List<ScannablePropertiesValue> getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(List<ScannablePropertiesValue> endPosition) {
		this.endPosition = endPosition;
	}

	@Override
	public String toString() {
		return "Acquisition template for " + type.toString() + ":" + subType.toString();
	}

}
