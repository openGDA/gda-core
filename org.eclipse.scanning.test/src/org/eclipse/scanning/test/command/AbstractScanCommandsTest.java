package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.nexus.ScanClusterProcessingChecker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractScanCommandsTest extends AbstractJythonTest {

	protected static ScanServlet servlet;

	/**
	 * Fake processing job queue or null if no processing
	 */
	protected static IJobQueue<StatusBean>       pjobQueue;


	@BeforeClass
	public static void init() {
		pjobQueue = null;
		ScanPointGeneratorFactory.init();
	}

	protected static IRunnableDeviceService      dservice;
	protected static IPointGeneratorService      gservice;
	protected static IEventService               eservice;
	protected static ILoaderService              lservice;
	protected static IDeviceWatchdogService      wservice;
	protected static ValidatorService            validator;
	protected static INexusFileFactory           fileFactory;

	@BeforeClass
	public static void create() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		eservice = ServiceTestHelper.getEventService();
		dservice  = ServiceTestHelper.getRunnableDeviceService();
		fileFactory = ServiceTestHelper.getNexusFileFactory();

		// Create an object for the servlet
		/**
		 *  This would be done by spring on the GDA Server
		 *  @see org.eclipse.scanning.server.servlet.AbstractJobQueueServlet
		 *  In spring we have something like:

		    {@literal <bean id="scanner" class="org.eclipse.scanning.server.servlet.ScanServlet">}
		    {@literal    <property name="broker"      value="tcp://p45-control:61616" />}
		    {@literal    <property name="submitQueue" value="uk.ac.diamond.p45.submitQueue" />}
		    {@literal    <property name="statusSet"   value="uk.ac.diamond.p45.statusSet"   />}
		    {@literal    <property name="statusTopic" value="uk.ac.diamond.p45.statusTopic" />}
		    {@literal </bean>}

		 */
		servlet = new ScanServlet();
		servlet.setBroker(uri.toString());
		servlet.connect(); // Gets called by Spring automatically
	}

	@AfterClass
	public static void disconnect()  throws Exception {
		try {
			servlet.getJobQueue().clearQueue();
			servlet.getJobQueue().clearRunningAndCompleted();
		} catch (Exception ignored) {
			// Not fatal if cannot clean them
		}
		servlet.disconnect();
	}

	protected String path;
	private boolean requireFile;

	public AbstractScanCommandsTest(boolean requireFile) {
		this.requireFile = requireFile;
	}

	@Before
	public void before() throws Exception {

		if (requireFile) {
			File output = File.createTempFile("test_nexus", ".nxs");
			output.deleteOnExit();
			path = output.getAbsolutePath().replace("\\\\", "\\").replace('\\', '/');
		}

		servlet.getJobQueue().clearQueue();
		servlet.getJobQueue().clearRunningAndCompleted();

		if (pjobQueue!=null) {
			pjobQueue.clearQueue();
			pjobQueue.clearRunningAndCompleted();
		}
	}

	protected List<ScanBean> runAndCheck(String name, boolean blocking, long maxScanTimeS) throws Exception {

		final IEventService eservice = Services.getEventService();

		// Let's listen to the scan and see if things happen when we run it
		final ISubscriber<IScanListener> subscriber = eservice.createSubscriber(new URI(servlet.getBroker()), servlet.getStatusTopic());
		final ISubmitter<ScanBean>       submitter  = eservice.createSubmitter(new URI(servlet.getBroker()),  servlet.getSubmitQueue());

		try {
			final List<ScanBean> beans = new ArrayList<>(13);
			final List<ScanBean> failed = new ArrayList<>(13);
			final List<ScanBean> startEvents = new ArrayList<>(13);

			final CountDownLatch latch = new CountDownLatch(1);
			subscriber.addListener(new IScanListener() {
				@Override
				public void scanEventPerformed(ScanEvent evt) {
					if (evt.getBean().getStatus()==Status.FAILED) failed.add(evt.getBean());
					if (evt.getBean().getPosition()!=null) {
						beans.add(evt.getBean());
					}
				}

				@Override
				public void scanStateChanged(ScanEvent evt) {
					if (evt.getBean().getStatus()==Status.FAILED) failed.add(evt.getBean());
					if (evt.getBean().scanStart()) {
						startEvents.add(evt.getBean()); // Should be just one
					}
	                if (evt.getBean().scanEnd()) {
				latch.countDown();
	                }
				}
			});


			// Ok done that, now we sent it off...
			pi.exec("submit("+name+", block="+(blocking?"True":"False")+", broker_uri='"+uri+"')");

			Thread.sleep(100);
			boolean ok = latch.await(maxScanTimeS, TimeUnit.SECONDS);
			if (!ok) throw new Exception("The latch broke before the scan finished!");

			if (failed.size()>0) throw new Exception(failed.get(0).getMessage());

			ScanBean start = startEvents.get(0);
			assertEquals(start.getSize(), beans.size());
			assertEquals(1, startEvents.size());

			return beans;

		} finally {
			subscriber.disconnect();
			submitter.disconnect();
		}
	}

	protected List<ScanBean> runAndCheck(String name, String mainDetectorName, String processingDetectorName, boolean blocking, long maxScanTimeSeconds) throws Exception {

		final IEventService eservice = Services.getEventService();

		// Let's listen to the scan and see if things happen when we run it
		final ISubscriber<IScanListener> subscriber = eservice.createSubscriber(new URI(servlet.getBroker()), servlet.getStatusTopic());
		final ISubmitter<ScanBean>       submitter  = eservice.createSubmitter(new URI(servlet.getBroker()),  servlet.getSubmitQueue());

		try {
			final List<ScanBean> beans = new ArrayList<>(13);
			final List<ScanBean> failed = new ArrayList<>(13);
			final List<ScanBean> startEvents = new ArrayList<>(13);

			final CountDownLatch latch = new CountDownLatch(1);
			subscriber.addListener(new IScanListener() {
				@Override
				public void scanEventPerformed(ScanEvent evt) {
					if (evt.getBean().getStatus()==Status.FAILED) failed.add(evt.getBean());
					if (evt.getBean().getPosition()!=null) {
						beans.add(evt.getBean());
					}
				}

				@Override
				public void scanStateChanged(ScanEvent evt) {
					if (evt.getBean().getStatus()==Status.FAILED) failed.add(evt.getBean());
					if (evt.getBean().scanStart()) {
						startEvents.add(evt.getBean()); // Should be just one
					}
	                if (evt.getBean().scanEnd()) {
				latch.countDown();
	                }
				}
			});


			// Ok done that, now we sent it off...
			pi.exec("submit("+name+", block="+(blocking?"True":"False")+", broker_uri='"+uri+"')");

			Thread.sleep(200);
			boolean ok = latch.await(maxScanTimeSeconds, TimeUnit.SECONDS);
			if (!ok) throw new Exception("The latch broke before the scan finished!");

			if (failed.size()>0) throw new Exception(failed.get(0).getMessage());

			ScanBean start = startEvents.get(0);
			assertEquals(start.getSize(), beans.size());
			assertEquals(1, startEvents.size());

			Thread.sleep(100);

			// Do Some checking
			ScanClusterProcessingChecker checker = new ScanClusterProcessingChecker(fileFactory, pjobQueue);
			checker.setDetectorName(mainDetectorName);
			checker.setProcessingName(processingDetectorName);
			checker.setScannableNames(Arrays.asList("xNex", "yNex"));
			checker.setFilePath(path);

			// Check the main nexus file
			checker.checkNexusFile(2, 2);

			// Check the processing bean was submitted successfully
			checker.checkSubmittedBean(true);

			return beans;

		} finally {
			subscriber.disconnect();
			submitter.disconnect();
		}
	}

	protected List<ScanBean> runAndCheckNoPython(ScanBean bean, long maxScanTimeS) throws Exception {

		final IEventService eservice = Services.getEventService();

		// Let's listen to the scan and see if things happen when we run it
		final ISubscriber<IScanListener> subscriber = eservice.createSubscriber(new URI(servlet.getBroker()), servlet.getStatusTopic());
		final ISubmitter<ScanBean>       submitter  = eservice.createSubmitter(new URI(servlet.getBroker()),  servlet.getSubmitQueue());

		try {
			final List<ScanBean> beans       = new ArrayList<>(13);
			final List<ScanBean> startEvents = new ArrayList<>(13);
			final List<ScanBean> endEvents   = new ArrayList<>(13);
			final CountDownLatch latch       = new CountDownLatch(1);

			subscriber.addListener(new IScanListener() {
				@Override
				public void scanEventPerformed(ScanEvent evt) {
					if (evt.getBean().getPosition()!=null) {
						beans.add(evt.getBean());
					}
				}

				@Override
				public void scanStateChanged(ScanEvent evt) {
					if (evt.getBean().scanStart()) {
						startEvents.add(evt.getBean()); // Should be just one
					}
	                if (evt.getBean().scanEnd()) {
				endEvents.add(evt.getBean());
				latch.countDown();
	                }
				}
			});


			// Ok done that, now we sent it off...
			submitter.submit(bean);

			Thread.sleep(200);
			boolean ok = latch.await(maxScanTimeS, TimeUnit.SECONDS);
			if (!ok) throw new Exception("The latch broke before the scan finished!");

			assertEquals(1, startEvents.size());
			assertEquals(1, endEvents.size());

			return beans;

		} finally {
			subscriber.disconnect();
			submitter.disconnect();
		}
	}


	protected ScanBean createStepScan() throws IOException {
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");

		final ScanRequest<?> req = new ScanRequest<>();
		req.setCompoundModel(new CompoundModel(new StepModel("fred", 0, 9, 1)));
		req.setMonitorNamesPerPoint(Arrays.asList("monitor"));

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.001);
		req.putDetector("detector", dmodel);

		bean.setScanRequest(req);
		return bean;
	}


}
