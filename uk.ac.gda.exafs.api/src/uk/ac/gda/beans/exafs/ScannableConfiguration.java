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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;

/**
 * Defines a position for a particular scannable.
 * The position is typed as a string to simplify serialisation to XML
 */
public class ScannableConfiguration implements Serializable {

	private String scannableName;

	/** typed as String to simplify serialisation to XML */
	private String position;

	public ScannableConfiguration() {}

	public ScannableConfiguration(String scannableName, String position) {
		this.scannableName = scannableName;
		this.position = position;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		final var prime = 31;
		var result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
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
		ScannableConfiguration other = (ScannableConfiguration) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScannableConfiguration [scannableName=" + scannableName + ", position=" + position + "]";
	}

}
