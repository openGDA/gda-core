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
package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

/**
 * A submitter is a queue connection which may receive a submission on to the queue.
 *
 * @author Matthew Gerring
 *
 * @param <T> Bean type which will be submitted.
 */
public interface ISubmitter<T> extends IReadOnlyQueueConnection<T> {

	/**
	 * Send a submission on to the queue.
	 * @param bean
	 */
	void submit(T bean) throws EventException;

	/**
	 * Send a submission on to the queue. Blocks until bean is
	 * updated with "final" status.
	 *
	 * This method depends on this.setStatusTopicName() already having
	 * been called with the appropriate status queue name by the
	 * user of this ISubmitter, because this method's implementation
	 * listens to the said status topic to determine when to return.
	 *
	 * @param bean
	 * @throws EventException
	 * @throws InterruptedException
	 * @throws IllegalStateException if this.getStatusTopicName() returns null.
	 */
	void blockingSubmit(T bean) throws EventException, InterruptedException, IllegalStateException;

	/**
	 * The status topic, if any, that after submission, the consumer will publish events from.
	 * May be left unset.
	 *
	 * @return
	 */
	String getStatusTopicName();

	/**
	 * The status topic, if any, that after submission, the consumer will publish events from.
	 * May be left unset.
     *
	 * @param name
	 */
	void setStatusTopicName(String name);

	/**
	 * @return the priority of messages submitted by this submitter.
	 */
	int getPriority();

	/**
	 * Sets the priority of messages submitted by this submitter.
	 * From the Javadoc from the underlying JMS implementation
	 * (javax.jms.MessageProducer#setPriority):
	 * <P>The JMS API defines ten levels of priority value, with 0 as the
	 * lowest priority and 9 as the highest. Clients should consider priorities
	 * 0-4 as gradations of normal priority and priorities 5-9 as gradations
	 * of expedited priority. Priority is set to 4 by default.
	 * @param priority
	 */
	void setPriority(int priority);

	/**
	 * @return the lifetime of messages
	 */
	long getLifeTime();


	/**
	 * Sets the lifetime of messages.
	 * @param lifetime
	 */
	void setLifeTime(long lifetime);

}
