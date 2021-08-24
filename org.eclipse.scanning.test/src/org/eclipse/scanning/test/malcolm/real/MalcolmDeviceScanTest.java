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

package org.eclipse.scanning.test.malcolm.real;

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_DATASETS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_STATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_FILENAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_PATH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_RANK;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_TYPE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_UNIQUEID;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.ATTRIBUTE_NAME_COMPLETED_STEPS;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.POSITION_COMPLETE_INTERVAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Test;
import org.mockito.Mock;

/**
 * A test that uses a {@link MalcolmDevice} in a scan.
 */
public class MalcolmDeviceScanTest extends AbstractMalcolmDeviceTest {

	private static final String UNIQUE_KEYS_DATASET_PATH = "/entry/NDAttributes/NDArrayUniqueId";

	@Mock
	private IScannableDeviceService scannableDeviceService;

	@Mock
	@SuppressWarnings("unchecked")
	private IScannable<Double> zAxisScannable = mock(IScannable.class);

	@Mock
	private IPublisher<ScanBean> publisher;

	private ScanBean scanBean = null;

	private int expectedNumPublishedBeans;

	private File outputFile;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		final Path outputDir = Paths.get(ServiceTestHelper.getFilePathService().getVisitDir());
		outputFile = Files.createTempFile(outputDir, "test_nexus", ".nxs").toFile();
		outputFile.deleteOnExit();

		when(zAxisScannable.getLevel()).thenReturn(1);
		doReturn(zAxisScannable).when(scannableDeviceService).getScannable("stage_z");
		when(zAxisScannable.getName()).thenReturn("stage_z");
		RunnableDeviceServiceImpl.setDeviceConnectorService(scannableDeviceService);
		initializeMalcolmDevice();
	}

	private ScanBean createExpectedStateChangeBean(Status status, Status previousStatus) throws Exception {
		if (scanBean == null) {
			scanBean = new ScanBean();
			scanBean.setHostName(InetAddress.getLocalHost().getHostName());
			scanBean.setExperimentId(ServiceHolder.getFilePathService().getVisit());
		}

		scanBean.setStatus(status);
		scanBean.setPreviousStatus(previousStatus);

		return new ScanBean(scanBean);
	}

	private ScanBean createExpectedCompleteStepsBean(int completedSteps) {
		scanBean.setStatus(Status.RUNNING);
		scanBean.setPreviousStatus(Status.RUNNING);

		scanBean.setPoint(completedSteps);
		scanBean.setMessage("Point " + completedSteps + " of " + scanBean.getSize());
		scanBean.setPercentComplete(((double) completedSteps / scanBean.getSize()) * 100);

		return new ScanBean(scanBean);
	}

	@Test
	public void testMalcolmScan() throws Exception {
		final IScanDevice scanner = testMalcolmScan(false);

		checkNexusFile(scanner);
	}

	@Test
	public void testMalcolmScanAborted() throws Exception {
		testMalcolmScan(true);
	}

	public IScanDevice testMalcolmScan(boolean abort) throws Exception {
		// Arrange. Use a special answer that collects
		// creates a answer that collects and clones the beans its called with - TODO move to setUp?
		final BeanCollectingAnswer<ScanBean> beanCaptor = BeanCollectingAnswer.forClass(ScanBean.class, ScanBean::new);
		doAnswer(beanCaptor).when(publisher).broadcast(any(ScanBean.class));

		// Act: create and configure the scan
		final IScanDevice scanner = createScan(abort);

		// Assert: verify the correct events have been fired while configuring the scan
		expectedNumPublishedBeans = 2;
		verify(publisher, times(expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		final List<ScanBean> beans = beanCaptor.getAllValues();
		// TODO for the moment the assertion below verifies what AcqusitionDevice currently does, so that the test passes
		// Before refactoring, these should be updated to reflect what we think the correct seqeuence is of
		// Status and DeviceState changes should be. Then the code should be refactored and fixed to pass the updated test
		assertThat(beans, hasSize(2));
		assertThat(beans.get(0), is(equalTo(createExpectedStateChangeBean(Status.SUBMITTED, null))));
		assertThat(beans.get(1), is(equalTo(createExpectedStateChangeBean(Status.PREPARING, Status.SUBMITTED))));

		// Arrange: set up the malcolm connection to respond to the run message with a WaitingAnswer
		final MalcolmMessage expectedRunMessage1 = createExpectedCallMessage(id++, MalcolmMethod.RUN, null);
		final WaitingAnswer<MalcolmMessage> run1Answer = new WaitingAnswer<>(createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedRunMessage1)).thenAnswer(run1Answer);

		// Act
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		scanner.start(null); // runs the scan in a separate thread

		// First inner scan
		run1Answer.waitUntilCalled();
		verify(publisher, times(++expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		assertThat(beans, hasSize(2));
		final ScanBean bean = beanCaptor.getValue();
		scanBean.setSize(200); // AcquisitionDevice has now set scan size and start time in the ScanBean
		scanBean.setStartTime(bean.getStartTime());
		assertThat(bean, is(equalTo(createExpectedStateChangeBean(Status.RUNNING, Status.PREPARING))));

		// Send some updates to the completed steps listener and check an updated ScanBean is published
		checkCompletedSteps(beanCaptor);

		// Pause the scan and check the correct messages are sent
		checkPauseAndResumeScan(beanCaptor, scanner);

		// set up the malcolm connection to create a respond to the second run message
		final MalcolmMessage expectedRunMessage2 = createExpectedCallMessage(id++, MalcolmMethod.RUN, null);
		final WaitingAnswer<MalcolmMessage> run2Answer = new WaitingAnswer<>(createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedRunMessage2)).thenAnswer(run2Answer);

		// Resume the Answer, will cause the MalcolmDevice.run method to return and the first inner scan to end
		run1Answer.resume();

		// Set the second inner scan to wait until released (the same WaitingAnswer object is used for both inner scans)
		run2Answer.waitUntilCalled();

		if (abort) {
			// tests aborting the scan during the second inner-scan
			testAbortScan(scanner, run2Answer);
		} else {
			final MalcolmMessage expectedResetMessage = createExpectedCallMessage(id++, MalcolmMethod.RESET, null);
			when(malcolmConnection.send(malcolmDevice, expectedResetMessage)).thenReturn(createExpectedMalcolmOkReply(null));

			// now release run and make sure the scan has finished
			run2Answer.resume();

			// Wait for the scan to finish. NOTE: increase this timeout to allow debugging
			final boolean scanCompleted = scanner.latch(10, TimeUnit.SECONDS);
			assertThat(scanCompleted, is(true));

			// Assert
			verify(malcolmConnection).send(malcolmDevice, expectedRunMessage1);
			verify(malcolmConnection).send(malcolmDevice, expectedRunMessage2);
			verify(malcolmConnection).send(malcolmDevice, expectedResetMessage);
			assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		}

		return scanner;
	}

	private void testAbortScan(final IRunnableDevice<ScanModel> scanner, final WaitingAnswer<MalcolmMessage> run2Answer) throws Exception {
		final MalcolmMessage expectedAbortMessage = createExpectedCallMessage(id++, MalcolmMethod.ABORT, null);
		when(malcolmConnection.send(malcolmDevice, expectedAbortMessage)).thenReturn(createExpectedMalcolmOkReply(null));

		scanner.abort();

		// ensure that aborting the scan doesn't cause the scan to complete before the malcolm run method has returned
		try {
			// check the scan hasn't finished by latching on to it, it should time out.
			final boolean scanCompleted = scanner.latch(5, TimeUnit.SECONDS);
			assertThat(scanCompleted, is(false)); // latch returns false if it times out, which we expect
			assertThat(scanner.getDeviceState(), is(DeviceState.ABORTED));
		} catch (Exception e) {
			// if the scan aborted, latch will rethrow the InterruptedException
			fail("Scan finished with exception: " + e.getMessage());
		}

		verify(malcolmConnection).send(malcolmDevice, expectedAbortMessage);

		final MalcolmMessage expectedResetMessage = createExpectedCallMessage(id++, MalcolmMethod.RESET, null);
		when(malcolmConnection.send(malcolmDevice, expectedResetMessage)).thenReturn(createExpectedMalcolmOkReply(null));

		// now release run and make sure the scan has finished
		run2Answer.resume();

		try {
			scanner.latch(5, TimeUnit.SECONDS); // latch will rethrow any exception AcquistionDevice.run threw
			fail("An InterruptedException was expected to be thrown");
		} catch (Exception e) {
			assertThat(e, is(instanceOf(InterruptedException.class)));
		}

		assertThat(scanner.getDeviceState(), is(DeviceState.ABORTED));
		verify(malcolmConnection).send(malcolmDevice, expectedResetMessage);
	}

	private void checkCompletedSteps(BeanCollectingAnswer<ScanBean> beanCaptor)
			throws EventException, InterruptedException {
		// fire an scan event updating last position and check the expected ScanBean is published
		Thread.sleep(MalcolmDevice.POSITION_COMPLETE_INTERVAL); // sleep for the position complete frequency, so the next event should fire

		int completedSteps = 25;
		scanEventListener.eventPerformed(createPositionEvent(completedSteps));
		verify(publisher, times(++expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		assertThat(beanCaptor.getValue(), is(equalTo(createExpectedCompleteStepsBean(completedSteps))));

		// fire another update, this one won't cause a ScanBean to be published as it occurs too soon after the previous one
		completedSteps = 30;
		scanEventListener.eventPerformed(createPositionEvent(completedSteps));
		verify(publisher, times(expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		assertThat((Collection<?>) beanCaptor.getAllValues(), is(empty()));

		// sleep for over the position complete interval before firing the next event, an ScanBean is published again this time
		Thread.sleep(POSITION_COMPLETE_INTERVAL + 10);
		completedSteps = 50;
		scanEventListener.eventPerformed(createPositionEvent(completedSteps));
		verify(publisher, times(++expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		assertThat(beanCaptor.getValue(), is(equalTo(createExpectedCompleteStepsBean(completedSteps))));
	}

	private void checkPauseAndResumeScan(BeanCollectingAnswer<ScanBean> beanCaptor, final IRunnableDevice<ScanModel> scanner) throws Exception {
		// Arrange
		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, "state")))
				.thenReturn(createExpectedMalcolmStateReply(DeviceState.RUNNING));
		when(malcolmConnection.send(malcolmDevice, createExpectedCallMessage(id++, MalcolmMethod.PAUSE, null)))
				.thenReturn(createExpectedMalcolmOkReply(null));

		// Act
		((IPausableDevice<?>) scanner).pause();

		// Assert
		expectedNumPublishedBeans += 2;
		verify(publisher, times(expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		final List<ScanBean> beans = beanCaptor.getAllValues();
		assertThat(beans, hasSize(2));
		// TODO DAQ-1410, ScanBean should only have DeviceState set to PAUSED when the MalcolmDevice sends us
		// a state change with a DeviceState of PAUSED.
		assertThat(beans.get(0), is(equalTo(createExpectedStateChangeBean(Status.PAUSED, Status.RUNNING))));
		assertThat(beans.get(1), is(equalTo(createExpectedStateChangeBean(Status.PAUSED, Status.RUNNING))));

		// send a state change event to say the malcolm device is paused, currently this is ignored
		stateChangeListener.eventPerformed(createStateChangeEvent(DeviceState.PAUSED));
		verify(publisher, times(expectedNumPublishedBeans)).broadcast(any(ScanBean.class));

		// Resume the scan and check the correct messages are sent
		// Arrange
		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, "state")))
				.thenReturn(createExpectedMalcolmStateReply(DeviceState.PAUSED));
		when(malcolmConnection.send(malcolmDevice, createExpectedCallMessage(id++, MalcolmMethod.RESUME, null)))
				.thenReturn(createExpectedMalcolmOkReply(null));

		// Act
		((IPausableDevice<?>) scanner).resume();

		// Assert
		verify(publisher, times(++expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		assertThat(beanCaptor.getValue(), is(equalTo(createExpectedStateChangeBean(
				Status.RESUMED, Status.RUNNING))));

		// send a state change event to say the malcolm device is running, currently this is ignored
		stateChangeListener.eventPerformed(createStateChangeEvent(DeviceState.RUNNING));
		verify(publisher, times(expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
	}

	private MalcolmMessage createPositionEvent(int numSteps) {
		final MalcolmMessage message = new MalcolmMessage();

		final NumberAttribute stepsAttr = new NumberAttribute();
		stepsAttr.setName(ATTRIBUTE_NAME_COMPLETED_STEPS);
		stepsAttr.setValue(numSteps);
		message.setValue(stepsAttr);

		return message;
	}

	private MalcolmMessage createStateChangeEvent(DeviceState state) {
		final MalcolmMessage message = new MalcolmMessage();
		message.setEndpoint(ATTRIBUTE_NAME_STATE);
		message.setType(Type.UPDATE);

		final ChoiceAttribute stateAttr = new ChoiceAttribute();
		stateAttr.setName(ATTRIBUTE_NAME_STATE);
		stateAttr.setValue(state.toString());
		message.setValue(stateAttr);

		return message;
	}

	private IScanDevice createScan(boolean abort) throws Exception {
		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel(); // Note stage_x and stage_y scannables controlled by malcolm
		gmodel.setxAxisName("stage_x");
		gmodel.setxAxisPoints(10);
		gmodel.setyAxisName("stage_y");
		gmodel.setyAxisPoints(10);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		final AxialStepModel stepModel = new AxialStepModel("stage_z", 0, 1, 1);
		final CompoundModel compoundModel = new CompoundModel(stepModel, gmodel);
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetector(malcolmDevice);
		if (!abort) { // don't write nexus when testing abort
			scanModel.setFilePath(outputFile.getAbsolutePath());
		}
		((AbstractMalcolmDevice) malcolmDevice).configureScan(scanModel);

		final MalcolmModel malcolmModel = createMalcolmModel();

		configureMocksForConfigure(scanModel, true);

		malcolmDevice.configure(malcolmModel);

		// Create and configure the scanner (AcquisitionDevice) this calls some method on MalcolmDevice which in turn
		// call methods in the mocked communication layer, so we need to set up replies for those
		final MalcolmMessage axesToMoveReply = createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y"));
		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES))).thenReturn(axesToMoveReply); // called from AcquisitionDevice.configure via setScannable
		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES))).thenReturn(axesToMoveReply); // called from AcquisitionDevice.configure via LocationManager and SubscanModerator constructors
		final MalcolmMessage datasetsReply = createDatasetsReply(pointGen, getExpectedMalcolmDetectorInfos(false));
		if (!abort) {
			when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_DATASETS))).thenReturn(datasetsReply);
			when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES))).thenReturn(axesToMoveReply); // called from AcquisitionDevice.configure via LocationManager and SubscanModerator constructors
		}

		return scanService.createScanDevice(scanModel, publisher);
	}

	@Override
	protected List<MalcolmDetectorInfo> getExpectedMalcolmDetectorInfos(boolean modified) {
		// in this test the malcolm device is configured with all default (0.0) exposure times
		// the malcolm device then sets these (due to the mock connector configuration)
		final List<MalcolmDetectorInfo> detectorInfos = getExpectedMalcolmDetectorInfos();
		if (!modified) {
			detectorInfos.stream().forEach(detInfo -> detInfo.setExposureTime(0.0));
		}
		return detectorInfos;
	}

	@Override
	protected MalcolmModel createMalcolmModel() {
		final MalcolmModel malcolmModel = super.createMalcolmModel();
		// set the exposure time of each detector to 0. Malcolm interprets this to mean use the maximum.
		malcolmModel.getDetectorModels().stream().forEach(det -> det.setExposureTime(0.0));
		return malcolmModel;
	}

	private MalcolmMessage createDatasetsReply(final IPointGenerator<?> pointGen, List<MalcolmDetectorInfo> detInfos) {
		final LinkedHashMap<String, Class<?>> types = new LinkedHashMap<>();
		types.put(DATASETS_TABLE_COLUMN_NAME, String.class);
		types.put(DATASETS_TABLE_COLUMN_FILENAME, String.class);
		types.put(DATASETS_TABLE_COLUMN_TYPE, String.class);
		types.put(DATASETS_TABLE_COLUMN_PATH, String.class);
		types.put(DATASETS_TABLE_COLUMN_RANK, Integer.class);
		types.put(DATASETS_TABLE_COLUMN_UNIQUEID, String.class);

		final MalcolmTable datasetsTable = new MalcolmTable(types);

		// we only write to one file, unlike a real malcolm device or DummyMalcolmDevice
		final int scanRank = pointGen.getRank();
		for (MalcolmDetectorInfo detectorInfo : detInfos) {
			final String detName = detectorInfo.getName();
			final String datasetName = detName + "." + detName; // just one dataset per detector
			final String fileName = detName + ".nxs";
			final String path = "/entry/" + detName + "/" + detName;
			datasetsTable.addRow(createDatasetRow(datasetName, fileName, MalcolmDatasetType.PRIMARY,
					path, scanRank + 2));
		}

		final String firstDetName = detInfos.get(0).getName();
		for (String axisName : pointGen.getNames()) {
			final String valueSetDataset = axisName + "." + NXpositioner.NX_VALUE + "_set";
			final String fileName = firstDetName + ".nxs";
			final String valueSetPath = "/entry/" + firstDetName + "/" + axisName + "_" + NXpositioner.NX_VALUE + "_set";
			datasetsTable.addRow(createDatasetRow(valueSetDataset, fileName, MalcolmDatasetType.POSITION_SET, valueSetPath, 1));

			final String datasetName = axisName + "." + NXpositioner.NX_VALUE;
			final String path = "/entry/" + firstDetName + "/" + axisName + "_" + NXpositioner.NX_VALUE;
			datasetsTable.addRow(createDatasetRow(datasetName, fileName, MalcolmDatasetType.POSITION_VALUE, path, scanRank + 2));
		}

		final TableAttribute datasetsAttr = new TableAttribute();
		datasetsAttr.setValue(datasetsTable);
		datasetsAttr.setName(MalcolmConstants.ATTRIBUTE_NAME_DATASETS);

		return createExpectedMalcolmOkReply(datasetsAttr);
	}

	private Map<String, Object> createDatasetRow(String name, String fileName,
			MalcolmDatasetType type, String path, int rank) {
		final Map<String, Object> datasetRow = new HashMap<>();
		datasetRow.put(DATASETS_TABLE_COLUMN_NAME, name);
		datasetRow.put(DATASETS_TABLE_COLUMN_FILENAME, fileName);
		datasetRow.put(DATASETS_TABLE_COLUMN_TYPE, type.name().toLowerCase());
		datasetRow.put(DATASETS_TABLE_COLUMN_PATH, path);
		datasetRow.put(DATASETS_TABLE_COLUMN_RANK, rank);
		datasetRow.put(DATASETS_TABLE_COLUMN_UNIQUEID, UNIQUE_KEYS_DATASET_PATH);
		return datasetRow;
	}

	private void checkNexusFile(IScanDevice scanner) throws Exception {
		final ScanModel scanModel = scanner.getModel();
		final TreeFile nexusTree = NexusTestUtils.loadNexusFile(scanModel.getFilePath(), true);
		final NXroot root = (NXroot) nexusTree.getGroupNode();
		final NXentry entry = root.getEntry();
		assertThat(entry, is(notNullValue()));
		final NXinstrument instrument = entry.getInstrument();

		final List<MalcolmDetectorInfo> detectorInfos = malcolmDevice.getDetectorInfos();
		assertThat(detectorInfos.size(), is(4));

		// check that the 'count_time' field for each detector is set according to the values returned
		// from the call to Malcolm.configure, rather than the default value of 0.0
		for (MalcolmDetectorInfo detInfo : detectorInfos) {
			final NXdetector detector = instrument.getDetector(detInfo.getName());

			assertThat(detector, is(notNullValue()));
			assertThat(detInfo.getExposureTime(), is(greaterThan(0.0))); // check the value in the detInfo is not 0.0
			assertThat(detector.getCount_timeScalar().doubleValue(), is(closeTo(detInfo.getExposureTime(), 1e-8)));
		}

		// TODO assert rest of scan file structure? Although this is already well tested elsewhere, e.g. MalcolmGridScanTest
		// Note that the datasets as defined by the 'datasets' attribute of the malcolm device (as configured by the
		// mock connector) are not actually written, as unlike DummyMalcolmDevice, MalcolmDevice does not write datasets
		// itself. Therefore they are not present in the nexus file, which seems to load succesfully with these broken links.
	}

}
