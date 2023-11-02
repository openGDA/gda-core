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
package org.eclipse.scanning.test.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.event.EventTimingsHelper;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Class to test that we can send DeviceRequests and getting a response
 *
 * @author Matthew Gerring
 *
 */
public class DeviceRequestTest extends BrokerTest {

	private static final String DEVICE_NAME_FRED = "fred";
	private static final String DEVICE_NAME_MANDELBROT = "mandelbrot";
	private static final String NO_DEVICES_FOUND_MESSAGE = "There were no devices found and at least the mandelbrot example should have been!";

	private static IRunnableDeviceService dservice;
	private static IEventService eservice;
	private IRequester<DeviceRequest> requester;
	private IResponder<DeviceRequest> responder;

	@BeforeAll
	public static void createServices() {
		eservice = ServiceTestHelper.getEventService();
		dservice = ServiceTestHelper.getRunnableDeviceService();
	}

	@BeforeEach
	public void start() throws Exception {
		ServiceTestHelper.registerTestDevices();

		connect();

		EventTimingsHelper.setConnectionRetryInterval(200); // Normally 2000
		EventTimingsHelper.setReceiveTimeout(100);
	}

	@AfterEach
	public void stop() throws EventException {
		EventTimingsHelper.setConnectionRetryInterval(2000); // Normally 2000
		if (requester != null) {
			requester.disconnect();
		}
		if (responder != null) {
			responder.disconnect();
		}
	}

	protected void connect() throws Exception {
		final DeviceServlet dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.setRequestTopic(EventConstants.DEVICE_REQUEST_TOPIC);
		dservlet.setResponseTopic(EventConstants.DEVICE_RESPONSE_TOPIC);
		dservlet.connect();

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		requester = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
		requester.setTimeout(10, TimeUnit.MINUTES); // It's a test, give it a little longer. // TODO change back to SECONDS
	}

	@Test
	public void simpleSerialize() throws Exception {
		final DeviceRequest in = new DeviceRequest();
		final String json = eservice.getEventConnectorService().marshal(in);
		final DeviceRequest back = eservice.getEventConnectorService().unmarshal(json, DeviceRequest.class);
        assertTrue(in.equals(back));
	}

	// @Test
	public void testGetDevices() throws Exception {
		final DeviceRequest req = new DeviceRequest();
		final DeviceRequest res = requester.post(req);

		if (res.getDevices().isEmpty()) {
			throw new Exception(NO_DEVICES_FOUND_MESSAGE);
		}
	}

	//@Test
//	public void testGetDevicesUsingString() throws Exception {
//		final ResponseConfiguration responseConfiguration = new ResponseConfiguration(ResponseType.ONE, 1000, TimeUnit.MILLISECONDS);
//
//		final List<DeviceRequest> responses = new ArrayList<>(1);
//
//		final ISubscriber<IBeanListener<DeviceRequest>> receive = eservice.createSubscriber(uri, EventConstants.DEVICE_RESPONSE_TOPIC);
//		// Just listen to our id changing.
//		receive.addListener("726c5d29-72f8-42e3-ba0c-51d26378065e", evt -> {
//			responses.add(evt.getBean());
//			responseConfiguration.countDown();
//		});
//
//		// Manually send a string without the extra java things...
//		final String rawString = "{\"uniqueId\":\"726c5d29-72f8-42e3-ba0c-51d26378065e\",\"deviceType\":\"RUNNABLE\",\"configure\":false}";
//
//		MessageProducer producer = null;
//		Connection      send     = null;
//		Session         session  = null;
//
//		try {
//			final QueueConnectionFactory connectionFactory = (QueueConnectionFactory) eservice.getEventConnectorService().createConnectionFactory(uri);
//			send = connectionFactory.createConnection();
//
//			session = send.createSession(false, Session.AUTO_ACKNOWLEDGE);
//			final Topic topic = session.createTopic(EventConstants.DEVICE_REQUEST_TOPIC);
//
//			producer = session.createProducer(topic);
//			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
//
//			// Send the request
//			producer.send(session.createTextMessage(rawString));
//		} finally {
//			try {
//				if (session != null) {
//					session.close();
//				}
//			} catch (JMSException e) {
//				throw new EventException("Cannot close session!", e);
//			}
//		}
//
//		responseConfiguration.latch(null); // Wait or die trying
//
//		if (responses.isEmpty()) {
//			throw new Exception("There was no response identified!");
//		}
//		if (responses.get(0).getDevices().isEmpty()) {
//			throw new Exception(NO_DEVICES_FOUND_MESSAGE);
//		}
//	}


	@Test
	public void testGetNamedDeviceModel() throws Exception {
		final DeviceRequest req = new DeviceRequest();
		req.setDeviceName(DEVICE_NAME_MANDELBROT);
		final DeviceRequest res = requester.post(req);
		if (res.getDevices().size() != 1) {
			throw new Exception(NO_DEVICES_FOUND_MESSAGE);
		}
	}

	@Test
	public void testInvalidName() throws Exception {
		final DeviceRequest req = new DeviceRequest();
		req.setDeviceName(DEVICE_NAME_FRED);
		final DeviceRequest res = requester.post(req);
		if (!res.isEmpty()) {
			throw new Exception("There should have been no devices found!");
		}
	}

	@Test
	public void testMandelbrotDeviceInfo() throws Exception {
		final DeviceRequest req = new DeviceRequest();
		req.setDeviceName(DEVICE_NAME_MANDELBROT);
		final DeviceRequest res = requester.post(req);

		@SuppressWarnings("unchecked")
		final DeviceInformation<MandelbrotModel> info = (DeviceInformation<MandelbrotModel>) res.getDeviceInformation();
		assertNotNull(NO_DEVICES_FOUND_MESSAGE, info);
		assertEquals("Example mandelbrot device", info.getDescription());
		assertEquals(DeviceRole.HARDWARE, info.getDeviceRole());
		assertNull(info.getHealth()); // TODO what does this attribute mean?
		assertEquals("Example Mandelbrot", info.getLabel());
		assertEquals(1, info.getLevel());
		assertEquals(DEVICE_NAME_MANDELBROT, info.getName());
		assertEquals(DeviceState.READY, info.getState());
		assertEquals(new HashSet<>(Arrays.asList(ScanMode.SOFTWARE)), info.getSupportedScanModes());

		final IRunnableDevice<MandelbrotModel> mandy = dservice.getRunnableDevice(DEVICE_NAME_MANDELBROT);
		assertEquals(mandy.getModel(), info.getModel());
	}

	@Test
	public void testMandelbrotConfigure() throws Exception {
		final DeviceRequest req1 = new DeviceRequest();
		req1.setDeviceName(DEVICE_NAME_MANDELBROT);
		final DeviceRequest res1 = requester.post(req1);

		@SuppressWarnings("unchecked")
		final DeviceInformation<MandelbrotModel> info = (DeviceInformation<MandelbrotModel>) res1.getDeviceInformation();
		assertNotNull(NO_DEVICES_FOUND_MESSAGE, info);

		final MandelbrotModel model = info.getModel();
		model.setExposureTime(0);
		assertTrue(info.getState() == DeviceState.READY); // We do not set an exposure as part of the test.

		// Now we will reconfigure the device and send a new request
		final DeviceRequest req2 = new DeviceRequest();
		req2.setDeviceName(DEVICE_NAME_MANDELBROT);
		model.setExposureTime(100);
		model.setEscapeRadius(15);
		req2.setDeviceModel(model);
		req2.setDeviceAction(DeviceAction.CONFIGURE);

		final DeviceRequest res2 = requester.post(req2);

		@SuppressWarnings("unchecked")
		final DeviceInformation<MandelbrotModel> info2 = (DeviceInformation<MandelbrotModel>) res2.getDeviceInformation();
		assertNotNull(NO_DEVICES_FOUND_MESSAGE, info2);
		assertEquals(100, model.getExposureTime(), 1e-15); // We do not set an exposure as part of the test.
		assertEquals(15, model.getEscapeRadius(), 1e-15); // We do not set an exposure as part of the test.
		assertEquals(DeviceState.ARMED, info2.getState()); // We do not set an exposure as part of the test.
	}

	@Test
	public void testGetAvailableAxes() throws Exception {
		final DeviceRequest req = new DeviceRequest();
		req.setDeviceName("malcolm");
		final DeviceRequest res = requester.post(req);
		assertNotNull(res);
		assertEquals(1, res.size());
		final DeviceInformation<?> deviceInfo = res.getDeviceInformation();
		assertNotNull(deviceInfo);

		final List<String> availableAxes = deviceInfo.getAvailableAxes();
		assertNotNull(availableAxes);
		assertThat(availableAxes, contains("stage_x", "stage_y"));
	}

	@Test
	public void testGetDatasets() throws Exception {
		final DeviceRequest req = new DeviceRequest();
		req.setDeviceName("malcolm");
		req.setGetDatasets(true);
		final DeviceRequest res = requester.post(req);

		final MalcolmTable datasetsTable = res.getDatasets();
		assertNotNull(datasetsTable);
	}
}
