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

public class SampleStagePosition implements Serializable{

	private Double sample_x = 0.;
	private Double sample_y = 0.;
	private Double sample_z = 0.;
	private Double sample_rotation;
	private Double sample_finerotation = 0.;
	private Double sample_roll = 0.;
	private Double sample_pitch = 0.;
	private String sample_name = "";
	private String sample_description = "";
	private Integer numberOfRepetitions = 1;

	public Double getSample_x() {
		return sample_x;
	}

	public void setSample_x(Double sample_x) {
		this.sample_x = sample_x;
	}

	public Double getSample_y() {
		return sample_y;
	}

	public void setSample_y(Double sample_y) {
		this.sample_y = sample_y;
	}

	public Double getSample_z() {
		return sample_z;
	}

	public void setSample_z(Double sample_z) {
		this.sample_z = sample_z;
	}

	public Double getSample_rotation() {
		return sample_rotation;
	}

	public void setSample_rotation(Double sample_rotation) {
		this.sample_rotation = sample_rotation;
	}

	public Double getSample_finerotation() {
		return sample_finerotation;
	}

	public void setSample_finerotation(Double sample_finerotation) {
		this.sample_finerotation = sample_finerotation;
	}

	public Double getSample_roll() {
		return sample_roll;
	}

	public void setSample_roll(Double sample_roll) {
		this.sample_roll = sample_roll;
	}

	public Double getSample_pitch() {
		return sample_pitch;
	}

	public void setSample_pitch(Double sample_pitch) {
		this.sample_pitch = sample_pitch;
	}

	public String getSample_name() {
		return sample_name;
	}

	public void setSample_name(String sample_name) {
		this.sample_name = sample_name;
	}

	public String getSample_description() {
		return sample_description;
	}

	public void setSample_description(String sample_description) {
		this.sample_description = sample_description;
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
		result = prime * result + ((numberOfRepetitions == null) ? 0 : numberOfRepetitions.hashCode());
		result = prime * result + ((sample_description == null) ? 0 : sample_description.hashCode());
		result = prime * result + ((sample_finerotation == null) ? 0 : sample_finerotation.hashCode());
		result = prime * result + ((sample_name == null) ? 0 : sample_name.hashCode());
		result = prime * result + ((sample_pitch == null) ? 0 : sample_pitch.hashCode());
		result = prime * result + ((sample_roll == null) ? 0 : sample_roll.hashCode());
		result = prime * result + ((sample_rotation == null) ? 0 : sample_rotation.hashCode());
		result = prime * result + ((sample_x == null) ? 0 : sample_x.hashCode());
		result = prime * result + ((sample_y == null) ? 0 : sample_y.hashCode());
		result = prime * result + ((sample_z == null) ? 0 : sample_z.hashCode());
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
		SampleStagePosition other = (SampleStagePosition) obj;
		if (numberOfRepetitions == null) {
			if (other.numberOfRepetitions != null)
				return false;
		} else if (!numberOfRepetitions.equals(other.numberOfRepetitions))
			return false;
		if (sample_description == null) {
			if (other.sample_description != null)
				return false;
		} else if (!sample_description.equals(other.sample_description))
			return false;
		if (sample_finerotation == null) {
			if (other.sample_finerotation != null)
				return false;
		} else if (!sample_finerotation.equals(other.sample_finerotation))
			return false;
		if (sample_name == null) {
			if (other.sample_name != null)
				return false;
		} else if (!sample_name.equals(other.sample_name))
			return false;
		if (sample_pitch == null) {
			if (other.sample_pitch != null)
				return false;
		} else if (!sample_pitch.equals(other.sample_pitch))
			return false;
		if (sample_roll == null) {
			if (other.sample_roll != null)
				return false;
		} else if (!sample_roll.equals(other.sample_roll))
			return false;
		if (sample_rotation == null) {
			if (other.sample_rotation != null)
				return false;
		} else if (!sample_rotation.equals(other.sample_rotation))
			return false;
		if (sample_x == null) {
			if (other.sample_x != null)
				return false;
		} else if (!sample_x.equals(other.sample_x))
			return false;
		if (sample_y == null) {
			if (other.sample_y != null)
				return false;
		} else if (!sample_y.equals(other.sample_y))
			return false;
		if (sample_z == null) {
			if (other.sample_z != null)
				return false;
		} else if (!sample_z.equals(other.sample_z))
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
