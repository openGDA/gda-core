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

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;

public class GDANexusDeviceAdapterFactory implements INexusDeviceAdapterFactory<Scannable> {

	private static final GDANexusDeviceAdapterFactory INSTANCE = new GDANexusDeviceAdapterFactory();

	private GDANexusDeviceAdapterFactory() {
		// private constructor to prevent instantiation
	}

	public static GDANexusDeviceAdapterFactory getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean canAdapt(Scannable device) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends NXobject> INexusDevice<N> createNexusDevice(Scannable device) throws NexusException {
		if (device instanceof INexusDevice<?>) {
			return (INexusDevice<N>) device;
		}

		if (device instanceof Detector) {
			return (INexusDevice<N>) createNexusDevice((Detector) device);
		}

		final ScannableNexusDevice<N> nexusWrapper = new ScannableNexusDevice<>(device);
		nexusWrapper.setWriteDemandValue(false);
		return nexusWrapper;
	}

	private INexusDevice<NXdetector> createNexusDevice(Detector detector) throws NexusException {
		if (detector instanceof NexusDetector) {
			// TODO DAQ-3177: implement support for NexusDetector, see NexusDataWriter.makeNexusDetectorGroups
			throw new UnsupportedOperationException("Detectors of type NexusDetector are not yet supported:" + detector.getName());
		} else if (canCreateOwnFiles(detector)) {
			// TODO DAQ-3178: implement support for file creator detector, see NexusDataWriter.makeFileCreatorDetector
			throw new UnsupportedOperationException("Detectors that create their own files are not yet supported:" + detector.getName());
		} else if (detector.getExtraNames().length > 0) {
			return new CounterTimerNexusDevice(detector);
		} else {
			// TODO DAQ-3179: implement support for generic detector, see NexusDataWriter.makeGenericDetector
			throw new UnsupportedOperationException("Generic detectors are not yet supported:" + detector.getName());
		}
	}

	private boolean canCreateOwnFiles(Detector detector) throws NexusException {
		try {
			return detector.createsOwnFiles();
		} catch (DeviceException e) {
			throw new NexusException(e);
		}
	}

}
