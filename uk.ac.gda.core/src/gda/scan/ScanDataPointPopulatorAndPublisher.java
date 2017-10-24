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

import gda.device.DeviceException;

import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanDataPointPopulatorAndPublisher implements Callable<Void> {

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPointPopulatorAndPublisher.class);

	private IScanDataPoint point;

	private ScanDataPointPublisher broadcaster;

	public ScanDataPointPopulatorAndPublisher(ScanDataPointPublisher broadcaster, IScanDataPoint point) {
		this.broadcaster = broadcaster;
		this.point = point;
		if( logger.isDebugEnabled())
			logger.debug("'{}': created", point.toString());
	}

	@Override
	public Void call() throws Exception {
		if( logger.isDebugEnabled())
			logger.debug("'{}': running", point.toString());

		convertPositionFuturesToPositions(point);

		if( logger.isDebugEnabled()){
			logger.debug("'{}': futures converted", point.toString());
			logger.debug("'{}' publishing", point.getUniqueName());
		}

		broadcaster.publish(point);
		if( logger.isDebugEnabled()){
			logger.debug("'{}' published", point.toString());
		}
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

			if( logger.isDebugEnabled())
				logger.debug("'{}' converting '{}'", point.toString(), name);
			Object pos = convertPositionFutureToPosition(name, possiblyFuture);
			if( logger.isDebugEnabled())
				logger.debug("'{}' converted '{}'", point.toString(), name);
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
