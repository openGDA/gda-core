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

import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

class AnalyserLiveDataDispatcher implements MonitorListener, Configurable, Findable {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserLiveDataDispatcher.class);

	private String plotName;
	protected VGScientaAnalyser analyser;
	private String name;
	private EpicsController epicsController;
	private String arrayPV, frameNumberPV;
	private long oldNumber;
	private Channel arrayChannel;
	private long sleeptime = 1000;

	private ThreadPoolExecutor executor;
	
	@Override
	public void configure() throws FactoryException {
		epicsController = EpicsController.getInstance();
		try {
			epicsController.setMonitor(epicsController.createChannel(frameNumberPV), this);
			arrayChannel = epicsController.createChannel(arrayPV);
		} catch (Exception e) {
			throw new FactoryException("Cannot set up monitoring of arrays", e);
		}
		executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
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
			logger.debug("sending some thing from "+arg0.toString()+" to plot "+plotName+" with axes from "+analyser.getName());
			
			int newvalue =((gov.aps.jca.dbr.INT) arg0.getDBR().convert(DBRType.INT)).getIntValue()[0];
			
			if (newvalue > oldNumber) {
				try {
					executor.submit(new Runnable() {
						@Override
						public void run() {
							try {
								plotNewArray();
								Thread.sleep(sleeptime);
							} catch (Exception e) {
								logger.error("exception caught preparing analyser live plot", e);
							}
						}
					});
				} catch (RejectedExecutionException ree) {
					logger.debug("plot jobs for "+plotName+"are queueing up, as expected in certain circumstances", ree);
				}
			}
			oldNumber = newvalue;
			
			
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live plot", e);
		}
	}

	protected AbstractDataset getArrayAsDataset(int x, int y) throws Exception {
		int[] dims = new int[] {x, y};
		int arraysize = dims[0]*dims[1];
		if (arraysize < 1) return null;
		double[] value = (double[]) arrayChannel.get(arraysize).getValue();
		return new DoubleDataset(value, dims);
	}
	
	protected AbstractDataset getXAxis() throws Exception {
		double[] xdata = analyser.getEnergyAxis();
		DoubleDataset xAxis = new DoubleDataset(xdata, new int[] { xdata.length });
		xAxis.setName("energies (eV)");
		return xAxis;
	}	
	
	protected AbstractDataset getYAxis() throws Exception {
		double[] ydata = analyser.getAngleAxis();
		DoubleDataset yAxis = new DoubleDataset(ydata, new int[] { ydata.length });
		if ("Transmission".equalsIgnoreCase(analyser.getLensMode())) {
			yAxis.setName("location (mm)");				
		} else 
			yAxis.setName("angles (deg)");
		return yAxis;
	}
	
	protected void plotNewArray() throws Exception {
		AbstractDataset xAxis = getXAxis();
		AbstractDataset yAxis = getYAxis();
		AbstractDataset ds = getArrayAsDataset(xAxis.getShape()[0], yAxis.getShape()[0]);
		logger.debug("dispatching plot to "+plotName);
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

	public long getSleeptime() {
		return sleeptime;
	}

	public void setSleeptime(long sleeptime) {
		this.sleeptime = sleeptime;
	}
}