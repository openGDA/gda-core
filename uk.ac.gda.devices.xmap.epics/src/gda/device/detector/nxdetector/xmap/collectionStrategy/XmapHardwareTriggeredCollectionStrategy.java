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

package gda.device.detector.nxdetector.xmap.collectionStrategy;

import java.util.*;

import gda.device.DeviceException;
import gda.device.detector.nxdata.*;
import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.*;
import gda.scan.ScanInformation;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * Drive the XIA Xmap card using hardware triggers in Constant Velocity scans.
 */
public class XmapHardwareTriggeredCollectionStrategy extends XmapSimpleAcquire {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(XmapHardwareTriggeredCollectionStrategy.class);
	private int pixelsPerBuffer = 124; // will always be this by default when auto pixels per buffer


	public XmapHardwareTriggeredCollectionStrategy(XmapAcquisitionBaseEpicsLayer xmap) throws Exception {
		super(xmap, -1);
		//this.ndHDF5PVProvider = nDHDF5PVProvider;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getXmap().setCollectMode(CollectionModeEnum.MCA_MAPPING);
		getXmap().setPresetMode(PresetMode.NO_PRESET);
		if (getXmap().isXmapMappingModeInstance("prepareForCollection in Hardware triggered Collection Strategy")){
			getXmap().getXmapMapping().setPixelAdvanceMode(PixelAdvanceMode.Gate);
			getXmap().getXmapMapping().setIgnoreGate(false);
			getXmap().getXmapMapping().setPixelsPerRun(numImages);
			getXmap().getXmapMapping().setAutoPixelsPerBuffer(false);
		}
	}


	@Override
	public void prepareForLine() throws Exception {
//		getXmap().start(); // restart collection at every line for this class
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

	@Override
	public List<String> getInputStreamNames() {
		List<String> fieldNames = new ArrayList<>();

			fieldNames.add("count_time");

			return fieldNames;

		//return new ArrayList<String>();
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<>();

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
		List<Double> times = new ArrayList<>();

		try {
				times.add(0.0);
		} catch (Exception e) {
				throw new DeviceException(e);
		}

		Vector<NXDetectorDataAppender> vector = new Vector<>();
		vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), times));
		return vector;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime)
			throws Exception {
		logger.deprecatedMethod("configureAcquireAndPeriodTimes(double)");
	}


}
