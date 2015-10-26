/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.xmap.controller.DummyXmapAcquisitionBaseEpicsLayer;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class DummyXmapHardwareTriggeredCollectionStrategy extends DummyXmapSimpleAcquire {


	public DummyXmapHardwareTriggeredCollectionStrategy(DummyXmapAcquisitionBaseEpicsLayer xmap) throws Exception {
		super(xmap, -1);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {

	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList("count_time");
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();
		formats.add("%.2f");
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		List<Double> times = new ArrayList<Double>();
		times.add(0.0);
		Vector<NXDetectorDataAppender> vector = new Vector<NXDetectorDataAppender>();
		vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), times));
		return vector;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		// do nothing here
	}

}
