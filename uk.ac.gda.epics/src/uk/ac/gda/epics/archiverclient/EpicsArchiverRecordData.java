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
 * Represents the data property of a response from the EPICS archiver REST API. The structure and naming of its properties
 * map directly onto the JSON objects supplied by the API. The top-level object is {@link EpicsArchiverRecord}) which holds
 * a collection of EpicsArchiverRecordData.
 *
 * @author too27251
 */

public class EpicsArchiverRecordData {

	private int secs;
	private double val;
	private int nanos;
	private int severity;
	private int status;

	@JsonProperty("secs")
	public int getSecs() {
		return secs;
	}

	@JsonProperty("secs")
	public void setSecs(int secs) {
		this.secs = secs;
	}

	@JsonProperty("val")
	public double getVal() {
		return val;
	}

	@JsonProperty("val")
	public void setVal(double val) {
		this.val = val;
	}

	@JsonProperty("nanos")
	public int getNanos() {
		return nanos;
	}

	@JsonProperty("nanos")
	public void setNanos(int nanos) {
		this.nanos = nanos;
	}

	@JsonProperty("severity")
	public int getSeverity() {
		return severity;
	}

	@JsonProperty("severity")
	public void setSeverity(int severity) {
		this.severity = severity;
	}

	@JsonProperty("status")
	public int getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(int status) {
		this.status = status;
	}
}
