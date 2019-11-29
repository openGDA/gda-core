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

package uk.ac.diamond.daq.mapping.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.daq.application.persistence.annotation.CustomPersistable;
import uk.ac.diamond.daq.application.persistence.annotation.Id;
import uk.ac.diamond.daq.application.persistence.annotation.Listable;
import uk.ac.diamond.daq.application.persistence.annotation.Persistable;
import uk.ac.diamond.daq.application.persistence.annotation.Version;
import uk.ac.diamond.daq.persistence.manager.serializer.ClassLoaderSerialisationMethod;

/**
 * Wrapper for {@link IMappingExperimentBean} so it can be used with the Persistence Service.
 */
@Persistable
public class PersistableMappingExperimentBean {
	public static final String SCAN_NAME_TITLE = "Scan Name";

	@Id
	@JsonProperty
	private long id;

	@Version
	@JsonProperty
	private long version;

	@Listable(value = SCAN_NAME_TITLE, searchResultOrder = 1)
	@JsonProperty
	private String scanName;

	@CustomPersistable(ClassLoaderSerialisationMethod.class)
	@JsonProperty
	private IMappingExperimentBean mappingBean;

	@JsonIgnore
	public long getId() {
		return id;
	}

	@JsonIgnore
	public long getVersion() {
		return version;
	}

	@JsonIgnore
	public String getScanName() {
		return scanName;
	}

	@JsonIgnore
	public void setScanName(String scanName) {
		this.scanName = scanName;
	}

	@JsonIgnore
	public IMappingExperimentBean getMappingBean() {
		mappingBean.setId(id);
		mappingBean.setDisplayName(scanName);
		return mappingBean;
	}

	@JsonIgnore
	public void setMappingBean(IMappingExperimentBean mappingBean) {
		this.mappingBean = mappingBean;
		this.id = mappingBean.getId();
		this.scanName = mappingBean.getDisplayName();
	}
}
