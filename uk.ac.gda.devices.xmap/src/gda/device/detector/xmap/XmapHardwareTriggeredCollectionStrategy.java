/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDController.PRESET_TYPES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.detector.xmap.edxd.NDHDF5PVProvider;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Drive the XIA Xmap card using hardware triggers in Constant Velocity scans.
 */
public class XmapHardwareTriggeredCollectionStrategy extends XmapSimpleAcquire {

	private NDHDF5PVProvider ndHDF5PVProvider;
	private int totalNumberImages;

	public XmapHardwareTriggeredCollectionStrategy(EDXDMappingController xmap,
			NDHDF5PVProvider nDHDF5PVProvider) throws DeviceException {
		super(xmap, -1);
		this.ndHDF5PVProvider = nDHDF5PVProvider;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getXmap().setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
		getXmap().setPresetType(PRESET_TYPES.NO_PRESET);
		getXmap().setPixelsPerRun(scanInfo.getDimensions()[1]);
		getXmap().setAutoPixelsPerBuffer(true);
		int pixelsPerBuffer = 124; // will always be this when auto pixels per buffer
		int numberOfBuffers = (scanInfo.getDimensions()[1] / pixelsPerBuffer )+ 1;
		ndHDF5PVProvider.setNumberOfPixels(numberOfBuffers); // TODO rename this method as it is wrong and misleading
		ndHDF5PVProvider.setNumExtraDims(0);
		totalNumberImages = 1;
		for(int dimSize : scanInfo.getDimensions()) {
			totalNumberImages *= dimSize;
		}
	}
	
	
	@Override
	public void prepareForLine() throws Exception {
		getXmap().start(); // restart collection at every line for this class
	}
	
	@Override
	public void collectData() throws Exception {
//		getXmap().start();
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}
	
	@Override
	public void atCommandFailure() throws Exception {
		getXmap().stop();
	}

	@Override
	public List<String> getInputStreamNames() {
		return new ArrayList<String>();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return new ArrayList<String>();
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {
		ArrayList<NXDetectorDataAppender> output = new ArrayList<NXDetectorDataAppender>();
		for (int i = 0; i < totalNumberImages; i++){
			output.add(new NXDetectorDataNullAppender()) ;
		}
		return output;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime)
			throws Exception {
		// do nothing here		
	}
}
