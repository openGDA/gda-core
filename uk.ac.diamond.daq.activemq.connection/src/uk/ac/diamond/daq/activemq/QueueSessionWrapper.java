package uk.ac.diamond.daq.activemq;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

public class QueueSessionWrapper extends SessionWrapper implements QueueSession {

	QueueSessionWrapper(QueueSession session) {
		super(session);
	}

	@Override
	public QueueReceiver createReceiver(Queue queue) throws JMSException {
		return ((QueueSession) session).createReceiver(queue);
	}

	@Override
	public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
		return ((QueueSession) session).createReceiver(queue, messageSelector);
	}

	@Override
	public QueueSender createSender(Queue queue) throws JMSException {
		return ((QueueSession) session).createSender(queue);
	}

}
