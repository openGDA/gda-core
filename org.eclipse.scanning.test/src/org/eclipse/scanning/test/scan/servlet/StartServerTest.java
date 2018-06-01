package org.eclipse.scanning.test.scan.servlet;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.server.servlet.AbstractConsumerServlet;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.junit.Test;

/**
 *
 * When the scan servlet is started:<ol>
 * <li>If there are things in the queue it should pause;</li>
 * <li>If the queue is empty it should not pause.</li>
 *
 * @author Matthew Gerring
 */
public class StartServerTest extends AbstractServletTest {

	@Override
	protected AbstractConsumerServlet<ScanBean> createServlet() throws EventException, URISyntaxException {
		// We don't create the sevlet here as one tests requires a bean to be submitted before connect is called
		return null;
	}

	@Test
	public void runServletEmptyQueue() throws Exception {
		servlet = new ScanServlet();
		servlet.setBroker(uri.toString());
		servlet.connect(); // Gets called by Spring automatically
		servlet.getConsumer().awaitStart();

		assertEquals(ConsumerStatus.RUNNING, servlet.getConsumer().getConsumerStatus());
	}

	@Test
	public void runServletSomethingInQueue() throws Exception {
		// We do not start it!
		servlet = new ScanServlet();
		servlet.setBroker(uri.toString());

		// Now there is something in the queue
		submit(servlet, createGridScan());

		servlet.connect(); // Gets called by Spring automatically
		servlet.getConsumer().awaitStart();

		assertEquals(ConsumerStatus.PAUSED, servlet.getConsumer().getConsumerStatus());
		servlet.getConsumer().clearQueue(servlet.getSubmitQueue());
		servlet.getConsumer().clearQueue(servlet.getStatusSet());
		servlet.getConsumer().cleanQueue(EventConstants.CMD_SET);
		servlet.disconnect();
	}

}
