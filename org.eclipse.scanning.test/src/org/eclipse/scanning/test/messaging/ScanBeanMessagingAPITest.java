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
package org.eclipse.scanning.test.messaging;

import static org.eclipse.scanning.api.event.EventConstants.STATUS_TOPIC;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.detector.DarkImageDetector;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ScanningTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Class to test the API changes for ScanRequest messaging.
 *
 * NOTE: Change Python messaging examples accordingly, if any of these
 * tests fail. The 'examples' package can be found in:
 * org.eclipse.scanning.example.messaging/scripts
 *
 * @author Martin Gaughran
 *
 */
public class ScanBeanMessagingAPITest extends BrokerTest {

	private MockScannableConnector connector;
	private ISubmitter<ScanBean> submitter;
	private ISubscriber<IBeanListener<StatusBean>> subscriber;
	private ScanServlet scanServlet;
	private DeviceServlet dservlet;

	@AfterEach
	public void stop() throws EventException {
		if (scanServlet != null) {
			scanServlet.getJobQueue().clearQueue();
			scanServlet.getJobQueue().clearRunningAndCompleted();
			scanServlet.getJobQueue().close();
		}

		disconnect(scanServlet);
		disconnect(submitter);
		disconnect(subscriber);
	}

	protected void setupScannableDeviceService() {

		registerScannableDevice(new MockScannable("drt_mock_scannable", 10d, 2, "µm"));

		MockScannable x = new MockNeXusScannable("drt_mock_nexus_scannable", 0d,  3, "mm");
		x.setRealisticMove(true);
		x.setRequireSleep(false);
		x.setMoveRate(10000);

		registerScannableDevice(x);
	}

	@SuppressWarnings("rawtypes")
	protected void registerScannableDevice(IScannable device) {
		connector.register(device);
	}

	protected void createDetectors() throws IOException, ScanningException {

		MandelbrotDetector mandy = new MandelbrotDetector();
		final DeviceInformation<MandelbrotModel> info = new DeviceInformation<MandelbrotModel>(); // This comes from extension point or spring in the real world.
		info.setName("drt_mock_mandelbrot_detector");
		info.setLabel("Example Mandelbrot");
		info.setDescription("Example mandelbrot device");
		info.setId("org.eclipse.scanning.example.detector.drtMandelbrotDetector");
		info.setIcon("org.eclipse.scanning.example/icon/mandelbrot.png");
		mandy.setName("drt_mock_mandelbrot_detector");
		mandy.setDeviceInformation(info);
		registerRunnableDevice(mandy);

		DarkImageDetector dandy = new DarkImageDetector();
		final DeviceInformation<DarkImageModel> info2 = new DeviceInformation<DarkImageModel>(); // This comes from extension point or spring in the real world.
		info2.setName("drt_mock_dark_image_detector");
		info2.setLabel("Example Dark Image");
		info2.setDescription("Example dark image device");
		info2.setId("org.eclipse.scanning.example.detector.drtDarkImageDetector");
		info2.setIcon("org.eclipse.scanning.example/icon/darkcurrent.png");
		dandy.setName("drt_mock_dark_image_detector");
		dandy.setDeviceInformation(info2);
		registerRunnableDevice(dandy);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void registerRunnableDevice(IRunnableDevice device) {
		ServiceProvider.getService(IRunnableDeviceService.class).register(device);
	}

	protected void startScanServlet() throws EventException, URISyntaxException {

		scanServlet = new ScanServlet();
		/*
		 *  Unique per JVM -> one MVStore per JVM
		 *  Cleared up by BrokerTest @AfterClass storeClose
		 */
		scanServlet.setSubmitQueue(ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID);
		scanServlet.setStatusTopic(STATUS_TOPIC);
		scanServlet.setBroker(uri.toString());
		scanServlet.setPauseOnStart(false);
		scanServlet.connect();

		dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.connect();

		submitter = ServiceProvider.getService(IEventService.class)
				.createSubmitter(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID);
	}

	protected void disconnect(IConnection service) throws EventException {
		if (service!=null) service.disconnect();
	}

	public List<String> getMessageResponses(String sentJson, int messageNum) throws Exception {

		ScanBean sentBean = ServiceProvider.getService(IEventService.class)
				.getEventConnectorService().unmarshal(sentJson, null);

		final List<StatusBean> beans = new ArrayList<>(messageNum);
		final CountDownLatch latch = new CountDownLatch(messageNum);

		IBeanListener<StatusBean> listener = new IBeanListener<StatusBean>() {

			@Override
			public void beanChangePerformed(BeanEvent<StatusBean> evt) {
				beans.add(evt.getBean());
				latch.countDown();
			}
		};

		subscriber.addListener(listener);

		submitter.submit(sentBean);

		boolean ok = latch.await(25, TimeUnit.SECONDS);

		if (!ok) throw new Exception("The latch broke before the scan responded!");

		if (beans.size() == 0) throw new Exception("No scan responses have been found!");

		final List<String> jsonList = new ArrayList<>(messageNum);

		for (StatusBean bean : beans) {
			String json = ServiceProvider.getService(IEventService.class)
					.getEventConnectorService().marshal(bean);
			jsonList.add(json);
		}

		// More elements can be added after latch breaking, but we only want first messageNum.
		return jsonList.subList(0, messageNum);
	}

	public File getTempFile() throws IOException {
		final File file = File.createTempFile("scan_api_test", ".nxs");
		System.err.println("Writing to file " + file);
		file.deleteOnExit();
		return file;
	}

	@Test
	public void testBasicScan() throws Exception {
		createDetectors(); // These aren't in the @Before method as only this method requires them
		startScanServlet();

		subscriber = ServiceProvider.getService(IEventService.class).createSubscriber(uri, STATUS_TOPIC);

		File tempfile = getTempFile();

		String sentJson = "{\"@type\":\"ScanBean\","
				+ "\"uniqueId\":\"c8f12aee-d56a-49f6-bc03-9c7de9415674\","
				+ "\"status\":\"SUBMITTED\","
				+ "\"percentComplete\":0.0,"
				+ "\"submissionTime\":1441796619734,"
				+ "\"scanRequest\":{\"@type\":\"ScanRequest\","
								 + "\"compoundModel\":{\"@type\":\"CompoundModel\","
								 + "\"models\":[{\"@type\":\"TwoAxisGridPointsModel\","
											  + "\"name\":\"Grid\","
											  + "\"boundingBox\":{\"@type\":\"BoundingBox\","
															   + "\"xAxisName\":\"stage_x\","
															   + "\"yAxisName\":\"stage_y\","
															   + "\"xAxisStart\":0.0,"
															   + "\"xAxisLength\":3.0,"
															   + "\"yAxisStart\":0.0,"
															   + "\"yAxisLength\":3.0"
															   + "},"
											  + "\"xAxisName\":\"stage_x\","
											  + "\"yAxisName\":\"stage_y\","
											  + "\"xAxisPoints\":5,"
											  + "\"yAxisPoints\":5,"
											  + "\"alternating\":false"
								 + "}]},"
								 + "\"ignorePreprocess\":false,"
								 + "\"filePath\":\"" + tempfile.getAbsolutePath().replace('\\', '/') + "\","
								 + "\"detectors\":{\"drt_mock_mandelbrot_detector\":{\"@type\":\"MandelbrotModel\","
																+ "\"columns\":301,"
																+ "\"enableNoise\":false,"
																+ "\"escapeRadius\":10.0,"
																+ "\"exposureTime\":0.1,"
																+ "\"imaginaryAxisName\":\"stage_y\","
																+ "\"maxImaginaryCoordinate\":1.2,"
																+ "\"maxIterations\":500,"
																+ "\"maxRealCoordinate\":1.5,"
																+ "\"name\":\"drt_mock_mandelbrot_detector\","
																+ "\"noiseFreeExposureTime\":5.0,"
																+ "\"points\":1000,"
																+ "\"realAxisName\":\"stage_x\","
																+ "\"rows\":241,"
																+ "\"saveImage\":false,"
																+ "\"saveSpectrum\":false,"
																+ "\"saveValue\":true,"
																+ "\"timeout\":0}"
								 + "}}},"
				+ "\"point\":0,"
				+ "\"size\":0,"
				+ "\"scanNumber\":0"
				+ "}";

		String expectedJson = "{\"@type\":\"ScanBean\","
				+ "\"uniqueId\":\"c8f12aee-d56a-49f6-bc03-9c7de9415674\","
				+ "\"status\":\"RUNNING\","
				+ "\"submissionTime\":1441796619734,"
				+ "\"scanRequest\":{\"@type\":\"ScanRequest\","
								 + "\"compoundModel\":{\"@type\":\"CompoundModel\","
								 + "\"models\":[{\"@type\":\"TwoAxisGridPointsModel\","
											  + "\"name\":\"Grid\","
											  + "\"boundingBox\":{\"@type\":\"BoundingBox\","
															   + "\"xAxisName\":\"stage_x\","
															   + "\"yAxisName\":\"stage_y\","
															   + "\"xAxisStart\":0.0,"
															   + "\"xAxisLength\":3.0,"
															   + "\"yAxisStart\":0.0,"
															   + "\"yAxisLength\":3.0"
															   + "},"
											  + "\"xAxisName\":\"stage_x\","
											  + "\"yAxisName\":\"stage_y\","
											  + "\"xAxisPoints\":5,"
											  + "\"yAxisPoints\":5,"
											  + "\"alternating\":false"
								 + "}]},"
								 + "\"ignorePreprocess\":false,"
								 + "\"filePath\":\"" + tempfile.getAbsolutePath().replace('\\', '/') + "\","
								 + "\"detectors\":{\"drt_mock_mandelbrot_detector\":{\"@type\":\"MandelbrotModel\","
																+ "\"columns\":301,"
																+ "\"enableNoise\":false,"
																+ "\"escapeRadius\":10.0,"
																+ "\"exposureTime\":0.1,"
																+ "\"imaginaryAxisName\":\"stage_y\","
																+ "\"maxImaginaryCoordinate\":1.2,"
																+ "\"maxIterations\":500,"
																+ "\"maxRealCoordinate\":1.5,"
																+ "\"name\":\"drt_mock_mandelbrot_detector\","
																+ "\"noiseFreeExposureTime\":5.0,"
																+ "\"points\":1000,"
																+ "\"realAxisName\":\"stage_x\","
																+ "\"rows\":241,"
																+ "\"saveImage\":false,"
																+ "\"saveSpectrum\":false,"
																+ "\"saveValue\":true,"
																+ "\"timeout\":0}"
								 + "}}},"
				+ "}";

		// If this can't get 8 messages, something must be wrong!
		List<String> returnedJsonList = getMessageResponses(sentJson, 8);


		// I only care if at least one of the returned messages has the correct response.
		boolean messageExists = false;

		for (String returnedJson : returnedJsonList) {
			if (new SubsetStatus(expectedJson, returnedJson).isSubset()) {
				messageExists = true;
				break;
			}
		}

		assertTrue("Failed to return correct scan response.", messageExists);

		assertTrue("Scan file has zero length.", tempfile.length() != 0);
	}

	@Test
	public void testStartEndScanMarshalling() throws Exception {
		// Let's just ensure that the 'start' and 'end' can be marshalled correctly.
		String json = "{\"@type\":\"ScanBean\","
				+ "\"uniqueId\":\"c8f12aee-d56a-49f6-bc03-9c7de9415674\","
				+ "\"status\":\"SUBMITTED\","
				+ "\"percentComplete\":0.0,"
				+ "\"submissionTime\":1441796619734,"
				+ "\"scanRequest\":{\"@type\":\"ScanRequest\","
								 + "\"compoundModel\":{\"@type\":\"CompoundModel\","
								 + "\"models\":[{\"@type\":\"TwoAxisGridPointsModel\","
											  + "\"name\":\"Grid\","
											  + "\"boundingBox\":{\"@type\":\"BoundingBox\","
															   + "\"xAxisName\":\"stage_x\","
															   + "\"yAxisName\":\"stage_y\","
															   + "\"xAxisStart\":0.0,"
															   + "\"xAxisLength\":3.0,"
															   + "\"yAxisStart\":0.0,"
															   + "\"yAxisLength\":3.0"
															   + "},"
											  + "\"xAxisName\":\"stage_x\","
											  + "\"yAxisName\":\"stage_y\","
											  + "\"xAxisPoints\":5,"
											  + "\"yAxisPoints\":5,"
											  + "\"alternating\":false"
								 + "}]},"
								 + "\"startPosition\":{\"values\":{\"p\":1.0,"
														+ "\"q\":2.0,"
														+ "\"T\":290.0},"
														+ "\"indices\":{},"
														+ "\"stepIndex\":-1,"
														+ "\"dimensionNames\":[[\"p\",\"q\",\"T\"]]"
										   + "},"
								 + "\"endPosition\":{\"values\":{\"p\":6.0,"
													  + "\"q\":7.0,"
													  + "\"T\":295.0},"
													  + "\"indices\":{},"
													  + "\"stepIndex\":-1,"
													  + "\"dimensionNames\":[[\"p\",\"q\",\"T\"]]"
										 + "},"
								 + "\"ignorePreprocess\":false,"
								 + "\"filePath\":\"tempfile\","
								 + "\"detectors\":{\"drt_mock_mandelbrot_detector\":{\"@type\":\"MandelbrotModel\","
																+ "\"columns\":301,"
																+ "\"enableNoise\":false,"
																+ "\"escapeRadius\":10.0,"
																+ "\"exposureTime\":0.1,"
																+ "\"imaginaryAxisName\":\"stage_y\","
																+ "\"maxImaginaryCoordinate\":1.2,"
																+ "\"maxIterations\":500,"
																+ "\"maxRealCoordinate\":1.5,"
																+ "\"name\":\"drt_mock_mandelbrot_detector\","
																+ "\"noiseFreeExposureTime\":5.0,"
																+ "\"points\":1000,"
																+ "\"realAxisName\":\"stage_x\","
																+ "\"rows\":241,"
																+ "\"saveImage\":false,"
																+ "\"saveSpectrum\":false,"
																+ "\"saveValue\":true,"
																+ "\"timeout\":0}"
								 + "}}},"
				+ "\"point\":0,"
				+ "\"size\":0,"
				+ "\"scanNumber\":0"
				+ "}";

		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		ScanBean input = eventService.getEventConnectorService().unmarshal(json, null);
		String output = eventService.getEventConnectorService().marshal(input);

		SubsetStatus.assertJsonContains("Marshaller does not work with ScanBean with start and end positions.", output, json);
	}
}
