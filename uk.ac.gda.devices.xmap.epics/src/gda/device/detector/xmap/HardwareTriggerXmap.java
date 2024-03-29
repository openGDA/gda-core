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

import java.util.*;

import gda.device.DeviceException;
import gda.device.detector.nxdata.*;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.scan.ScanInformation;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * Drive the XIA Xmap card using hardware triggers. Data is only available after
 * the scan has finished as it is written directly to an HDF5 file by the XMAP
 * card.
 * <p>
 * This plugin returns an index of each point instead.
 */
public class HardwareTriggerXmap extends XmapSimpleAcquire {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(HardwareTriggerXmap.class);
	protected Double index = 0.0;

	public HardwareTriggerXmap(EDXDMappingController xmap, double readoutTime) throws DeviceException {
		super(xmap, -1);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getXmap().setCollectionMode(COLLECTION_MODES.MCA_MAPPING);

		int[] scanDims = scanInfo.getDimensions();
		int totalFrames = scanDims[0] * scanDims[1];
		getXmap().setPixelsPerRun(totalFrames);
		index = 0.0;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

	@Override
	public List<String> getInputStreamNames() {
		ArrayList<String> names = new ArrayList<>();
		names.add("index");
		return names;
	}

	@Override
	public List<String> getInputStreamFormats() {
		ArrayList<String> names = new ArrayList<>();
		names.add("%d");
		return names;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		index++;
		ArrayList<Double> names = new ArrayList<>();
		names.add(index);
		Vector<NXDetectorDataAppender> vector = new Vector<>();
		vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), names));
		return vector;
	}

	@Override
	@Deprecated(since="GDA 8.26")
	public void configureAcquireAndPeriodTimes(double collectionTime)
			throws Exception {
		logger.deprecatedMethod("configureAcquireAndPeriodTimes(double)");
	}
}
