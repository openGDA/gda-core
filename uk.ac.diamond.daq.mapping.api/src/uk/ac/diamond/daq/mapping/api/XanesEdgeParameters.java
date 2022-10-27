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

import static uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.TrackingMethod.REFERENCE;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;

/**
 * Parameters specific to tracking a XANES edge.
 */
public class XanesEdgeParameters {

	public enum TrackingMethod {
		REFERENCE,
		EDGE
	}

	private EdgeToEnergy edgeToEnergy;
	private LinesToTrackEntry linesToTrack;
	private String trackingMethod = REFERENCE.toString();
	private String visitId = "";
	private boolean enforcedShape = true;
	private int percentage = 20;

	public EdgeToEnergy getEdgeToEnergy() {
		return edgeToEnergy;
	}

	public void setEdgeToEnergy(EdgeToEnergy edgeToEnergy) {
		this.edgeToEnergy = edgeToEnergy;
	}

	public LinesToTrackEntry getLinesToTrack() {
		return linesToTrack;
	}

	public void setLinesToTrack(LinesToTrackEntry linesToTrack) {
		this.linesToTrack = linesToTrack;
	}

	public String getTrackingMethod() {
		return trackingMethod;
	}

	public void setTrackingMethod(String trackingMethod) {
		this.trackingMethod = trackingMethod;
	}

	public String getVisitId() {
		return visitId;
	}

	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}

	public boolean isEnforcedShape() {
		return enforcedShape;
	}

	public void setEnforcedShape(boolean enforcedShape) {
		this.enforcedShape = enforcedShape;
	}

	public Integer getPercentage() {
		return percentage;
	}

	public void setPercentage(Integer percentage) {
		this.percentage = percentage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(edgeToEnergy, enforcedShape, linesToTrack, percentage, trackingMethod, visitId);
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
		return Objects.equals(edgeToEnergy, other.edgeToEnergy) && enforcedShape == other.enforcedShape
				&& Objects.equals(linesToTrack, other.linesToTrack) && percentage == other.percentage
				&& Objects.equals(trackingMethod, other.trackingMethod) && Objects.equals(visitId, other.visitId);
	}

	@Override
	public String toString() {
		return "XanesEdgeParameters [edgeToEnergy=" + edgeToEnergy + ", linesToTrack=" + linesToTrack
				+ ", trackingMethod=" + trackingMethod + ", visitId=" + visitId + ", enforcedShape=" + enforcedShape
				+ ", percentage=" + percentage + "]";
	}

	/**
	 * Class holding a line to track and the processing file(s) in which it is defined
	 */
	public static class LinesToTrackEntry {
		private String line = "";
		private SortedSet<String> filePaths = Collections.emptySortedSet();

		public LinesToTrackEntry() {
			// default constructor for JSON
		}

		public LinesToTrackEntry(String line, SortedSet<String> filePaths) {
			this.line = line;
			this.filePaths = filePaths;
		}

		public String getLine() {
			return line;
		}

		public void setLine(String line) {
			this.line = line;
		}

		public SortedSet<String> getFilePaths() {
			return filePaths;
		}

		public void setFilePaths(SortedSet<String> filePaths) {
			this.filePaths = filePaths;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((line == null) ? 0 : line.hashCode());
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
			LinesToTrackEntry other = (LinesToTrackEntry) obj;
			if (line == null) {
				if (other.line != null)
					return false;
			} else if (!line.equals(other.line))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "LinesToTrackEntry [line=" + line + ", filePaths=" + filePaths + "]";
		}
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
}
