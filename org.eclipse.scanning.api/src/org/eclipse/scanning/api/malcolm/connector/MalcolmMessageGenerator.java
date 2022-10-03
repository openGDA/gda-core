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
package org.eclipse.scanning.api.malcolm.connector;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;

/**
 * A factory class for generating messages to send to malcolm.
 * <em>Care should be taken that all message to malcolm from all {@link IMalcolmDevice}s
 * should use the same message generator</em>
 */
public class MalcolmMessageGenerator implements IMalcolmMessageGenerator {

	private volatile long nextId = 0;

	private MalcolmMessage createMalcolmMessage() {
		MalcolmMessage ret = new MalcolmMessage();
		ret.setId(nextId++);
		return ret;
	}

	@Override
	public MalcolmMessage createSubscribeMessage(String subscription) {
		final MalcolmMessage msg = createMalcolmMessage();
		msg.setType(Type.SUBSCRIBE);
		msg.setEndpoint(subscription);
		return msg;
	}

	@Override
	public MalcolmMessage createUnsubscribeMessage() {
		final MalcolmMessage msg = createMalcolmMessage();
		msg.setType(Type.UNSUBSCRIBE);
		return msg;
	}

	@Override
	public MalcolmMessage createGetMessage(String cmd) {
		final MalcolmMessage msg = createMalcolmMessage();
		msg.setType(Type.GET);
		msg.setEndpoint(cmd);
		return msg;
	}

	@Override
	public MalcolmMessage createCallMessage(final MalcolmMethod method) {
		final MalcolmMessage msg = createMalcolmMessage();
		msg.setType(Type.CALL);
		msg.setMethod(method);
		return msg;
	}

	@Override
	public MalcolmMessage createCallMessage(MalcolmMethod method, Object arg) {
		final MalcolmMessage msg = createCallMessage(method);
		msg.setArguments(arg);
		return msg;
	}

}
