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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.device.INexusDeviceAdapterFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;

public class GDANexusDeviceAdapterFactory implements INexusDeviceAdapterFactory<Scannable> {

	private static class NexusDetectorAdapterFactory implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device) {
			return device instanceof NexusDetector;
		}

		@Override
		public <N extends NXobject> INexusDevice<N> createNexusDevice(Detector detector) throws NexusException {
			throw new UnsupportedOperationException("Detectors of type NexusDetector are not yet supported:" + detector.getName());
		}

	}

	private static class DetectorCreatesOwnFilesAdapterFactory implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device) {
			try {
				return device instanceof Detector && ((Detector) device).createsOwnFiles();
			} catch (DeviceException e) {
				throw new RuntimeException("Error calling createsOwnFiles on detector: " + ((Scannable) device).getName());
			}
		}

		@Override
		public <N extends NXobject> INexusDevice<N> createNexusDevice(Detector device) throws NexusException {
			// TODO DAQ-3178: implement support for file creator detector, see NexusDataWriter.makeFileCreatorDetector
			throw new UnsupportedOperationException("Detectors that create their own files are not yet supported:" + device.getName());
		}

	}

	private static class CounterTimerAdapterFactory implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device) {
			return device instanceof Detector &&
					((Detector) device).getExtraNames() != null &&
					((Detector) device).getExtraNames().length > 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector) throws NexusException {
			return new CounterTimerNexusDevice(detector);
		}

	}

	private static class GenericDetectorAdapterFactory implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device) {
			return device instanceof Detector;
		}

		@Override
		public <N extends NXobject> INexusDevice<N> createNexusDevice(Detector detector) throws NexusException {
			// TODO DAQ-3179: implement support for generic detector, see NexusDataWriter.makeGenericDetector
			throw new UnsupportedOperationException("Generic detectors are not yet supported:" + detector.getName());
		}

	}

	private static class ScannableAdapterFactory implements INexusDeviceAdapterFactory<Scannable> {

		@Override
		public boolean canAdapt(Object device) {
			return device instanceof Scannable && !(device instanceof Detector);
		}

		@Override
		public <N extends NXobject> INexusDevice<N> createNexusDevice(Scannable device) throws NexusException {
			final ScannableNexusDevice<N> nexusDevice = new ScannableNexusDevice<N>(device);
			nexusDevice.setWriteDemandValue(false);
			return nexusDevice;
		}

	}

	private final List<INexusDeviceAdapterFactory<? extends Scannable>> adapterFactories;

	public GDANexusDeviceAdapterFactory() {
		// private constructor to prevent external instantiation

		// create the list of adapter factories.
		// The way this is used is very similar to the Chain of Responsibility
		// design pattern, except that we are using a list rather than each element referring to the next.
		// Note: the order of the elements is very important, as each are tried in turn. This emulates the
		// logic in NexusDataWrite.makeDetectorEntry
		adapterFactories = new ArrayList<>();
		adapterFactories.add(new NexusDetectorAdapterFactory());
		adapterFactories.add(new DetectorCreatesOwnFilesAdapterFactory());
		adapterFactories.add(new CounterTimerAdapterFactory());
		adapterFactories.add(new GenericDetectorAdapterFactory());
		adapterFactories.add(new ScannableAdapterFactory());
	}

	@Override
	public boolean canAdapt(Object device) {
		return device instanceof Scannable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends NXobject> INexusDevice<N> createNexusDevice(Scannable device) throws NexusException {
		if (device instanceof INexusDevice<?>) {
			return (INexusDevice<N>) device;
		}
		for (INexusDeviceAdapterFactory<? extends Scannable> adapterFactory : adapterFactories) {
			if (adapterFactory.canAdapt(device)) {
				return ((INexusDeviceAdapterFactory<Scannable>)adapterFactory).createNexusDevice(device);
			}
		}

		final ScannableNexusDevice<N> nexusWrapper = new ScannableNexusDevice<>(device);
		nexusWrapper.setWriteDemandValue(false);
		return nexusWrapper;
	}

}
