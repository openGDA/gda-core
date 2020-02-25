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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the meta property of a response from the EPICS archiver REST API. The structure and naming of its properties
 * map directly onto the JSON objects supplied by the API. The top-level object is {@link EpicsArchiverRecord}) which has a single
 * instance of this class.
 *
 * @author too27251
 */

public class EpicsArchiverRecordMetadata {

	private String name;
	private String egu;
	private String prec;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("EGU")
	public String getEgu() {
		return egu;
	}

	@JsonProperty("EGU")
	public void setEgu(String egu) {
		this.egu = egu;
	}

	@JsonProperty("PREC")
	public String getPrec() {
		return prec;
	}

	@JsonProperty("PREC")
	public void setPrec(String prec) {
		this.prec = prec;
	}
}
