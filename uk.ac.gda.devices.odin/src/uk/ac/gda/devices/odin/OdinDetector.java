/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.odin;

import java.util.Objects;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;

/**
 * Detector device for Odin containing a controller to communicate with hardware and a strategy
 * for specific collection types.
 */
public class OdinDetector extends DetectorBase implements NexusDetector, PositionCallableProvider<NexusTreeProvider> {

	private static final Logger logger = LoggerFactory.getLogger(OdinDetector.class);
	private static final long serialVersionUID = -74052607473419331L;

	private OdinDetectorController controller;
	private OdinStrategy collectionStrategy;
	private volatile int scanPointNumber;
	private Callable<NexusTreeProvider> latestPositionCallable;
	private String detectorID = "OdinDetector";
	private String detectorType = "OdinDetector";
	private String description = "OdinDetector";




	@Override
	public void configure() throws FactoryException {

		Objects.requireNonNull(collectionStrategy, "Collection strategy must be set");

		setInputNames(collectionStrategy.getInputNames());
		setExtraNames(collectionStrategy.getExtraNames());
		setOutputFormat(collectionStrategy.getOutputFormat());
		setConfigured(true);
	}


	@Override
	public void atScanStart() throws DeviceException {
		scanPointNumber = 0;
		logger.debug("Start of scan");
		int scanNumber = InterfaceProvider.getCurrentScanInformationHolder()
				.getCurrentScanInformation().getScanNumber();
		collectionStrategy.prepareWriterForScan(getName(), scanNumber, collectionTime);
	}


	@Override
	public void collectData() throws DeviceException {
		logger.debug("Starting collection");
		latestPositionCallable = null;
		scanPointNumber++;
		collectionStrategy.prepareWriterForPoint(scanPointNumber);
	}

	@Override
	public int getStatus() throws DeviceException {
		return collectionStrategy.getStatus();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		collectionStrategy.waitWhileBusy(scanPointNumber);
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		logger.debug("Readout called for: {}", getName());
		return getNXDetectorData();
	}

	private NXDetectorData getNXDetectorData() throws DeviceException {
		try {
			return collectionStrategy.getNXDetectorData(getName(), collectionTime, scanPointNumber);
		} catch (Exception e) {
			throw new DeviceException("Error in readout for " + getName(), e);
		}
	}


	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	public void setDetectorID(String detectorID) {
		this.detectorID = detectorID;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return detectorType;
	}

	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}

	public void setController(OdinDetectorController controller) {
		this.controller = controller;
	}

	public OdinDetectorController getController() {
		return this.controller;
	}



	@Override
	public void atScanEnd() throws DeviceException {
		logger.debug("End of scan");
		controller.stopCollection(); // stop detector acquire
		controller.endRecording(); // stop data writer
	}

	@Override
	public void stop() throws DeviceException {
		controller.stopCollection();
		controller.endRecording();
	}


	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		if (latestPositionCallable != null) {
			return latestPositionCallable;
		}
		else {
			NexusTreeProvider data = readout();
			latestPositionCallable = () -> data;
			return latestPositionCallable;
		}
	}


	public OdinStrategy getCollectionStrategy() {
		return collectionStrategy;
	}


	public void setCollectionStrategy(OdinStrategy collectionStrategy) {
		this.collectionStrategy = collectionStrategy;
	}

}
