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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.device.SimpleNexusMetadataDevice;
import org.eclipse.dawnsci.nexus.template.NexusTemplate;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IDeviceWatchdogModel;
import org.eclipse.scanning.api.device.models.TopupWatchdogModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockTopupScannable;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.sequencer.watchdog.AbstractWatchdog;
import org.eclipse.scanning.sequencer.watchdog.DeviceWatchdogService;
import org.eclipse.scanning.sequencer.watchdog.TopupWatchdog;
import org.eclipse.scanning.server.servlet.ScanProcess;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

public class ScanProcessTest {

	private IRunnableDeviceService dservice;
	private IScannableDeviceService connector;
	private MockScriptService sservice;
	private INexusFileFactory fileFactory;

	@BeforeEach
	public void setUp() throws Exception {
		ServiceTestHelper.setupServices();

		sservice = ServiceTestHelper.getScriptService();
		fileFactory = ServiceTestHelper.getNexusFileFactory();
		connector = ServiceTestHelper.getScannableDeviceService();
		dservice = ServiceTestHelper.getRunnableDeviceService();

		final RunnableDeviceServiceImpl dserviceImpl = (RunnableDeviceServiceImpl) dservice;

		final MandelbrotModel model = new MandelbrotModel("p", "q");
		model.setName("mandelbrot");
		model.setExposureTime(0.00001);
		dserviceImpl.register(TestDetectorHelpers.createAndConfigureMandelbrotDetector(model));

		SimpleNexusMetadataDevice userNexusDevice = new SimpleNexusMetadataDevice("user", NexusBaseClass.NX_USER);
		final Map<String, Object> userData = new HashMap<>();
		userData.put(NXuser.NX_NAME, "John Smith");
		userData.put(NXuser.NX_ROLE, "Beamline Scientist");
		userData.put(NXuser.NX_ADDRESS, "Diamond Light Source, Didcot, Oxfordshire, OX11 0DE");
		userData.put(NXuser.NX_EMAIL, "john.smith@diamond.ac.uk");
		userData.put(NXuser.NX_FACILITY_USER_ID, "wgp76868");
		userNexusDevice.setNexusMetadata(userData);
		ServiceHolder.getNexusDeviceService().register(userNexusDevice);
	}

	@Test
	public void testScriptFilesRun() throws Exception {
		// Arrange
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();
		scanRequest.setCompoundModel(new CompoundModel(new AxialStepModel("xNex", 0, 9, 1)));

		final ScriptRequest before = new ScriptRequest();
		before.setFile("/path/to/before.py");
		before.setLanguage(ScriptLanguage.PYTHON);
		scanRequest.setBeforeScript(before);

		final ScriptRequest after = new ScriptRequest();
		after.setFile("/path/to/after.py");
		after.setLanguage(ScriptLanguage.PYTHON);
		scanRequest.setAfterScript(after);

		scanBean.setScanRequest(scanRequest);
		final ScanProcess process = new ScanProcess(scanBean, null, true);

		// Act
		process.execute();

		// Assert
		final List<ScriptRequest> scriptRequests = (sservice).getScriptRequests();
		assertThat(scriptRequests.size(), is(2));
		assertThat(scriptRequests, hasItems(before, after));
		assertThat(sservice.getNamedValue(IScriptService.VAR_NAME_SCAN_BEAN), is(equalTo(scanBean)));
		assertThat(sservice.getNamedValue(IScriptService.VAR_NAME_SCAN_REQUEST), is(equalTo(scanRequest)));
		assertThat(sservice.getNamedValue(IScriptService.VAR_NAME_SCAN_MODEL), is(instanceOf(ScanModel.class)));
		assertThat(sservice.getNamedValue(IScriptService.VAR_NAME_SCAN_PATH), is(instanceOf(IPointGenerator.class)));
	}

	@Test
	public void testSimpleNest() throws Exception {
		// Arrange
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();

		final CompoundModel cmodel = new CompoundModel(Arrays.asList(new AxialStepModel("T", 290, 291, 1), new TwoAxisGridPointsModel("xNex", "yNex", 2, 2)));
		cmodel.setRegions(Arrays.asList(new ScanRegion(new RectangularROI(0, 0, 3, 3, 0), "xNex", "yNex")));
		scanRequest.setCompoundModel(cmodel);

		final Map<String, IDetectorModel> dmodels = new HashMap<>(3);
		final MandelbrotModel model = new MandelbrotModel("xNex", "yNex");
		model.setName("mandelbrot");
		model.setExposureTime(0.001);
		dmodels.put("mandelbrot", model);
		scanRequest.setDetectors(dmodels);

		final File tmp = File.createTempFile("scan_nested_test", ".nxs");
		tmp.deleteOnExit();
		scanRequest.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.

		scanBean.setScanRequest(scanRequest);
		final ScanProcess process = new ScanProcess(scanBean, null, true);

		// Act
		process.execute();
	}

	/**
	 * A simple class wrapping a map from classes to the Mockito mock instance of that class.<br>
	 * Wrapping the map in a such a class like this allows the get method to by type safe, removing the need for casts.
	 */
	private static class Mocks {

		private final Map<Class<?>, Object> map = new HashMap<>();

		Mocks(Class<?>... klasses) {
			for (Class<?> klass : klasses) {
				map.put(klass, mock(klass));
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T get(Class<T> klass) {
			return (T) map.get(klass);
		}
	}

	/**
	 * A simple wrapper around running a {@link ScanProcess} is another thread and waiting for it to finish
	 */
	private static class ScanTask {

		private final ExecutorService executor = Executors.newSingleThreadExecutor();
		private final ScanProcess process;
		private Exception exception;

		private ScanTask(ScanProcess p) {
			process = p;
		}

		private void start() {
			final Runnable r = () -> {
				try {
					process.execute();
				} catch (EventException e) {
					exception = e;
				}
			};
			executor.execute(r);
		}

		public void awaitCompletion() throws Exception {
			executor.shutdown();
			final boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
			assertThat("Timed out waiting for ScanProcess.execute", terminated, is(true));
			if (exception != null) {
				throw exception;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Mocks setupMocks() throws Exception {
		// only used for the newer mockito based test methods in this class
		final Mocks mocks = new Mocks(IDeviceWatchdogService.class, IDeviceController.class,
				IScanService.class, IScanDevice.class, IPointGeneratorService.class,
				IPointGenerator.class, ScanPointIterator.class, IPositioner.class, ScanModel.class,
				IScriptService.class);

		// assign the mocks used more than once to local variables for ease of reading
		final IDeviceController mockDeviceController = mocks.get(IDeviceController.class);
		@SuppressWarnings("rawtypes")
		final IScanDevice mockDevice = mocks.get(IScanDevice.class);
		final IScanService mockScanService = mocks.get(IScanService.class);
		@SuppressWarnings("rawtypes")
		final IPointGenerator mockPointGen = mocks.get(IPointGenerator.class);
		final ScanPointIterator mockScanPointIterator = mocks.get(ScanPointIterator.class);

		when(mockDeviceController.getDevice()).thenReturn((IPausableDevice) mockDevice);
		when(mockDevice.getModel()).thenReturn(mocks.get(ScanModel.class)); // prevents an NPE in ScanProcess.runScript
		when(mockScanService.createScanDevice(nullable(ScanModel.class), nullable(IPublisher.class), eq(false))).thenReturn(mockDevice);
		when(mocks.get(IDeviceWatchdogService.class).create(any(IPausableDevice.class), any(ScanBean.class))).thenReturn(mockDeviceController);
		when(mockScanService.createPositioner(ScanProcess.class.getSimpleName())).thenReturn(mocks.get(IPositioner.class));
		when(mocks.get(IPointGeneratorService.class).createCompoundGenerator(any(CompoundModel.class))).thenReturn(mockPointGen);
		when(mockPointGen.size()).thenReturn(100); // these three lines required by ScanEstimator constructor
		final IPosition firstPoint = new Scalar<>("xNex", 0, 0);
		when(mockPointGen.getFirstPoint()).thenReturn(firstPoint);
		when(mockPointGen.iterator()).thenReturn(mockScanPointIterator);
		when(mockScanPointIterator.next()).thenReturn(firstPoint);

		final Services services = new Services(); // the set methods of Services actually set a static field
		services.setWatchdogService(mocks.get(IDeviceWatchdogService.class));
		services.setRunnableDeviceService(mockScanService);
		services.setScanService(mockScanService);
		services.setScriptService(mocks.get(IScriptService.class));
		services.setGeneratorService(mocks.get(IPointGeneratorService.class));

		return mocks;
	}

	private ScanBean createScanBean() {
		// only used for the newer mockito based test methods in this class
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();
		scanBean.setScanRequest(scanRequest);
		scanRequest.setCompoundModel(new CompoundModel(new AxialStepModel("xNex", 0, 9, 1)));

		final ScriptRequest beforeScript = new ScriptRequest();
		beforeScript.setFile("/path/to/before.py");
		beforeScript.setLanguage(ScriptLanguage.PYTHON);
		scanRequest.setBeforeScript(beforeScript);

		final ScriptRequest afterScript = new ScriptRequest();
		afterScript.setFile("/path/to/after.py");
		afterScript.setLanguage(ScriptLanguage.PYTHON);
		scanRequest.setAfterScript(afterScript);

		final MapPosition startPosition = new MapPosition();
		startPosition.put("z", 0);
		scanRequest.setStartPosition(startPosition);

		final MapPosition endPosition = new MapPosition();
		endPosition.put("z", 10.0);
		scanRequest.setEnd(endPosition);

		return scanBean;
	}

	@Test
	public void testTerminateMovingPosition() throws Exception {
		// Arrange
		final ScanBean scanBean = createScanBean();
		final ScanRequest scanRequest = scanBean.getScanRequest();

		final Mocks mocks = setupMocks();
		final IPositioner mockPositioner = mocks.get(IPositioner.class);
		final WaitingAnswer<Boolean> startPositionAnswer = new WaitingAnswer<>(true);
		when(mockPositioner.setPosition(scanRequest.getStartPosition())).thenAnswer(startPositionAnswer);

		// Act
		final ScanProcess scanProcess = new ScanProcess(scanBean, null, true);
		final ScanTask task = new ScanTask(scanProcess);
		task.start();
		startPositionAnswer.waitUntilCalled();
		verify(mocks.get(IPositioner.class)).setPosition(scanRequest.getStartPosition());
		scanProcess.terminate();
		startPositionAnswer.resume();
		task.awaitCompletion();

		// Assert
		verify(mocks.get(IPositioner.class)).abort();
		verify(mocks.get(IScriptService.class), never()).execute(any(ScriptRequest.class)); // no scripts run (before or after)
		verifyNoInteractions(mocks.get(IScanDevice.class)); // scan not run
		verify(mocks.get(IPositioner.class), never()).setPosition(scanRequest.getEndPosition()); // end position not moved to
		assertThat(scanBean.getStatus(), is(Status.TERMINATED));
	}

	@Test
	public void testTerminateScript() throws Exception {
		// Arrange
		final ScanBean scanBean = createScanBean();
		final ScanRequest scanRequest = scanBean.getScanRequest();

		final Mocks mocks = setupMocks();
		final WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>(null);
		final IScriptService mockScriptService = mocks.get(IScriptService.class);
		doAnswer(waitingAnswer).when(mockScriptService).execute(scanRequest.getBeforeScript());

		// Act
		// we need to run the scanProcess in another thread, so that we can call terminate in this thread
		final ScanProcess process = new ScanProcess(scanBean, null, true);
		final ScanTask task = new ScanTask(process);
		task.start();
		waitingAnswer.waitUntilCalled();

		verify(mocks.get(IPositioner.class)).setPosition(scanRequest.getStartPosition()); // start position was moved to
		verify(mockScriptService).execute(scanRequest.getBeforeScript()); // before script was called

		process.terminate();
		verify(mockScriptService).abortScripts(); // verify that scriptService.abortScripts was called by scanProcess
		waitingAnswer.resume(); // resume the answer to allow scriptService.execute and then scanProcess.execute to finish
		task.awaitCompletion();

		verifyNoInteractions(mocks.get(IScanDevice.class)); // scan not run
		verify(mockScriptService, never()).execute(scanRequest.getAfterScript()); // after script not called
		verify(mocks.get(IPositioner.class), never()).setPosition(scanRequest.getEndPosition()); // end position not moved to
		assertThat(scanBean.getStatus(), is(Status.TERMINATED));
	}

	@Test
	public void testTerminateScanAfterScriptRun() throws Exception {
		testTerminateScan(true);
	}

	@Test
	public void testTerminateScanAfterScriptNotRun() throws Exception {
		testTerminateScan(false);
	}

	/**
	 * Test the case where the scan is terminated by the user
	 *
	 * @param alwaysRunAfterScript
	 *            if <code>true</code>, we would expect the "after scan" script to be run, if <code>false</code>, we
	 *            would expect it not to be run<br>
	 * @throws Exception
	 */
	private void testTerminateScan(boolean alwaysRunAfterScript) throws Exception {
		// Arrange
		final ScanBean scanBean = createScanBean();
		final ScanRequest scanRequest = scanBean.getScanRequest();
		scanRequest.setAlwaysRunAfterScript(alwaysRunAfterScript);

		final Mocks mocks = setupMocks();

		// set up a WaitingAnswer as the answer to mockDevice.run(), so we can call scanProcess.terminate
		final WaitingAnswer<Void> runScanWaitingAnswer = new WaitingAnswer<>(null);
		doAnswer(runScanWaitingAnswer).when(mocks.get(IScanDevice.class)).run(null);

		// Act
		// we need to run the scanProcess execute method in another thread, so that we can call terminate in this thread
		final ScanProcess scanProcess = new ScanProcess(scanBean, null, true);
		final ScanTask task = new ScanTask(scanProcess);
		task.start();
		runScanWaitingAnswer.waitUntilCalled();

		verify(mocks.get(IPositioner.class)).setPosition(scanRequest.getStartPosition()); // start position was moved to
		verify(mocks.get(IScriptService.class)).execute(scanRequest.getBeforeScript()); // before script was called
		verify(mocks.get(IScanDevice.class)).run(null); // scan was run
		scanProcess.terminate();

		// Assert
		verify(mocks.get(IDeviceController.class)).abort(scanProcess.getClass().getName()); // verify that deviceController.abort was called by the scanProcess
		runScanWaitingAnswer.resume(); // resume the answer to allow deviceController.run and then scanProcess.execute to finish
		task.awaitCompletion(); // wait for end of scan

		if (alwaysRunAfterScript) {
			verify(mocks.get(IScriptService.class), times(1)).execute(scanRequest.getAfterScript()); // after script always called
		} else {
			verify(mocks.get(IScriptService.class), never()).execute(scanRequest.getAfterScript()); // after script not called
		}
		verify(mocks.get(IPositioner.class), never()).setPosition(scanRequest.getEndPosition()); // end position not moved to
		assertThat(scanBean.getStatus(), is(Status.TERMINATED));
	}

	@Test
	public void testScanFailsAfterScriptRun() throws Exception {
		testScanFails(true);
	}

	@Test
	public void testScanFailsAfterScriptNotRun() throws Exception {
		testScanFails(false);
	}

	/**
	 * Test the case where the scan fails
	 *
	 * @param alwaysRunAfterScript
	 *            if <code>true</code>, we would expect the "after scan" script to be run, if <code>false</code>, we
	 *            would expect it not to be run<br>
	 * @throws Exception
	 */
	private void testScanFails(boolean alwaysRunAfterScript) throws Exception {
		// Arrange
		final ScanBean scanBean = createScanBean();
		final ScanRequest scanRequest = scanBean.getScanRequest();
		scanRequest.setAlwaysRunAfterScript(alwaysRunAfterScript);

		final Mocks mocks = setupMocks();

		final ScanningException e = new ScanningException("Something went wrong.");
		doThrow(e).when(mocks.get(IScanDevice.class)).run(null);

		// Act
		final ScanProcess process = new ScanProcess(scanBean, null, true);
		process.execute();

		verify(mocks.get(IPositioner.class)).setPosition(scanRequest.getStartPosition()); // start position was moved to
		verify(mocks.get(IScriptService.class)).execute(scanRequest.getBeforeScript()); // before script was called
		verify(mocks.get(IScanDevice.class)).run(null); // scan was run

		if (alwaysRunAfterScript) {
			verify(mocks.get(IScriptService.class), times(1)).execute(scanRequest.getAfterScript()); // after script alwats called
		} else {
			verify(mocks.get(IScriptService.class), never()).execute(scanRequest.getAfterScript()); // after script not called
		}

		verify(mocks.get(IPositioner.class), never()).setPosition(scanRequest.getEndPosition()); // end position not moved to
		assertThat(scanBean.getStatus(), is(Status.FAILED));
	}

	/**
	 * Tests that ScanProcess.execute changes the state of the scan bean
	 *
	 * @throws Exception
	 */
	@Test
	public void testStateChanges() throws Exception {
		// Arrange
		final ScanBean scanBean = createScanBean();
		final ScanRequest scanRequest = scanBean.getScanRequest();

		// the waiting answers for before/after script, start/stop position and running the scan
		// allow us to verify the state of the scan being at each point in the scan
		final Mocks mocks = setupMocks();
		final WaitingAnswer<Void> beforeScriptAnswer = new WaitingAnswer<>(null);
		final WaitingAnswer<Boolean> startPositionAnswer = new WaitingAnswer<>(true);
		final WaitingAnswer<Void> runScanAnswer = new WaitingAnswer<>(null);
		final WaitingAnswer<Void> afterScriptAnswer = new WaitingAnswer<>(null);
		final WaitingAnswer<Boolean> endPositionAnswer = new WaitingAnswer<>(true);

		final IScriptService mockScriptService = mocks.get(IScriptService.class);
		final IPositioner mockPositioner = mocks.get(IPositioner.class);
		@SuppressWarnings("unchecked")
		final IPausableDevice<ScanModel> mockDevice = mocks.get(IScanDevice.class);
		@SuppressWarnings("unchecked")
		final IPublisher<ScanBean> mockPublisher = mock(IPublisher.class);
		when(mockPositioner.setPosition(scanRequest.getStartPosition())).thenAnswer(startPositionAnswer);
		when(mockPositioner.setPosition(scanRequest.getEndPosition())).thenAnswer(endPositionAnswer);
		doAnswer(beforeScriptAnswer).when(mockScriptService).execute(scanRequest.getBeforeScript());
		doAnswer(afterScriptAnswer).when(mockScriptService).execute(scanRequest.getAfterScript());
		doAnswer(runScanAnswer).when(mocks.get(IScanDevice.class)).run(null);
		final InOrder inOrder = inOrder(mockPositioner, mockScriptService, mockDevice);

		// Act
		final ScanProcess process = new ScanProcess(scanBean, mockPublisher, true); // Create the ScanProcess
		verify(mockPublisher).broadcast(scanBean); // verify the bean was broadcast with status PREPARING
		assertThat(scanBean.getStatus(), is(Status.PREPARING));
		assertThat(scanBean.getMessage(), is(nullValue()));

		// run the scan in another thread
		final ScanTask scanTask = new ScanTask(process);
		scanTask.start();

		// checks while moving to start position
		startPositionAnswer.waitUntilCalled();
		inOrder.verify(mockPositioner).setPosition(scanRequest.getStartPosition());
		verify(mockPublisher, times(2)).broadcast(scanBean);
		assertThat(scanBean.getStatus(), is(Status.PREPARING));
		assertThat(scanBean.getMessage(), is("Moving to start position"));
		startPositionAnswer.resume();

		// checks while running before script
		beforeScriptAnswer.waitUntilCalled();
		inOrder.verify(mockScriptService).execute(scanRequest.getBeforeScript());
		verify(mockPublisher, times(3)).broadcast(scanBean);
		assertThat(scanBean.getStatus(), is(Status.PREPARING));
		assertThat(scanBean.getMessage(), is(equalTo("Running script before.py")));
		beforeScriptAnswer.resume();

		// checks while scan is running
		runScanAnswer.waitUntilCalled();
		verify(mockPublisher, times(4)).broadcast(scanBean);
		assertThat(scanBean.getStatus(), is(Status.RUNNING));
		assertThat(scanBean.getMessage(), is(equalTo("Starting scan")));
		verify(mockDevice).configure(any(ScanModel.class)); // TODO doesn't work with inOrder, why not?
		inOrder.verify(mockDevice).run(null);
		runScanAnswer.resume();

		// checks while running after script
		afterScriptAnswer.waitUntilCalled();
		inOrder.verify(mockScriptService).execute(scanRequest.getAfterScript());
		verify(mockPublisher, times(6)).broadcast(scanBean);
		assertThat(scanBean.getStatus(), is(Status.FINISHING));
		assertThat(scanBean.getMessage(), is(equalTo("Running script after.py")));
		afterScriptAnswer.resume();

		// checks while moving to end position
		endPositionAnswer.waitUntilCalled();
		inOrder.verify(mockPositioner).setPosition(scanRequest.getEndPosition());
		verify(mockPublisher, times(7)).broadcast(scanBean);
		assertThat(scanBean.getStatus(), is(Status.FINISHING));
		assertThat(scanBean.getMessage(), is(equalTo("Moving to end position")));
		endPositionAnswer.resume();

		// check after ScanProcess.execute has finished
		verify(mockPublisher, timeout(200).times(8)).broadcast(scanBean);
		assertThat(scanBean.getStatus(), is(Status.COMPLETE));
		assertThat(scanBean.getMessage(), is(equalTo("Scan Complete")));

		scanTask.awaitCompletion();
	}

	@Test
	public void testScannableAndMonitors() throws Exception {
		// Arrange
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();

		final CompoundModel cmodel = new CompoundModel(Arrays.asList(new AxialStepModel("T", 290, 300, 2), new TwoAxisGridPointsModel("xNex", "yNex", 2, 2)));
		cmodel.setRegions(Arrays.asList(new ScanRegion(new RectangularROI(0, 0, 3, 3, 0), "xNex", "yNex")));
		scanRequest.setCompoundModel(cmodel);

		final Map<String, IDetectorModel> dmodels = new HashMap<>(3);
		final MandelbrotModel model = new MandelbrotModel("xNex", "yNex");
		model.setName("mandelbrot");
		model.setExposureTime(0.001);
		dmodels.put("mandelbrot", model);
		scanRequest.setDetectors(dmodels);
		scanRequest.setMonitorNamesPerPoint(Arrays.asList("T"));
		scanRequest.setMonitorNamesPerScan(Arrays.asList("perScanMonitor1", "perScanMonitor2", "user"));

		final File tmp = File.createTempFile("scan_nested_test", ".nxs");
		tmp.deleteOnExit();
		scanRequest.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.

		scanBean.setScanRequest(scanRequest);
		final ScanProcess process = new ScanProcess(scanBean, null, true);

		// Act
		process.execute();

		// Assert
		try (NexusFile nf = fileFactory.newNexusFile(tmp.getAbsolutePath())) {
			nf.openToRead();

			final TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			final NXroot root = (NXroot) nexusTree.getGroupNode();
			final NXentry entry = root.getEntry();
			final NXinstrument instrument = entry.getInstrument();

			// check the NXpositioner for the monitor 'T' is present
			final NXpositioner tPos = instrument.getPositioner("T");
			assertThat(tPos, is(notNullValue()));
			final IDataset tPosValueDataset = tPos.getValue();
			assertThat(tPosValueDataset, is(notNullValue()));
			assertThat(tPosValueDataset.getRank(), is(3));
			assertThat(tPosValueDataset.getShape(), is(equalTo(new int[] { 6, 2, 2 })));

			for (String perScanMonitorName : scanRequest.getMonitorNamesPerScan()) {
				if (perScanMonitorName.equals("user")) {
					final NXuser user = entry.getUser(perScanMonitorName);
					assertThat(user, is(notNullValue()));
					final SimpleNexusMetadataDevice metadataDevice =
							(SimpleNexusMetadataDevice) ServiceHolder.getNexusDeviceService().getNexusDevice(perScanMonitorName);
					final Map<String, Object> userData = metadataDevice.getNexusMetadata();
					assertEquals(userData.size(), user.getNumberOfDataNodes());
					for (Map.Entry<String, Object> metadataEntry : userData.entrySet()) {
						assertEquals(metadataEntry.getValue(), user.getString(metadataEntry.getKey()));
					}
				} else {
					final NXpositioner pos = instrument.getPositioner(perScanMonitorName);
					assertThat(pos, is(notNullValue()));
					final IDataset posValueDataset = pos.getValue();
					assertThat(posValueDataset, is(notNullValue()));
					assertThat(posValueDataset.getRank(), is(0));
					assertThat(posValueDataset.getShape(), is(new int[0]));
				}
			}

			final NXdata mandelbrot = entry.getData("mandelbrot");
			assertThat(mandelbrot, is(notNullValue()));
			assertThat(mandelbrot.getDataNode("T"), is(nullValue()));
		}
	}

	@Test
	public void testStartAndEndPos() throws Exception {
		// Arrange
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();
		scanRequest.setCompoundModel(new CompoundModel(new AxialStepModel("xNex", 0, 9, 1)));

		final MapPosition start = new MapPosition();
		start.put("p", 1.0);
		start.put("q", 2.0);
		start.put("r", 3.0);
		scanRequest.setStartPosition(start);

		final MapPosition end = new MapPosition();
		end.put("p", 6.0);
		end.put("q", 7.0);
		end.put("r", 8.0);
		scanRequest.setEnd(end);

		scanBean.setScanRequest(scanRequest);
		final ScanProcess process = new ScanProcess(scanBean, null, true);

		// Act
		process.execute();

		// Assert
		for (String scannableName : start.getNames()) {
			final Number startPos = start.getDouble(scannableName);
			final Number endPos = end.getDouble(scannableName);

			final IScannable<Number> scannable = connector.getScannable(scannableName);
			final MockScannable mockScannable = (MockScannable) scannable;

			mockScannable.verify(start.getDouble(scannableName), start);
			mockScannable.verify(end.getDouble(scannableName), end);

			final List<Number> values = mockScannable.getValues();
			assertThat(values.get(0), is(equalTo(startPos)));
			assertThat(values.get(values.size() - 1), is(equalTo(endPos)));
		}
	}

	/**
	 * A very simple watchdog class used by {@link ScanProcessTest#testWatchdogsStarted()}
	 * to test that methods invoked with {@link ScanStart} annotation are called.
	 * Note: The watchdogs registered with {@link DeviceWatchdogService} are not the template objects added to
	 * each scan. Instead they are used as templates, with a new instance being created through
	 * {@link Class#newInstance()} and the template's model set on the new instance. For this reason,
	 * we can't simply use mockito to verify that that start method is called.
	 */
	public static class AnnotatedWatchdog extends AbstractWatchdog<IDeviceWatchdogModel> {

		protected static int numWatchdogsStarted = 0;

		@Override
		public String getId() {
			return getClass().getName();
		}

		@ScanStart
		public void start() {
			numWatchdogsStarted++;
		}

		@Override
		public boolean isPausing() {
			return false;
		}
	}

	@Test
	public void testWatchdogsStarted() throws Exception {
		// Arrange
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();
		scanRequest.setCompoundModel(new CompoundModel(new AxialStepModel("xNex", 0, 9, 1)));
		scanBean.setScanRequest(scanRequest);
		final ScanProcess process = new ScanProcess(scanBean, null, true);

		final AnnotatedWatchdog watchdog1 = new AnnotatedWatchdog();
		watchdog1.setName("watchdog1");
		watchdog1.activate();
		final AnnotatedWatchdog watchdog2 = new AnnotatedWatchdog();
		watchdog2.setName("watchdog2");
		watchdog2.activate();

		// Act
		process.execute();

		// Assert
		assertThat(AnnotatedWatchdog.numWatchdogsStarted, is(2));

		// remove the watchdogs
		watchdog1.deactivate();
		watchdog2.deactivate();
	}

	@Test
	public void testTopupWatchdog() throws Exception {
		// Arrange
		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();

		final Map<String, IDetectorModel> dmodels = new HashMap<>(3);
		final MandelbrotModel model = new MandelbrotModel("xNex", "yNex");
		model.setName("mandelbrot");
		model.setExposureTime(1.0);
		dmodels.put("mandelbrot", model);
		scanRequest.setDetectors(dmodels);

		scanRequest.setCompoundModel(new CompoundModel(new AxialStepModel("xNex", 0, 3, 1)));
		scanBean.setScanRequest(scanRequest);

		final MockTopupScannable topupScannable = (MockTopupScannable) (IScannable<?>) connector.getScannable("topup");
		topupScannable.start();

		final TopupWatchdogModel topupModel = new TopupWatchdogModel();
		topupModel.setCountdownName("topup");
		topupModel.setCooloff(200);
		topupModel.setWarmup(200);
		topupModel.setTopupTime(150);
		topupModel.setPeriod(5000);

		final TopupWatchdog topupWatchdog = new TopupWatchdog(topupModel);
		topupWatchdog.activate();

		// A mock publisher to capture the bean at each stage in the scan, used to collect a set of statii
		final IPublisher<ScanBean> mockPublisher = mock(IPublisher.class);
		final Set<Status> statii = EnumSet.noneOf(Status.class);
		final Answer<Void> statusCollectingAnswer = invocation -> {
			statii.add(((StatusBean) invocation.getArgument(0)).getStatus());
			return null;
		};
		doAnswer(statusCollectingAnswer).when(mockPublisher).broadcast(any(ScanBean.class));
		final ScanProcess process = new ScanProcess(scanBean, mockPublisher, true);

		process.execute();

		assertThat(statii, containsInAnyOrder(Status.PREPARING, Status.RUNNING, Status.PAUSED, Status.RESUMED, Status.COMPLETE));
	}

	@Test
	public void testTemplates() throws Exception {
		// Arrange
		final String[] templateFilePaths = { "one.yaml", "two.yaml", "three.yaml" };
		final String templateRoot = ServiceHolder.getFilePathService().getPersistenceDir();
		final String[] resolvedFilePaths = Arrays.stream(templateFilePaths)
				.map(filePath -> templateRoot + File.separator + filePath)
				.toArray(String[]::new);

		ScanBean scanBean = new ScanBean();
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setCompoundModel(new CompoundModel(new AxialStepModel("xNex", 0, 9, 1)));
		scanRequest.setTemplateFilePaths(new HashSet<>(Arrays.asList(templateFilePaths)));

		scanBean.setScanRequest(scanRequest);
		ScanProcess process = new ScanProcess(scanBean, null, true);

		NexusTemplateService mockTemplateService = mock(NexusTemplateService.class);
		new org.eclipse.dawnsci.nexus.scan.ServiceHolder().setTemplateService(mockTemplateService);
		NexusTemplate[] mockTemplates = new NexusTemplate[templateFilePaths.length];
		for (int i = 0; i < templateFilePaths.length; i++) {
			mockTemplates[i] = mock(NexusTemplate.class);
			when(mockTemplateService.loadTemplate(resolvedFilePaths[i])).thenReturn(mockTemplates[i]);
		}

		// Act
		process.execute();

		for (int i = 0; i < templateFilePaths.length; i++) {
			verify(mockTemplateService).loadTemplate(resolvedFilePaths[i]);
			verify(mockTemplates[i]).apply(any(Tree.class));
		}
	}

	@Test
	public void testMalcolmValidation_valid() throws Exception {
		testMalcolmValidation(true);
	}

	@Test
	public void testMalcolmValidation_invalid() throws Exception {
		testMalcolmValidation(false);
	}

	public void testMalcolmValidation(boolean valid) throws Exception {
		// Arrange
		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("stage_x");
		gmodel.setxAxisPoints(5);
		gmodel.setyAxisName("stage_y");
		gmodel.setyAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0, 0, 3, 3));

		final DummyMalcolmModel dmodel = new DummyMalcolmModel();
		dmodel.setName("malcolm");
		dmodel.setExposureTime(valid ? 0.1 : -0.1); // use an negative exposure time for fail validation
		dservice.register(TestDetectorHelpers.createAndConfigureDummyMalcolmDetector(dmodel));

		final ScanBean scanBean = new ScanBean();
		final ScanRequest scanRequest = new ScanRequest();
		scanRequest.setCompoundModel(new CompoundModel(gmodel));
		scanRequest.putDetector("malcolm", dmodel);

		scanBean.setScanRequest(scanRequest);
		final ScanProcess process = new ScanProcess(scanBean, null, true);

		// Act
		process.execute();

		// process.execute doesn't throw an exception if there's a problem with the
		// scan, instead it sets the status of the scan bean to FAILED
		final Status expectedStatus = valid ? Status.COMPLETE : Status.FAILED;
		final String expectedMessage = valid ? "Scan Complete" : "The exposure time for 'malcolm' must be non-zero!";
		assertThat(process.getBean().getStatus(), is(expectedStatus));
		assertThat(process.getBean().getMessage(), is(equalTo(expectedMessage)));
	}

}
