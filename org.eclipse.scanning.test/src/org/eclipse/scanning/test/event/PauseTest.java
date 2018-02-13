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
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.Before;

/**
 * Class to test that we can run
 *
 * @author Matthew Gerring
 *
 */
public class PauseTest extends AbstractPauseTest{


	@Before
	public void createServices() throws Exception {

		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE!
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(createNonOSGIActivemqMarshaller());
		eservice = new EventServiceImpl(activemqConnectorService); // Do not copy this get the service from OSGi!

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		submitter  = eservice.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
		consumer   = eservice.createConsumer(uri, EventConstants.SUBMISSION_QUEUE, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC);
		consumer.setName("Test Consumer");
		consumer.clearQueue(EventConstants.SUBMISSION_QUEUE);
		consumer.clearQueue(EventConstants.STATUS_SET);
	}
}
