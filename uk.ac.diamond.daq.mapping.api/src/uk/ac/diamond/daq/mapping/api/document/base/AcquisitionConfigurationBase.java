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

import java.util.Map;

import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;

public class AcquisitionConfigurationBase<T extends AcquisitionParametersBase> implements AcquisitionConfiguration<T> {

	private T acquisitionParameters;
	private Map<String, String> metadata;
	private ImageCalibration imageCalibration;
	private MultipleScans multipleScans;

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
}