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
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.junit.After;
import org.junit.Before;

public class MConsumerPluginTest extends AbstractMConsumerTest {


    private static IEventService service;

	public static IEventService getService() {
		return service;
	}

	public static void setService(IEventService service) {
		MConsumerPluginTest.service = service;
	}

	@Before
	public void createServices() throws Exception {

		eservice = MConsumerPluginTest.service;

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		submitter  = eservice.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
		consumer   = eservice.createConsumer(uri, EventConstants.SUBMISSION_QUEUE, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC, EventConstants.HEARTBEAT_TOPIC, EventConstants.CMD_TOPIC);
		consumer.setName("Test Consumer 1");
		consumer.clearQueue();
		consumer.clearQueue(EventConstants.STATUS_SET);
	}

	@After
	public void dispose() throws EventException {
		submitter.disconnect();
		consumer.clearQueue();
		consumer.clearQueue(EventConstants.STATUS_SET);
		consumer.disconnect();
	}

}
