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

import java.util.Objects;

/**
 * Describes a scannable position in the client properties
 */
public class ScannablePropertiesValue {

	public enum PositionType {
		/** The absolute target position is configured **/
		ABSOLUTE,

		/** The configured position should be added to the current position of the scannable **/
		RELATIVE,

		/** position not set: use scannable's current position **/
		CURRENT
	}

	private String scannableName;
	private ScannableKeys scannableKeys;
	private PositionType positionType = PositionType.ABSOLUTE;
	private Object position;


	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/**
	 * Identifies the scannable.
	 * If {@code null}, get the scannable name directly via {@link #getScannableName()}
	 */
	public ScannableKeys getScannableKeys() {
		return scannableKeys;
	}

	public void setScannableKeys(ScannableKeys scannableKeys) {
		this.scannableKeys = scannableKeys;
	}

	/**
	 * The type of this will depend on the scannable referred to by {@link #getScannableKeys()}.<p>
	 * May be {@code null}; see {@link #getPositionType()} for a hint on handling.
	 */
	public Object getPosition() {
		return position;
	}

	public void setPosition(Object position) {
		this.position = position;
	}

	/** A hint for interpreting {@link #getPosition()}. */
	public PositionType getPositionType() {
		return positionType;
	}

	public void setPositionType(PositionType positionType) {
		this.positionType = positionType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(position, positionType, scannableKeys);
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
		return Objects.equals(position, other.position) && positionType == other.positionType
				&& Objects.equals(scannableKeys, other.scannableKeys);
	}

}