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
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class XmapSimpleAcquire extends AbstractXmapTriggeringStrategy {

	protected Double index = new Double(0.0);

	public XmapSimpleAcquire(EDXDMappingController xmap, XBufferEPICsPlugin xbuf, double readoutTime)
			throws DeviceException {
		super(xmap, xbuf);
		getXmap().setAquisitionTime(readoutTime);

	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, numImages, scanInfo);
		if (numImages != 1) {
			throw new IllegalArgumentException(
					"This single exposure triggering strategy expects to expose only 1 spectra");
		}
		getXmap().setPixelsPerRun(numImages);
		getXmap().setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
		index = new Double(0.0);
	}

	@Override
	public double getAcquireTime() throws Exception {
		return getXmap().getAcquisitionTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return 0;
	}

	@Override
	public void collectData() throws Exception {
		getXmap().start();
	}

	@Override
	public int getStatus() throws DeviceException {
		return getXmap().getStatus();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
	}

	@Override
	public void stop() throws Exception {
		completeCollection();
	}

	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();
	}

	@Override
	public void completeCollection() throws Exception {
		getXmap().stop();
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return 1;
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
	}

	@Override
	public boolean isGenerateCallbacks() {
		return false;
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public List<String> getInputStreamNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.add("index");
		return names;
	}

	@Override
	public List<String> getInputStreamFormats() {
		ArrayList<String> names = new ArrayList<String>();
		names.add("%d");
		return names;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		// 	return new ArrayList<NXDetectorDataAppender>();
		index++;
		ArrayList<Double> names = new ArrayList<Double>();
		names.add(index);
		Vector<NXDetectorDataAppender> vector = new Vector<NXDetectorDataAppender>();
		vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), names));
		return vector;
	}
}
