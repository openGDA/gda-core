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

import java.net.URL;
import java.time.Period;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import uk.ac.gda.api.acquisition.Acquisition;

/**
 * Describes a specific tomography execution
 *
 * @author Maurizio Nagni
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
public class AcquisitionBase<T extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> implements Acquisition<T> {

	private UUID uuid;
	private String name;
	private String description;
	private Period executionPeriod;
	private URL acquisitionLocation;

	private T acquisitionConfiguration;

	@Override
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Period getExecutionPeriod() {
		return executionPeriod;
	}

	public void setExecutionPeriod(Period executionPeriod) {
		this.executionPeriod = executionPeriod;
	}

	@Override
	public URL getAcquisitionLocation() {
		return acquisitionLocation;
	}

	public void setAcquisitionLocation(URL acquisitionLocation) {
		this.acquisitionLocation = acquisitionLocation;
	}

	@Override
	public T getAcquisitionConfiguration() {
		return acquisitionConfiguration;
	}

	public void setAcquisitionConfiguration(T acquisitionConfiguration) {
		this.acquisitionConfiguration = acquisitionConfiguration;
	}
}
