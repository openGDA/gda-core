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
import java.util.SortedSet;

/**
 * Parameters specific to tracking a XANES edge.
 */
public class XanesEdgeParameters {

	public enum TrackingMethod {
		REFERENCE,
		EDGE
	}

	private LinesToTrackEntry linesToTrack;
	private String trackingMethod = REFERENCE.toString();
	private String visitId = "";

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

	@Override
	public String toString() {
		return "XanesEdgeParameters [linesToTrack=" + linesToTrack + ", trackingMethod=" + trackingMethod + ", visitId="
				+ visitId + "]";
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
		public String toString() {
			return "LinesToTrackEntry [line=" + line + ", filePaths=" + filePaths + "]";
		}
	}
}
