/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document.base;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Implementation of {@link AcquisitionConfiguration} which uses classes extending {@link AcquisitionParametersBase}
 *
 * @param <T> a class extending {@code AcquisitionParametersBase}
 *
 * @author Maurizio Nagni
 */
public class AcquisitionConfigurationBase<T extends AcquisitionParametersBase> implements AcquisitionConfiguration<T> {

	private T acquisitionParameters;
	private Map<String, String> metadata;
	private ImageCalibration imageCalibration;
	private MultipleScans multipleScans;
	private List<ProcessingRequestPair<?>> processingRequest;
	private Set<DevicePositionDocument> endPosition;

	@Override
	public T getAcquisitionParameters() {
		return acquisitionParameters;
	}
	public void setAcquisitionParameters(T acquisitionParameters) {
		this.acquisitionParameters = acquisitionParameters;
	}
	@Override
	public Map<String, String> getMetadata() {
		return metadata;
	}
	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
	@Override
	public ImageCalibration getImageCalibration() {
		return imageCalibration;
	}
	public void setImageCalibration(ImageCalibration imageCalibration) {
		this.imageCalibration = imageCalibration;
	}
	@Override
	public MultipleScans getMultipleScans() {
		return multipleScans;
	}
	public void setMultipleScans(MultipleScans multipleScans) {
		this.multipleScans = multipleScans;
	}
	@Override
	public List<ProcessingRequestPair<?>> getProcessingRequest() {
		return processingRequest;
	}
	public void setProcessingRequest(List<ProcessingRequestPair<?>> processingRequest) {
		this.processingRequest = processingRequest;
	}

	/**
	 * Defines where the beamline is supposed to return at the end of the acquisition.
	 * <p>
	 * An Acquisition may be composed by multiple sections: acquisition, calibrations (that is dark, flat) eventually others.
	 * Each of those section may well have its specific configuration so the beamline may change accordingly.
	 * On the other hand this property, if not empty, represent the state where to move when all the other sections have been executed.
	 * </p>
	 * @return a set of position documents, otherwise an empty set.
	 */
	@Override
	public Set<DevicePositionDocument> getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(Set<DevicePositionDocument> endPosition) {
		this.endPosition = endPosition;
	}
}
