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

import static gda.jython.InterfaceProvider.getJythonServerNotifer;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.DataWriter;
import gda.device.DeviceException;
/**
 * Broadcasts {@link ScanDataPoint}s and writes them to a {@link DataWriter}. This is also created
 * with a pointWrittenCallback which acts to fire specific event updates once data has been written
 * to disk.
 */
public class ScanDataPointPublisher {

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPointPublisher.class);
	private final DataWriter dataWriter;
	private final ScanBase dataSourceForObservers;
	private final Consumer<Integer> pointWrittenCallback;

	public ScanDataPointPublisher(DataWriter dataWriter, ScanBase dataSourceForObservering, Consumer<Integer> pointWrittenCallback) {
		this.dataWriter = dataWriter;
		this.dataSourceForObservers = dataSourceForObservering;
		this.pointWrittenCallback = pointWrittenCallback;
	}

	protected void publish(IScanDataPoint point) throws Exception {
		synchronized(dataWriter) {
			dataWriter.addData(point);
		}

		// update the filename (if this was the first data point and so
		// filename would never be defined until first data added
		point.setCurrentFilename(dataWriter.getCurrentFileName());

		// notify IObservers of this scan (e.g. GUI panels)
		getJythonServerNotifer().notifyServer(dataSourceForObservers, point);

		// Send update event now that data has been written and flushed to file
		pointWrittenCallback.accept(point.getCurrentPointNumber());
	}

	public DataWriter getDataWriter() {
		return dataWriter;
	}

	public void shutdown() throws DeviceException {
		try {
			logger.debug("Calling data writer complete collection from SDPPL");
			synchronized(dataWriter) {
				dataWriter.completeCollection();
			}
		} catch (Exception e) {
			throw new DeviceException("problem shutting down datawriter",e);
		}
	}
}
