/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.dawnsci.remotedataset.test.utilities.mock.LoaderServiceMock;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IParserService;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.command.ParserServiceImpl;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.ScanningEventsClassRegistry;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.file.MockFilePathService;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.watchdog.DeviceWatchdogService;
import org.eclipse.scanning.test.event.BillStatusBean;
import org.eclipse.scanning.test.event.FredStatusBean;
import org.eclipse.scanning.test.scan.servlet.MockScriptService;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.utilities.scan.mock.MockOperationService;

import uk.ac.gda.common.activemq.test.TestSessionService;

/**
 * <p>
 * The utility class is designed to to help tests to setup services in the way OSGi would in the real application.
 * </p>
 * <p>
 * It was created for the move to Oxygen 3 TP where OSGi have restricted the use of static methods to bind services.
 * This made the existing approached used in tests very difficult.
 * </p>
 *
 * @author James Mudd
 */
public final class ServiceTestHelper {

	private ServiceTestHelper() {
		// Prevent instances
	}

	private static MarshallerService marshallerService;
	private static ActivemqConnectorService activemqConnectorService;
	private static EventServiceImpl eventServiceImpl;
	private static IScannableDeviceService scannableDeviceService;
	private static NexusScanFileService nexusScanFileService;
	private static INexusDeviceService nexusDeviceService;
	private static RunnableDeviceServiceImpl runnableDeviceService;
	private static IPointGeneratorService pointGeneratorService;
	private static ValidatorService validatorService;
	private static MockScriptService scriptService;
	private static ILoaderService loaderService;
	private static IDeviceWatchdogService watchdogService;
	private static INexusFileFactory nexusFileFactory;
	private static IFilePathService filePathService;
	private static NexusTemplateService templateService;
	private static IParserService parserService;
	private static IOperationService operationService;

	/**
	 * <p>
	 * If you write a unit test which uses services this can be called to automatically setup services like OSGi would.
	 * </p>
	 * <p>
	 * It creates instances of each service and then <i>injects</i> them into each of the "service holder" classes.
	 * Static access to the service via the holder will then work.
	 * </p>
	 */
	public static void setupServices() {
		setupServices(false);
	}

	public static void setupServices(boolean remote) {
		ScanPointGeneratorFactory.init();

		marshallerService = createMarshallerService();
		filePathService = new MockFilePathService();
		activemqConnectorService = createActivemqConnectorService();
		eventServiceImpl = new EventServiceImpl(activemqConnectorService);
		scannableDeviceService = createScannableConnectorService(remote);
		runnableDeviceService = new RunnableDeviceServiceImpl(scannableDeviceService);
		pointGeneratorService = new PointGeneratorService();
		nexusScanFileService = new NexusScanFileServiceImpl();
		nexusDeviceService = new NexusDeviceService();
		validatorService = createValidatorService();
		scriptService = new MockScriptService();
		loaderService = new LoaderServiceMock();
		watchdogService = new DeviceWatchdogService();
		nexusFileFactory = new NexusFileFactoryHDF5();
		templateService = new NexusTemplateServiceImpl();
		parserService = createParserService();
		operationService = new MockOperationService();

		setupServiceHolders();
	}

	private static MockScannableConnector createScannableConnectorService(boolean remote) {
		return new MockScannableConnector(
				remote ? eventServiceImpl.createPublisher(BrokerTest.uri, EventConstants.POSITION_TOPIC) : null);
	}

	private static void setupServiceHolders() {
		setupOESServiceHolder();
		setupOEDNServiceHolder();
		setupOEDNSServiceHolder();
		setupOESEServices();
		setupOESCServices();
		setupOESDServices();
		setupOESSSServices();
		setupOESMCServices();
	}

	private static void setupOESSSServices() {
		final org.eclipse.scanning.server.servlet.Services services = new org.eclipse.scanning.server.servlet.Services();
		services.setConnector(scannableDeviceService);
		services.setNexusDeviceService(nexusDeviceService);
		services.setEventService(eventServiceImpl);
		services.setFilePathService(filePathService);
		services.setGeneratorService(pointGeneratorService);
		services.setMessagingService(activemqConnectorService);
		services.setRunnableDeviceService(runnableDeviceService);
		services.setScanService(runnableDeviceService);
		services.setScriptService(scriptService);
		services.setValidatorService(validatorService);
		services.setWatchdogService(watchdogService);
	}

	private static void setupOESCServices() {
		final org.eclipse.scanning.command.Services services = new org.eclipse.scanning.command.Services();
		services.setEventService(eventServiceImpl);
		services.setGeneratorService(pointGeneratorService);
		services.setRunnableDeviceService(runnableDeviceService);
		services.setScannableDeviceService(scannableDeviceService);
	}

	private static void setupOESDServices() {
		final org.eclipse.scanning.device.Services services = new org.eclipse.scanning.device.Services();
		services.setScannableDeviceService(scannableDeviceService);
	}

	private static void setupOESMCServices() {
		final org.eclipse.scanning.malcolm.core.Services services = new org.eclipse.scanning.malcolm.core.Services();
		services.setFilePathService(filePathService);
		services.setPointGeneratorService(pointGeneratorService);
		services.setRunnableDeviceService(runnableDeviceService);
	}

	private static void setupOESEServices() {
		final org.eclipse.scanning.example.Services services = new org.eclipse.scanning.example.Services();
		services.setEventService(eventServiceImpl);
		services.setPointGeneratorService(pointGeneratorService);
		services.setRunnableDeviceService(runnableDeviceService);
		services.setScannableDeviceService(scannableDeviceService);
	}

	private static void setupOEDNServiceHolder() {
		final org.eclipse.dawnsci.nexus.ServiceHolder serviceHolder = new org.eclipse.dawnsci.nexus.ServiceHolder();
		serviceHolder.setNexusFileFactory(nexusFileFactory);
	}

	private static void setupOEDNSServiceHolder() {
		final org.eclipse.dawnsci.nexus.scan.ServiceHolder serviceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		serviceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());
		serviceHolder.setNexusDeviceService(nexusDeviceService);
		serviceHolder.setTemplateService(templateService);
	}

	private static void setupOESServiceHolder() {
		final org.eclipse.scanning.sequencer.ServiceHolder serviceHolder = new org.eclipse.scanning.sequencer.ServiceHolder();
		serviceHolder.setEventService(eventServiceImpl);
		serviceHolder.setNexusScanFileService(nexusScanFileService);
		serviceHolder.setNexusDeviceService(nexusDeviceService);
		serviceHolder.setFilePathService(filePathService);
		serviceHolder.setGeneratorService(pointGeneratorService);
		serviceHolder.setLoaderService(loaderService);
		serviceHolder.setMarshallerService(marshallerService);
		serviceHolder.setOperationService(operationService);
		serviceHolder.setParserService(parserService);
		serviceHolder.setRunnableDeviceService(runnableDeviceService);
		serviceHolder.setWatchdogService(watchdogService);
	}

	private static IParserService createParserService() {
		ParserServiceImpl parserServiceImpl = new ParserServiceImpl();
		parserServiceImpl.setPointGeneratorService(pointGeneratorService);
		return parserServiceImpl;
	}

	/**
	 * This creates and registers lots of test and mock devices for use in scanning tests.
	 *
	 * @throws ScanningException
	 *             if setup fails
	 * @throws IOException
	 *             if setup fails
	 */
	public static void registerTestDevices() throws ScanningException, IOException {

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.000001);
		runnableDeviceService.register(TestDetectorHelpers.createAndConfigureMockDetector(dmodel));

		MandelbrotModel model = new MandelbrotModel("xNex", "yNex");
		model.setName("mandelbrot");
		model.setExposureTime(0.00001);
		runnableDeviceService.register(TestDetectorHelpers.createAndConfigureMandelbrotDetector(model));

		model = new MandelbrotModel("xNex", "yNex");
		model.setName("m");
		model.setExposureTime(0.00001);
		runnableDeviceService.register(TestDetectorHelpers.createAndConfigureMandelbrotDetector(model));

		final DummyMalcolmDevice dummyMalcolm = new DummyMalcolmDevice();
		final DeviceInformation<IMalcolmModel> malcInfo = new DeviceInformation<>();
		malcInfo.setName("malcolm");
		malcInfo.setLabel("Malcolm");
		malcInfo.setDescription("Example malcolm device");
		malcInfo.setId("org.eclipse.scanning.example.malcolm.dummyMalcolmDevice");
		dummyMalcolm.setDeviceInformation(malcInfo);
		runnableDeviceService._register("malcolm", dummyMalcolm);

		MandelbrotDetector mandelbrotDetector = new MandelbrotDetector();
		// This comes from extension point or spring in the real world.
		final DeviceInformation<MandelbrotModel> info = new DeviceInformation<MandelbrotModel>();
		info.setName("mandelbrot");
		info.setLabel("Example Mandelbrot");
		info.setDescription("Example mandelbrot device");
		info.setId("org.eclipse.scanning.example.detector.mandelbrotDetector");
		info.setIcon("org.eclipse.scanning.example/icon/mandelbrot.png");
		mandelbrotDetector.setDeviceInformation(info);
		mandelbrotDetector.setName("mandelbrot");
		runnableDeviceService._register("mandelbrot", mandelbrotDetector);
	}

	private static ActivemqConnectorService createActivemqConnectorService() {
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(marshallerService);
		activemqConnectorService.setFilePathService(filePathService);
		activemqConnectorService.setSessionService(new TestSessionService());
		return activemqConnectorService;
	}

	private static ValidatorService createValidatorService() {
		final ValidatorService validator = new ValidatorService();
		validator.setEventService(eventServiceImpl);
		validator.setPointGeneratorService(pointGeneratorService);
		validator.setRunnableDeviceService(runnableDeviceService);
		return validator;
	}

	public static MarshallerService createMarshallerService() {
		return new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new ScanningEventsClassRegistry(),
						new ScanningTestClassRegistry(FredStatusBean.class, BillStatusBean.class)),
				Arrays.asList(new PointsModelMarshaller()));
	}

	public static EventServiceImpl getEventService() {
		return eventServiceImpl;
	}

	public static IPointGeneratorService getPointGeneratorService() {
		return pointGeneratorService;
	}

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public static IScannableDeviceService getScannableDeviceService() {
		return scannableDeviceService;
	}

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public static INexusFileFactory getNexusFileFactory() {
		return nexusFileFactory;
	}

	public static IScanService getScanService() {
		return runnableDeviceService;
	}

	public static IDeviceWatchdogService getDeviceWatchdogService() {
		return watchdogService;
	}

	public static MockScriptService getScriptService() {
		return scriptService;
	}

	public static IFilePathService getFilePathService() {
		return filePathService;
	}

	public static ValidatorService getValidatorService() {
		return validatorService;
	}
}
