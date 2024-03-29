/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import java.util.ArrayList;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.scan.IScanDataPoint;

/**
 * For use in two-directional raster scans (faster raster or raster-map-return-write).
 * <p>
 * This 'flips' (changes the data point number) and buffers the data points from the odd numbered rows which run in the
 * returning direction. It then issues the data points in the normal order as if this was a regular two dimensional scan
 * and the data points have the correct numbering.
 */
public class XasAsciiNexusDatapointCompletingDataWriter implements DataWriter {

	private static final Logger logger = LoggerFactory.getLogger(XasAsciiNexusDatapointCompletingDataWriter.class);

	private volatile int lastWrittenDataPoint = -1;

	// this does the flipping of the SDP indexes
	private TwoDScanRowReverser indexer;
	// this does the file writing, but in an asynchronous way
	private DatapointCompletingDataWriter sink;
	private Hashtable<Integer, IScanDataPoint> pointsBuffer = new Hashtable<>();
	private XasAsciiNexusDataWriter xasAsciiNexusDataWriter;

	public XasAsciiNexusDatapointCompletingDataWriter() {
		xasAsciiNexusDataWriter = new XasAsciiNexusDataWriter();
		sink = new DatapointCompletingDataWriter();
		sink.setDatawriter(xasAsciiNexusDataWriter);
	}

	@Override
	public void addData(IScanDataPoint point) throws Exception {
		// flip the point
		if (getIndexer() != null) {
			indexer.indexData(point);
		}

		// add the point, but if its out of order then buffer it
		if (point.getCurrentPointNumber() == lastWrittenDataPoint + 1) {
			addPointToDataWriterAndFlush(point);
		} else {
			addPointToBufferAndFlush(point);
		}
	}

	private synchronized void addPointToDataWriterAndFlush(IScanDataPoint point) throws Exception {
		logger.debug("writing point - " + point.getCurrentPointNumber());
		sink.addData(point);
		lastWrittenDataPoint = point.getCurrentPointNumber();
		flushBuffer();
	}

	private synchronized void addPointToBufferAndFlush(IScanDataPoint point) throws Exception {
		// add to buffer
		pointsBuffer.put(point.getCurrentPointNumber(), point);
		flushBuffer();
	}

	private void flushBuffer() throws Exception {
		// see if we can empty the buffer in the correct order, so points written to file correctly
		for (Integer i = lastWrittenDataPoint + 1;; i++) {

			if (!pointsBuffer.containsKey(i)) {
				break;
			}

			IScanDataPoint pointToWrite = pointsBuffer.get(i);
			logger.debug("writing buffered point - " + pointToWrite.getCurrentPointNumber());
			sink.addData(pointToWrite);
			lastWrittenDataPoint = pointToWrite.getCurrentPointNumber();
			pointsBuffer.remove(i);

			if (pointsBuffer.size() == 0) {
				break;
			}
		}
	}

	public TwoDScanRowReverser getIndexer() {
		return indexer;
	}

	public void setIndexer(TwoDScanRowReverser indexer) {
		this.indexer = indexer;
	}

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		sink.addDataWriterExtender(dataWriterExtender);
	}

	@Override
	public void completeCollection() throws Exception {
		sink.completeCollection();
	}

	public void completeCollection(IDataWriterExtender parent) {
		sink.completeCollection(parent);
	}

	@Override
	public boolean equals(Object obj) {
		return sink.equals(obj);
	}

	@Override
	public String getCurrentFileName() {
		return sink.getCurrentFileName();
	}

	@Override
	public int getCurrentScanIdentifier() {
		return sink.getCurrentScanIdentifier();
	}

	public DataWriter getDatawriter() {
		return sink.getDatawriter();
	}

	@Override
	public ArrayList<String> getHeader() {
		return sink.getHeader();
	}

	@Override
	public int hashCode() {
		return sink.hashCode();
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		sink.removeDataWriterExtender(dataWriterExtender);
	}

	public void setDatawriter(DataWriter datawriter) {
		sink.setDatawriter(datawriter);
	}

	@Override
	public void setHeader(ArrayList<String> header) {
		sink.setHeader(header);
	}

	@Override
	public void setHeader(String header) {
		sink.setHeader(header);
	}

	@Override
	public String toString() {
		return sink.toString();
	}

	/**
	 * The underlying data writer which will create the Nexus and Ascii files. The same used in other scans.
	 * <p>
	 * Access is provided to enable extra settings to be made on this object.
	 *
	 * @return XasAsciiNexusDataWriter
	 */
	public XasAsciiNexusDataWriter getXasDataWriter() {
		return xasAsciiNexusDataWriter;
	}

	@Override
	public void configureScanNumber(int scanNumber) throws Exception {
		// TODO Auto-generated method stub

	}
}