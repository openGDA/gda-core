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

import static org.eclipse.scanning.api.event.core.ResponseConfiguration.DEFAULT_TIMEOUT;
import static org.eclipse.scanning.api.event.core.ResponseConfiguration.DEFAULT_TIME_UNIT;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.core.ResponseConfiguration;
import org.eclipse.scanning.api.event.core.ResponseConfiguration.ResponseType;

class RequesterImpl<T extends IdBean> extends AbstractRequestResponseConnection implements IRequester<T> {

	private ResponseConfiguration responseConfiguration;

	RequesterImpl(URI uri, String reqTopic, String resTopic, IEventService eservice) {
		super(uri, reqTopic, resTopic, eservice);
		responseConfiguration = new ResponseConfiguration(ResponseType.ONE, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
	}

	@Override
	public void setTimeout(long time, TimeUnit unit) {
		responseConfiguration.setTimeout(time, unit);
	}

	@Override
	public T post(final T request) throws EventException, InterruptedException {
		return post(request, null);
	}

	@Override
	public T post(final T request, ResponseConfiguration.ResponseWaiter waiter) throws EventException, InterruptedException {

		try (
			final IPublisher<T> publisher = eservice.createPublisher(getUri(), getRequestTopic());
			final ISubscriber<IBeanListener<T>> subscriber = eservice.createSubscriber(getUri(), getResponseTopic())) {

			// Just listen to our id changing.
			subscriber.addListener(request.getUniqueId(), evt -> {
				final T response = evt.getBean();
				request.merge(response); // The bean must implement merge, for instance DeviceRequest.
				responseConfiguration.countDown();
			});

			// Send the request
			publisher.broadcast(request);

			responseConfiguration.latch(waiter); // Wait or die trying

			return request;
		}
	}

	@Override
	public ResponseConfiguration getResponseConfiguration() {
		return responseConfiguration;
	}

	@Override
	public void setResponseConfiguration(ResponseConfiguration responseConfiguration) {
		this.responseConfiguration = responseConfiguration;
	}

}
