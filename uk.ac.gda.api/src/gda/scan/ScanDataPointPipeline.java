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

/**
 * Populates ScanDataPoints with positions (from Scannables) and data (from Detectors) and then broadcasts them and
 * writes them to a data writer.
 */
public interface ScanDataPointPipeline {

	/**
	 * Calls any Callables found in scannablePositions or detectorData, and then broadcasts the point (as if it came
	 * from the scan itself) and adds it to a datawriter.
	 * 
	 * @param point scannablePositions or detectorData may contain Callables
	 * @throws DeviceException
	 * @throws Exception
	 */
	void put(IScanDataPoint point) throws DeviceException, Exception;

	/**
	 * Retrieves the data writer from the pipeline.
	 * 
	 * @return data writer
	 */
	DataWriter getDataWriter();

	/**
	 * Blocks while waiting for pipeline to empty, stops all threads and closes data writer.
	 * If the pipeline does not empty in the specified time then the callable tasks are cancelled.
	 * 
	 * @throws InterruptedException 
	 * @throws DeviceException if the pipeline did not shutdown gracefully and required forceful interruption
	 */
	void shutdown(long timeoutMillis) throws DeviceException, InterruptedException;
}
