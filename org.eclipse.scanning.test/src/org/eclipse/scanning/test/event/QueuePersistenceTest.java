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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.JobQueueImpl;
import org.eclipse.scanning.event.queue.IPersistentModifiableIdQueue;
import org.eclipse.scanning.event.queue.SynchronizedModifiableIdQueue;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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

	/**
	 * This test checks that if an exception is thrown loading the queue then the JobQueueImpl
	 * recovers (i.e. doesn't throw an exception itself), but instead deletes the queue persistence file
	 * used by MVStore and new empty queues (submission queue and status queue) are created backed
	 * by a new persistence file at the same path.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testQueueVersionChange() throws Exception {
		final String queueName = "testQueue";
		final Path queueFilePath = Paths.get(eventConnectorService.getPersistenceDir(), queueName + ".db");
		assertThat(queueFilePath.toFile().exists(), is(false));
		queueFilePath.toFile().deleteOnExit();


		try (MockedStatic<MVStore> staticMock = mockStatic(MVStore.class)) {

			final MVStore mockMvStore = Mockito.mock(MVStore.class);

			when(MVStore.open(anyString())).thenReturn(mockMvStore).thenCallRealMethod();
			when(mockMvStore.openMap(anyString())).thenThrow(IllegalArgumentException.class);

			assertThat(queueFilePath.toFile().exists(), is(false));
			final Instant startTime = Instant.now();

			final IJobQueue<StatusBean> jobQueue = new JobQueueImpl<>(uri, "testQueue", EventConstants.STATUS_TOPIC,
					EventConstants.QUEUE_STATUS_TOPIC, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC,
					eventConnectorService, eventService);

			verify(mockMvStore).closeImmediately();

			// verify that a new file was created
			assertThat(queueFilePath.toFile().exists(), is(true));
			final Instant fileCreationTime = Files.readAttributes(queueFilePath, BasicFileAttributes.class)
					.creationTime().toInstant();
			assertThat(fileCreationTime, is(greaterThanOrEqualTo(startTime.truncatedTo(ChronoUnit.SECONDS))));

			assertThat(jobQueue.getSubmissionQueue(), is(empty()));
			assertThat(jobQueue.getRunningAndCompleted(), is(empty()));

			jobQueue.close();
		}
	}

	/**
	 * This test check that if an exception is thrown when iterating over an apparently successfully
	 * loaded queue, then the JobQueueImpl constructor recovers (i.e. doesn't throw an exception itself),
	 * but instead deletes the queue persistence file used by MVStore and new empty
	 * queues (submission queue and status queue) are created backed by a new persistence file at the same path.
	 *
	 * @throws Exception
	 */
	@Test
	public void testQueueVersionChange2() throws Exception {
		final String queueName = "testQueue2";
		final Path queueFilePath = Paths.get(eventConnectorService.getPersistenceDir(), queueName + ".db");
		assertThat(queueFilePath.toFile().exists(), is(false));
		queueFilePath.toFile().deleteOnExit();

		assertThat(queueFilePath.toFile().exists(), is(false));
		final Instant startTime = Instant.now();

		final AtomicBoolean firstCall = new AtomicBoolean(true);
		final IJobQueue<StatusBean> jobQueue = new JobQueueImpl<StatusBean>(uri, queueName,
				EventConstants.STATUS_TOPIC, EventConstants.QUEUE_STATUS_TOPIC,
				EventConstants.CMD_TOPIC,
				EventConstants.ACK_TOPIC, eventConnectorService, eventService) {

				// the aim is to trigger an exception when the queue is iterated over
				// this is done by anonymously subclassing both JobQueueImp and SynchronizedModifiableIdQueue
				// because unfortunately I wasn't able to get this working with mockito/powermock.
				@Override
				protected IPersistentModifiableIdQueue<StatusBean> createQueue(
						IEventConnectorService connectorService, MVStore store, String queueName) {
					if (firstCall.getAndSet(false)) {
						return new SynchronizedModifiableIdQueue<StatusBean>(connectorService, store, queueName) {

							@Override
							public Iterator<StatusBean> iterator() {
								throw new RuntimeException();
							}
						};
					}

					return super.createQueue(connectorService, store, queueName);
				}
		};

		// verify that a new file was created
		assertThat(queueFilePath.toFile().exists(), is(true));
		final Instant fileCreationTime = Files.readAttributes(queueFilePath, BasicFileAttributes.class).creationTime().toInstant();
		assertThat(fileCreationTime, is(greaterThanOrEqualTo(startTime.truncatedTo(ChronoUnit.SECONDS))));

		assertThat(jobQueue.getSubmissionQueue(), is(empty()));
		assertThat(jobQueue.getRunningAndCompleted(), is(empty()));

		jobQueue.close();
	}

}
