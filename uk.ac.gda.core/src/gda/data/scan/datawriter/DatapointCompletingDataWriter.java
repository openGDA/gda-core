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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.scan.IScanDataPoint;
import uk.ac.gda.common.exception.GDAException;

public class DatapointCompletingDataWriter extends DataWriterBase {

	private DataWriter sink;

	private final ExecutorService datapointCompleterSingleThreadpool;

	private Throwable exception;

	private static final Logger logger = LoggerFactory.getLogger(DatapointCompletingDataWriter.class);

	public DatapointCompletingDataWriter() {
		datapointCompleterSingleThreadpool = Executors.newFixedThreadPool(1);
	}

	private boolean pointContainsCallablePosition(IScanDataPoint point) {
		return point.getPositions().stream().anyMatch(Callable.class::isInstance);
	}

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		// don't hold it here but pass it to the sink so that super.addData(point) is called at the right point
		sink.addDataWriterExtender(dataWriterExtender);
	}

	@Override
	public void addData(IScanDataPoint point) throws Exception {
		throwException();
		if (pointContainsCallablePosition(point)) {
			DatapointCompleterTask task = new DatapointCompleterTask(sink, point);
			datapointCompleterSingleThreadpool.execute(task);
		} else {
			sink.addData(point);
		}
	}

	synchronized void setExceptionAndShutdownNow(Throwable e) {
		exception = e;
		logger.info("Exception caught computing a point", e);
		shutdownNow();
	}

	/**
	 * If an exception has been caught then throw it.
	 *
	 * @throws DeviceException
	 */
	private void throwException() throws GDAException {
		if (exception != null){
			throw new GDAException("Exception caught completing datapoint in datawriter", exception);
		}
	}

	private void shutdownNow() {
		if (!datapointCompleterSingleThreadpool.isShutdown()) {
			logger.info("Shutting down datapointCompleterExecutor NOW.");
			datapointCompleterSingleThreadpool.shutdownNow();
		}
	}

	private void shutdown() {
		if (datapointCompleterSingleThreadpool.isShutdown())
			return;

		logger.debug("Shutting down datapointCompleterThreadPool gently.");
		datapointCompleterSingleThreadpool.shutdown();

		try {
			datapointCompleterSingleThreadpool.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			logger.warn("InterruptedException during shutdown - there may be orphaned threads", (Object[]) e.getStackTrace());
		}
		logger.debug("Shutting down datapointCompleterThreadPool gently - completed");
	}

	@Override
	public void completeCollection() throws Exception {
		logger.debug("DAtapointcmDW complete collection is called");
		throwException();
		shutdown();
		logger.debug("DAtapointcmDW  sink complete collection is called");
		sink.completeCollection();
	}

	@Override
	public String getCurrentFileName() {
		return sink.getCurrentFileName();
	}

	@Override
	public int getCurrentScanIdentifier() {
		return sink.getCurrentScanIdentifier();
	}

	@Override
	public ArrayList<String> getHeader() {
		return sink.getHeader();
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		sink.removeDataWriterExtender(dataWriterExtender);
	}

	@Override
	public void setHeader(ArrayList<String> header) {
		sink.setHeader(header);
	}

	@Override
	public void setHeader(String header) {
		sink.setHeader(header);
	}

	public void setDatawriter(DataWriter datawriter) {
		this.sink = datawriter;
	}

	public DataWriter getDatawriter() {
		return sink;
	}


	private final class DatapointCompleterTask implements Runnable {

		private final IScanDataPoint point;

		private final DataWriter taskSink;

		public DatapointCompleterTask(DataWriter sink, IScanDataPoint point) {
			this.taskSink = sink;
			this.point = point;
		}

		@Override
		public void run() {
			final List<Object> positions = point.getPositions();
			for (int i = 0; i < positions.size(); i++) {
				if (positions.get(i) instanceof Callable<?>) {
					Object pos;
					try {
						pos = ((Callable<?>) positions.get(i)).call();
						positions.set(i, pos);
					} catch (Exception e) {
						setExceptionAndShutdownNow(e);
						return;
					}
				}
			}
			try {
				logger.info("Writing queued data point {}", point.getCurrentPointNumber());
				taskSink.addData(point);
			} catch (Exception e) {
				setExceptionAndShutdownNow(e);
			}
		}
	}

	@Override
	public void configureScanNumber(int scanNumber) throws Exception {
		sink.configureScanNumber(scanNumber);
	}
}
