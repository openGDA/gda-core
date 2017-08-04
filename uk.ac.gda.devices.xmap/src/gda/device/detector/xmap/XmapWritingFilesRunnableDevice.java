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

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.xmap.api.XmapRunnableDeviceModel;
import gda.device.detector.xmap.api.XmapWritingFilesRunnableDeviceModel;
import uk.ac.diamond.daq.detectors.addetector.AreaDetectorWritingFilesRunnableDevice;
import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;

/**
 * <p>
 * GDA 9 detector for XMAP
 * </p>
 * This implementation uses the XMAP controller to control the detector itself, but Area Detector to write the data.<br>
 * See XmapRunnableDevice for an implementation that uses GDA to write the file.
 *
 * @author Anthony Hull
 */
public class XmapWritingFilesRunnableDevice extends XmapRunnableDeviceBase {

	private static final Logger logger = LoggerFactory.getLogger(XmapWritingFilesRunnableDevice.class);

	private AreaDetectorWritingFilesRunnableDevice areaDetectorRunnableDevice = new AreaDetectorWritingFilesRunnableDevice();

	@PreConfigure
	public void preConfigure(ScanInformation info) {
		areaDetectorRunnableDevice.preConfigure(info);
	}

	@Override
	public void configure(XmapRunnableDeviceModel model) throws ScanningException {
		if (!(model instanceof XmapWritingFilesRunnableDeviceModel)) {
			final String message = "Xmap detector must be configured with an XmapWritingFilesRunnableDeviceModel";
			logger.error(message);
			throw new ScanningException(message);
		}

		setDeviceState(DeviceState.CONFIGURING);

		final XmapWritingFilesRunnableDeviceModel xmapWFModel = (XmapWritingFilesRunnableDeviceModel) model;

		final XmapRunnableDeviceModel xmapModel = new XmapRunnableDeviceModel();
		xmapModel.setName(xmapWFModel.getXmapDetectorName());
		xmapModel.setExposureTime(xmapWFModel.getExposureTime());
		xmapModel.setTimeout(xmapWFModel.getTimeout());
		configureXmapDetector(xmapModel);

		configureAreaDetector(xmapWFModel);

		setDeviceState(DeviceState.ARMED);
	}

	private void configureAreaDetector(XmapWritingFilesRunnableDeviceModel xmapModel) throws ScanningException {
		final AreaDetectorRunnableDeviceModel adModel = new AreaDetectorRunnableDeviceModel();
		adModel.setName(xmapModel.getAreaDetectorName());
		adModel.setExposureTime(xmapModel.getExposureTime());
		adModel.setTimeout(xmapModel.getTimeout());

		areaDetectorRunnableDevice.setName(getName());
		areaDetectorRunnableDevice.setBean(getBean());
		areaDetectorRunnableDevice.configure(adModel);
	}

	@PostConfigure
	public void postConfigure() {
		areaDetectorRunnableDevice.postConfigure();
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo scanInfo) throws NexusException {
		return areaDetectorRunnableDevice.getNexusProvider(scanInfo);
	}

	@Override
	public boolean write(IPosition position) throws ScanningException {
		// Doesn't need to do anything the area detector is writing the file which is already linked.
		return true;
	}

}
