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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class designed to cache all the data from scan data points. It allows quick retrieval
 * of <em>sorted</em> basic scan data for use in Jython or by scan processing.
 * <p>
 * Note: this class stores values as returned by {@link IScanDataPoint#getAllValuesAsDoubles()}, which attempts to
 * convert all values to a {@link Double}. Any value that cannot be converted to a Double is replaced with {@code null}.
 *
 * @see ScanDataPointCache ScanDataPointCache - for a quicker but unsorted implementation
 * @since 9.8
 */
public class SortedScanDataPointCache extends DataPointCache {
	private static final Logger logger = LoggerFactory.getLogger(SortedScanDataPointCache.class);

	/** Cache to store data for quick access */
	private final SortedSet<Double[]> cache = new TreeSet<>(Arrays::compare);

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
		return cache.stream().map(dp -> dp[idx]).toList();
	}

}
