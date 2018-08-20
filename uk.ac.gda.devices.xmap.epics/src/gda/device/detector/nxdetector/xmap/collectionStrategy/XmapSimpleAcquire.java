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

import gda.device.DeviceException;
import gda.device.detector.nxdetector.xmap.controller.XmapAcquisitionBaseEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.CollectionModeEnum;
import gda.scan.ScanInformation;

/**
 * Drive the XIA Xmap card using its own internal clock. Data is only available
 * after the scan has finished as it is written directly to an HDF5 file by the
 * XMAP card.
 * <p>
 * This plugin could be used as a base for a software triggered strategy, but as
 * this is not a full implementation it is abstract.
 */
public abstract class XmapSimpleAcquire implements XmapCollectionStrategyBeanInterface {

	private XmapAcquisitionBaseEpicsLayer xmap;

	public XmapSimpleAcquire(XmapAcquisitionBaseEpicsLayer xmap, double readoutTime) throws Exception {
		this.xmap = xmap;
		xmap.setAquisitionTime(readoutTime);

	}

	@Override
	public XmapAcquisitionBaseEpicsLayer getXmap() {
		return xmap;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages,
			ScanInformation scanInfo) throws Exception {
		if (numImages != 1) {
			throw new IllegalArgumentException(
					"This single exposure triggering strategy expects to expose only 1 spectra");
		}
		xmap.setCollectMode(CollectionModeEnum.MCA_SPECTRA);

	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection,
			ScanInformation scanInfo) throws Exception {
		throw new UnsupportedOperationException(
				"Must be operated via prepareForCollection(collectionTime, numberImagesPerCollection)");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( xmap == null)
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
	public int getStatus() throws Exception{
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
	public int getNumberImagesPerCollection(double collectionTime)
			throws Exception {
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
}
