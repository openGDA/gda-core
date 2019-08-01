/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.status.StatusBean;
import org.junit.Test;

public class QueuePersistenceTest extends AbstractJobQueueTest {

	@Test
	public void testQueuePersistence() throws Exception {
		// create and submit the beans for the status queue and wait for them to finish
		final List<StatusBean> beans = createAndSubmitBeans("one", "two", "three");
		// the mock process time needs to be increased so that the beans will not all be complete
		// before MVStore's background thread saves them to disk - it runs once per second
		setMockProcessTime(1000);
		CountDownLatch latch = new CountDownLatch(beans.size());
		setupMockProcesses(beans, latch);
		startJobQueue();

		boolean success = latch.await(getMockProcessTime() * (beans.size() + 1), TimeUnit.SECONDS);
		assertThat(success, is(true));

		final List<StatusBean> runningAndCompleted = jobQueue.getRunningAndCompleted();
		assertThat(jobQueue.getSubmissionQueue(), is(empty()));
		assertThat(runningAndCompleted, is(equalTo(beans)));

		// stop the consumer thread
		jobQueue.stop();
		jobQueue.awaitStop();
		assertThat(jobQueue.isActive(), is(false));

		// create and submit the beans for the submission queue
		final List<StatusBean> beans2 = createAndSubmitBeans("four", "five", "six");
		final List<StatusBean> queued = jobQueue.getSubmissionQueue();
		assertThat(queued, is(equalTo(beans2)));

		jobQueue.close();
		jobQueue = null;

		// create a new consumer and check the queues are read correctly
		createJobQueue();

		assertThat(jobQueue.getSubmissionQueue(), is(equalTo(queued)));
		assertThat(jobQueue.getRunningAndCompleted(), is(equalTo(runningAndCompleted)));

		// let's add some more jobs to the queue to be extra sure
		beans2.addAll(createAndSubmitBeans("seven", "eight"));
		assertThat(jobQueue.getSubmissionQueue(), is(equalTo(beans2)));
	}

}
