/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.jython.JythonServerFacade;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.microfocus.util.MicroFocusMappableDataProvider;
import uk.ac.gda.client.microfocus.util.MicroFocusMappableDataProviderFactory;
import uk.ac.gda.client.microfocus.util.MicroFocusNexusPlotter;
import uk.ac.gda.client.microfocus.util.ObjectStateManager;

public class MicroFocusDisplayController {
	private MicroFocusMappableDataProvider detectorProvider;
	private MicroFocusMappableDataProvider currentDetectorProvider;
	private MicroFocusNexusPlotter plotter = new MicroFocusNexusPlotter();
	private String[] trajectoryScannableName;
	private String xScannable = "sc_MicroFocusSampleX";
	private String yScannable = "sc_MicroFocusSampleY";
	private String detectorFile;
	private String zScannableName;
	private String trajectoryCounterTimerName;

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusDisplayController.class);

	public MicroFocusDisplayController() {
		super();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.gda.microfocus.xScannableName");
		if ((null != config) && (config.length > 0)) {
			xScannable = config[0].getAttribute("name");
			logger.debug("the x scannable from extn is " + xScannable);
		}
		config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.microfocus.yScannableName");
		if ((null != config) && (config.length > 0)) {
			yScannable = config[0].getAttribute("name");
			logger.debug("the y scannable from extn is " + yScannable);
		}
		config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.microfocus.zScannableName");
		if ((null != config) && (config.length > 0)) {
			zScannableName = config[0].getAttribute("name");
			logger.debug("the z scannable from extn is " + zScannableName);
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

	private void setDataProviderForElement(String selectedElement) {
		if (detectorProvider.hasPlotData(selectedElement))
			currentDetectorProvider = detectorProvider;
		else
			currentDetectorProvider = null;
	}

	public void displayMap(String selectedElement) {
		// check whether map scan is running
		String mfd = JythonServerFacade.getInstance().evaluateCommand("map.getMFD()");
		String active = null;
		if (mfd != null && !mfd.equals("None"))
			active = JythonServerFacade.getInstance().evaluateCommand("map.getMFD().isActive()");

		// if a map scan is running then disable the provider but what does provider provide? Names are important!
		if (active != null) {
			if (active.equals("True")) {
				disableProvider();
			}
		}

		if (selectedElement.equals("I0")) {
			if (detectorProvider != null) {
				plotter.plotMapFromServer(selectedElement);
				plotter.plotDataset(detectorProvider.getI0data());
			} else {
				plotter.plotMapFromServer(selectedElement);
			}
		} else if (selectedElement.equals("It")) {
			if (detectorProvider != null) {
				plotter.plotDataset(detectorProvider.getItdata());
			} else {
				plotter.plotMapFromServer(selectedElement);
			}
		} else if (ObjectStateManager.isActive(detectorProvider)) {
			if (plotter != null) {
				setDataProviderForElement(selectedElement);
				plotter.setDataProvider(currentDetectorProvider);
				plotter.plotElement(selectedElement);
			}
		} else {
			plotter.plotMapFromServer(selectedElement);
		}
	}

	public void displayMap(String selectedElement, String filePath, Object bean) {
		if (plotter == null)
			plotter = new MicroFocusNexusPlotter();
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
		// hack warning!!
		// (Need to refactor so there is a single, distributed object which controls the map plotting instead of having
		// two competing methods)
		JythonServerFacade.getInstance().runCommand("map.getMFD().setActive(False)");
		displayMap(selectedElement);

		logger.debug("displayed map for " + selectedElement + " using " + currentDetectorProvider.getClass());
	}

	public void displayMap(String selectedElement, String filePath) {
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

		logger.debug("displayed map for " + selectedElement + " using " + currentDetectorProvider.getClass());
	}

	public void setDetectorFile(String filename) {
		this.detectorFile = filename;
	}

	public Double getZ() {
		if (currentDetectorProvider != null && ObjectStateManager.isActive(detectorProvider)) {
			return currentDetectorProvider.getZValue();
		}
		return null;
	}

	public void disableProvider() {
		ObjectStateManager.setInactive(detectorProvider);
	}
}
