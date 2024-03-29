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

package org.opengda.detector.electronanalyser.server;

import java.util.Arrays;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

class AnalyserSweptLiveDataDispatcher extends FindableConfigurableBase {
	private static final Logger logger = LoggerFactory.getLogger(AnalyserSweptLiveDataDispatcher.class);

	private String plotName;
	private VGScientaAnalyser analyser;
	private EpicsController epicsController;
	private String arrayPV;

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			epicsController = EpicsController.getInstance();
			try {
				epicsController.setMonitor(epicsController.createChannel(arrayPV), this::onMonitorChanged);
			} catch (Exception e) {
				throw new FactoryException("Cannot set up monitoring of arrays", e);
			}
			setConfigured(true);
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

	private void onMonitorChanged(MonitorEvent arg0) {
		try {
			logger.debug("sending some thing from "+arg0.toString()+" to plot "+plotName+" with axes from "+analyser.getName());
			double[] value = (double[]) arg0.getDBR().getValue();

			int[] dims = new int[] {analyser.getNdArray().getPluginBase().getArraySize1_RBV(), analyser.getNdArray().getPluginBase().getArraySize0_RBV()};
			int arraysize = dims[0]*dims[1];
			if (arraysize < 1) return;
			value = Arrays.copyOf(value, arraysize);
			Dataset ds = DatasetFactory.createFromObject(value, dims);

			double[] xdata = analyser.getEnergyAxis();
			double[] ydata = analyser.getAngleAxis();
			Dataset xAxis = DatasetFactory.createFromObject(xdata);
			Dataset yAxis = DatasetFactory.createFromObject(ydata);
			xAxis.setName("energies (eV)");
			if ("Transmission".equalsIgnoreCase(analyser.getLensMode())) {
				yAxis.setName("location (mm)");
			} else {
				yAxis.setName("angles (deg)");
			}

			SDAPlotter.imagePlot(plotName, xAxis, yAxis, ds);
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live plot", e);
		}
	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}
}