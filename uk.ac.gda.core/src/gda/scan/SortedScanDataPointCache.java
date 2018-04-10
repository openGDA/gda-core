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

package gda.scan;

import static java.util.Collections.addAll;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class designed to cache all the data from scan data points. It allows quick retrieval
 * of <em>sorted</em> basic scan data for use in Jython or by scan processing.
 *
 * @see ScanDataPointCache ScanDataPointCache - for a quicker but unsorted implementation
 * @since 9.8
 */
public class SortedScanDataPointCache extends DataPointCache {
	private static final Logger logger = LoggerFactory.getLogger(SortedScanDataPointCache.class);

	/** Cache to store data for quick access */
	private final Set<Double[]> cache = new TreeSet<>(SortedScanDataPointCache::compare);

	/** Fields included in this scan */
	private final List<String> fields = new ArrayList<>();

	@Override
	public void addDataPoint(IScanDataPoint sdp) {
		cache.add(sdp.getAllValuesAsDoubles());
		logger.trace("Added point {} of {} to cache", sdp.getCurrentPointNumber(), sdp.getNumberOfPoints());
	}

	@Override
	protected void initialise(IScanDataPoint sdp) {
		logger.debug("Initialising cache...");
		// Remove cached data from previous scan
		cache.clear();
		fields.clear();
		addAll(fields, sdp.getScannableHeader());
		fields.addAll(sdp.getDetectorHeader());
	}

	@Override
	public List<Double> getPositionsFor(String scannableName) {
		logger.trace("Getting positions for: {}", scannableName);
		int idx = fields.indexOf(scannableName);
		if (idx < 0) {
			logger.debug("Name '{}' not found in data point cache. Fields found: {}", scannableName, fields);
			throw new IllegalArgumentException(scannableName + " not found in data point cache "+ fields);
		}
		return cache.stream().map(dp -> dp[idx]).collect(toList());
	}

	/**
	 * Compare two arrays of doubles by first element then second etc. If one array is subset of the other,
	 * the shorter one is considered first.
	 * @param arr1
	 * @param arr2
	 * @return <0, 0 or >0 if the arrays are in order, equal or reversed respectively
	 */
	private static int compare(Double[] arr1, Double[] arr2) {
		int minLength = arr1.length < arr2.length ? arr1.length : arr2.length;
		for (int i = 0; i < minLength; i++) {
			int comp = Double.compare(arr1[i], arr2[i]);
			if (comp == 0) {
				continue;
			}
			return comp;
		}
		return arr1.length - arr2.length;
	}

}
