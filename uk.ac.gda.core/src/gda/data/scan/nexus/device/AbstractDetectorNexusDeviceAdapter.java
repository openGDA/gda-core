/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.data.scan.nexus.device;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.INeXusInfoWriteable;
import gda.device.Detector;
import gda.device.DeviceException;

public abstract class AbstractDetectorNexusDeviceAdapter extends AbstractNexusDeviceAdapter<NXdetector> {

	protected static final String FIELD_NAME_ID = "id";

	private static final Logger logger = LoggerFactory.getLogger(AbstractDetectorNexusDeviceAdapter.class);

	protected Object firstPointData;

	protected AbstractDetectorNexusDeviceAdapter(Detector detector) {
		super(detector);
	}

	public void setFirstPointData(Object detectorData) {
		firstPointData = detectorData;
	}

	protected Detector getDetector() {
		return (Detector) super.getDevice();
	}

	@Override
	protected NXdetector createNexusObject(NexusScanInfo info) throws NexusException {
		final NXdetector detGroup = NexusNodeFactory.createNXdetector();

		final Detector detector = getDetector();
		try {
			writeMetaDataFields(detGroup, detector);
		} catch (DeviceException e) {
			throw new NexusException("Error reading device metadata", e);
		}

		// Note: unlike NexusDataWriter, we do not support INeXusInfoWriteable
		// this seems to be little used in practise
		if (detector instanceof INeXusInfoWriteable) {
			logger.warn("INeXusInfoWriteable is not supported by data writer {}", getClass().getSimpleName());
		}

		writeDataFields(info, detGroup);

		// add fields for attributes, e.g. name, description. This allows custom metadata to be added
		registerAttributes(detGroup);

		return detGroup;
	}

	protected void writeMetaDataFields(final NXdetector detGroup, final Detector detector) throws DeviceException {
		if (StringUtils.isNotEmpty(detector.getDescription())) {
			detGroup.setDescriptionScalar(detector.getDescription());
		}
		if (StringUtils.isNotEmpty(detector.getDetectorType())) {
			detGroup.setTypeScalar(detector.getDetectorType());
		}
		if (StringUtils.isNotEmpty(detector.getDetectorID())) {
			detGroup.setDataset(FIELD_NAME_ID, DatasetFactory.createFromObject(detector.getDetectorID()));
		}
		// TODO DAQ-8203 add any metadata added by appenders. See NexusDataWriter.addDeviceMetadata
	}

	protected abstract void writeDataFields(NexusScanInfo info, NXdetector detGroup) throws NexusException;

	@Override
	public void scanEnd() throws NexusException {
		nexusObject = null;
	}

}
