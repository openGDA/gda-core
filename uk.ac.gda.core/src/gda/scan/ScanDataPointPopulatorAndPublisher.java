/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

public class ScanDataPointPopulatorAndPublisher implements Callable<Void> {

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPointPopulatorAndPublisher.class);

	private IScanDataPoint point;

	private ScanDataPointPublisher broadcaster;

	public ScanDataPointPopulatorAndPublisher(ScanDataPointPublisher broadcaster, IScanDataPoint point) {
		this.broadcaster = broadcaster;
		this.point = point;
		logger.debug("'{}': created", point);
	}

	@Override
	public Void call() throws Exception {
		logger.debug("'{}': running", point);

		convertPositionFuturesToPositions(point);
		logger.trace("'{}': futures converted", point);

		logger.debug("'{}' publishing", point);
		broadcaster.publish(point);
		logger.debug("'{}' published", point);

		return null;
	}

	private void convertPositionFuturesToPositions(IScanDataPoint point) throws Exception {
		point.setScannablePositions(convertFuturesToResults(point.getScannableNames(), point.getScannablePositions()));
		point.setDetectorData(convertFuturesToResults(point.getDetectorNames(), point.getDetectorData()));
	}

	private List<Object> convertFuturesToResults(List<String> names, List<Object> positions) throws Exception {
		final List<Object> newPositions = new ArrayList<>();
		for (int posIndex = 0; posIndex < positions.size(); posIndex++) {
			final Object futureOrPosition = positions.get(posIndex);
			final String name = names.get(posIndex);

			logger.trace("'{}' converting '{}'", point, name);
			final Object newPosition = getResultForFuture(name, futureOrPosition);
			logger.trace("'{}' converted '{}'", point, name);

			newPositions.add(newPosition);
		}
		return newPositions;
	}

	private Object getResultForFuture(String name, Object possiblyFuture) throws DeviceException, InterruptedException {
		if (!(possiblyFuture instanceof Future<?>)) return possiblyFuture;

		try {
			return ((Future<?>) possiblyFuture).get();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			throw new DeviceException(String.format("Exception while computing point %d %s position", point.getCurrentPointNumber(), name), cause);
		} catch (InterruptedException e) {
			logger.warn("Interrupted while waiting for point {} {} position computation to complete", point.getCurrentPointNumber(), name, e);
			throw e;
		}
	}
}
