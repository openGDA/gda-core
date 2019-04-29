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

import java.util.Vector;
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
		convertDevices(point.getScannableNames(), point.getPositions());
		convertDevices(point.getDetectorNames(), point.getDetectorData());
	}

	private void convertDevices(Vector<String> names, Vector<Object> positions) throws Exception {
		for (int i = 0; i < positions.size(); i++) {
			Object possiblyFuture = positions.get(i);
			String name = names.get(i);

			logger.trace("'{}' converting '{}'", point, name);
			Object pos = convertPositionFutureToPosition(name, possiblyFuture);
			logger.trace("'{}' converted '{}'", point, name);

			positions.set(i, pos);
		}
	}

	private Object convertPositionFutureToPosition(String name, Object possiblyFuture) throws Exception {
		if (!(possiblyFuture instanceof Future<?>)) return possiblyFuture;

		try {
			return ((Future<?>) possiblyFuture).get();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			throw new DeviceException(
					String.format(
						"Exception while computing point %d %s position",
						point.getCurrentPointNumber(),
						name),
					cause);
		} catch (InterruptedException e) {
			logger.warn("Interrupted while waiting for point %d %s position computation to complete", point.getCurrentPointNumber(), name, e);
			throw e;
		}
	}
}
