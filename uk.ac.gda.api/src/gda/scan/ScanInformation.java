/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.scan;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.Collection;

/**
 * Object that provides information about a scan, but not its data.
 */
public class ScanInformation implements Serializable {

	public static final ScanInformation EMPTY = new ScanInformationBuilder().build();

	private final int[] dimensions;
	private final int scanNumber;
	private final String[] scannableNames;
	private final String[] detectorNames;
	private final String filename;
	private final String instrument;
	private final int numberOfPoints;

	/** Cache of toString representation */
	private String toStringCache;

	private ScanInformation(ScanInformationBuilder info) {
		dimensions = info.dimensions;
		scanNumber = info.scanNumber;
		scannableNames = info.scannableNames;
		detectorNames = info.detectorNames;
		filename = info.filename;
		instrument = info.instrument;
		numberOfPoints = info.numberOfPoints;
	}

	public int[] getDimensions() {
		return dimensions;
	}

	public String[] getScannableNames() {
		return scannableNames;
	}

	public String[] getDetectorNames() {
		return detectorNames;
	}

	public String getFilename() {
		return filename;
	}

	public String getInstrument() {
		return instrument;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public int getScanNumber() {
		return scanNumber;
	}

	@Override
	public String toString() {
		if (toStringCache == null) {
			StringBuilder sb = new StringBuilder(String.format("Scan %d : A Scan of rank %d with the dimensions: ", scanNumber, dimensions.length));
			sb.append(stream(dimensions).mapToObj(String::valueOf).collect(joining("x")));
			sb.append(" over scannables: ");
			sb.append(stream(scannableNames).collect(joining(", ")));
			sb.append(" using detectors: ");
			sb.append(stream(detectorNames).collect(joining(", ")));
			toStringCache = sb.toString();
		}
		return toStringCache;
	}

	public static class ScanInformationBuilder {
		private int[] dimensions = new int[] {};
		private int scanNumber = -1;
		private String[] scannableNames = new String[] {};
		private String[] detectorNames = new String[] {};
		private String filename = "";
		private String instrument = "";
		private int numberOfPoints = -1;

		public static ScanInformationBuilder from(ScanInformation info) {
			return new ScanInformationBuilder()
					.dimensions(info.dimensions)
					.scanNumber(info.scanNumber)
					.scannableNames(info.scannableNames)
					.detectorNames(info.detectorNames)
					.filename(info.filename)
					.instrument(info.instrument)
					.numberOfPoints(info.numberOfPoints);
		}

		public ScanInformationBuilder dimensions(int... dimensions) {
			this.dimensions = dimensions;
			return this;
		}
		public ScanInformationBuilder scanNumber(int scanNumber) {
			this.scanNumber = scanNumber;
			return this;
		}
		public ScanInformationBuilder scannableNames(String... scannableNames) {
			this.scannableNames = scannableNames;
			return this;
		}
		public ScanInformationBuilder scannableNames(Collection<String> scannableNames) {
			this.scannableNames = scannableNames.toArray(new String[] {});
			return this;
		}
		public ScanInformationBuilder detectorNames(String... detectorNames) {
			this.detectorNames = detectorNames;
			return this;
		}
		public ScanInformationBuilder detectorNames(Collection<String> detectorNames) {
			this.detectorNames = detectorNames.toArray(new String[] {});
			return this;
		}
		public ScanInformationBuilder filename(String filename) {
			this.filename = filename;
			return this;
		}
		public ScanInformationBuilder instrument(String instrument) {
			this.instrument = instrument;
			return this;
		}
		public ScanInformationBuilder numberOfPoints(int numberOfPoints) {
			this.numberOfPoints = numberOfPoints;
			return this;
		}

		public ScanInformation build() {
			return new ScanInformation(this);
		}
	}
}
