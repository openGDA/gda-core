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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

class AnalyserLiveDataDispatcher implements MonitorListener, Configurable, Findable {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserLiveDataDispatcher.class);

	private String plotName;
	protected VGScientaAnalyser analyser;
	private String name;
	private EpicsController epicsController;
	private String arrayPV, frameNumberPV;
	private long oldNumber = -1;
	private Channel arrayChannel;

	private ThreadPoolExecutor executor;

	@Override
	public void configure() throws FactoryException {
		epicsController = EpicsController.getInstance();
		executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
		try {
			arrayChannel = epicsController.createChannel(arrayPV);
			epicsController.setMonitor(epicsController.createChannel(frameNumberPV), this);
		} catch (Exception e) {
			throw new FactoryException("Cannot set up monitoring of arrays", e);
		}
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
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

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		try {
			logger.trace("might soon be sending some thing to plot " + plotName + " with axes from " + analyser.getName() + " because of " + arg0.toString());

			int newvalue =((gov.aps.jca.dbr.INT) arg0.getDBR().convert(DBRType.INT)).getIntValue()[0];

			if (newvalue > oldNumber && newvalue > 0) {
				try {
					executor.submit(new Runnable() {
						@Override
						public void run() {
							try {
								plotNewArray();
							} catch (Exception e) {
								logger.error("exception caught preparing analyser live plot", e);
							}
						}
					});
					logger.trace("plot jobs for " + plotName + " queued successfully");
				} catch (RejectedExecutionException ree) {
					logger.debug("plot jobs for "+plotName+" are queueing up, as expected in certain circumstances, so this one got skipped");
				}
			}
			oldNumber = newvalue;

		} catch (Exception e) {
			logger.error("exception caught preparing analyser live plot", e);
		}
	}

	protected Dataset getArrayAsDataset(int x, int y) throws Exception {
		int[] dims = new int[] {x, y};
		int arraysize = dims[0]*dims[1];
		if (arraysize < 1) return null;
		logger.trace("about to get array for " + plotName);
//		double[] value = (double[]) arrayChannel.get(arraysize).getValue();
		// return DatasetFactory.createFromObject(DoubleDataset.class, value, dims);
		float[] array = epicsController.cagetFloatArray(arrayChannel, arraysize);
		return DatasetFactory.createFromObject(array, dims);
	}

	protected Dataset getXAxis() throws Exception {
		double[] xdata = analyser.getEnergyAxis();
		Dataset xAxis = DatasetFactory.createFromObject(xdata);
		xAxis.setName("energies (eV)");
		return xAxis;
	}

	protected Dataset getYAxis() throws Exception {
		double[] ydata = analyser.getAngleAxis();
		Dataset yAxis = DatasetFactory.createFromObject(ydata);
		if ("Transmission".equalsIgnoreCase(analyser.getLensMode())) {
			yAxis.setName("location (mm)");
		} else
			yAxis.setName("angles (deg)");
		return yAxis;
	}

	protected void plotNewArray() throws Exception {
		Dataset xAxis = getXAxis();
		Dataset yAxis = getYAxis();
		Dataset ds = getArrayAsDataset(yAxis.getShape()[0], xAxis.getShape()[0]);
		if (ds == null)
			return;
		if (ds.max().intValue() <= 0)
			logger.warn("something fishy - no positive values in sight");
		logger.trace("dispatching plot to " + plotName);
		SDAPlotter.imagePlot(plotName, xAxis, yAxis, ds);
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

}