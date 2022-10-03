/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.event;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("DAQ-2088 Tests have not been implemented")
public class SubscriberTest extends BrokerTest {

	private IPublisher<StatusBean> publisher;
	private ISubscriber<?> subscriber;

	@BeforeEach
	public void start() throws Exception {
		// TODO set notification and receive frequencies?
	}

	@AfterEach
	public void stop() throws Exception {
		publisher.disconnect();
		subscriber.disconnect();
	}

	@Test
	public void testSimpleSubscriber() throws Exception {
		subscriber.addListener(null);

	}

	@Test
	public void testSynchronous() {
		// TODO
	}

	@Test
	public void testAsynchronous() {
		// TODO
	}




}
