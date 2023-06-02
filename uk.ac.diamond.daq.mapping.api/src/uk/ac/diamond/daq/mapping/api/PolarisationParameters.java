/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import java.util.Objects;

public class PolarisationParameters {

	public enum Polarisation {
		LH("Linear Horizontal", Direction.LINEAR),
		LV("Linear Vertical", Direction.LINEAR),
		CR("Circular Right", Direction.CIRCULAR),
		CL("Circular Left", Direction.CIRCULAR);

		private final String label;
		private final Direction direction;

		public enum Direction {
			LINEAR, CIRCULAR
		}

		private Polarisation(String label, Direction direction) {
			this.label = label;
			this.direction = direction;
		}

		public String getLabel() {
			return label;
		}

		public Direction getDirection() {
			return direction;
		}

	}

	private Polarisation polarisation;
	private Double phase;

	public Double getPhase() {
		return phase;
	}

	public void setPhase(Double phase) {
		this.phase = phase;
	}

	public Polarisation getPolarisation() {
		return polarisation;
	}

	public void setPolarisation(Polarisation polarisation) {
		this.polarisation = polarisation;
	}


	@Override
	public int hashCode() {
		return Objects.hash(phase, polarisation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolarisationParameters other = (PolarisationParameters) obj;
		return Objects.equals(phase, other.phase) && polarisation == other.polarisation;
	}

	@Override
	public String toString() {
		return "PolarisationParameters [polarisation=" + polarisation + ", phase=" + phase + "]";
	}

	public static class Phase {

		private String element;
		private Double position;

		public Phase() {

		}

		public Phase(String element, Double position) {
			this.element = element;
			this.position = position;
		}

		public String getElement() {
			return element;
		}

		public void setElement(String element) {
			this.element = element;
		}

		public Double getPosition() {
			return position;
		}

		public void setPosition(Double position) {
			this.position = position;
		}

		@Override
		public int hashCode() {
			return Objects.hash(element, position);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Phase other = (Phase) obj;
			return Objects.equals(element, other.element) && Objects.equals(position, other.position);
		}

		@Override
		public String toString() {
			return "Phase [element=" + element + ", position=" + position + "]";
		}

	}

}
