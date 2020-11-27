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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;

/**
 * A poster broadcasts a request of an object with a unique id. The server then fills in information about this object
 * and broadcasts it back. The UUID must be set to determine which response belongs with whcih request.
 * <p>
 * This mimics web server functionality but uses the existing messaging system without requiring a web server
 * configuration. An alternative to this might be to start a jetty server on the acquisition server and response to
 * requests directly.
 * <p>
 * A use case for this is where a client would like to get a list of detectors. NOTE: Unlike a web server this paradigm
 * allows multiple responders to reply on a topic. Therefore for instance if many malcolm devices are available on
 * different ports, a post will be made asking for those devices and the response from many listeners of the post topic
 * collated.
 *
 * @param <T>
 *            You <b>must</b> override the merge(...) method in your type. This puts the reponder's information into
 *            your bean. In the case where there are multiple reponders (like a list of detectors) you must be additive
 *            in the merge method.
 * @author Matthew Gerring
 */
public interface IRequester<T extends IdBean> extends IRequestResponseConnection {

	enum ResponseType {
		/**
		 * One response is required, after that comes in, return. If the timeout is reached throw an exception.
		 * <p>
		 * This is more efficient because the timeout is not waited for, it can return as soon as the first message is
		 * encountered.
		 */
		ONE,

		/**
		 * Multiple responses are required, wait for the response timeout and collate all responses. If no response
		 * comes in, throw an exception.
		 * <p>
		 * Less efficient but caters for multiple sources of the response if there may be some.
		 */
		ONE_OR_MORE;
	}

	static final ResponseType DEFAULT_RESPONSE_TYPE = ResponseType.ONE;
	static final long DEFAULT_TIMEOUT = 100;
	static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

	/**
	 * Set the {@link ResponseType} required (see above)
	 */
	void setResponseType(ResponseType responseType);

	/**
	 * Set the timeout for the request
	 */
	void setTimeout(long time, TimeUnit unit);

	/**
	 * Requests a response from the request and returns it. This method blocks until the response has been retrieved
	 * with the correct {@link UUID}.
	 * <p>
	 * Calls post and waits for the timeout until one or more reponses have come in (depending on the response
	 * configuration) then returns.
	 *
	 * @param request
	 *            the request to be sent
	 * @return the response to the request
	 * @throws EventException
	 */
	T post(T request) throws EventException, InterruptedException;

	/**
	 * Same as {@link #post(IdBean)} with an optional {@link IResponseWaiter} (may be null) which provides the ability
	 * to return true if the post should carry on waiting. This is useful for instance in the case where a scannable is
	 * setting position. It will have notified position recently and if the waiter thinks it is still alive there is not
	 * reason to timeout. This is useful in setPosition(...) calls for scannables that can take an indeterminate time
	 * but should still timeout if they go inactive.
	 *
	 * @param request
	 *            the request to be sent
	 * @param waiter
	 *            IResponseWaiter (can be null) which determines if the post should carry on waiting after an initial
	 *            timeout
	 * @return the response to the request
	 * @throws EventException
	 * @throws InterruptedException
	 */
	T post(T request, IResponseWaiter waiter) throws EventException, InterruptedException;
}
