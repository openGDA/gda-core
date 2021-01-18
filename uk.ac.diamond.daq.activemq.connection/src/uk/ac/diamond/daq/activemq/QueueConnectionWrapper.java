package uk.ac.diamond.daq.activemq;

import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;

public class QueueConnectionWrapper extends ConnectionWrapper implements QueueConnection {
	
	QueueConnectionWrapper(String connectionUri, QueueConnection connection){
		super(connectionUri, connection);
	}
	@Override
	public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
		return ((QueueConnection) connection).createQueueSession(transacted, acknowledgeMode);
	}

	@Override
	public ConnectionConsumer createConnectionConsumer(Queue queue, String messageSelector,
			ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		return ((QueueConnection) connection).createConnectionConsumer(queue, messageSelector, sessionPool, maxMessages);

	}

}
