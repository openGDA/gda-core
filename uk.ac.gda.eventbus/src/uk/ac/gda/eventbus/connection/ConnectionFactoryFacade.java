/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.gda.eventbus.connection;

import java.net.URI;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Class exists to avoid dependency on org.apache.activemq leaking around the code
 * base. Please use this facade to keep things modular.
 * 
 * @author Matthew Gerring
 *
 */
public class ConnectionFactoryFacade {

	/**
	 * Create a ConnectionFactory using activemq
	 * @param uri
	 * @return a queue connection factory
	 * @throws JMSException
	 */
	public static QueueConnectionFactory createConnectionFactory(final String uri) throws JMSException {
		return new ActiveMQConnectionFactory(uri);
	}

	/**
	 * Create a ConnectionFactory using activemq
	 * @param uri
	 * @return a queue connection factory
	 * @throws JMSException
	 */
	public static QueueConnectionFactory createConnectionFactory(final URI uri) throws JMSException {
		return new ActiveMQConnectionFactory(uri);
	}
}
