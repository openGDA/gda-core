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

import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ConsumerImpl;

public class ConsumerControlTest extends AbstractConsumerControlTest {

	@Override
	protected void doPauseConsumer() throws Exception {
		consumer.pause();
	}

	@Override
	protected void doResumeConsumer() throws Exception {
		consumer.resume();
	}

	@Override
	protected void doStopConsumer() throws Exception {
		consumer.stop();
	}

	@Override
	protected void doRestartConsumer() throws Exception {
		((ConsumerImpl<StatusBean>) consumer).restart();
	}

	@Override
	protected void doClearQueue() throws Exception {
		consumer.clearQueue();
	}

}
