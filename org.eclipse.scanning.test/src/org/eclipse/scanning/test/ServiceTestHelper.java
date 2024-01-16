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
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IParserService;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.process.IPreprocessorService;
import org.eclipse.scanning.api.script.IScriptService;
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
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.watchdog.DeviceWatchdogService;
import org.eclipse.scanning.server.servlet.PreprocessorService;
import org.eclipse.scanning.test.event.BillStatusBean;
import org.eclipse.scanning.test.event.FredStatusBean;
import org.eclipse.scanning.test.scan.servlet.MockScriptService;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.utilities.scan.mock.MockOperationService;

import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.mq.activemq.ManagedActiveMQSessionService;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

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
		ServiceProvider.setService(ISessionService.class, new ManagedActiveMQSessionService());
		ServiceProvider.setService(IMarshallerService.class, createMarshallerService());
		ServiceProvider.setService(IFilePathService.class, new MockFilePathService());
		ServiceProvider.setService(IEventService.class, new EventServiceImpl(createActivemqConnectorService()));
		final IScannableDeviceService scannableDeviceService = createScannableConnectorService(remote);
		final IScanService scanService = new RunnableDeviceServiceImpl(scannableDeviceService);
		ServiceProvider.setService(IPointGeneratorService.class, new PointGeneratorService());
		ServiceProvider.setService(ILoaderService.class, new LoaderServiceImpl());
		ServiceProvider.setService(IOperationService.class, new MockOperationService());
		ServiceProvider.setService(IParserService.class, createParserService());
		ServiceProvider.setService(IRunnableDeviceService.class, scanService);
		ServiceProvider.setService(IScanService.class, scanService);
		ServiceProvider.setService(IScannableDeviceService.class, scannableDeviceService);
		ServiceProvider.setService(IDeviceWatchdogService.class, new DeviceWatchdogService());
		ServiceProvider.setService(NexusScanFileService.class, new NexusScanFileServiceImpl());
		ServiceProvider.setService(IValidatorService.class, createValidatorService());
		ServiceProvider.setService(INexusDeviceService.class, new NexusDeviceService());
		ServiceProvider.setService(IScriptService.class, new MockScriptService());
		ServiceProvider.setService(NexusBuilderFactory.class, new DefaultNexusBuilderFactory());
		ServiceProvider.setService(NexusTemplateService.class, new NexusTemplateServiceImpl());
		ServiceProvider.setService(INexusFileFactory.class, new NexusFileFactoryHDF5());
		ServiceProvider.setService(IPreprocessorService.class, new PreprocessorService());
	}

	private static MockScannableConnector createScannableConnectorService(boolean remote) {
		IPublisher<Location> publisher = remote ? ServiceProvider.getService(IEventService.class)
				.createPublisher(BrokerTest.uri, EventConstants.POSITION_TOPIC) : null;
		return new MockScannableConnector(publisher);
	}

	private static IParserService createParserService() {
		ParserServiceImpl parserServiceImpl = new ParserServiceImpl();
		parserServiceImpl.setPointGeneratorService(ServiceProvider.getService(IPointGeneratorService.class));
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
		final IRunnableDeviceService runnableDeviceService = ServiceProvider.getService(IRunnableDeviceService.class);

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
		dummyMalcolm.setName("malcolm");
		final DeviceInformation<IMalcolmModel> malcInfo = new DeviceInformation<>();
		malcInfo.setName("malcolm");
		malcInfo.setLabel("Malcolm");
		malcInfo.setDescription("Example malcolm device");
		malcInfo.setId("org.eclipse.scanning.example.malcolm.dummyMalcolmDevice");
		dummyMalcolm.setDeviceInformation(malcInfo);
		runnableDeviceService.register(dummyMalcolm);

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
		runnableDeviceService.register(mandelbrotDetector);
	}

	private static ActivemqConnectorService createActivemqConnectorService() {
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(ServiceProvider.getService(IMarshallerService.class));
		activemqConnectorService.setFilePathService(ServiceProvider.getService(IFilePathService.class));
		activemqConnectorService.setSessionService(ServiceProvider.getService(ISessionService.class));
		return activemqConnectorService;
	}

	private static ValidatorService createValidatorService() {
		final ValidatorService validator = new ValidatorService();
		validator.setEventService(ServiceProvider.getService(IEventService.class));
		validator.setPointGeneratorService(ServiceProvider.getService(IPointGeneratorService.class));
		validator.setRunnableDeviceService(ServiceProvider.getService(IRunnableDeviceService.class));
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

}
