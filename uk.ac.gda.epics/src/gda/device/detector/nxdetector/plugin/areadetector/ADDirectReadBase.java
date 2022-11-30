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

import java.util.Arrays;
import java.util.List;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.GetPluginBaseAvailable;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NonAsynchronousNXPlugin;
import gda.scan.ScanInformation;

public abstract class ADDirectReadBase implements NonAsynchronousNXPlugin {

	private final GetPluginBaseAvailable ndArray;

	private boolean enableDuringScan = true;

	private boolean writeDataToFile=true;

	private boolean firstReadoutInScan = true;

	private boolean alreadyPrepared=false;

	private String ndArrayPortVal;

	private boolean blocking=true;

	protected ADDirectReadBase(GetPluginBaseAvailable ndArray) {
		this.ndArray = ndArray;
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
		getPlugin().getPluginBase().disableCallbacks();
		getPlugin().getPluginBase().setBlockingCallbacks(isBlocking() ? 1:0); //use camera memory
		resetCounters();
		getPlugin().getPluginBase().enableCallbacks();
		firstReadoutInScan = true;
		alreadyPrepared=true;
	}
	protected void setNDArrayPortAndAddress() throws Exception {
		if( getNdArrayPortVal() != null && getNdArrayPortVal().length()>0)
			getPlugin().getPluginBase().setNDArrayPort(getNdArrayPortVal());
	}
	private void resetCounters() throws Exception {
		getPlugin().getPluginBase().setDroppedArrays(0);
		getPlugin().getPluginBase().setArrayCounter(0);
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
				return new NXDetectorDataArrayAppender(getData(), firstReadoutInScan);
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

	public GetPluginBaseAvailable getPlugin() {
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

	/**
	 * Read/get the actual data using the plugin
	 * @throws Exception
	 */
	protected abstract NexusGroupData getData() throws Exception;

}
