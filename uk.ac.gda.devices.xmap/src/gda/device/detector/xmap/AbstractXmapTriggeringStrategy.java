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

import gda.device.detector.addetector.triggering.CollectionStrategyBeanInterface;
import gda.device.detector.xmap.XBufferEPICsPlugin.EnableCallbacks;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.scan.ScanInformation;

public abstract class AbstractXmapTriggeringStrategy implements CollectionStrategyBeanInterface {

	private final EDXDMappingController xmap;
	private final XBufferEPICsPlugin xbuf;

	public AbstractXmapTriggeringStrategy(EDXDMappingController xmap, XBufferEPICsPlugin xbuf) {
		this.xmap = xmap;
		this.xbuf = xbuf;
	}

	public EDXDMappingController getXmap() {
		return xmap;
	}

	public XBufferEPICsPlugin getXbuf() {
		return xbuf;
	}

	@Override
	public double getAcquireTime() throws Exception {
		return xmap.getAcquisitionTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return 0;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {

	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		xbufEnableCallbacks();
	}

	public void xbufEnableCallbacks() throws Exception {
		xbuf.setEnableCallbacks_RBV(EnableCallbacks.ENABLE.ordinal());
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return false;
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
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		throw new UnsupportedOperationException(
				"Must be operated via prepareForCollection(collectionTime, numberImagesPerCollection)");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (xmap == null)
			throw new RuntimeException("xmap is not set");
	}

}
