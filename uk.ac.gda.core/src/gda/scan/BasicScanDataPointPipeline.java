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

import gda.data.scan.datawriter.DataWriter;
import gda.device.DeviceException;

import org.python.core.PyException;

/**
 * Implementation of {@link ScanDataPointPipeline} that runs in a single thread.
 */
public class BasicScanDataPointPipeline implements ScanDataPointPipeline {

	private final ScanDataPointPublisher broadcaster;

	public BasicScanDataPointPipeline(ScanDataPointPublisher broadcaster) {
		this.broadcaster = broadcaster;
	}
	
	@Override
	public DataWriter getDataWriter() {
		return getBroadcaster().getDataWriter();
	}
	
	protected ScanDataPointPublisher getBroadcaster() {
		return broadcaster;
	}
	
	
	@Override
	public void put(IScanDataPoint point) throws DeviceException, Exception {
		try {
			getBroadcaster().publish(point);
		} catch (Exception e) {
			throw wrappedException(e);
		}
	}

	protected static DeviceException wrappedException(Throwable e) {
		String message = (e instanceof PyException) ? e.toString() : e.getMessage();
		if (message == null) {
			message = e.getClass().getSimpleName();
		}
		return new DeviceException(message , e);
	}

	@Override
	public void shutdown(long timeoutMillis) throws DeviceException {
		try {
			getBroadcaster().shutdown();
		} catch (Exception e) {
			throw new DeviceException("Problem shutting down pipeline", e);
		}
	}

	@Override
	public void shutdownNow() throws DeviceException {
		shutdown(0);
	}

}