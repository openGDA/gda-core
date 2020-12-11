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
package org.eclipse.scanning.event;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponseWaiter;
import org.eclipse.scanning.api.event.core.ISubscriber;

class RequesterImpl<T extends IdBean> extends AbstractRequestResponseConnection implements IRequester<T> {

	private ResponseType responseType = DEFAULT_RESPONSE_TYPE;
	private long defTimeout = DEFAULT_TIMEOUT;
	private TimeUnit defTimeUnit = DEFAULT_TIME_UNIT;
	private CountDownLatch latch;
	private boolean somethingFound;


	RequesterImpl(URI uri, String reqTopic, String resTopic, IEventService eservice) {
		super(uri, reqTopic, resTopic, eservice);
	}

	@Override
	public T post(final T request) throws EventException, InterruptedException {
		return post(request, null);
	}

	@Override
	public T post(final T request, long timeout, TimeUnit timeUnit) throws EventException, InterruptedException {
		return post(request, null, timeout, timeUnit);
	}

	@Override
	public T post(final T request, IResponseWaiter waiter) throws EventException, InterruptedException {
		return post(request, waiter, defTimeout, defTimeUnit);
	}

	@Override
	public T post(final T request, IResponseWaiter waiter, long timeout, TimeUnit timeUnit) throws EventException, InterruptedException {

		try (
			final IPublisher<T> publisher = eservice.createPublisher(getUri(), getRequestTopic());
			final ISubscriber<IBeanListener<T>> subscriber = eservice.createSubscriber(getUri(), getResponseTopic())) {

			// Just listen to our id changing.
			subscriber.addListener(request.getUniqueId(), evt -> {
				final T response = evt.getBean();
				request.merge(response); // The bean must implement merge, for instance DeviceRequest.
				countDown();
			});

			// Send the request
			publisher.broadcast(request);

			latch(waiter, timeout, timeUnit); // Wait or die trying

			return request;
		}
	}

	private void latch(IResponseWaiter waiter, long timeout, TimeUnit timeUnit) throws EventException, InterruptedException {
		if (waiter == null) {
			// Default to waiting just one timeout period
			waiter = () -> false;
		}

		if (responseType == ResponseType.ONE) {
			// Wait for the first response, subject to timing out
			latch = new CountDownLatch(1);
			boolean ok = latch.await(timeout, timeUnit);
			while (!ok && waiter.waitAgain()) {
				ok = latch.await(timeout, timeUnit);
			}
			// waitAgain() could be false leaving ok as false, so we recheck it
			ok = latch.await(timeout, timeUnit);
			if (!ok) {
				throw new EventException("The timeout of " + timeout + " " + timeUnit + " was reached and no response occurred!");
			}
		} else if (responseType == ResponseType.ONE_OR_MORE) {
			// Wait for the specified time, regardless of how many responses are received
			somethingFound = false;

			Thread.sleep(timeUnit.toMillis(timeout));
			while (waiter.waitAgain()) {
				Thread.sleep(timeUnit.toMillis(timeout));
			}
			if (!somethingFound) {
				throw new EventException("The timeout of " + timeout + " " + timeUnit + " was reached and no response occurred!");
			}
		}
	}

	private void countDown() {
		somethingFound = true;
		if (latch != null) {
			latch.countDown();
		}
	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		this.defTimeout = timeout;
		this.defTimeUnit = timeUnit;
	}

	@Override
	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}
}
