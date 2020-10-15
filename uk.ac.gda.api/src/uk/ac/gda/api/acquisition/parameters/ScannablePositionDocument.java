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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Describes the required position for a {@code Scannable} .
 *
 * <p>
 * The {@code Scannable} is assumed one-dimensional.
 * Having a limited number of cases, instead define two separate documents, this class uses a {@link ScannablePositionDocument.ValueType}
 * to discriminate between a numeric position ( {@link #getPosition()}) and a predefined, labelled one ({@link #getLabelledPosition()}).
 * <p>
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScannablePositionDocument.Builder.class)
public class ScannablePositionDocument {

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
	 * scannable
	 */
	private final String scannable;
	/**
	 * A label to identify uniquely the role of this scannable
	 */
	private final String axis;

	private final ValueType valueType;

	/**
	 * The required position for the scannable
	 */
	private final double position;
	/**
	 * The required predefined position, i.e. to drive a {@code ScannablePositioner}
	 */
	private final String labelledPosition;

	private ScannablePositionDocument(String scannable, String axis, ValueType valueType, double position, String labelledPosition) {
		super();
		this.scannable = scannable;
		this.axis = axis;
		this.valueType = valueType;
		this.position = position;
		this.labelledPosition = labelledPosition;
	}

	public String getScannable() {
		return scannable;
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
		return "ScannablePositionDocument [scannable=" + scannable + ", axis=" + axis + ", position=" + position
				+ ", labelledPosition=" + labelledPosition + "]";
	}


	@JsonPOJOBuilder
	public static class Builder {
		private String scannable;
		private String axis;
		private ValueType valueType;
		private double position;
		private String labelledPosition;

		public Builder() {
		}

		public Builder(final ScannablePositionDocument parent) {
			this.scannable = parent.getScannable();
			this.axis = parent.getAxis();
			this.valueType = parent.getValueType();
			this.position = parent.getPosition();
			this.labelledPosition = parent.getLabelledPosition();
		}

		public Builder withScannable(String scannable) {
			this.scannable = scannable;
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
		 * Define the scannable position.
		 * @param position the expected position
		 * @return the class builder
		 */
		public Builder withPosition(double position) {
			this.position = position;
			return this;
		}

		/**
		 * Define the scannable position as label, i.e. from an enumeration.
		 * @param position the expected label position
		 * @return the class builder
		 */
		public Builder withLabelledPosition(String position) {
			this.labelledPosition = position;
			return this;
		}

		public ScannablePositionDocument build() {
			return new ScannablePositionDocument(scannable, axis, valueType, position, labelledPosition);
		}
	}
}
