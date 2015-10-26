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
import gda.device.detector.nxdetector.CollectionStrategyBeanInterface;
import gda.device.detector.nxdetector.xmap.controller.DummyXmapAcquisitionBaseEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.DummyXmapAcquisitionBaseEpicsLayer.CollectionModeEnum;
import gda.scan.ScanInformation;

import java.util.List;
import java.util.NoSuchElementException;

public class DummyXmapSimpleAcquire implements CollectionStrategyBeanInterface {
	private DummyXmapAcquisitionBaseEpicsLayer xmap;

	public DummyXmapSimpleAcquire(DummyXmapAcquisitionBaseEpicsLayer xmap, double readoutTime) throws Exception {
		this.xmap = xmap;
		xmap.setAquisitionTime(readoutTime);

	}

	public DummyXmapAcquisitionBaseEpicsLayer getXmap() {
		return xmap;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		if (numImages != 1) {
			throw new IllegalArgumentException("This single exposure triggering strategy expects to expose only 1 spectrum");
		}
		xmap.setCollectMode(CollectionModeEnum.MCA_SPECTRA);

	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		throw new UnsupportedOperationException("Must be operated via prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo)");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (xmap == null)
			throw new RuntimeException("xmap is not set");
	}

	@Override
	public String getName() {
		return "controller";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public double getAcquireTime() throws Exception {
		return xmap.getAquisitionTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return 0;
	}

	@Override
	public void collectData() throws Exception {
		xmap.setStart();
	}

	@Override
	public int getStatus() throws Exception {
		return xmap.getStatus();
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
		xmap.setStop();
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
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return false;
	}

	@Override
	public List<String> getInputStreamNames() {
		return null;
	}

	@Override
	public List<String> getInputStreamFormats() {
		return null;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		return null;
	}

}
