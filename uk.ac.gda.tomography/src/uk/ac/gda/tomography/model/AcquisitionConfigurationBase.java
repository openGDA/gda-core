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

package uk.ac.gda.tomography.model;

import java.util.Map;
import java.util.Set;

import gda.device.Device;

public class AcquisitionConfigurationBase<T extends AcquisitionParameters> implements AcquisitionConfiguration<T> {

	private Set<Device> devices;
	private T acquisitionParameters;
	private Map<String, String> metadata;


	@Override
	public Set<Device> getDevices() {
		return devices;
	}
	public void setDevices(Set<Device> devices) {
		this.devices = devices;
	}
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



}
