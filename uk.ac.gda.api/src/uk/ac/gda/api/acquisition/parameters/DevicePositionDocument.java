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
 * <p>
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = DevicePositionDocument.Builder.class)
public class DevicePositionDocument {

	/**
	 * An identifier, usually a Spring bean name, to allow an acquisition controller to retrieve a real instance of the
	 * device
	 */
	private final String device;
	/**
	 * A label to identify uniquely the role of this device
	 */
	private final String axis;

	/**
	 * The required position for the device
	 */
	private final Object position;

	private DevicePositionDocument(String scannable, String axis, Object position) {
		this.device = scannable;
		this.axis = axis;
		this.position = position;
	}

	public String getDevice() {
		return device;
	}

	public String getAxis() {
		return axis;
	}

	public Object getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return String.format("DevicePositionDocument [%s: %s]", device, position);
	}

	@Override
	public int hashCode() {
		return Objects.hash(axis, device, position);
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
				&& Objects.equals(position, other.position);
	}


	@JsonPOJOBuilder
	public static class Builder {
		private String device;
		private String axis;
		private Object position;

		public Builder() {
		}

		public Builder(final DevicePositionDocument parent) {
			this.device = parent.getDevice();
			this.axis = parent.getAxis();
			this.position = parent.getPosition();
		}

		public Builder withDevice(String device) {
			this.device = device;
			return this;
		}

		public Builder withAxis(String axis) {
			this.axis = axis;
			return this;
		}

		/**
		 * Define the device position.
		 * @param position the expected position
		 * @return the class builder
		 */
		public Builder withPosition(Object position) {
			this.position = position;
			return this;
		}

		public DevicePositionDocument build() {
			return new DevicePositionDocument(device, axis, position);
		}
	}
}
