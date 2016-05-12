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

import gda.data.nexus.extractor.NexusGroupData;
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

	private boolean enableDuringScan = true;

	private boolean writeDataToFile=true;

	private boolean firstReadoutInScan = true;

	private boolean alreadyPrepared=false;

	private String ndArrayPortVal;

	private boolean blocking=true;

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
		if(!isEnabled())
			return;
		if( alreadyPrepared)
			return;
		setNDArrayPortAndAddress();
		getNdArray().getPluginBase().disableCallbacks();
		getNdArray().getPluginBase().setBlockingCallbacks(isBlocking() ? 1:0); //use camera memory
		resetCounters();
		getNdArray().getPluginBase().enableCallbacks();
		firstReadoutInScan = true;
		alreadyPrepared=true;
	}
	protected void setNDArrayPortAndAddress() throws Exception {
		if( getNdArrayPortVal() != null && getNdArrayPortVal().length()>0)
			getNdArray().getPluginBase().setNDArrayPort(getNdArrayPortVal());
	}
	private void resetCounters() throws Exception {
		getNdArray().getPluginBase().setDroppedArrays(0);
		getNdArray().getPluginBase().setArrayCounter(0);
	}
	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
		alreadyPrepared=false;
		if(!isEnabled())
			return;
	}

	@Override
	public void atCommandFailure() throws Exception {
		stop();
	}

	@Override
	public void stop() throws Exception {
		alreadyPrepared=false;
		if(!isEnabled())
			return;
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

		if (isEnabled() && isWriteDataToFile()) {
			try {
				return new NXDetectorDataArrayAppender(ArrayData.readArrayData(ndArray), firstReadoutInScan);
			} catch (Exception e) {
				throw new DeviceException(getName() + "Error reading array data",e);
			}
		}
		return new NXDetectorDataNullAppender();
	}

	public boolean isEnabled() {
		return enableDuringScan;
	}

	public void setEnabled(boolean enabled) {
		this.enableDuringScan = enabled;
	}

	public NDArray getNdArray() {
		return ndArray;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public boolean isWriteDataToFile() {
		return writeDataToFile;
	}

	public void setWriteDataToFile(boolean writeDataToFile) {
		this.writeDataToFile = writeDataToFile;
	}

	public String getNdArrayPortVal() {
		return ndArrayPortVal;
	}

	public void setNdArrayPortVal(String ndArrayPortVal) {
		this.ndArrayPortVal = ndArrayPortVal;
	}

}
class NXDetectorDataArrayAppender implements NXDetectorDataAppender {
	private boolean firstReadoutInScan=true;
	private NexusGroupData arrayData;

	NXDetectorDataArrayAppender(NexusGroupData arrayData2, boolean firstReadoutInScan)  {
		arrayData = arrayData2;
		this.firstReadoutInScan = firstReadoutInScan;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException{
		try {
			arrayData.isDetectorEntryData = true;
			data.addData(detectorName, "data", arrayData, null, 1);
			if (firstReadoutInScan) {
				// TODO add sensible axes
				firstReadoutInScan=false;
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
}