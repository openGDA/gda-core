/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties.stage.position;

import gda.device.EnumPositioner;
import gda.device.IScannableMotor;

/**
 * Describes a scannable prosition in the client properties
 *
 * @author Maurizio Nagni
 */
public class ScannablePropertiesValue {

	private ScannableKeys scannableKeys;
	private String labelledPosition;
	private double position;
	private double delta;

	/**
	 * Identifies the scannable
	 *
	 * @return a scannable identificator
	 */
	public ScannableKeys getScannableKeys() {
		return scannableKeys;
	}

	public void setScannableKeys(ScannableKeys scannableKeys) {
		this.scannableKeys = scannableKeys;
	}

	/**
	 * For an {@link EnumPositioner} scannable return the required position.
	 *
	 * @return the required position
	 */
	public String getLabelledPosition() {
		return labelledPosition;
	}

	public void setLabelledPosition(String labelledPosition) {
		this.labelledPosition = labelledPosition;
	}

	/**
	 * For an {@link IScannableMotor} return the required position.
	 *
	 * @return the required position
	 */
	public double getPosition() {
		return position;
	}

	public void setPosition(double position) {
		this.position = position;
	}

	/**
	 * The quantity to move the scannable from the actual position. Valid only for an {@link IScannableMotor}.
	 *
	 * @return the quantity to move the scannable
	 */
	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(delta);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((labelledPosition == null) ? 0 : labelledPosition.hashCode());
		temp = Double.doubleToLongBits(position);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((scannableKeys == null) ? 0 : scannableKeys.hashCode());
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
		ScannablePropertiesValue other = (ScannablePropertiesValue) obj;
		if (Double.doubleToLongBits(delta) != Double.doubleToLongBits(other.delta))
			return false;
		if (labelledPosition == null) {
			if (other.labelledPosition != null)
				return false;
		} else if (!labelledPosition.equals(other.labelledPosition))
			return false;
		if (Double.doubleToLongBits(position) != Double.doubleToLongBits(other.position))
			return false;
		if (scannableKeys == null) {
			if (other.scannableKeys != null)
				return false;
		} else if (!scannableKeys.equals(other.scannableKeys))
			return false;
		return true;
	}


}