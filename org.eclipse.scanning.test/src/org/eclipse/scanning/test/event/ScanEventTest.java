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
package org.eclipse.scanning.test.event;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.junit.jupiter.api.BeforeEach;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Designed to be run outside OSGi
 *
 * @author Matthew Gerring
 *
 */
public class ScanEventTest extends AbstractScanEventTest{

	@BeforeEach
	public void createServices() {
		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		publisher = eventService.createPublisher(uri, EventConstants.SCAN_TOPIC);
		subscriber = eventService.createSubscriber(uri, EventConstants.SCAN_TOPIC);
	}

}
