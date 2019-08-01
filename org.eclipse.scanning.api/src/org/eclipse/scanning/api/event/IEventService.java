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
package org.eclipse.scanning.api.event;

import java.net.URI;
import java.util.EventListener;

import org.eclipse.scanning.api.event.core.IJmsQueueReader;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 *
 * The scanning event service allows one to subscribe to
 * and broadcast events. It may be backed by the EventBus or
 * plain JMS queues and topics depending on the service implementor.
 *
 * <ul>
 *   <li>{@link IJobQueue} encapsulates a queue that beans can be submitted to,
 *     and a consumer thread that removes items from the head of the queue and creates and runs
 *     a process for them. It also published bean updates to a status topic and can be controlled
 *     via a command topic;</li>
 *   <li>{@link ISubmitter} to submit to a JMS queue.</li>
 *   <li>{@link IJmsQueueReader} to read a JMS queue and submit any items found immediately to the {@link IJobQueue}.
 *   <li>{@link IPublisher} can be used to publish to a JMS topic. For example the {@link IJobQueue}
 *   implementation uses one to pubilsh bean update to the status topic;</li>
 *   <li>{@link ISubscriber} can be used to subscribe to a JMS topic. For example this can be used to listen
 *     to bean updates from an {@link IJobQueue} as the process for a job is run;</li>
 *   <li>{@link IRequester} to post a request to a topic and listen for a response. </li>
 *   <li>{@link IResponder} to listen for requests on a topic and post a response. This</li>
 * </ul>
 *<p>
 * Note that {@link ISubmitter} and {@link IJmsQueueReader} are legacy interface and should not be used
 * by new code. {@link IJobQueue} replaced IConsumer, which used to consume beans from the head of a JMS queue,
 * it did not have its own submit method. An {@link ISubmitter} was required to submit beans to the tail of the
 * queue. This is no longer necessary as {@link IJobQueue} contains a queue in memory and does have its own
 * {@link IJobQueue#submit(Object)} method - on the client an proxy should be used by calling
 * {@link IEventService#createJobQueueProxy(URI, String)}. Due to existing code still using submitters,
 * or other mechanisms to submit a bean to a JMS queue to run a job, {@link IJmsQueueReader} was developed
 * as a temporary measure. This works by running a loop consuming items from a JMS queue and immediately
 * submitting them to the {@link IJobQueue} with the same submission queue name.
 *<p>
 * <pre>
 * <code>
 *   IEventService service = ... // OSGi
 *   final IEventSubscriber subscriber = service.createSubscriber(...);
 *
 *   IScanListener listener = new IScanListener() { // Listen to any scan
 *       void scanEventPerformed(ScanEvent evt) {
 *           ScanBean scan = evt.getBean();
 *           System.out.println(scan.getName()+" @ "+scan.getPercentComplete());
 *       }
 *   };
 *
 *   subscriber.addScanListener(listener);
 *   // Subscribe to anything
 *
 *
 *
 *   IEventService service = ... // OSGi
 *
 *   final IPublisher publisher = service.createPublisher(...);
 *   final ScanBean scan = new ScanBean(...);
 *
 *   publisher.broadcast(scan);
 *
 *   // An event comes internally that the scan has changed state, so we notify like this:
 *   scan.setPercentComplete(3.14);
 *   publisher.broadcast(scan);
 *   </code>
 *   </pre>
 *
 * @author Matthew Gerring
 *
 */
public interface IEventService {

	/**
	 * Creates an ISubscriber for the topic with the given name.
	 * Useful on the client for adding event listeners to be notified.
	 *
	 *  Scan events have a unique id with which to ascertain if a given scan event
	 *  came from given scan.
	 *
	 * @param uri - the location of the JMS broker
	 * @return IEventManager
	 */
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topicName);


	/**
	 * Creates an IEventPublisher for the topic with the given name.
	 *
	 * @param uri - the location of the JMS broker
	 * @return IEventManager
	 */
	public <U> IPublisher<U> createPublisher(URI uri, String topicName);

	/**
	 * Create a submitter for adding a bean of type U onto the queue.
	 * @param uri
	 * @param queueName
	 * @return the new submitter
	 */
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName);


	/**
	 * Create an {@link IJobQueue} with the default submission queue, status topic and command topic names
	 * @param uri
	 * @return the new job queue
	 */
	public <U extends StatusBean> IJobQueue<U> createJobQueue(URI uri) throws EventException;

	/**
	 * Create a consumer with the given submission queue, and status topic names.
	 *
	 * @param uri
	 * @param submissionQueueName name of the submission queue
	 * @param statusTopicName
	 * @return the new job queue
	 */
	public <U extends StatusBean> IJobQueue<U> createJobQueue(URI uri, String submissionQueueName,
						                                        String statusTopicName) throws EventException;

	/**
	 * Create a job queue with the given submission queue, status queue, status topic, consumer status topic,
	 * command topic and command acknowledgement topic names.
	 *
	 * @param uri the uri of the message
	 * @param submissionQueueName
	 * @param statusTopicName
	 * @param commandTopicName
	 * @param commandAckTopicName
	 * @return the new job queue
	 */
	public <U extends StatusBean> IJobQueue<U> createJobQueue(URI uri, String submissionQueueName,
						                                        String statusTopicName,
						                                        String consumerStatusTopicName,
						                                        String commandTopicName,
						                                        String commandAckTopicName) throws EventException;

	/**
	 * Create a Jms queue reader. This will read messages from the JMS (ActiveMq) queue with the given name
	 * and add it to the submission queue of the {@link IJobQueue} with the same name.
	 * @param uri
	 * @param submissionQueueName
	 * @return the JMS queue reader
	 * @throws EventException
	 */
	public <U extends StatusBean> IJmsQueueReader<U> createJmsQueueReader(URI uri, String submissionQueueName) throws EventException;

	/**
	 * Returns the job queue for the given submission queue name.
	 * <em>For server-side code only! Client-side code should use {@link #createJobQueueProxy(URI, String)}.</em>
	 *
	 * @param submissionQueueName
	 * @return the consumer for the given queue name
	 * @throws EventException thrown if no job queue exists for the given queue name.
	 */
	public IJobQueue<? extends StatusBean> getJobQueue(String submissionQueueName) throws EventException;

	/**
	 * Create a proxy for the {@link IJobQueue} for the given submission queue name, using the default
	 * command and command acknowledgement topics.
	 *
	 * @param uri
	 * @param submissionQueueName
	 * @return a proxy to the job queue for the given queue name
	 * @throws EventException
	 */
	public <U extends StatusBean> IJobQueue<U> createJobQueueProxy(URI uri, String submissionQueueName) throws EventException;

	/**
	 * Create a proxy for the {@link IJobQueue} for the given submission queue. The given command topic and
	 * command acknowledgement topic names are used to communicate with the job queue.
	 *
	 * @param uri
	 * @param submissionQueueName
	 * @param commandTopicName
	 * @param commandAckTopicName
	 * @return a proxy to the job queue for the given queue name
	 * @throws EventException
	 */
	public <U extends StatusBean> IJobQueue<U> createJobQueueProxy(URI uri, String submissionQueueName,
			String commandTopicName, String commandAckTopicName) throws EventException;

	/**
	 * Disconnect all JMS resource used by all {@link IJobQueue}s and unregister them from this service.
	 * @throws EventException
	 */
	public void disposeJobQueue() throws EventException;

	/**
	 * A poster encapsulates sending and receiving a reply. For instance request a list of
	 * detectors on the server. This is the same as creating a broadcaster, sending an object
	 * then subscribing to the reply.
	 *
	 * @param uri
	 * @param requestTopic
	 * @param responseTopic
	 * @return
	 * @throws EventException
	 */
	public <T extends IdBean> IRequester<T> createRequestor(URI uri, String requestTopic, String responseTopic) throws EventException;

	/**
	 * Creates a responder on a given topic.
	 *
	 * @param uri
	 * @param requestTopic
	 * @param responseTopic
	 * @return
	 * @throws EventException
	 */
	public <T extends IdBean> IResponder<T> createResponder(URI uri, String requestTopic, String responseTopic) throws EventException;

	/**
	 * The current event connector service that this event service is using to
	 * talk to messaging and to marshall objects.
	 *
	 * @return
	 */
	public IEventConnectorService getEventConnectorService();

	/**
	 * Use this call to create a remote service. A wrapper will be created around the service
	 * such that methods called on the client will cause an event to trigger which has a response
	 * generated by the server.
	 *
	 * The event service caches remote services assuming that each service should exist once.
	 *
	 * @param uri
	 * @param serviceClass
	 * @return
	 * @throws EventException
	 */
	public <T> T createRemoteService(URI uri, Class<T> serviceClass) throws EventException;
}
