/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs.i20;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

public class CryostatSampleDetails implements Serializable {
	private static final long serialVersionUID = 8260616108202580177L;
	private Double position = 0.0;
	private Double finePosition = 0.0;
	private String sample_name = "";
	private String sampleDescription = "";
	private Integer numberOfRepetitions = 1;

	public Double getPosition() {
		return position;
	}

	public void setPosition(Double position) {
		this.position = position;
	}

	public Double getFinePosition() {
		return finePosition;
	}

	public void setFinePosition(Double finePosition) {
		this.finePosition = finePosition;
	}

	public String getSample_name() {
		return sample_name;
	}

	public void setSample_name(String sample_name) {
		this.sample_name = sample_name;
	}

	public String getSampleDescription() {
		return sampleDescription;
	}

	public void setSampleDescription(String sampleDescription) {
		this.sampleDescription = sampleDescription;
	}

	public Integer getNumberOfRepetitions() {
		return numberOfRepetitions;
	}

	public void setNumberOfRepetitions(Integer numberOfRepetitions) {
		this.numberOfRepetitions = numberOfRepetitions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((finePosition == null) ? 0 : finePosition.hashCode());
		result = prime * result + ((numberOfRepetitions == null) ? 0 : numberOfRepetitions.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((sampleDescription == null) ? 0 : sampleDescription.hashCode());
		result = prime * result + ((sample_name == null) ? 0 : sample_name.hashCode());
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
		CryostatSampleDetails other = (CryostatSampleDetails) obj;
		if (finePosition == null) {
			if (other.finePosition != null)
				return false;
		}
		else if (!finePosition.equals(other.finePosition))
			return false;
		if (numberOfRepetitions == null) {
			if (other.numberOfRepetitions != null)
				return false;
		}
		else if (!numberOfRepetitions.equals(other.numberOfRepetitions))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		}
		else if (!position.equals(other.position))
			return false;
		if (sampleDescription == null) {
			if (other.sampleDescription != null)
				return false;
		}
		else if (!sampleDescription.equals(other.sampleDescription))
			return false;
		if (sample_name == null) {
			if (other.sample_name != null)
				return false;
		}
		else if (!sample_name.equals(other.sample_name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

}