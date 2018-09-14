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
package org.eclipse.scanning.test.scan.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.AbstractConsumerServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractServletTest extends BrokerTest {

	@BeforeClass
	public static void create() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();
	}

	/**
	 * The servlet started in createServlet() if any.
	 */
	protected static AbstractConsumerServlet<?> servlet;

    /**
     *
     * @return
     * @throws Exception
     */
	protected abstract <T extends StatusBean> AbstractConsumerServlet<T> createServlet() throws Exception;

	@Before
	public void before() throws Exception {
		servlet = createServlet();
		if (servlet!=null) {
			servlet.getConsumer().clearQueue();
			servlet.getConsumer().clearRunningAndCompleted();
		}
	}

	@After
	public void disconnect()  throws Exception {
		if (servlet!=null) {
			servlet.getConsumer().clearQueue();
			servlet.getConsumer().clearRunningAndCompleted();
			servlet.disconnect();
		}
	}

	protected ScanBean createStepScan() {
		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");

		final ScanRequest<?> req = new ScanRequest<>();
		req.setCompoundModel(new CompoundModel(new StepModel("xNex", 0, 9, 1)));
		req.setMonitorNamesPerPoint(Arrays.asList("monitor"));

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.0001);
		req.putDetector("detector", dmodel);

		bean.setScanRequest(req);
		return bean;
	}

	protected ScanBean createStepGridScan(int outerScanNum) throws IOException {

		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");

		final ScanRequest<?> req = new ScanRequest<IROI>();
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);

		GridModel gmodel = new GridModel();
		gmodel.setSlowAxisPoints(2);
		gmodel.setFastAxisPoints(2);
		gmodel.setBoundingBox(box);
		gmodel.setFastAxisName("xNex");
		gmodel.setSlowAxisName("yNex");

		// 2 models
		List<IScanPathModel> models = new ArrayList<>(outerScanNum+1);
		for (int i = 0; i < outerScanNum; i++) {
			models.add(new StepModel("neXusScannable"+i, 1, 2, 1));
		}
		models.add(gmodel);
		req.setCompoundModel(new CompoundModel(models.toArray(new IScanPathModel[models.size()])));
		req.setMonitorNamesPerPoint(Arrays.asList("monitor"));

		final File tmp = File.createTempFile("scan_servlet_test", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.

		// 2 detectors
		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setRealAxisName("xNex");
		mandyModel.setImaginaryAxisName("yNex");
		mandyModel.setExposureTime(0.001);
		req.putDetector("mandelbrot", mandyModel);

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.001);
		req.putDetector("detector", dmodel);

		bean.setScanRequest(req);
		return bean;
	}

	protected ScanBean createGridScan() throws IOException {


		// We write some pojos together to define the scan
		final ScanBean bean = new ScanBean();
		bean.setName("Hello Scanning World");

		final ScanRequest<?> req = new ScanRequest<IROI>();
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);

		GridModel gmodel = new GridModel();
		gmodel.setSlowAxisPoints(2);
		gmodel.setFastAxisPoints(2);
		gmodel.setBoundingBox(box);
		gmodel.setFastAxisName("xNex");
		gmodel.setSlowAxisName("yNex");

		req.setCompoundModel(new CompoundModel(gmodel));
		req.setMonitorNamesPerPoint(Arrays.asList("monitor"));

		final File tmp = File.createTempFile("scan_servlet_test", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.

		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setRealAxisName("xNex");
		mandyModel.setImaginaryAxisName("yNex");
		mandyModel.setExposureTime(0.001);
		req.putDetector("mandelbrot", mandyModel);

		bean.setScanRequest(req);
		return bean;
	}

	protected List<ScanBean> runAndCheck(ScanBean bean, long maxScanTimeS) throws Exception {

		final IEventService eservice = Services.getEventService();

		// Let's listen to the scan and see if things happen when we run it
		final ISubscriber<IScanListener> subscriber = eservice.createSubscriber(new URI(servlet.getBroker()), servlet.getStatusTopic());

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
			submit(servlet, bean);

			boolean ok = latch.await(maxScanTimeS, TimeUnit.SECONDS);
			if (!ok) throw new Exception("The latch broke before the scan finished!");

			assertEquals(1, startEvents.size());
			assertTrue(endEvents.size()>0);

			return beans;

		} finally {
			subscriber.disconnect();
		}
	}

	protected void submit(AbstractConsumerServlet<?> servlet, ScanBean bean) throws Exception {

		// Ok done that, now we sent it off...
		final ISubmitter<ScanBean> submitter  = ServiceTestHelper.getEventService().createSubmitter(new URI(servlet.getBroker()),  servlet.getSubmitQueue());
		submitter.submit(bean);
		submitter.disconnect();
	}

}
