/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gov.aps.jca.Channel;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

class AnalyserLiveDataDispatcher extends FindableConfigurableBase {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserLiveDataDispatcher.class);

	private String plotName;
	private IElectronAnalyser analyser;
	private final  EpicsController epicsController = EpicsController.getInstance();
	private String arrayPV;
	private String frameNumberPV;
	private String acquirePV;
	private Channel arrayChannel;
	private boolean sumFrames = false; // false by default to maintain backwards compatibility with Spring config
	private Dataset summedFrames;

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		try {
			arrayChannel = epicsController.createChannel(arrayPV);

			final Channel frameNumber = epicsController.createChannel(frameNumberPV);
			epicsController.setMonitor(frameNumber, this::updatedFrameReceived);

			// If we are accumulating frames need to know when a new acquisition starts so we can clear the summedFrames
			if (sumFrames) {
				final Channel acquireChannel = epicsController.createChannel(acquirePV);
				epicsController.setMonitor(acquireChannel, this::acquireStatusChanged);
			}

		} catch (Exception e) {
			logger.error("Error setting up analyser live visualisation", e);
			throw new FactoryException("Cannot set up monitoring of arrays", e);
		}
		setConfigured(true);
	}

	private void updatedFrameReceived(final MonitorEvent event) {
		logger.trace("Might soon be sending some thing to plot {} with axes from {} because of {}", plotName, analyser.getName(), event);

		try {
			executor.submit(this::plotNewArray);
			logger.trace("Plot jobs for {} queued successfully", plotName);
		} catch (RejectedExecutionException ree) {
			logger.debug("Plot jobs for {} are queueing up, as expected in certain circumstances, so this one got skipped", plotName);
			logger.trace("Exception for rejected execution", ree);
		}
	}

	private void acquireStatusChanged(final MonitorEvent event) {
		// This could be a start or stop event but it doesn't actually matter so don't need to parse the event
		logger.trace("Received change of acquire state: {}", event);

		// Remove the existing summed data
		summedFrames = null;
	}

	private IDataset getArrayAsDataset(int x, int y) throws Exception {
		int[] dims = new int[] {x, y};
		int arraySize = dims[0]*dims[1];
		if (arraySize < 1) {
			throw new IllegalArgumentException(String.format("arraySize was less than 1. x=%d y=%d", x, y));
		}
		logger.trace("About to get array for {}", plotName);
		// Get as float[] not double[] for performance
		float[] array = epicsController.cagetFloatArray(arrayChannel, arraySize);
		Dataset newData = DatasetFactory.createFromObject(array, dims);
		// Flip the data across the 0 position of the Y axis I05-221
		Dataset flipedData = DatasetUtils.flipUpDown(newData);

		if (!sumFrames) {
			return flipedData; // If we're not accumulating just return the newest data
		}
		else { // We are summing frames
			if (summedFrames == null) { // i.e A new acquire has just started
				summedFrames = flipedData;
			}
			else {
				// Add the new data to the existing summed data
				summedFrames.iadd(flipedData);
			}
			return summedFrames;
		}
	}

	private IDataset getXAxis() throws Exception {
		double[] xdata = analyser.getEnergyAxis();
		logger.info("X axis size is " + xdata.length);
		IDataset xAxis = DatasetFactory.createFromObject(xdata);
		xAxis.setName("energies (eV)");
		return xAxis;
	}

	private IDataset getYAxis() throws Exception {
		double[] ydata = analyser.getAngleAxis();
		logger.info("Y axis size is " + ydata.length);
		// Get as a list so we can reverse it easily
		List<Double> list = Arrays.stream(ydata).boxed().collect(Collectors.toList());
		Collections.reverse(list); // Flips the Y scale I05-221
		IDataset yAxis = DatasetFactory.createFromObject(list);

		if (analyser.getLensMode().toLowerCase().contains("spat")) {
			yAxis.setName("location (mm)");
		} else
			yAxis.setName("angles (deg)");
		return yAxis;
	}

	private void plotNewArray() {
		try {
			IDataset xAxis = getXAxis();
			IDataset yAxis = getYAxis();
			IDataset ds = getArrayAsDataset(yAxis.getShape()[0], xAxis.getShape()[0]);

			logger.trace("Dispatching plot to {}", plotName);
			SDAPlotter.imagePlot(plotName, xAxis, yAxis, ds);
		} catch (Exception e) {
			logger.error("Exception caught preparing analyser live plot", e);
		}
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public IElectronAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IElectronAnalyser analyser) {
		this.analyser = analyser;
	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

	public String getFrameNumberPV() {
		return frameNumberPV;
	}

	public void setFrameNumberPV(String frameNumberPV) {
		this.frameNumberPV = frameNumberPV;
	}

	public Channel getArrayChannel() {
		return arrayChannel;
	}

	public void setArrayChannel(Channel arrayChannel) {
		this.arrayChannel = arrayChannel;
	}

	public String getAcquirePV() {
		return acquirePV;
	}

	public void setAcquirePV(String acquirePV) {
		this.acquirePV = acquirePV;
	}

	public boolean isSumFrames() {
		return sumFrames;
	}

	public void setSumFrames(boolean sumFrames) {
		this.sumFrames = sumFrames;
	}

}