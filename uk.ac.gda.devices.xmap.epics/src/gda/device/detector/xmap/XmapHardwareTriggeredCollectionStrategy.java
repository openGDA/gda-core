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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDController.PIXEL_ADVANCE_MODE;
import gda.device.detector.xmap.edxd.EDXDController.PRESET_TYPES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.scan.ScanInformation;

/**
 * Drive the XIA Xmap card using hardware triggers in Constant Velocity scans.
 */
public class XmapHardwareTriggeredCollectionStrategy extends XmapSimpleAcquire {


	private int pixelsPerBuffer = 124; // will always be this by default when auto pixels per buffer


	public XmapHardwareTriggeredCollectionStrategy(EDXDMappingController xmap) throws DeviceException {
		super(xmap, -1);
		//this.ndHDF5PVProvider = nDHDF5PVProvider;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getXmap().setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
		getXmap().setPresetType(PRESET_TYPES.NO_PRESET);
		getXmap().setPixelAdvanceMode(PIXEL_ADVANCE_MODE.GATE);
		getXmap().setIgnoreGate(false);
		getXmap().setPixelsPerRun(numImages);
		getXmap().setAutoPixelsPerBuffer(false);

	}


	@Override
	public void prepareForLine() throws Exception {
//		getXmap().start(); // restart collection at every line for this class
	}

	@Override
	public void collectData() throws Exception {
		getXmap().start();
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
		List<String> fieldNames = new ArrayList<String>();

			fieldNames.add("count_time");

			return fieldNames;

		//return new ArrayList<String>();
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();

			formats.add("%.2f");

		return formats;
		//return new ArrayList<String>();
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {
		/*ArrayList<NXDetectorDataAppender> output = new ArrayList<NXDetectorDataAppender>();
		//for (int i = 0; i < totalNumberImages; i++){
			output.add(new NXDetectorDataNullAppender()) ;
		//}
		return output;*/
		List<Double> times = new ArrayList<Double>();

		try {
				times.add(0.0);
		} catch (Exception e) {
				throw new DeviceException(e);
		}

		Vector<NXDetectorDataAppender> vector = new Vector<NXDetectorDataAppender>();
		vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), times));
		return vector;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime)
			throws Exception {
		// do nothing here
	}


}
