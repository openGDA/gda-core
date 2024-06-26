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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.detector.DarkImageDetector;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Class to test the API changes for DeviceRequest messaging.
 *
 * NOTE: Change Python messaging examples accordingly, if any of these
 * tests fail. The 'examples' package can be found in:
 * org.eclipse.scanning.example.messaging/scripts
 *
 * @author Martin Gaughran
 *
 */
public class DeviceRequestMessagingAPITest extends BrokerTest {

	private IRequester<DeviceRequest> requester;
	private DeviceServlet dservlet;

	@BeforeEach
	public void setUp() throws Exception {
		setupScannableDevices();
		setupRunnableDevices();

		connect();
	}

	protected void setupScannableDevices() {

		registerScannableDevice(new MockScannable("drt_mock_scannable", 10d, 2, "µm"));

		MockScannable x = new MockNeXusScannable("drt_mock_nexus_scannable", 0d,  3, "mm");
		x.setRealisticMove(true);
		x.setRequireSleep(false);
		x.setMoveRate(10000);

		registerScannableDevice(x);
	}

	protected void registerScannableDevice(IScannable<?> device) {
		ServiceProvider.getService(IScannableDeviceService.class).register(device);
	}

	protected void setupRunnableDevices() throws IOException, ScanningException {

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

	protected void connect() throws EventException, URISyntaxException {

		dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.connect();

		requester = ServiceProvider.getService(IEventService.class)
				.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
		requester.setTimeout(10, TimeUnit.SECONDS);
	}

	@AfterEach
	public void stop() throws EventException {
		if (requester != null) requester.disconnect();
		if (dservlet != null) dservlet.disconnect();
	}

	public String getMessageResponse(String sentJson) throws Exception {
		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		DeviceRequest req = eventService.getEventConnectorService().unmarshal(sentJson, null);
		DeviceRequest res = requester.post(req);
		return eventService.getEventConnectorService().marshal(res);
	}


	@Test
	public void testGetListOfScannables() throws Exception {

		String sentJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"SCANNABLE\"}";
		String expectedJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"SCANNABLE\",\"devices\":[{\"name\":\"drt_mock_scannable\"},{\"name\":\"drt_mock_nexus_scannable\"}]}";

		String returnedJson = getMessageResponse(sentJson);

		SubsetStatus.assertJsonContains("Failed to return all expected scannable devices.", returnedJson, expectedJson);
	}

	@Test
	public void testGetNamedScannable() throws Exception {

		String sentJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"SCANNABLE\",\"deviceName\":\"drt_mock_scannable\"}";
		String expectedJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"SCANNABLE\",\"devices\":[{\"name\":\"drt_mock_scannable\",\"level\":2,\"unit\":\"µm\",\"upper\":1000,\"lower\":-1000,\"activated\":false,\"busy\":false}]}";

		String returnedJson = getMessageResponse(sentJson);

		SubsetStatus.assertJsonContains("Failed to return named scannable device.", returnedJson, expectedJson);


		String unexpectedJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"SCANNABLE\",\"devices\":[{\"name\":\"drt_mock_scannable\",\"level\":2,\"unit\":\"µm\",\"upper\":1000,\"lower\":-1000,\"activated\":false,\"busy\":false},{\"name\":\"drt_mock_nexus_scannable\",\"level\":3,\"unit\":\"mm\",\"upper\":1000,\"lower\":-1000,\"activated\":false,\"busy\":false}]}";

		SubsetStatus.assertJsonDoesNotContain("Received more than just the named scannable device.", returnedJson, unexpectedJson);
	}

	@Test
	public void testGetListOfRunnables() throws Exception {

		String sentJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"RUNNABLE\"}";
		String expectedJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"RUNNABLE\",\"devices\":[{\"name\":\"drt_mock_mandelbrot_detector\", \"label\":\"Example Mandelbrot\", \"description\":\"Example mandelbrot device\", \"id\":\"org.eclipse.scanning.example.detector.drtMandelbrotDetector\", \"icon\":\"org.eclipse.scanning.example/icon/mandelbrot.png\"},{\"name\":\"drt_mock_dark_image_detector\", \"label\":\"Example Dark Image\", \"description\":\"Example dark image device\", \"id\":\"org.eclipse.scanning.example.detector.drtDarkImageDetector\", \"icon\":\"org.eclipse.scanning.example/icon/darkcurrent.png\"}]}";

		String returnedJson = getMessageResponse(sentJson);

		SubsetStatus.assertJsonContains("Failed to return all expected scannable devices.", returnedJson, expectedJson);
	}

	@Test
	public void testGetNamedRunnable() throws Exception {

		String sentJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"RUNNABLE\",\"deviceName\":\"drt_mock_mandelbrot_detector\"}";
		String expectedJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"RUNNABLE\",\"devices\":[{\"name\":\"drt_mock_mandelbrot_detector\", \"label\":\"Example Mandelbrot\", \"description\":\"Example mandelbrot device\", \"id\":\"org.eclipse.scanning.example.detector.drtMandelbrotDetector\", \"icon\":\"org.eclipse.scanning.example/icon/mandelbrot.png\"}]}";

		String returnedJson = getMessageResponse(sentJson);

		SubsetStatus.assertJsonContains("Failed to return named runnable device.\n", returnedJson, expectedJson);

		String unexpectedJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"RUNNABLE\",\"devices\":[{\"name\":\"drt_mock_mandelbrot_detector\", \"label\":\"Example Mandelbrot\", \"description\":\"Example mandelbrot device\", \"id\":\"org.eclipse.scanning.example.detector.drtMandelbrotDetector\", \"icon\":\"org.eclipse.scanning.example/icon/mandelbrot.png\"},{\"name\":\"drt_mock_dark_image_detector\", \"label\":\"Example Dark Image\", \"description\":\"Example dark image device\", \"id\":\"org.eclipse.scanning.example.detector.drtDarkImageDetector\", \"icon\":\"org.eclipse.scanning.example/icon/darkcurrent.png\"}]}";

		SubsetStatus.assertJsonDoesNotContain("Received more than just the named runnable device.", returnedJson, unexpectedJson);
	}

	@Test
	public void testConfigureModel() throws Exception {

		String sentJson = "{\"@type\":\"DeviceRequest\",\"configure\":\"true\",\"deviceType\":\"RUNNABLE\",\"deviceName\":\"drt_mock_mandelbrot_detector\",\"deviceAction\":\"CONFIGURE\",\"deviceModel\":{\"@type\":\"MandelbrotModel\",\"columns\":100,\"rows\":72,\"name\":\"drt_mock_mandelbrot_model\"}}";
		String expectedJson = "{\"@type\":\"DeviceRequest\",\"deviceType\":\"RUNNABLE\",\"deviceName\":\"drt_mock_mandelbrot_detector\",\"devices\":[{\"name\":\"drt_mock_mandelbrot_model\", \"model\":{\"@type\":\"MandelbrotModel\",\"columns\":100,\"rows\":72,\"name\":\"drt_mock_mandelbrot_model\"}}]}";

		String returnedJson = getMessageResponse(sentJson);

		SubsetStatus.assertJsonContains("Failed to return new model with device.\n", returnedJson, expectedJson);
	}
}
