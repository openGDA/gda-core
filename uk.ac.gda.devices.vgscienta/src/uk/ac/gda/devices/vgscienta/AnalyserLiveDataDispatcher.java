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

package uk.ac.gda.devices.vgscienta;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.Channel;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

class AnalyserLiveDataDispatcher implements Configurable, Findable {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserLiveDataDispatcher.class);

	private String plotName;
	private IVGScientaAnalyserRMI analyser;
	private String name;
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

		if (!sumFrames) {
			return newData; // If we're not accumulating just return the newest data
		}
		else { // We are summing frames
			if (summedFrames == null) { // i.e A new acquire has just started
				summedFrames = newData;
			}
			else {
				// Add the new data to the existing summed data and return it.
				return summedFrames.iadd(newData);
			}
			return DatasetFactory.createFromObject(array, dims);
		}
	}

	private IDataset getXAxis() throws Exception {
		double[] xdata = analyser.getEnergyAxis();
		IDataset xAxis = DatasetFactory.createFromObject(xdata);
		xAxis.setName("energies (eV)");
		return xAxis;
	}

	private IDataset getYAxis() throws Exception {
		double[] ydata = analyser.getAngleAxis();
		IDataset yAxis = DatasetFactory.createFromObject(ydata);
		if ("Transmission".equalsIgnoreCase(analyser.getLensMode())) {
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

	public IVGScientaAnalyserRMI getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyserRMI analyser) {
		this.analyser = analyser;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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