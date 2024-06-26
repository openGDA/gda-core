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

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class GDANexusDeviceAdapterFactory implements INexusDeviceAdapterFactory<Scannable> {

	private static class NexusDetectorAdapterFactory implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device) {
			return device instanceof NexusDetector;
		}

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector) throws NexusException {
			if (LocalProperties.check(PROPERTY_NAME_USE_LEGACY_NEXUS_DETECTOR_ADAPTER)) {
				return new LegacyNexusDetectorNexusDevice((NexusDetector) detector);
			}
			return new NexusDetectorNexusDevice((NexusDetector) detector);
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

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector) throws NexusException {
			return new FileCreatorDetectorNexusDevice(detector);
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

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector) throws NexusException {
			return new GenericDetectorNexusDevice(detector);
		}

	}

	private static class ScannableAdapterFactory implements INexusDeviceAdapterFactory<Scannable> {

		@Override
		public boolean canAdapt(Object device) {
			return device instanceof Scannable && !(device instanceof Detector);
		}

		@Override
		public <N extends NXobject> INexusDevice<N> createNexusDevice(Scannable device) throws NexusException {
			final ScannableNexusDeviceConfiguration config = getScannableNexusDeviceConfiguration(device.getName());
			return config != null ? new ConfiguredScannableNexusDevice<>(device, config) :
				new DefaultScannableNexusDevice<>(device);
		}

		private ScannableNexusDeviceConfiguration getScannableNexusDeviceConfiguration(String deviceName) {
			final ScannableNexusDeviceConfigurationRegistry registry = ServiceProvider.getService(ScannableNexusDeviceConfigurationRegistry.class);
			return registry.getScannableNexusDeviceConfiguration(deviceName);
		}

	}

	public static final String PROPERTY_NAME_USE_LEGACY_NEXUS_DETECTOR_ADAPTER = "gda.nexus.nexusScanDataWriter.useLegacyNexusDetectorAdapter";

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

		// this line cannot be reached, the configured adapters support all Scannables
		throw new IllegalArgumentException("Cannot create an INexusDevice for scannable: " + device.getName());
	}

}
