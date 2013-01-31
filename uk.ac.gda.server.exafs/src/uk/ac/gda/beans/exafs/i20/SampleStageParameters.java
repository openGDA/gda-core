/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

/**
 * class to hold sample stage parameters
 */
public class SampleStageParameters implements Serializable {

	private Boolean useSample1 = false;
	private Double sample1_x = 0.;
	private Double sample1_y = 0.;
	private Double sample1_z = 0.;
	private Double sample1_rotation;
	private Double sample1_finerotation = 0.;
	private Double sample1_roll = 0.;
	private Double sample1_pitch = 0.;
	private String sample1_name  = "";
	private String sample1_description = "";
	private Boolean useSample2 = false;
	private Double sample2_x = 0.;
	private Double sample2_y = 0.;
	private Double sample2_z = 0.;
	private Double sample2_rotation = 0.;
	private Double sample2_finerotation = 0.;
	private Double sample2_roll = 0.;
	private Double sample2_pitch = 0.;
	private String sample2_name  = "";
	private String sample2_description = "";
	private Boolean useSample3 = false;
	private Double sample3_x = 0.;
	private Double sample3_y = 0.;
	private Double sample3_z = 0.;
	private Double sample3_rotation = 0.;
	private Double sample3_finerotation = 0.;
	private Double sample3_roll = 0.;
	private Double sample3_pitch = 0.;
	private String sample3_name  = "";
	private String sample3_description = "";
	private Boolean useSample4 = false;
	private Double sample4_x = 0.;
	private Double sample4_y = 0.;
	private Double sample4_z = 0.;
	private Double sample4_rotation = 0.;
	private Double sample4_finerotation = 0.;
	private Double sample4_roll = 0.;
	private Double sample4_pitch = 0.;
	private String sample4_name  = "";
	private String sample4_description = "";

	/*
	 * useful methods when used in scripts
	 */
	
	public Boolean[] getUses() {
		return new Boolean[]{useSample1,useSample2,useSample3,useSample4};
	}
	
	public Double[] getXs() {
		return new Double[] { sample1_x, sample2_x, sample3_x, sample4_x };
	}

	public Double[] getYs() {
		return new Double[] { sample1_y, sample2_y, sample3_y, sample4_y };
	}

	public Double[] getZs() {
		return new Double[] { sample1_z, sample2_z, sample3_z, sample4_z };
	}

	public Double[] getRotations() {
		return new Double[] { sample1_rotation, sample2_rotation, sample3_rotation, sample4_rotation };
	}

	public Double[] getFineRotations() {
		return new Double[] { sample1_finerotation, sample2_finerotation, sample3_finerotation, sample4_finerotation };
	}

	public Double[] getRolls() {
		return new Double[] { sample1_roll, sample2_roll, sample3_roll, sample4_roll };
	}

	public Double[] getPitches() {
		return new Double[] { sample1_pitch, sample2_pitch, sample3_pitch, sample4_pitch };
	}

	public String[] getSampleNames() {
		return new String[] { sample1_name, sample2_name, sample3_name, sample4_name };
	}

	public String[] getSampleDescriptions() {
		return new String[] { sample1_description, sample2_description, sample3_description, sample4_description };
	}

	public Boolean getUseSample1() {
		return useSample1;
	}

	public void setUseSample1(Boolean useSample1) {
		this.useSample1 = useSample1;
	}

	public Double getSample1_x() {
		return sample1_x;
	}

	public void setSample1_x(Double sample1_x) {
		this.sample1_x = sample1_x;
	}

	public Double getSample1_y() {
		return sample1_y;
	}

	public void setSample1_y(Double sample1_y) {
		this.sample1_y = sample1_y;
	}

	public Double getSample1_z() {
		return sample1_z;
	}

	public void setSample1_z(Double sample1_z) {
		this.sample1_z = sample1_z;
	}

	public Double getSample1_rotation() {
		return sample1_rotation;
	}

	public void setSample1_rotation(Double sample1_rotation) {
		this.sample1_rotation = sample1_rotation;
	}

	public Double getSample1_finerotation() {
		return sample1_finerotation;
	}

	public void setSample1_finerotation(Double sample1_finerotation) {
		this.sample1_finerotation = sample1_finerotation;
	}

	public Double getSample1_roll() {
		return sample1_roll;
	}

	public void setSample1_roll(Double sample1_roll) {
		this.sample1_roll = sample1_roll;
	}

	public Double getSample1_pitch() {
		return sample1_pitch;
	}

	public void setSample1_pitch(Double sample1_pitch) {
		this.sample1_pitch = sample1_pitch;
	}

	public String getSample1_name() {
		return sample1_name;
	}

	public void setSample1_name(String sample1_name) {
		this.sample1_name = sample1_name;
	}

	public String getSample1_description() {
		return sample1_description;
	}

	public void setSample1_description(String sample1_description) {
		this.sample1_description = sample1_description;
	}

	public Boolean getUseSample2() {
		return useSample2;
	}

	public void setUseSample2(Boolean useSample2) {
		this.useSample2 = useSample2;
	}

	public Double getSample2_x() {
		return sample2_x;
	}

	public void setSample2_x(Double sample2_x) {
		this.sample2_x = sample2_x;
	}

	public Double getSample2_y() {
		return sample2_y;
	}

	public void setSample2_y(Double sample2_y) {
		this.sample2_y = sample2_y;
	}

	public Double getSample2_z() {
		return sample2_z;
	}

	public void setSample2_z(Double sample2_z) {
		this.sample2_z = sample2_z;
	}

	public Double getSample2_rotation() {
		return sample2_rotation;
	}

	public void setSample2_rotation(Double sample2_rotation) {
		this.sample2_rotation = sample2_rotation;
	}

	public Double getSample2_finerotation() {
		return sample2_finerotation;
	}

	public void setSample2_finerotation(Double sample2_finerotation) {
		this.sample2_finerotation = sample2_finerotation;
	}

	public Double getSample2_roll() {
		return sample2_roll;
	}

	public void setSample2_roll(Double sample2_roll) {
		this.sample2_roll = sample2_roll;
	}

	public Double getSample2_pitch() {
		return sample2_pitch;
	}

	public void setSample2_pitch(Double sample2_pitch) {
		this.sample2_pitch = sample2_pitch;
	}

	public String getSample2_name() {
		return sample2_name;
	}

	public void setSample2_name(String sample2_name) {
		this.sample2_name = sample2_name;
	}

	public String getSample2_description() {
		return sample2_description;
	}

	public void setSample2_description(String sample2_description) {
		this.sample2_description = sample2_description;
	}

	public Boolean getUseSample3() {
		return useSample3;
	}

	public void setUseSample3(Boolean useSample3) {
		this.useSample3 = useSample3;
	}

	public Double getSample3_x() {
		return sample3_x;
	}

	public void setSample3_x(Double sample3_x) {
		this.sample3_x = sample3_x;
	}

	public Double getSample3_y() {
		return sample3_y;
	}

	public void setSample3_y(Double sample3_y) {
		this.sample3_y = sample3_y;
	}

	public Double getSample3_z() {
		return sample3_z;
	}

	public void setSample3_z(Double sample3_z) {
		this.sample3_z = sample3_z;
	}

	public Double getSample3_rotation() {
		return sample3_rotation;
	}

	public void setSample3_rotation(Double sample3_rotation) {
		this.sample3_rotation = sample3_rotation;
	}

	public Double getSample3_finerotation() {
		return sample3_finerotation;
	}

	public void setSample3_finerotation(Double sample3_finerotation) {
		this.sample3_finerotation = sample3_finerotation;
	}

	public Double getSample3_roll() {
		return sample3_roll;
	}

	public void setSample3_roll(Double sample3_roll) {
		this.sample3_roll = sample3_roll;
	}

	public Double getSample3_pitch() {
		return sample3_pitch;
	}

	public void setSample3_pitch(Double sample3_pitch) {
		this.sample3_pitch = sample3_pitch;
	}

	public String getSample3_name() {
		return sample3_name;
	}

	public void setSample3_name(String sample3_name) {
		this.sample3_name = sample3_name;
	}

	public String getSample3_description() {
		return sample3_description;
	}

	public void setSample3_description(String sample3_description) {
		this.sample3_description = sample3_description;
	}

	public Boolean getUseSample4() {
		return useSample4;
	}

	public void setUseSample4(Boolean useSample4) {
		this.useSample4 = useSample4;
	}

	public Double getSample4_x() {
		return sample4_x;
	}

	public void setSample4_x(Double sample4_x) {
		this.sample4_x = sample4_x;
	}

	public Double getSample4_y() {
		return sample4_y;
	}

	public void setSample4_y(Double sample4_y) {
		this.sample4_y = sample4_y;
	}

	public Double getSample4_z() {
		return sample4_z;
	}

	public void setSample4_z(Double sample4_z) {
		this.sample4_z = sample4_z;
	}

	public Double getSample4_rotation() {
		return sample4_rotation;
	}

	public void setSample4_rotation(Double sample4_rotation) {
		this.sample4_rotation = sample4_rotation;
	}

	public Double getSample4_finerotation() {
		return sample4_finerotation;
	}

	public void setSample4_finerotation(Double sample4_finerotation) {
		this.sample4_finerotation = sample4_finerotation;
	}

	public Double getSample4_roll() {
		return sample4_roll;
	}

	public void setSample4_roll(Double sample4_roll) {
		this.sample4_roll = sample4_roll;
	}

	public Double getSample4_pitch() {
		return sample4_pitch;
	}

	public void setSample4_pitch(Double sample4_pitch) {
		this.sample4_pitch = sample4_pitch;
	}

	public String getSample4_name() {
		return sample4_name;
	}

	public void setSample4_name(String sample4_name) {
		this.sample4_name = sample4_name;
	}

	public String getSample4_description() {
		return sample4_description;
	}

	public void setSample4_description(String sample4_description) {
		this.sample4_description = sample4_description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sample1_description == null) ? 0 : sample1_description.hashCode());
		result = prime * result + ((sample1_finerotation == null) ? 0 : sample1_finerotation.hashCode());
		result = prime * result + ((sample1_name == null) ? 0 : sample1_name.hashCode());
		result = prime * result + ((sample1_pitch == null) ? 0 : sample1_pitch.hashCode());
		result = prime * result + ((sample1_roll == null) ? 0 : sample1_roll.hashCode());
		result = prime * result + ((sample1_rotation == null) ? 0 : sample1_rotation.hashCode());
		result = prime * result + ((sample1_x == null) ? 0 : sample1_x.hashCode());
		result = prime * result + ((sample1_y == null) ? 0 : sample1_y.hashCode());
		result = prime * result + ((sample1_z == null) ? 0 : sample1_z.hashCode());
		result = prime * result + ((sample2_description == null) ? 0 : sample2_description.hashCode());
		result = prime * result + ((sample2_finerotation == null) ? 0 : sample2_finerotation.hashCode());
		result = prime * result + ((sample2_name == null) ? 0 : sample2_name.hashCode());
		result = prime * result + ((sample2_pitch == null) ? 0 : sample2_pitch.hashCode());
		result = prime * result + ((sample2_roll == null) ? 0 : sample2_roll.hashCode());
		result = prime * result + ((sample2_rotation == null) ? 0 : sample2_rotation.hashCode());
		result = prime * result + ((sample2_x == null) ? 0 : sample2_x.hashCode());
		result = prime * result + ((sample2_y == null) ? 0 : sample2_y.hashCode());
		result = prime * result + ((sample2_z == null) ? 0 : sample2_z.hashCode());
		result = prime * result + ((sample3_description == null) ? 0 : sample3_description.hashCode());
		result = prime * result + ((sample3_finerotation == null) ? 0 : sample3_finerotation.hashCode());
		result = prime * result + ((sample3_name == null) ? 0 : sample3_name.hashCode());
		result = prime * result + ((sample3_pitch == null) ? 0 : sample3_pitch.hashCode());
		result = prime * result + ((sample3_roll == null) ? 0 : sample3_roll.hashCode());
		result = prime * result + ((sample3_rotation == null) ? 0 : sample3_rotation.hashCode());
		result = prime * result + ((sample3_x == null) ? 0 : sample3_x.hashCode());
		result = prime * result + ((sample3_y == null) ? 0 : sample3_y.hashCode());
		result = prime * result + ((sample3_z == null) ? 0 : sample3_z.hashCode());
		result = prime * result + ((sample4_description == null) ? 0 : sample4_description.hashCode());
		result = prime * result + ((sample4_finerotation == null) ? 0 : sample4_finerotation.hashCode());
		result = prime * result + ((sample4_name == null) ? 0 : sample4_name.hashCode());
		result = prime * result + ((sample4_pitch == null) ? 0 : sample4_pitch.hashCode());
		result = prime * result + ((sample4_roll == null) ? 0 : sample4_roll.hashCode());
		result = prime * result + ((sample4_rotation == null) ? 0 : sample4_rotation.hashCode());
		result = prime * result + ((sample4_x == null) ? 0 : sample4_x.hashCode());
		result = prime * result + ((sample4_y == null) ? 0 : sample4_y.hashCode());
		result = prime * result + ((sample4_z == null) ? 0 : sample4_z.hashCode());
		result = prime * result + ((useSample1 == null) ? 0 : useSample1.hashCode());
		result = prime * result + ((useSample2 == null) ? 0 : useSample2.hashCode());
		result = prime * result + ((useSample3 == null) ? 0 : useSample3.hashCode());
		result = prime * result + ((useSample4 == null) ? 0 : useSample4.hashCode());
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
		SampleStageParameters other = (SampleStageParameters) obj;
		if (sample1_description == null) {
			if (other.sample1_description != null)
				return false;
		} else if (!sample1_description.equals(other.sample1_description))
			return false;
		if (sample1_finerotation == null) {
			if (other.sample1_finerotation != null)
				return false;
		} else if (!sample1_finerotation.equals(other.sample1_finerotation))
			return false;
		if (sample1_name == null) {
			if (other.sample1_name != null)
				return false;
		} else if (!sample1_name.equals(other.sample1_name))
			return false;
		if (sample1_pitch == null) {
			if (other.sample1_pitch != null)
				return false;
		} else if (!sample1_pitch.equals(other.sample1_pitch))
			return false;
		if (sample1_roll == null) {
			if (other.sample1_roll != null)
				return false;
		} else if (!sample1_roll.equals(other.sample1_roll))
			return false;
		if (sample1_rotation == null) {
			if (other.sample1_rotation != null)
				return false;
		} else if (!sample1_rotation.equals(other.sample1_rotation))
			return false;
		if (sample1_x == null) {
			if (other.sample1_x != null)
				return false;
		} else if (!sample1_x.equals(other.sample1_x))
			return false;
		if (sample1_y == null) {
			if (other.sample1_y != null)
				return false;
		} else if (!sample1_y.equals(other.sample1_y))
			return false;
		if (sample1_z == null) {
			if (other.sample1_z != null)
				return false;
		} else if (!sample1_z.equals(other.sample1_z))
			return false;
		if (sample2_description == null) {
			if (other.sample2_description != null)
				return false;
		} else if (!sample2_description.equals(other.sample2_description))
			return false;
		if (sample2_finerotation == null) {
			if (other.sample2_finerotation != null)
				return false;
		} else if (!sample2_finerotation.equals(other.sample2_finerotation))
			return false;
		if (sample2_name == null) {
			if (other.sample2_name != null)
				return false;
		} else if (!sample2_name.equals(other.sample2_name))
			return false;
		if (sample2_pitch == null) {
			if (other.sample2_pitch != null)
				return false;
		} else if (!sample2_pitch.equals(other.sample2_pitch))
			return false;
		if (sample2_roll == null) {
			if (other.sample2_roll != null)
				return false;
		} else if (!sample2_roll.equals(other.sample2_roll))
			return false;
		if (sample2_rotation == null) {
			if (other.sample2_rotation != null)
				return false;
		} else if (!sample2_rotation.equals(other.sample2_rotation))
			return false;
		if (sample2_x == null) {
			if (other.sample2_x != null)
				return false;
		} else if (!sample2_x.equals(other.sample2_x))
			return false;
		if (sample2_y == null) {
			if (other.sample2_y != null)
				return false;
		} else if (!sample2_y.equals(other.sample2_y))
			return false;
		if (sample2_z == null) {
			if (other.sample2_z != null)
				return false;
		} else if (!sample2_z.equals(other.sample2_z))
			return false;
		if (sample3_description == null) {
			if (other.sample3_description != null)
				return false;
		} else if (!sample3_description.equals(other.sample3_description))
			return false;
		if (sample3_finerotation == null) {
			if (other.sample3_finerotation != null)
				return false;
		} else if (!sample3_finerotation.equals(other.sample3_finerotation))
			return false;
		if (sample3_name == null) {
			if (other.sample3_name != null)
				return false;
		} else if (!sample3_name.equals(other.sample3_name))
			return false;
		if (sample3_pitch == null) {
			if (other.sample3_pitch != null)
				return false;
		} else if (!sample3_pitch.equals(other.sample3_pitch))
			return false;
		if (sample3_roll == null) {
			if (other.sample3_roll != null)
				return false;
		} else if (!sample3_roll.equals(other.sample3_roll))
			return false;
		if (sample3_rotation == null) {
			if (other.sample3_rotation != null)
				return false;
		} else if (!sample3_rotation.equals(other.sample3_rotation))
			return false;
		if (sample3_x == null) {
			if (other.sample3_x != null)
				return false;
		} else if (!sample3_x.equals(other.sample3_x))
			return false;
		if (sample3_y == null) {
			if (other.sample3_y != null)
				return false;
		} else if (!sample3_y.equals(other.sample3_y))
			return false;
		if (sample3_z == null) {
			if (other.sample3_z != null)
				return false;
		} else if (!sample3_z.equals(other.sample3_z))
			return false;
		if (sample4_description == null) {
			if (other.sample4_description != null)
				return false;
		} else if (!sample4_description.equals(other.sample4_description))
			return false;
		if (sample4_finerotation == null) {
			if (other.sample4_finerotation != null)
				return false;
		} else if (!sample4_finerotation.equals(other.sample4_finerotation))
			return false;
		if (sample4_name == null) {
			if (other.sample4_name != null)
				return false;
		} else if (!sample4_name.equals(other.sample4_name))
			return false;
		if (sample4_pitch == null) {
			if (other.sample4_pitch != null)
				return false;
		} else if (!sample4_pitch.equals(other.sample4_pitch))
			return false;
		if (sample4_roll == null) {
			if (other.sample4_roll != null)
				return false;
		} else if (!sample4_roll.equals(other.sample4_roll))
			return false;
		if (sample4_rotation == null) {
			if (other.sample4_rotation != null)
				return false;
		} else if (!sample4_rotation.equals(other.sample4_rotation))
			return false;
		if (sample4_x == null) {
			if (other.sample4_x != null)
				return false;
		} else if (!sample4_x.equals(other.sample4_x))
			return false;
		if (sample4_y == null) {
			if (other.sample4_y != null)
				return false;
		} else if (!sample4_y.equals(other.sample4_y))
			return false;
		if (sample4_z == null) {
			if (other.sample4_z != null)
				return false;
		} else if (!sample4_z.equals(other.sample4_z))
			return false;
		if (useSample1 == null) {
			if (other.useSample1 != null)
				return false;
		} else if (!useSample1.equals(other.useSample1))
			return false;
		if (useSample2 == null) {
			if (other.useSample2 != null)
				return false;
		} else if (!useSample2.equals(other.useSample2))
			return false;
		if (useSample3 == null) {
			if (other.useSample3 != null)
				return false;
		} else if (!useSample3.equals(other.useSample3))
			return false;
		if (useSample4 == null) {
			if (other.useSample4 != null)
				return false;
		} else if (!useSample4.equals(other.useSample4))
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
