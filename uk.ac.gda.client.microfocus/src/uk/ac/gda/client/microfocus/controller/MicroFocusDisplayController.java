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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.client.microfocus.util.MicroFocusMappableDataProvider;
import uk.ac.gda.client.microfocus.util.MicroFocusMappableDataProviderFactory;
import uk.ac.gda.client.microfocus.util.MicroFocusNexusPlotter;

public class MicroFocusDisplayController {
	private MicroFocusMappableDataProvider fileDataProvider;
	private MicroFocusNexusPlotter plotter = new MicroFocusNexusPlotter();
	private String[] trajectoryScannableName;
	private String xScannableName = "sc_MicroFocusSampleX";
	private String yScannableName = "sc_MicroFocusSampleY";
	private String zScannableName = "sc_sample_z";
	private String detectorFile;
	private String trajectoryCounterTimerName;
	private boolean fileIsDataSource = false;

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusDisplayController.class);

	public MicroFocusDisplayController() {
		super();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.gda.microfocus.xScannableName");
		if ((null != config) && (config.length > 0)) {
			xScannableName = config[0].getAttribute("name");
			logger.debug("the x scannable from extn is " + xScannableName);
		}
		config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.microfocus.yScannableName");
		if ((null != config) && (config.length > 0)) {
			yScannableName = config[0].getAttribute("name");
			logger.debug("the y scannable from extn is " + yScannableName);
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

	public void displayMap(String selectedElement, Integer selectedChannel) {
		if (selectedElement.equals("I0")) {
			if (fileDataProvider != null && fileIsDataSource) {
				plotter.plotDataset(fileDataProvider.getI0data());
			} else {
				plotter.plotMapFromServer("I0",0);
			}
		} else if (selectedElement.equals("It")) {
			if (fileDataProvider != null && fileIsDataSource) {
				plotter.plotDataset(fileDataProvider.getItdata());
			} else {
				plotter.plotMapFromServer("It",0);
			}
		} else if (fileIsDataSource) {
			if (plotter != null) {
				plotter.plotElement(fileDataProvider, selectedElement, selectedChannel);
			}
		} else {
			plotter.plotMapFromServer(selectedElement,selectedChannel);
		}
	}

	public void displayMap(String selectedElement, String filePath, IRichBean bean, Integer channelToDisplay) {
		if (plotter == null)
			plotter = new MicroFocusNexusPlotter();
		fileDataProvider = MicroFocusMappableDataProviderFactory.getInstance(bean);
		fileDataProvider.setXScannableName(xScannableName);
		fileDataProvider.setYScannableName(yScannableName);
		fileDataProvider.setTrajectoryScannableName(trajectoryScannableName);
		fileDataProvider.setZScannableName(zScannableName);
		fileDataProvider.setTrajectoryCounterTimerName(trajectoryCounterTimerName);

		fileDataProvider.loadBean(bean);
		fileDataProvider.loadData(filePath);
		setFileIsDataSource(true);
		displayMap(selectedElement,channelToDisplay);
		
		logger.debug("displayed map for " + selectedElement + " using " + fileDataProvider.getClass());
	}

	public void displayMap(String selectedElement, String filePath, Integer channelToDisplay) {
		if (plotter == null)
			plotter = new MicroFocusNexusPlotter();

		if (fileDataProvider == null) {
			fileDataProvider = MicroFocusMappableDataProviderFactory.getInstance(detectorFile);
			fileDataProvider.setXScannableName(xScannableName);
			fileDataProvider.setYScannableName(yScannableName);
			fileDataProvider.setTrajectoryScannableName(trajectoryScannableName);
			fileDataProvider.setZScannableName(zScannableName);
			fileDataProvider.setTrajectoryCounterTimerName(trajectoryCounterTimerName);
		}
		fileDataProvider.loadBean();
		fileDataProvider.loadData(filePath);
		setFileIsDataSource(true);
		displayMap(selectedElement,channelToDisplay);

		logger.debug("displayed map for " + selectedElement + " using " + fileDataProvider.getClass());
	}

	public void setDetectorFile(String filename) {
		this.detectorFile = filename;
	}

	public Double getZ() {
		if (isFileIsDataSource()) {
			return fileDataProvider.getZValue();
		}
		return plotter.getZValueFromServer();
	}

	public void disableFileDataProvider() {
		setFileIsDataSource(false);
	}

	public boolean isFileIsDataSource() {
		return fileIsDataSource;
	}

	public void setFileIsDataSource(boolean fileIsDataSource) {
		this.fileIsDataSource = fileIsDataSource;
	}
}
