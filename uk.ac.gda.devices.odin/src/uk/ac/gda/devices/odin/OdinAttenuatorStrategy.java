/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import uk.ac.gda.devices.odin.control.OdinDetectorController;

public class OdinAttenuatorStrategy extends OdinSingleFileStrategy {

	private static final Logger logger = LoggerFactory.getLogger(OdinAttenuatorStrategy.class);

	private final OdinDetectorController controller;

	private String basePv;
	private final PV<String> filterState;
	private final PV<String> filterMode;
	private final PV<Integer> resetFilters;
	private final PV<Integer> clearError;
	private final PV<Integer> startSingleShot;
	private final PV<Double> timeout;

	private int numOfFastFrames = 10;
	private double fastExpTime = 0.01;

	public OdinAttenuatorStrategy(OdinDetectorController controller, String basePv) {
		super(controller);
		this.controller = controller;
		this.basePv = basePv;
		Objects.requireNonNull(this.basePv);
		filterState = LazyPVFactory.newEnumPV(basePv + "STATE", String.class);
		resetFilters = LazyPVFactory.newEnumPV(basePv + "RESET", Integer.class);
		clearError = LazyPVFactory.newEnumPV(basePv + "ERROR:CLEAR", Integer.class);
		startSingleShot = LazyPVFactory.newEnumPV(basePv + "SINGLESHOT:START", Integer.class);
		filterMode = new PVWithSeparateReadback<>(LazyPVFactory.newEnumPV(basePv + "MODE", String.class),
				LazyPVFactory.newReadOnlyEnumPV(basePv + "MODE_RBV", String.class));
		timeout = LazyPVFactory.newDoublePV(basePv + "TIMEOUT");
	}

	/**
	 * Set correct attenuator mode. Also set the filter timeout to a reasonably long value to ensure
	 * the timeout does not occur whilst the strategy is changed and detector re-armed
	 */
	@Override
	public void prepareWriterForScan(String detName, int scanNumber, double collectionTime) throws DeviceException {
		logger.info("Preparing filters for single shot scan");
		try {
			timeout.putWait(collectionTime + 6.0);
			filterMode.putWait("SINGLESHOT");
			filterState.waitForValue("SINGLESHOT_WAITING"::equals, 5.0);
		} catch (IOException | IllegalStateException | TimeoutException e) {
			throw new DeviceException("Could not set attenuator mode");
		} catch (InterruptedException e) {
			logger.error("Interrupted while setting attenuator mode", e);
			Thread.currentThread().interrupt();
		}
	}


	/**
	 * In this strategy a fast acquisition series is performed to allow the attenuators to
	 * be configured, these frames are not recorded.
	 * <p>
	 * After this the detector and filewriter are configured for the main exposure which is recored to file.
	 */
	@Override
	public void prepareWriterForPoint(int pointNumber) throws DeviceException {
		logger.info("Preparing point for single shot scan");

		var scanAcquirePeriod = controller.getAcquirePeriod();
		var scanAcquireTime = controller.getAcquireTime();
		controller.setCompressionMode("no_hdf");
		controller.setNumImages(numOfFastFrames);
		controller.setAcquirePeriod(fastExpTime);
		controller.setAcquireTime(fastExpTime);
		try {
			logger.info("Reset filter state");
			resetFilters.putWait(1);
			clearError.putWait(1);
			logger.info("Performing automatic attenuation");
			startSingleShot.putWait(1);
			controller.startCollection();
			filterState.waitForValue("SINGLESHOT_COMPLETE"::equals, 10);
			controller.waitWhileAcquiring();

		} catch (TimeoutException e) {
			throw new DeviceException("Timed out waiting for auto attentuation to complete");
		} catch (IllegalStateException | IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(e);
		} finally {
			// Ensure that this is always restored if attenuation fails
			controller.setCompressionMode("off");
		}


		// Restore to intermediate state -> exp time, num images,
		controller.setNumImages(1);
		controller.setAcquirePeriod(scanAcquirePeriod);
		controller.setAcquireTime(scanAcquireTime);

		super.prepareWriterForPoint(pointNumber);
	}

	public int getNumOfFastFrames() {
		return numOfFastFrames;
	}

	public void setNumOfFastFrames(int numOfFastFrames) {
		this.numOfFastFrames = numOfFastFrames;
	}

	public double getFastExpTime() {
		return fastExpTime;
	}

	public void setFastExpTime(double fastExpTime) {
		this.fastExpTime = fastExpTime;
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

}
