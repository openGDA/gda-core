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

package uk.ac.diamond.daq.experiment.api.plan.event;

import java.io.Serializable;
import java.time.Instant;

import uk.ac.diamond.daq.experiment.api.plan.ISegment;

/**
 * Keep track of when a particular {@link ISegment} begins and ends, and what signal caused it to terminate
 */
public class SegmentRecord implements Serializable {

	private static final long serialVersionUID = 7981329060834102814L;

	private String segmentName;
	private String sampleEnvironmentName;
	private long startTime;
	private long endTime;

	private double terminationSignal;

	public SegmentRecord(String segmentName, String sampleEnvironmentName) {
		startTime = Instant.now().toEpochMilli();
		this.segmentName = segmentName;
		this.sampleEnvironmentName = sampleEnvironmentName;
	}

	public SegmentRecord() {}

	public void terminated(double terminationSignal) {
		endTime = Instant.now().toEpochMilli();
		this.terminationSignal = terminationSignal;
	}

	public String getSegmentName() {
		return segmentName;
	}

	public void setSegmentName(String name) {
		this.segmentName = name;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public double getTerminationSignal() {
		return terminationSignal;
	}

	public void setTerminationSignal(double terminationSignal) {
		this.terminationSignal = terminationSignal;
	}

	public String getSampleEnvironmentName() {
		return sampleEnvironmentName;
	}

	public void setSampleEnvironmentName(String sampleEnvironmentName) {
		this.sampleEnvironmentName = sampleEnvironmentName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (endTime ^ (endTime >>> 32));
		result = prime * result + ((sampleEnvironmentName == null) ? 0 : sampleEnvironmentName.hashCode());
		result = prime * result + ((segmentName == null) ? 0 : segmentName.hashCode());
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		long temp;
		temp = Double.doubleToLongBits(terminationSignal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SegmentRecord other = (SegmentRecord) obj;
		if (endTime != other.endTime)
			return false;
		if (sampleEnvironmentName == null) {
			if (other.sampleEnvironmentName != null)
				return false;
		} else if (!sampleEnvironmentName.equals(other.sampleEnvironmentName))
			return false;
		if (segmentName == null) {
			if (other.segmentName != null)
				return false;
		} else if (!segmentName.equals(other.segmentName))
			return false;
		if (startTime != other.startTime)
			return false;
		if (Double.doubleToLongBits(terminationSignal) != Double.doubleToLongBits(other.terminationSignal))
			return false;
		return true;
	}

}
