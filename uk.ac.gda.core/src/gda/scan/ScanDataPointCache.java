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

import static org.apache.commons.lang3.ArrayUtils.removeAll;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class designed to cache all the data from scan data points. It allows very quick retrieval of basic scan
 * data for use in Jython or by scan processing.
 *
 * @author James Mudd
 */
public class ScanDataPointCache extends DataPointCache {

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPointCache.class);

	/** Cache holding the same data that would be printed to the terminal. Linked to ensure order as the map is iterated over*/
	private final Map<String, List<Double>> cache = new LinkedHashMap<>();

	@Override
	protected void addDataPoint(IScanDataPoint sdp) {
		// Get all the scannable and detector positions
		final List<Double> positions = Arrays.asList(sdp.getAllValuesAsDoubles());
		if (cache.size() == positions.size()) {
			populateCache(sdp, positions);
			return;
		}

		String[] pointNames = ArrayUtils.addAll(sdp.getScannableHeader(), sdp.getDetectorHeader().toArray(String[]::new));

		// find index for each duplicated point name if any
		Map<String, List<Integer>> duplicates = getDuplicatesInHeader(pointNames);

		if (cache.size() > positions.size() || (positions.size() > cache.size() && duplicates.isEmpty())) {
			throw new IllegalArgumentException("Cache won't work SDP contains different number of positions than expected."
					+ " cacheSize=" + cache.size()
					+ " pointSize=" + positions.size()
					+ " cacheNames=" + cache.keySet()
					+ " pointNames=" + pointNames);
		}

		if (positions.size() > cache.size() && !duplicates.isEmpty()) {
			logger.warn(
					"SDP contains different number of positions than expected. cacheSize={}, pointSize={}, cacheNames={}, pointNames={}",
					cache.size(), positions.size(), cache.keySet(), pointNames);
			duplicates.forEach((k, v) -> logger.warn("duplicate name '{}' are found at index {}", k, v));
			Double[] array = positions.toArray(new Double[positions.size()]);
			// merge duplicate indexes for all keys (point names) after remove last index from the values of each key
			Integer[] mergedDuplicateIndexes = duplicates.values().stream().map(s -> s.remove(s.size() - 1))
					.collect(Collectors.toList()).toArray(new Integer[] {});
			// remove duplicated position values for each key but keep the last value
			List<Double> reducedpositions = Arrays.asList(toObject(removeAll(toPrimitive(array), toPrimitive(mergedDuplicateIndexes))));
			logger.debug(
					"reduced positions for SDP processor. cacheSize={}, pointSize={}, cacheNames={}, pointValues={}",
					cache.size(), reducedpositions.size(), cache.keySet(), reducedpositions);
			populateCache(sdp, reducedpositions);
		}
	}

	private void populateCache(IScanDataPoint sdp, final List<Double> positions) {
		final Iterator<Double> positionIterator = positions.iterator();

		// Loop over the scannables adding their positions from this point
		for (List<Double> scannablePositions : cache.values()) {
			scannablePositions.add(positionIterator.next());
		}
		logger.trace("Added point {} of {} to cache", sdp.getCurrentPointNumber(), sdp.getNumberOfPoints());
	}

	@Override
	protected void initialise(IScanDataPoint sdp) {
		logger.debug("Initialising cache...");
		// Remove cached data from previous scan
		cache.clear();

		final int scanPoints = sdp.getNumberOfPoints();

		// getNames returns the scannable and detector names in order
		for (String scannableName : sdp.getScannableHeader()) {
			cache.putIfAbsent(scannableName, new ArrayList<>(scanPoints));
		}

		for (String scannableName : sdp.getDetectorHeader()){
			cache.putIfAbsent(scannableName, new ArrayList<>(scanPoints));
		}

		logger.debug("Cache initalised. Size is {} scannables x {} points", cache.size(), scanPoints);
	}

	@Override
	public List<Double> getPositionsFor(String scannableName) {
		logger.trace("Getting positions for: {}", scannableName);
		if (!cache.containsKey(scannableName)) {
			throw new IllegalArgumentException(scannableName + " not found in data point cache");
		}
		return cache.get(scannableName);
	}

	private Map<String, List<Integer>> getDuplicatesInHeader(String ... data) {
	    Map<String, List<Integer>> duplicates = IntStream.range(0, data.length)
	            .boxed()
	            .collect(Collectors.groupingBy(i -> data[i], LinkedHashMap::new, Collectors.toList()));
	    duplicates.entrySet().removeIf(e -> e.getValue().size() < 2);

	    return duplicates;
	}
}
