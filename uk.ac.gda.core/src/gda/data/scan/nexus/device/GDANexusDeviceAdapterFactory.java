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
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.device.INexusDeviceAdapterFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class GDANexusDeviceAdapterFactory implements INexusDeviceAdapterFactory<Scannable> {

	private static void checkIfDeviceCanAdopt(INexusDeviceAdapterFactory<?> adaopter, Scannable scannable, ScanRole scanRole) {
		if (!adaopter.canAdapt(scannable, scanRole)) {
			throw new IllegalArgumentException("Cannot createNexusDevice for " + adaopter.getClass().getSimpleName() + " using scannable: " + scannable.getName() + " with scan role " + scanRole);
		}
	}

	private static class NexusDetectorAdapter implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device, ScanRole scanRole) {
			return device instanceof NexusDetector && scanRole != ScanRole.MONITOR_PER_SCAN;
		}

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector, ScanRole scanRole) throws NexusException {
			checkIfDeviceCanAdopt(this, detector, scanRole);
			if (LocalProperties.check(PROPERTY_NAME_USE_LEGACY_NEXUS_DETECTOR_ADAPTER)) {
				return new LegacyNexusDetectorNexusDevice((NexusDetector) detector);
			}
			return new NexusDetectorNexusDevice((NexusDetector) detector);
		}
	}

	private static class DetectorCreatesOwnFilesAdapter implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device, ScanRole scanRole) {
			try {
				return device instanceof Detector detector && detector.createsOwnFiles() && scanRole != ScanRole.MONITOR_PER_SCAN;
			} catch (DeviceException e) {
				throw new RuntimeException("Error calling createsOwnFiles on detector: " + ((Scannable) device).getName());
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector, ScanRole scanRole) throws NexusException {
			checkIfDeviceCanAdopt(this, detector, scanRole);
			return new FileCreatorDetectorNexusDevice(detector);
		}
	}

	private static class CounterTimerAdapter implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device, ScanRole scanRole) {
			return device instanceof Detector detector &&
				detector.getExtraNames() != null &&
				detector.getExtraNames().length > 0 &&
				scanRole != ScanRole.MONITOR_PER_SCAN;
		}

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector, ScanRole scanRole) throws NexusException {
			checkIfDeviceCanAdopt(this, detector, scanRole);
			return new CounterTimerNexusDevice(detector);
		}
	}

	private static class GenericDetectorAdapter implements INexusDeviceAdapterFactory<Detector> {

		@Override
		public boolean canAdapt(Object device, ScanRole scanRole) {
			return device instanceof Detector && scanRole != ScanRole.MONITOR_PER_SCAN;
		}

		@SuppressWarnings("unchecked")
		@Override
		public INexusDevice<NXdetector> createNexusDevice(Detector detector, ScanRole scanRole) throws NexusException {
			checkIfDeviceCanAdopt(this, detector, scanRole);
			return new GenericDetectorNexusDevice(detector);
		}
	}

	private static class ScannableAdapter implements INexusDeviceAdapterFactory<Scannable> {

		@Override
		public boolean canAdapt(Object device, ScanRole scanRole) {
			return device instanceof Scannable;
		}

		@Override
		public <N extends NXobject> INexusDevice<N> createNexusDevice(Scannable scannable, ScanRole scanRole) throws NexusException {
			checkIfDeviceCanAdopt(this, scannable, scanRole);
			final ScannableNexusDeviceConfiguration config = getScannableNexusDeviceConfiguration(scannable.getName());
			return config != null ? new ConfiguredScannableNexusDevice<>(scannable, config) :
				new DefaultScannableNexusDevice<>(scannable);
		}

		private ScannableNexusDeviceConfiguration getScannableNexusDeviceConfiguration(String deviceName) {
			final ScannableNexusDeviceConfigurationRegistry registry = ServiceProvider.getService(ScannableNexusDeviceConfigurationRegistry.class);
			return registry.getScannableNexusDeviceConfiguration(deviceName);
		}
	}

	public static final String PROPERTY_NAME_USE_LEGACY_NEXUS_DETECTOR_ADAPTER = "gda.nexus.nexusScanDataWriter.useLegacyNexusDetectorAdapter";

	private final List<INexusDeviceAdapterFactory<? extends Scannable>> adapters;

	public GDANexusDeviceAdapterFactory() {
		// private constructor to prevent external instantiation

		// create the list of adapters.
		// The way this is used is very similar to the Chain of Responsibility
		// design pattern, except that we are using a list rather than each element referring to the next.
		// Note: the order of the elements is very important, as each are tried in turn. This emulates the
		// logic in NexusDataWriter.makeDetectorEntry
		adapters = new ArrayList<>();
		adapters.add(new NexusDetectorAdapter());
		adapters.add(new DetectorCreatesOwnFilesAdapter());
		adapters.add(new CounterTimerAdapter());
		adapters.add(new GenericDetectorAdapter());
		adapters.add(new ScannableAdapter());
	}

	@Override
	public boolean canAdapt(Object device, ScanRole scanRole) {
		return device instanceof Scannable || (device instanceof Scannable && device instanceof INexusDevice<?>);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <N extends NXobject> INexusDevice<N> createNexusDevice(Scannable device, ScanRole scanRole) throws NexusException {
		if (device instanceof INexusDevice<?>) {
			return (INexusDevice<N>) device;
		}
		if (scanRole == null) throw new IllegalArgumentException("scanRole cannot be null!");

		for (INexusDeviceAdapterFactory<? extends Scannable> adapter : adapters) {
			if (adapter.canAdapt(device, scanRole)) {
				return ((INexusDeviceAdapterFactory<Scannable>)adapter).createNexusDevice(device, scanRole);
			}
		}
		// this line cannot be reached, the configured adapters support all Scannables
		throw new IllegalArgumentException("Cannot create an INexusDevice for scannable: " + device.getName());
	}
}
