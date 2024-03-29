/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * Clients do not need to consume this service, it is used to provide connection
 * to devices which already exist, like those in GDA8.
 * <p>
 * A bundle implements this service and the scanning consumes the service. The
 * tests provide the service directly.
 * <p>
 * This service is designed to be implemented by GDA8 to provide scannables
 * (existing ones) to the new system. This can be done easily as
 * {@link IScannable} is a subset of Scannable in GDA8.
 * <p>
 * It also provides {@link IWritableDetector}'s. These are analogous to, but not
 * the same as Detector in GDA. The service wraps both gda.device.Detector into
 * {@link IWritableDetector} and gda.px.detector.Detector into
 * {@link IWritableDetector}. In the first case the model provides the
 * collection time and anything else required. In the later case the model
 * provides the file path and omegaStart required.
 * <p>
 * It mirrors the other connector services like the JMS one, which push the
 * details of getting connections outside the core scanning API using
 * declarative services.
 *
 * @author Matthew Gerring
 *
 */
public interface IScannableDeviceService {

	/**
	 * Used to register a device. This is required so that spring may create
	 * detectors and call the register method by telling the detector to register
	 * itself.
	 *
	 * @param device
	 */
	<T> void register(IScannable<T> scannable);

	/**
	 * Get the names of all scannables known to the connector.
	 * @return
	 * @throws ScanningException
	 */
	List<String> getScannableNames() throws ScanningException;

	/**
	 * Get a scannable by name.
	 * @param name name of scannable to find
	 * @return scannable, never <code>null</code>
	 * @throws ScanningException if no scannable with the given name could be found
	 */
	<T> IScannable<T> getScannable(String name) throws ScanningException;

	/**
	 * Returns a list of the scannables for the given {@link IPosition}s.
	 * @param position an {@link IPosition}
	 * @return list of scannables for the given position
	 * @throws ScanningException if one or more of the scannables cannot be found
	 */
	default List<IScannable<?>> getScannables(IPosition position) throws ScanningException {
		return getScannables(position.getNames());
	}

	/**
	 * Returns a list of the scannables with the given names
	 * @param scannableNames a list of scannable names
	 * @return list of scannables for the given names
	 * @throws ScanningException if one or more of the scannables cannot be found
	 */
	default List<IScannable<?>> getScannables(List<String> scannableNames) throws ScanningException {
		final List<IScannable<?>> scannables = new ArrayList<>(scannableNames.size());
		for (String scannableName : scannableNames) {
			scannables.add(getScannable(scannableName));
		}

		return scannables;
	}

	/**
	 * Returns the set of global per-scan monitors that should be added to all scans.
	 * This is used to support legacy (GDA8) spring configurations. Should not be called
	 * by client code.
	 * @deprecated use {@link IDefaultScanConfigurations} instead
	 *  TODO this method should eventually be removed
	 * @return global per-scan monitor names
	 */
	@Deprecated(since="GDA 9.3", forRemoval=true)
	default Set<String> getGlobalPerScanMonitorNames() {
		DeprecationLogger.getLogger(getClass()).deprecatedMethod("getGlobalPerScanMonitorNames()", null, "org.eclipse.scanning.api.device.IDefaultScanConfigurations");
		return Collections.emptySet();
	}

	/**
	 * Returns the set of the names required per-scan monitors for the given scannable name.
	 * This is used to support legacy (GDA8) spring configurations. Should not be called
	 * by client code.
	 * @param scannableName scannable to get required per-scan monitor names for
	 * @deprecated this feature should not be used for new code,
	 *    if this behaviour is required we should add a GDA9 way to do define scannable dependencies
	 * @return names of required per-scan monitors for the scannable with the given name
	 */
	@Deprecated(since="GDA 9.3", forRemoval=true)
	default Set<String> getRequiredPerScanMonitorNames(String scannableName) {
		DeprecationLogger.getLogger(getClass()).deprecatedMethod("getRequiredPerScanMonitorNames(String)");
		return Collections.emptySet();
	}

	default void handleDeviceError(String name, Exception e) {
		System.err.println("Failure getting device information for " + name);
		e.printStackTrace();
	}


}
