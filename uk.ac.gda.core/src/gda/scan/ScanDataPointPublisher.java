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
import gda.data.scan.datawriter.DataWriter;
import gda.device.DeviceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Broadcasts {@link ScanDataPoint}s and writes them to a {@link DataWriter}.
 */
public class ScanDataPointPublisher {

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPointPublisher.class);
	protected final DataWriter dataWriter;
	protected final ScanBase dataSourceForObservers;

	public ScanDataPointPublisher(DataWriter dataWriter, ScanBase dataSourceForObservering) {
		this.dataWriter = dataWriter;
		this.dataSourceForObservers = dataSourceForObservering;
	}

	protected void publish(IScanDataPoint point) throws Exception {
		dataWriter.addData(point);
	
		// update the filename (if this was the first data point and so
		// filename would never be defined until first data added
		point.setCurrentFilename(dataWriter.getCurrentFileName());
	
		// notify IObservers of this scan (e.g. GUI panels)
		getJythonServerNotifer().notifyServer(dataSourceForObservers, point);
	}

	public DataWriter getDataWriter() {
		return dataWriter;
	}
	
	public void shutdown() throws DeviceException {
		try {
			logger.debug("Calling data writer complete collection from SDPPL");
			dataWriter.completeCollection();
		} catch (Exception e) {
			throw new DeviceException("problem shutting down datawriter",e);
		}
	}
}
