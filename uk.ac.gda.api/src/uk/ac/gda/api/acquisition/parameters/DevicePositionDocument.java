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

package uk.ac.gda.api.acquisition.parameters;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Describes the required position for a displaceable device.
 *
 * <p>
 * The device is assumed one-dimensional.
 * Having a limited number of cases, instead define two separate documents, this class uses a {@link DevicePositionDocument.ValueType}
 * to discriminate between a numeric position ( {@link #getPosition()}) and a predefined, labelled one ({@link #getLabelledPosition()}).
 * <p>
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = DevicePositionDocument.Builder.class)
public class DevicePositionDocument {

	/**
	 * Defines the position value type represented by this document
	 */
	public enum ValueType {
		/**
		 * a numeric value
		 */
		NUMERIC,
		/**
		 * a string value
		 */
		LABELLED
	}

	/**
	 * An identifier, usually a Spring bean name, to allow an acquisition controller to retrieve a real instance of the
	 * device
	 */
	private final String device;
	/**
	 * A label to identify uniquely the role of this device
	 */
	private final String axis;

	private final ValueType valueType;

	/**
	 * The required position for the device
	 */
	private final double position;
	/**
	 * The required predefined position, i.e. to drive a {@code ScannablePositioner}
	 */
	private final String labelledPosition;

	private DevicePositionDocument(String scannable, String axis, ValueType valueType, double position, String labelledPosition) {
		super();
		this.device = scannable;
		this.axis = axis;
		this.valueType = valueType;
		this.position = position;
		this.labelledPosition = labelledPosition;
	}

	public String getDevice() {
		return device;
	}

	public String getAxis() {
		return axis;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public double getPosition() {
		return position;
	}

	public String getLabelledPosition() {
		return labelledPosition;
	}

	@Override
	public String toString() {
		return "ScannablePositionDocument [device=" + device + ", axis=" + axis + ", position=" + position
				+ ", labelledPosition=" + labelledPosition + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(axis, device, labelledPosition, position, valueType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DevicePositionDocument other = (DevicePositionDocument) obj;
		return Objects.equals(axis, other.axis) && Objects.equals(device, other.device)
				&& Objects.equals(labelledPosition, other.labelledPosition)
				&& Double.doubleToLongBits(position) == Double.doubleToLongBits(other.position)
				&& valueType == other.valueType;
	}

	/**
	 * Returns a compact tuple formatted as (device, value)
	 */
	public String toCompactString() {
		return String.format("(%s, %s)", device, ValueType.NUMERIC.equals(getValueType()) ? position : labelledPosition);
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String device;
		private String axis;
		private ValueType valueType;
		private double position;
		private String labelledPosition;

		public Builder() {
		}

		public Builder(final DevicePositionDocument parent) {
			this.device = parent.getDevice();
			this.axis = parent.getAxis();
			this.valueType = parent.getValueType();
			this.position = parent.getPosition();
			this.labelledPosition = parent.getLabelledPosition();
		}

		public Builder withDevice(String device) {
			this.device = device;
			return this;
		}

		public Builder withAxis(String axis) {
			this.axis = axis;
			return this;
		}

		public Builder withValueType(ValueType valueType) {
			this.valueType = valueType;
			return this;
		}

		/**
		 * Define the device position.
		 * @param position the expected position
		 * @return the class builder
		 */
		public Builder withPosition(double position) {
			this.position = position;
			return this;
		}

		/**
		 * Define the device position as label, i.e. from an enumeration.
		 * @param position the expected label position
		 * @return the class builder
		 */
		public Builder withLabelledPosition(String position) {
			this.labelledPosition = position;
			return this;
		}

		public DevicePositionDocument build() {
			return new DevicePositionDocument(device, axis, valueType, position, labelledPosition);
		}
	}
}
