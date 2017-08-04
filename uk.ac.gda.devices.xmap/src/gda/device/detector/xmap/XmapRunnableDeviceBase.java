/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.xmap.api.XmapRunnableDeviceModel;
import gda.factory.Finder;

public abstract class XmapRunnableDeviceBase extends AbstractRunnableDevice<XmapRunnableDeviceModel>
		implements IWritableDetector<XmapRunnableDeviceModel>, INexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(XmapRunnableDeviceBase.class);

	protected NexusXmap xmapDetector;
	protected String xmapDetectorName;

	public XmapRunnableDeviceBase() {
		super(ServiceHolder.getRunnableDeviceService());
	}

	protected void configureXmapDetector(XmapRunnableDeviceModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		// Get the detector named in the model
		xmapDetectorName = model.getName();
		xmapDetector = Finder.getInstance().find(xmapDetectorName);
		if (xmapDetector == null) {
			final String message = "Could not find XMAP detector: " + xmapDetectorName;
			logger.error(message);
			throw new ScanningException(message);
		}

		// Configure detector
		try {
			xmapDetector.setAcquisitionTime(model.getExposureTime());
			xmapDetector.configure();
			xmapDetector.atScanStart();
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			final String message = "Configuring controller failed";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}

		super.configure(model);
	}

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		setDeviceState(DeviceState.RUNNING);
		try {
			xmapDetector.clearAndStart();
			xmapDetector.waitWhileBusy();
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			final String message = "Acquiring from detector failed";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}
		setDeviceState(DeviceState.ARMED);
	}
}