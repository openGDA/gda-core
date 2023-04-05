/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;

import gda.data.ServiceHolder;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * Implementation of {@link IScannableDeviceService} for GDA8 devices.
 * {@link #getScannable(String)} using {@link Finder} to find {@link Findable}
 * @author Matthew Gerring, Matthew Dickie
 */
public class ScannableDeviceConnectorService implements IScannableDeviceService {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ScannableDeviceConnectorService.class);

	private static IScannableDeviceService instance;
	public static IScannableDeviceService getInstance() {
		return instance;
	}

	public ScannableDeviceConnectorService() {
		instance = this;
	}


	private Map<String, IScannable<?>> scannables = null;

	@Override
	public <T> void register(IScannable<T> scannable) {
		if (scannables == null)
			scannables = new HashMap<>();
		scannables.put(scannable.getName(), scannable);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		boolean jythonScannable = false;

		if (scannables == null)
			scannables = new HashMap<>();

		// first check whether an existing IScannable with this name in the cache, if so return it
		if (scannables.containsKey(name)) {
			IScannable<T> scannable = (IScannable<T>) scannables.get(name);
			if (scannable == null)
				throw new ScanningException("Cannot find scannable with name " + name);
			return scannable;
		}

		// if not, see if we can find a gda.device.Scannable with this name using the Finder mechanism
		Optional<Scannable> found = Finder.findOptionalOfType(name, Scannable.class);
		Scannable scannable = found.filter(s -> !(s instanceof Detector)).orElse(null);

		if (scannable == null) {
			// if not, see if we can find a gda.device.Scannable with this name in the jython namespace
			Object jythonObject = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name);
			if (jythonObject instanceof Scannable && !(jythonObject instanceof Detector)) {
				scannable = (Scannable) jythonObject;
				jythonScannable = true;
			}
		}

		// if we still haven't found the scannable, throw a ScanningException
		if (scannable == null) {
			throw new ScanningException("Cannot find scannable with name " + name);
		}

		// create a ScannableNexusWrapper wrapping the gda.device.Scannable
		IScannable<T> iscannable = createScannableWrapper(name, scannable, jythonScannable);

		// cache the IScannable for future use
		scannables.put(name, iscannable);

		return iscannable;
	}

	/**
	 * Create a new {@link ScannableNexusWrapper} implementing {@link IScannable} wrapping the legacy {@link Scannable}.
	 * @param name name of the scannable
	 * @param scannable the scannable to wrap
	 * @param jythonScannable <code>true</code> if the scannable is a jython scannable, <code>false</code> otherwise
	 * @return a {@link ScannableNexusWrapper} wrapping the {@link Scannable}
	 * @throws ScanningException
	 */
	@SuppressWarnings("unchecked")
	private <T> IScannable<T> createScannableWrapper(String name, Scannable scannable, boolean jythonScannable) {
		IScannable<T> iscannable = null;
		if (scannable instanceof IScannable) {
			/**
			 * They may provide into the finder a class which is
			 * both IScannable and Scannable, the two interfaces do not
			 * mind sitting over a GDA8 scannable, providing if it is marked
			 * as IScannable<Object>.
			 *
			 * It can then also be marked as INexusDevice in order to implement
			 * the nexus writing part of the scannable.
			 */
			iscannable = ((IScannable<T>) scannable);
		} else {
			/**
			 * This section deals with automatically wrapping a GDA8 Scannable
			 * into an object which is IScannable and INexusDevice. If this is
			 * done the INexusDevice writes as an NXPositioner in a default location.
			 */
			if (jythonScannable) {
				iscannable = (IScannable<T>) new JythonScannableNexusWrapper<>(name);
			} else {
				iscannable = (IScannable<T>) new ScannableNexusWrapper<>(scannable);
			}

			if (iscannable instanceof AbstractScannable) {
				((AbstractScannable<T>) iscannable).setScannableDeviceService(this);
			}
		}

		return iscannable;
	}

	@Override
	public List<String> getScannableNames() throws ScanningException {
		final Set<String> scannableNames = new HashSet<>();

		// add the names of findable scannables
		final List<Scannable> scannableRefs = Finder.listFindablesOfType(Scannable.class);
		for (Scannable scannable : scannableRefs) {
			if (!(scannable instanceof Detector)) { // exclude detectors
				String scannableName = scannable.getName();
				scannableName = scannableName.substring(scannableName.lastIndexOf('.') + 1);
				scannableNames.add(scannableName);
			}
		}

		// add the names of the jython scannables
		final Set<String> jythonScannableNames = Finder.findSingleton(JythonServer.class).getAllObjectsOfType(Scannable.class).entrySet().stream()
				.filter(entry -> !(entry.getValue() instanceof Detector)).map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		scannableNames.addAll(jythonScannableNames);

		if (scannables!=null) {
			scannableNames.addAll(scannables.keySet()); // They can be added by spring.
		}

		return new ArrayList<>(scannableNames);
	}

	@Override
	@Deprecated(since="GDA 9.3", forRemoval=true)
	public Set<String> getGlobalPerScanMonitorNames() {
		logger.deprecatedMethod("getGlobalPerScanMonitorNames()", null, "org.eclipse.scanning.api.device.IDefaultScanConfigurations");
		return ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables();
	}

	@Override
	@Deprecated(since="GDA 9.3", forRemoval=true)
	public Set<String> getRequiredPerScanMonitorNames(String scannableName) {
		logger.deprecatedMethod("getRequiredPerScanMonitorNames(String)", null, null);
		ScannableWriter writer = ServiceHolder.getNexusDataWriterConfiguration().getLocationMap().get(scannableName);
		if (writer != null) {
			Collection<String> requiredScannables = writer.getPrerequisiteScannableNames();
			if (requiredScannables != null) {
				return new HashSet<>(requiredScannables);
			}
		}

		return Collections.emptySet();
	}

	@Override
	public void handleDeviceError(String name, Exception e) {
		logger.warn("Failure getting device information for " + name, e);
	}

}
