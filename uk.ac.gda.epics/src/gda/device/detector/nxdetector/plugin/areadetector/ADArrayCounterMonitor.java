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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADArrayCounterMonitor implements NXPlugin {

	//
	private boolean waitForArrayCounter = true;
	
	private final ADBase adBase;

	private Integer nextIndexToGiveOut;
	
	public ADArrayCounterMonitor(ADBase adBase) {
		this.adBase = adBase;
	}
	
	public boolean isWaitForArrayCounter() {
		return waitForArrayCounter;
	}

	public void setWaitForArrayCounter(boolean waitForArrayCounter) {
		this.waitForArrayCounter = waitForArrayCounter;
	}

	@Override
	public String getName() {
		return "counter_monitor";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		nextIndexToGiveOut = adBase.getArrayCounter_RBV() + 1;
	}
	
	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
		nextIndexToGiveOut = null; // to avoid confusion
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}
	
	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		if (nextIndexToGiveOut==null) {
			throw new IllegalStateException("nextIndexToGiveOut is null. Ensure prepareForCollection is called before read.");
		}
		NXDetectorDataAppender appender;
		if (isWaitForArrayCounter()) {
			appender = new ArrayCounterWaiter(adBase, nextIndexToGiveOut);
		} else {
			appender = new NXDetectorDataNullAppender();
		}
		nextIndexToGiveOut++;
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
			appenders.add(appender);
		return appenders;
	}

	@Override
	public boolean callReadBeforeNextExposure() {
		return false;
	}
	


}

class ArrayCounterWaiter implements NXDetectorDataAppender {
	
	private final ADBase adBase;
	private final int desiredCount;

	private static final Logger logger = LoggerFactory.getLogger(ArrayCounterWaiter.class);

	ArrayCounterWaiter(ADBase adBase, int desiredCount) {
		this.adBase = adBase;
		this.desiredCount = desiredCount;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		

		// 1. Wait for the array counter
		String msg = "waiting for '" + detectorName + "' array counter PV to reach " + desiredCount;
		
		logger.info(Thread.currentThread().getName() + " " + msg);
		try {
			adBase.waitForArrayCounterToReach(desiredCount, Double.MAX_VALUE);
		} catch (Exception e) {
			throw new DeviceException("Problem " + msg);
		}
		
		// 2. do nothing to data
	}
	
}
