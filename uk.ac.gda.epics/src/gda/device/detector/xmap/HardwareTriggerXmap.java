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
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.epics.CAClient;
import gda.scan.ScanInformation;

/**
 *
 */
public class HardwareTriggerXmap extends XmapSimpleAcquire {

	/**
	 * @param xmap
	 * @param xbuf
	 * @param readoutTime
	 * @throws DeviceException
	 */
	public HardwareTriggerXmap(EDXDMappingController xmap, XBufferEPICsPlugin xbuf, double readoutTime)
			throws DeviceException {
		super(xmap, xbuf, -1);
	}
	
	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		//configureTriggerMode();
		xbufEnableCallbacks();
		getXmap().setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
		
		int [] scanDims = scanInfo.getDimensions();
		int totalFrames = scanDims[0] * scanDims[1];
//		CAClient ca = new CAClient();
//		ca.caput("BL08I-EA-DET-02:PixelsPerRun",totalFrames);
//		ca.caput("BL08I-EA-DET-02:HDF:ExtraDimSizeN",scanDims[0]);
//		ca.caput("BL08I-EA-DET-02:HDF:ExtraDimSizeX",scanDims[1]);
//		ca.caput("BL08I-EA-DET-02:HDF:FileNumber",scanInfo.getScanNumber());
//		ca.caput("BL08I-EA-DET-02:HDF:Capture",1);
//		ca.caput("BL08I-EA-DET-02:EraseStart",1);
		getXmap().setPixelsPerRun(totalFrames);
		index = new Double(0.0);
		//getAdBase().setNumImages(numImages);
		//enableOrDisableCallbacks();
	}
	
	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}
}
