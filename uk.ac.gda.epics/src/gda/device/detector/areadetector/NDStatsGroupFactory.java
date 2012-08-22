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

package gda.device.detector.areadetector;

import gda.device.detector.areadetector.v17.NDStats;

public class NDStatsGroupFactory {

	/**
	 * Returns an {@link NDStatsGroup} representing for the 'stats' portion of the provided {@link NDStats} plugin.
	 * Filed names are: "min", "max", "total", "net", "mean" and "sigma.
	 * 
	 * @param ndStats
	 * @return An NDStatsGroup
	 */
	public static NDStatsGroup getStatsInstance(NDStats ndStats) {
		return new NDStatsStatsGroup(ndStats);
	}

	/**
	 * Returns an {@link NDStatsGroup} representing for the 'stats' portion of the provided {@link NDStats} plugin.
	 * Field names are: "centroidX", "centroidY", "centroid_sigmaX", "centroid_sigmaY", "centroid_sigmaXY"
	 * @param ndStats
	 * @return An NDStatsGroup
	 */
	public static NDStatsGroup getCentroidInstance(NDStats ndStats) {
		return new NDCentroidStatsGroup(ndStats);
	}

	private abstract static class AbstractNDStatsGroup implements NDStatsGroup {

		private final String[] fieldNames;

		private final String name;

		private final NDStats ndStats;

		private final String[] fieldFormats;

		AbstractNDStatsGroup(String name, String[] fieldNames, String[] fieldFormats, NDStats ndStats) {
			this.ndStats = ndStats;
			this.fieldFormats = fieldFormats;
			this.name = name;
			this.fieldNames = fieldNames;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public final String[] getFieldNames() {
			return fieldNames;
		}

		@Override
		public final String[] getFieldFormats() {
			return fieldFormats;
		}

		@Override
		public NDStats getNdStats() {
			return ndStats;
		}

	}

	private static class NDStatsStatsGroup extends AbstractNDStatsGroup {

		public NDStatsStatsGroup(NDStats ndStats) {
			super("Stats", new String[] { "min", "max", "total", "net", "mean", "sigma" }, new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g" }, ndStats);
		}

		@Override
		public Double[] getCurrentDoubleVals() throws Exception {
			double minValue = getNdStats().getMinValue_RBV();
			double maxValue = getNdStats().getMaxValue_RBV();
			double total = getNdStats().getTotal_RBV();
			double net = getNdStats().getNet_RBV();
			double meanValue = getNdStats().getMeanValue_RBV();
			double sigma = getNdStats().getSigma_RBV();
			return new Double[] { minValue, maxValue, total, net, meanValue, sigma };
		}

	}

	private static class NDCentroidStatsGroup extends AbstractNDStatsGroup {

		public NDCentroidStatsGroup(NDStats ndStats) {
			super("Centroid", new String[] { "centroidX", "centroidY", "centroid_sigmaX", "centroid_sigmaY",
					"centroid_sigmaXY" }, new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g",
			"%5.5g" }, ndStats);
		}

		@Override
		public Double[] getCurrentDoubleVals() throws Exception {
			double centroidX = getNdStats().getCentroidX_RBV();
			double centroidY = getNdStats().getCentroidY_RBV();
			double sigmaX = getNdStats().getSigmaX_RBV();
			double sigmaY = getNdStats().getSigmaY_RBV();
			double sigmaXY = getNdStats().getSigmaXY_RBV();
			return new Double[] { centroidX, centroidY, sigmaX, sigmaY, sigmaXY };
		}
	}

}
