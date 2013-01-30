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

package uk.ac.gda.client.microfocus.controller;

import gda.analysis.RCPPlotter;
import gda.device.DeviceException;
import gda.jython.JythonServerFacade;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.gda.client.microfocus.util.MicroFocusMappableDataProvider;
import uk.ac.gda.client.microfocus.util.MicroFocusMappableDataProviderFactory;
import uk.ac.gda.client.microfocus.util.MicroFocusNexusPlotter;
import uk.ac.gda.client.microfocus.util.ObjectStateManager;

public class MicroFocusDisplayController {
	private MicroFocusMappableDataProvider detectorProvider;
	private MicroFocusMappableDataProvider currentDetectorProvider;
	private String xScannable = "sc_MicroFocusSampleX";
	private String yScannable = "sc_MicroFocusSampleY";
	private MicroFocusNexusPlotter plotter;
	private String detectorFile;
	private int currentDetectorElementNo;
	private String[] trajectoryScannableName;
	private String zScannableName;
	private String trajectoryCounterTimerName;

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusDisplayController.class);

	public MicroFocusDisplayController() {
		super();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.gda.microfocus.xScannableName");
		if ((null != config) && (config.length > 0)) {
			xScannable = config[0].getAttribute("name");
			logger.info("the x scannable ffrom extn is " + xScannable);
		}
		config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.microfocus.yScannableName");
		if ((null != config) && (config.length > 0)) {
			yScannable = config[0].getAttribute("name");
			logger.info("the y scannable ffrom extn is " + yScannable);
		}
		config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.microfocus.zScannableName");
		if ((null != config) && (config.length > 0)) {
			zScannableName = config[0].getAttribute("name");
			logger.info("the z scannable ffrom extn is " + zScannableName);
		}
		config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.gda.microfocus.trajectory.xScannableName");
		if ((null != config) && (config.length > 0)) {
			trajectoryScannableName = new String[config.length];
			for (int i = 0; i < config.length; i++)
				trajectoryScannableName[i] = config[i].getAttribute("name");
		}
		config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.gda.microfocus.trajectory.counterTimerName");
		if ((null != config) && (config.length > 0)) {
			trajectoryCounterTimerName = config[0].getAttribute("name");
		}

	}

	@SuppressWarnings("static-access")
	public boolean displayPlot(int x, int y) throws Exception {
		if (currentDetectorProvider != null && ObjectStateManager.isActive(detectorProvider)) {
			double[] spectrum = currentDetectorProvider.getSpectrum(this.currentDetectorElementNo, y, x);
			if (spectrum != null) {
				AbstractDataset yaxis = AbstractDataset.array(spectrum);

				logger.info("Plotting spectrum for " + this.currentDetectorElementNo + "," + x + "," + y);
				try {
					RCPPlotter.plot("McaPlot", yaxis);
					return true;
				} catch (DeviceException e) {
					logger.error("Unable to plot the spectrum for " + x + " " + y, e);
					throw new Exception("Unable to plot the spectrum for " + x + " " + y, e);

				}
			}
			throw new Exception("No Spectrum available for index " + x + "," + y);
		}
		// server needs to show the spectrum
		logger.info("Plotting spectrum for " + this.currentDetectorElementNo + "," + x + "," + y);
		JythonServerFacade.getInstance().evaluateCommand(
				"map.getMFD().plotSpectrum(" + this.currentDetectorElementNo + "," + x + "," + y + ")");
		return true;
	}

	private void setDataProviderForElement(String selectedElement) {
		if (detectorProvider.hasPlotData(selectedElement))
			currentDetectorProvider = detectorProvider;
		else
			currentDetectorProvider = null;

	}

	public void displayMap(String selectedElement) throws Exception {
		// check whether map scan is running
		String active = JythonServerFacade.getInstance().evaluateCommand("map.getMFD().isActive()");

		// if a map scan is running then disable the provider but what does provider provide? Names are important!
		if (active != null) {
			if (active.equals("True")) {
				disableProvider();
			}
		}

		if (ObjectStateManager.isActive(detectorProvider)) {
			if (plotter != null) {
				setDataProviderForElement(selectedElement);
				// if (currentDetectorProvider != null) {
				plotter.setDataProvider(currentDetectorProvider);
				plotter.plotElement(selectedElement);
				// } else
				// throw new Exception("Unable to display map for element " + selectedElement);
			}
		} else
			JythonServerFacade.getInstance().evaluateCommand("map.getMFD().displayPlot(\"" + selectedElement + "\")");
	}

	public void displayMap(String selectedElement, String filePath, Object bean) throws Exception {
		if (plotter == null)
			plotter = new MicroFocusNexusPlotter();
		plotter.setPlottingWindowName("MapPlot");
		detectorProvider = MicroFocusMappableDataProviderFactory.getInstance(bean);
		ObjectStateManager.register(detectorProvider);
		detectorProvider.setXScannableName(xScannable);
		detectorProvider.setYScannableName(yScannable);
		detectorProvider.setTrajectoryScannableName(trajectoryScannableName);
		detectorProvider.setZScannableName(zScannableName);
		detectorProvider.setTrajectoryCounterTimerName(trajectoryCounterTimerName);

		
		detectorProvider.loadBean(bean);
		detectorProvider.loadData(filePath);
		ObjectStateManager.setActive(detectorProvider);
		displayMap(selectedElement);

		logger.info("displayed map for " + selectedElement + " " + currentDetectorProvider);
	}

	public void displayMap(String selectedElement, String filePath) throws Exception {
		if (plotter == null)
			plotter = new MicroFocusNexusPlotter();
		plotter.setPlottingWindowName("MapPlot");

		if (detectorProvider == null) {
			detectorProvider = MicroFocusMappableDataProviderFactory.getInstance(detectorFile);
			ObjectStateManager.register(detectorProvider);
			detectorProvider.setXScannableName(xScannable);
			detectorProvider.setYScannableName(yScannable);
			detectorProvider.setTrajectoryScannableName(trajectoryScannableName);
			detectorProvider.setZScannableName(zScannableName);
			detectorProvider.setTrajectoryCounterTimerName(trajectoryCounterTimerName);
		}
		detectorProvider.loadBean();
		detectorProvider.loadData(filePath);
		ObjectStateManager.setActive(detectorProvider);
		displayMap(selectedElement);

		logger.info("displayed map for " + selectedElement + " " + currentDetectorProvider);
	}

	public void setDetectorFile(String filename) {
		this.detectorFile = filename;
	}

	public void dispose() {
	}

	public double[] getXY(int y, int x) {
		double xy[] = new double[3];
		if (currentDetectorProvider != null && ObjectStateManager.isActive(detectorProvider)) {
			xy[0] = currentDetectorProvider.getXarray()[x];
			xy[1] = currentDetectorProvider.getYarray()[y];
			xy[2] = currentDetectorProvider.getZValue();
			return xy;
		}
		Object s = JythonServerFacade.getInstance().evaluateCommand("map.getMFD().getXY(" + x + "," + y + ")");

		if (s instanceof String) {
			String xyString = (String) s;
			xyString = xyString.substring(xyString.indexOf("[") + 1, xyString.indexOf("]"));
			StringTokenizer tokens = new StringTokenizer(xyString, ",");
			for (int i = 0; i < xy.length; i++) {
				if (tokens.hasMoreTokens())
					xy[i] = Double.valueOf(tokens.nextToken());
			}
		}
		logger.info("the xy from server is " + s);

		return xy;
	}

	public void disableProvider() {
		ObjectStateManager.setInactive(detectorProvider);
	}

	public void setDetectorElementNumber(int selection) {
		this.currentDetectorElementNo = selection;
	}
}
