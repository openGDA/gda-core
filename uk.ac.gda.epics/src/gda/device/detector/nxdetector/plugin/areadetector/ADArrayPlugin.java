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
import gda.device.detector.addetector.ArrayData;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NonAsynchronousNXPlugin;
import gda.scan.ScanInformation;

import java.util.Arrays;
import java.util.List;

public class ADArrayPlugin implements NonAsynchronousNXPlugin {
	
	final private NDArray ndArray;

	private boolean enabled = true;

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
		return isEnabled();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
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
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}
	
	@Override
	public NXDetectorDataAppender read() throws DeviceException {
		firstReadoutInScan = false;
		if (!isEnabled()) {
			return new NXDetectorDataNullAppender();
		}
		try {
			return new NXDetectorDataArrayAppender(ArrayData.readArrayData(ndArray), firstReadoutInScan);
		} catch (Exception e) {
			throw new DeviceException(getName() + "Error reading array data",e);
		}
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
	private static String ARRAY_DATA_NAME = "arrayData";
	private boolean firstReadoutInScan=true;
	private ArrayData arrayData;

	NXDetectorDataArrayAppender(ArrayData arrayData2, boolean firstReadoutInScan)  {
		arrayData = arrayData2;
		this.firstReadoutInScan = firstReadoutInScan;
	}
	
	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException{
		try {
			data.addData(detectorName, "data", arrayData.getDims(), arrayData.getNexusType(), arrayData.getDataVals(), ARRAY_DATA_NAME, 1);
			if (firstReadoutInScan) {
				// TODO add sensible axes
				firstReadoutInScan=false;
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
}