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

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 *
 * An interface for devices that are activatable and part of the scan.
 * For runnable devices (detectors etc) they get included as detectors,
 * for scannables activated scannables are included as monitors.
 *
 * This is normally used to mark detectors as being active so that if
 * scan algorithm data is contstructed they will be included in the scan.
 *
 * NOTE: Normally the scan device is told specifically which detectors to use
 * it does not take into account the isActivated() value when the scan is run.
 * Instead this is used when the scan data is being constructed.
 *
 * Activated devices are used when a scan is constructed and the state is saved.
 * This allows devices created in Spring to be activated and therefore run in
 * a default scan. However the actual devices run in the scan are just defaulted
 * to those activated. It is perfectly possible to run non-activated devices
 * by putting them in the scan request.
 *
 * TODO: this interface is now deprecated and should be removed at some future point.
 *   This can only be done when the {@code <activated>} property has been removed
 *   from all beamline configuration files
 *
 * @author Matthew Gerring
 * @deprecated whether monitors (i.e. scannables) will be included in the next scan
 *   or not should not be a property of the monitor on the server. Instead this
 *   should be configured in the client and used to determine the {@link ScanRequest}.
 *   Configuring this on the server caused a large number of client-server requests
 *   to build the ScanRequest by asking the server which monitors were activated.
 *   Default monitors can be configured by setting.
 *
 */
@Deprecated
public interface IActivatable {

	/**
	 *
	 * @return true if device is activated.
	 */
	default boolean isActivated() {
		return false;
	}

	/**
	 *
	 * @param activated
	 * @return the old value of activated
	 */
	default boolean setActivated(boolean activated)  throws ScanningException {
		throw new ScanningException("setActivated is not implemented!");
	}
}
