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
package org.eclipse.scanning.api.scan;

import java.util.Collection;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 *
 * This interface is used to provide custom scanning methods
 * to the IRunnableDeviceService.
 *
 * @author Matthew Gerring
 *
 */
public interface IScanService extends IRunnableDeviceService {

	/**
	 * Used to register a scan participant. Once registered any scan
	 * created will use the participant.
	 *
	 * @param device
	 */
	void addScanParticipant(Object device);

	/**
	 * Used to remove a scan participant. Once registered any scan
	 * created will use the participant it must be removed to stop this.
	 *
	 * @param device
	 */
	void removeScanParticipant(Object device);

	/**
	 * The list of objects to be run as participants with the scan.
	 * @return
	 */
	Collection<Object> getScanParticipants();

	/**
	 * Create and configure a new device that represents a scan.
	 *
	 * @param model The model providing the configuration for the scan
	 * @return A new {@link IScanDevice}
	 * @throws ScanningException
	 */
	IScanDevice createScanDevice(ScanModel model) throws ScanningException;

	/**
	 * Create and configure a new device that represents a scan.
	 * Configure it if requested.
	 * TODO: Use Builder pattern rather than a flag?
	 *
	 * @param model The model providing the configuration for the scan
	 * @param configure Should the new device be automatically configured?
	 * @return A new {@link IScanDevice}
	 * @throws ScanningException
	 */
	IScanDevice createScanDevice(
			ScanModel model,
			boolean configure) throws ScanningException;

	/**
	 * Create and configure a new device that represents a scan,
	 * the scan will publish events to the supplied publisher.
	 * TODO: Use Builder pattern rather than a flag?
	 *
	 * @param model The model providing the configuration for the scan
	 * @param eventPublisher A publisher that the scan will use to produce
	 * 						 events.
	 * @return A new {@link IScanDevice}
	 * @throws ScanningException
	 */
	IScanDevice createScanDevice(
			ScanModel model,
			IPublisher<ScanBean> eventPublisher) throws ScanningException;

	/**
	 * Create and configure a new device that represents a scan,
	 * the scan will publish events to the supplied publisher.
	 * Configure the device if requested.
	 * TODO: Use Builder pattern rather than a flag?
	 *
	 * @param model The model providing the configuration for the scan
	 * @param eventPublisher A publisher that the scan will use to produce
	 * 						 events.
	 * @param configure Should the new device be automatically configured?
	 * @return A new {@link IScanDevice}
	 * @throws ScanningException
	 */
	IScanDevice createScanDevice(
			ScanModel model,
			IPublisher<ScanBean> eventPublisher,
			boolean configure) throws ScanningException;
}
