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

package gda.device.detector.addetectorprovisional;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import static gda.device.detector.addetector.ADDetector.readoutArrayIntoNXDetectorData;

public class ADArrayPlugin implements ADDetectorPlugin {
	
	final private NDArray ndArray;

	private boolean enabled = false;

	private boolean firstReadoutInScan = true;

	public ADArrayPlugin(NDArray ndArray) {
		this.ndArray = ndArray;
	}

	@Override
	public String getName() {
		return "array";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection) throws Exception {
		if (isEnabled()) {
			ndArray.getPluginBase().enableCallbacks();
			ndArray.getPluginBase().setBlockingCallbacks((short) 1);
		} else {
			ndArray.getPluginBase().disableCallbacks();
			ndArray.getPluginBase().setBlockingCallbacks((short) 0);
		}
		firstReadoutInScan = true;
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public List<String> getInputStreamExtraNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}
	
	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		if (isEnabled()) {
			appenders.add(new NXDetectorDataArrayAppender(ndArray, firstReadoutInScan));
		} else {
			appenders.add(new NXDetectorDataNullAppender());
		}
		firstReadoutInScan = false;
		// disabled
		return appenders;
	}

	public boolean isEnabled() {
		return enabled;
	}
	

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public NDArray getNdArray() {
		return ndArray;
	}
}
class NXDetectorDataArrayAppender implements NXDetectorDataAppender {

	private boolean firstReadoutInScan;
	private NDArray ndArray;

	NXDetectorDataArrayAppender(NDArray ndArray, boolean firstReadoutInScan) {
		this.ndArray = ndArray;
		this.firstReadoutInScan = firstReadoutInScan;
		
	}
	
	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException{
		try {
			readoutArrayIntoNXDetectorData(data, ndArray, firstReadoutInScan, detectorName);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		
	}
	
}