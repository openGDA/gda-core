/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

/**
 * Parameters specific to tracking a XANES edge.
 */
public class XanesEdgeParameters {

	public enum TrackingMethod {
		REFERENCE,
		EDGE
	}

	private EdgeToEnergy edgeToEnergy;
	private LineToTrack lineToTrack;
	private TrackingMethod trackingMethod = TrackingMethod.REFERENCE;
	private SparseParameters sparseParameters;

	public EdgeToEnergy getEdgeToEnergy() {
		return edgeToEnergy;
	}

	public void setEdgeToEnergy(EdgeToEnergy edgeToEnergy) {
		this.edgeToEnergy = edgeToEnergy;
	}

	public LineToTrack getLineToTrack() {
		return lineToTrack;
	}

	public void setLineToTrack(LineToTrack lineToTrack) {
		this.lineToTrack = lineToTrack;
	}

	public TrackingMethod getTrackingMethod() {
		return trackingMethod;
	}

	public void setTrackingMethod(TrackingMethod trackingMethod) {
		this.trackingMethod = trackingMethod;
	}

	public SparseParameters getSparseParameters() {
		return sparseParameters;
	}

	public void setSparseParameters(SparseParameters sparseParameters) {
		this.sparseParameters = sparseParameters;
	}

	@Override
	public int hashCode() {
		return Objects.hash(edgeToEnergy, lineToTrack, sparseParameters, trackingMethod);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XanesEdgeParameters other = (XanesEdgeParameters) obj;
		return Objects.equals(edgeToEnergy, other.edgeToEnergy) && Objects.equals(lineToTrack, other.lineToTrack)
				&& Objects.equals(sparseParameters, other.sparseParameters)
				&& Objects.equals(trackingMethod, other.trackingMethod);
	}

	@Override
	public String toString() {
		return "XanesEdgeParameters [edgeToEnergy=" + edgeToEnergy + ", lineToTrack=" + lineToTrack
				+ ", trackingMethod=" + trackingMethod + ", sparseParameters=" + sparseParameters + "]";
	}

	/**
	 * Maps element/edge in user-readable format (e.g. "Fe-K") to the corresponding edge energy<br>
	 * Used as input for the combo box for the user to choose the edge to scan
	 */
	public static class EdgeToEnergy {
		private String edge;
		private double energy;

		public EdgeToEnergy() {
			// default constructor for JSON
		}

		public EdgeToEnergy(String edge, double energy) {
			this.edge = edge;
			this.energy = energy;
		}

		public String getEdge() {
			return edge;
		}

		public void setEdge(String edge) {
			this.edge = edge;
		}

		public double getEnergy() {
			return energy;
		}

		public void setEnergy(double energy) {
			this.energy = energy;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((edge == null) ? 0 : edge.hashCode());
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
			EdgeToEnergy other = (EdgeToEnergy) obj;
			if (edge == null) {
				if (other.edge != null)
					return false;
			} else if (!edge.equals(other.edge))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "EdgeToEnergy [edge=" + edge + ", energy=" + energy + "]";
		}
	}

	public static class LineToTrack {
		private String element;
		private String line;

		public LineToTrack() {

		}

		public LineToTrack(String element, String line) {
			this.element = element;
			this.line = line;
		}

		public String getElement() {
			return element;
		}

		public void setElement(String element) {
			this.element = element;
		}

		public String getLine() {
			return line;
		}

		public void setLine(String line) {
			this.line = line;
		}

		@Override
		public int hashCode() {
			return Objects.hash(element, line);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LineToTrack other = (LineToTrack) obj;
			return Objects.equals(element, other.element) && Objects.equals(line, other.line);
		}

		@Override
		public String toString() {
			return "LineToTrack [element=" + element + ", line=" + line + "]";
		}
	}

	public static class SparseParameters {
		private int percentage = 20;

		public SparseParameters() {

		}

		public Integer getPercentage() {
			return percentage;
		}

		public void setPercentage(Integer percentage) {
			this.percentage = percentage;
		}

		@Override
		public int hashCode() {
			return Objects.hash(percentage);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SparseParameters other = (SparseParameters) obj;
			return percentage == other.percentage;
		}

		@Override
		public String toString() {
			return "SparseParameters [percentage=" + percentage + "]";
		}
	}
}
