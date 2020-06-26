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

package uk.ac.diamond.daq.mapping.api.document;

import java.net.URL;
import java.time.Period;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.AcquisitionConfiguration;

/**
 * Implements a {@link Acquisition} subclass for mapping-specific documents. Classes extending this one may define
 * domain specific acquisitions (diffraction, tomography, etc) inside the mapping framework.
 *
 * <p>
 * The class is annotated with {@link JsonTypeInfo} consequently is possible to serialise/deserialize subclasses that
 * are registered with the jackson {@link ObjectMapper}
 * </p>
 * @see DocumentMapper DiffractionParameterAcquisition
 * @author Maurizio Nagni
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
public class AcquisitionBase<T extends AcquisitionConfiguration<?>> implements Acquisition<T> {

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
