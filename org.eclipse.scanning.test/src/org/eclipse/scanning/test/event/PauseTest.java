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
import org.eclipse.scanning.test.ScanningTestUtils;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;

/**
 * Class to test that we can run
 *
 * @author Matthew Gerring
 *
 */
public class PauseTest extends AbstractPauseTest {

	@Before
	public void createServices() throws Exception {
		ServiceTestHelper.setupServices();
		eservice = ServiceTestHelper.getEventService();

		submitter  = eservice.createSubmitter(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID);

		jobQueue = eservice.createJobQueue(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID, EventConstants.STATUS_TOPIC);
		jobQueue.setName("Test Queue");
		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();

		jmsQueueReader = eservice.createJmsQueueReader(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID);
		jmsQueueReader.start();
	}
}