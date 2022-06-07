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
import java.util.Objects;
import java.util.UUID;

import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.AcquisitionType;

/**
 * Describes a specific tomography execution
 *
 * @author Maurizio Nagni
 */
public class AcquisitionBase<T extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> implements Acquisition<T> {

	private UUID uuid;
	private String name;
	private String description;
	private Period executionPeriod;
	private URL acquisitionLocation;

	private T acquisitionConfiguration;
	private AcquisitionEngineDocument acquisitionEngine;

	private AcquisitionType type;

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

	@Override
	public AcquisitionEngineDocument getAcquisitionEngine() {
		return acquisitionEngine;
	}

	public void setAcquisitionEngine(AcquisitionEngineDocument acquisitionEngine) {
		this.acquisitionEngine = acquisitionEngine;
	}

	@Override
	public AcquisitionType getType() {
		return type;
	}

	public void setType(AcquisitionType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(acquisitionConfiguration, acquisitionEngine, acquisitionLocation, description,
				executionPeriod, name, type, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AcquisitionBase<?> other = (AcquisitionBase<?>) obj;
		return Objects.equals(acquisitionConfiguration, other.acquisitionConfiguration)
				&& Objects.equals(acquisitionEngine, other.acquisitionEngine)
				&& Objects.equals(acquisitionLocation, other.acquisitionLocation)
				&& Objects.equals(description, other.description)
				&& Objects.equals(executionPeriod, other.executionPeriod) && Objects.equals(name, other.name)
				&& type == other.type && Objects.equals(uuid, other.uuid);
	}

}
