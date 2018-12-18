/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.plotting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IndexIterator;

public class DetectorRates implements Serializable {
	/** Threshold above which to report high points */
	private static Map<String, Integer> threshold = new HashMap<>();

	public String detName, detType;
	public float countingTime, maxCounts, integratedCounts;
	public List<HighCount> highCounts = new ArrayList<>();
	@Override
	public String toString() {
		return String.format("%s detector %s has been counting for %5.3f seconds and aquired %5.5g total counts (%5.5g in the peak)",
				detType, detName, countingTime, integratedCounts, maxCounts);
	}

	public void setHighCounts(Dataset ds) {
		IndexIterator iterator = ds.getPositionIterator();
		int detThreshold = threshold.getOrDefault(detName, -1);
		if (detThreshold > 0) {
			while (iterator.hasNext()) {
				int[] pos = iterator.getPos();
				int value = ds.getInt(pos);
				if (value > detThreshold) {
					highCounts.add(new HighCount(pos[0], pos[1], value));
				}
			}
		}
	}

	public static int getThreshold(String detector) {
		return threshold.getOrDefault(detector, Integer.MAX_VALUE);
	}

	public static void setThreshold(String detector, int threshold) {
		DetectorRates.threshold.put(detector, threshold);
	}

	public static class HighCount implements Serializable {
		int x, y, count;
		public HighCount(int x, int y, int value) {
			this.x = x;
			this.y = y;
			this.count = value;
		}
		@Override
		public String toString() {
			return String.format("(%d, %d): %d", x, y, count);
		}
	}
}