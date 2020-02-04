/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.archiverclient;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a response from the EPICS archiver REST API. The structure and naming of its properties
 * map directly onto the JSON objects supplied by the API.
 *
 * @author too27251
 */

public class EpicsArchiverRecord {

	private EpicsArchiverRecordMetadata meta;
	private List<EpicsArchiverRecordData> data;

	@JsonProperty("meta")
	public EpicsArchiverRecordMetadata getMeta() {
		return meta;
	}

	@JsonProperty("meta")
	public void setMeta(EpicsArchiverRecordMetadata meta) {
		this.meta = meta;
	}

	@JsonProperty("data")
	public List<EpicsArchiverRecordData> getData() {
		return data;
	}

	@JsonProperty("data")
	public void setData(List<EpicsArchiverRecordData> data) {
		this.data = data;
	}
}
