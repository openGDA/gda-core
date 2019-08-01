package org.eclipse.scanning.test.scan.servlet;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.server.servlet.AbstractJobQueueServlet;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * When the scan servlet is started:<ol>
 * <li>If there are things in the queue it should pause;</li>
 * <li>If the queue is empty it should not pause.</li>
 *
 * Note: if the test methods in this class time out ensure that LD_LIBRARY_PATH is set to
 * ${project_loc:hdf.hdf5lib}/lib/${target.os}-${target.arch}
 *
 * @author Matthew Gerring
 */
public class StartServerTest extends AbstractServletTest {

	@Override
	protected AbstractJobQueueServlet<ScanBean> createServlet() throws EventException, URISyntaxException {
		// We don't create the sevlet here as one tests requires a bean to be submitted before connect is called
		return null;
	}

	@Test
	public void runServletEmptyQueue() throws Exception {
		servlet = new ScanServlet();
		servlet.setBroker(uri.toString());
		servlet.connect(); // Gets called by Spring automatically
		servlet.getJobQueue().awaitStart();

		assertEquals(QueueStatus.RUNNING, servlet.getJobQueue().getQueueStatus());
	}

	@Test
	@Ignore("Reinstate when queue persistence is reintroduced (DAQ-1704)")
	public void runServletSomethingInQueue() throws Exception {
		// We do not start it!
		servlet = new ScanServlet();
		servlet.setBroker(uri.toString());

		// Now there is something in the queue
		submit(servlet, createGridScan());

		servlet.connect(); // Gets called by Spring automatically
		servlet.getJobQueue().awaitStart();

		assertEquals(QueueStatus.PAUSED, servlet.getJobQueue().getQueueStatus());
		servlet.getJobQueue().clearQueue();
		servlet.getJobQueue().clearRunningAndCompleted();
		servlet.disconnect();
	}

}
