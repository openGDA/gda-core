/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.jython;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.ServiceHolder;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.factory.Finder;
import gda.jython.IScanDataPointObserver;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.TerminalOutput;
import gda.scan.IScanDataPoint;
import gda.scan.ScanEvent;
import uk.ac.diamond.daq.server.services.scan.ScanService;

/**
 * JMS Jython service. This creates a consumer to listen for requests from clients and a topic which clients subscribe
 * to for Jython output. The JSF is used to facilitate this.
 */
public class JythonService extends FindableConfigurableBase implements MessageListener, IScanDataPointObserver {

	private static final Logger logger = LoggerFactory.getLogger(JythonService.class);
	private static final String COMMAND_QUEUE = "JYTHONCOMMANDQUEUE";
	private static final String UPDATE_QUEUE = "JYTHONUPDATEQUEUE";

	private Session session;
	private MessageProducer jyUpdate;

	// dispatch to scan service
	private ScanService scanService = new ScanService();

	@Override
	public void configure() throws FactoryException {
		try {
			session = ServiceHolder.getSessionService().getSession();
			MessageConsumer consumer = session.createConsumer(new ActiveMQQueue(COMMAND_QUEUE));
			var topic = session.createTopic(UPDATE_QUEUE);
			jyUpdate = session.createProducer(topic);
			consumer.setMessageListener(this);
			var jython = Finder.findLocalSingleton(Jython.class);
			var jsf = JythonServerFacade.getInstance();
			jython.addIObserver(this);
			jsf.addIScanDataPointObserver(this);
			setConfigured(true);
		} catch (JMSException e) {
			throw new FactoryException("Unable to start Jython service", e);
		}
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof TerminalOutput) {
			publishTerminalOutput(((TerminalOutput) arg).getOutput());
		}
		if (arg instanceof IScanDataPoint && source instanceof JythonServerFacade) {
			IScanDataPoint sdpt = (IScanDataPoint) arg;
			publishTerminalOutput(sdpt.toFormattedString() + "\n");
			scanService.scanDataPoint(sdpt);
		}
		if (arg instanceof ScanEvent) {
			scanService.scanEvent((ScanEvent) arg);
		}

	}

	private void publishTerminalOutput(String arg) {
		try {
			jyUpdate.send(session.createTextMessage(arg));
		} catch (JMSException e) {
			logger.error("Could not publish Jython output to terminals");
		}

	}

	@Override
	public void onMessage(Message message) {
		try {
			TextMessage resonseMessage;
			JythonServerFacade.getInstance().runsource(((TextMessage) message).getText());
			resonseMessage = session.createTextMessage(""); // response is not used
			resonseMessage.setJMSCorrelationID(message.getJMSMessageID());
			MessageProducer producer = session.createProducer(message.getJMSReplyTo());
			producer.send(resonseMessage);
		} catch (JMSException e) {
			logger.error("Error consuming Jython command");
		}

	}

}
