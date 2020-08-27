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

import static java.util.Arrays.asList;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_DATASETS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_HEALTH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_STATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_AXES_TO_MOVE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_DETECTORS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_FILE_DIR;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_FILE_TEMPLATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_GENERATOR;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_META;
import static org.eclipse.scanning.api.points.models.AxialStepModel.createStaticAxialModel;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.FILE_EXTENSION_H5;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.STANDARD_MALCOLM_ERROR_STR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.malcolm.core.EpicsMalcolmModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.malcolm.core.Services;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Test;

/**
 * A test class for testing a real {@link MalcolmDevice}, but with a mock {@link IMalcolmConnection}.
 */
public class MalcolmDeviceTest extends AbstractMalcolmDeviceTest {

	@FunctionalInterface
	public interface MalcolmCall {
		void call(IMalcolmDevice malcolmDevice) throws Exception;
	}

	private static final String OUTPUT_DIR = "/path/to/ixx-1234";

	protected MalcolmModel createMalcolmModel() {
		final MalcolmModel malcolmModel = new MalcolmModel();
		malcolmModel.setName("testMalcolm");
		malcolmModel.setExposureTime(0.1);
		final List<IMalcolmDetectorModel> detectorModels = new ArrayList<>();
		detectorModels.add(new MalcolmDetectorModel("det1", 0.1, 1, true));
		detectorModels.add(new MalcolmDetectorModel("det2", 0.05, 2, true));
		detectorModels.add(new MalcolmDetectorModel("det3", 0.1, 1, false));
		detectorModels.add(new MalcolmDetectorModel("det4", 0.02, 5, true));
		malcolmModel.setDetectorModels(detectorModels);
		return malcolmModel;
	}

	private EpicsMalcolmModel createExpectedEpicsMalcolmModel(IPointGenerator<CompoundModel> pointGen, CompoundModel compoundModel,
			String outputDir, List<MalcolmDetectorInfo> detectorInfos) throws Exception {
		if (outputDir == null) {
			outputDir = Services.getFilePathService().getTempDir();
		}
		final String fileTemplate = Paths.get(outputDir).getFileName().toString() + "-%s." + FILE_EXTENSION_H5;

		// create a copy of the compound model, so that we're not cheating when comparing the
		// expected model with the one actually used
		final CompoundModel model = pointGen == null ? new CompoundModel(new StaticModel()):
					pointGen.getModel();

		final CompoundModel copiedModel = new CompoundModel(model);
		copiedModel.setDuration(0.1);
		copiedModel.setMutators(Collections.emptyList());
		pointGen = pointGenService.createCompoundGenerator(copiedModel);

		final List<String> axesToMove = pointGen.getNames(); // we don't test using non-malcolm axes
		final MalcolmTable detectorsTable = createExpectedDetectorsMalcolmTable(detectorInfos);
		final int[] breakpoints = calculateExpectedBreakpoints(compoundModel);

		return new EpicsMalcolmModel(outputDir, fileTemplate, axesToMove, pointGen, detectorsTable, breakpoints);
	}

	private int[] calculateExpectedBreakpoints(CompoundModel compoundModel) throws GeneratorException {
		if (compoundModel == null) return null;
		final List<IScanPointGeneratorModel> compoundModels = compoundModel.getModels();
		final IScanPointGeneratorModel lastModel = compoundModels.get(compoundModels.size() - 1);
		if (!(lastModel instanceof InterpolatedMultiScanModel)) return null;

		final InterpolatedMultiScanModel multiScanModel = (InterpolatedMultiScanModel) lastModel;
		final List<IScanPointGeneratorModel> concatModels = multiScanModel.getModels();

		final int[] sizes = new int[concatModels.size()];
		for (int i = 0; i < concatModels.size(); i++) {
			final IPointGenerator<?> modelPointGen = Services.getPointGeneratorService().createGenerator(concatModels.get(i));
			sizes[i] = modelPointGen.size();
		}

		return sizes;
	}

	@Test
	public void testGetDeviceState() throws Exception {
		// test some common values, so we know it returns the value from the connection and always the same
		final DeviceState[] deviceStates = new DeviceState[] {
				DeviceState.READY, DeviceState.ARMED, DeviceState.RUNNING, DeviceState.PAUSED,
				DeviceState.SEEKING, DeviceState.ABORTED, DeviceState.FAULT, DeviceState.DISABLED
		};

		for (int i = 0; i < deviceStates.length; i++) {
			// Arrange: set up mocks
			final DeviceState deviceState = deviceStates[i];
			MalcolmMessage expectedMessage = createExpectedMalcolmMessage(i, Type.GET, ATTRIBUTE_NAME_STATE);
			when(malcolmConnection.send(malcolmDevice, expectedMessage)).thenReturn(
					createExpectedMalcolmStateReply(deviceState));

			// Act / Assert
			assertThat(malcolmDevice.getDeviceState(), is(deviceState));

			// Assert: check the expected message has been sent
			verify(malcolmConnection).send(malcolmDevice, expectedMessage);
		}
	}

	@Test
	public void testGetDeviceHealth() throws Exception {
		final String[] healthValues = new String[] { "ok", "fault" }; // TODO what are the possible values?
		for (int i = 0; i < healthValues.length; i++) {
			// Arrange
			final String deviceHealth = healthValues[i];
			MalcolmMessage expectedMessage = createExpectedMalcolmMessage(i, Type.GET, ATTRIBUTE_NAME_HEALTH);
			when(malcolmConnection.send(malcolmDevice, expectedMessage)).thenReturn(
					createExpectedMalcolmHealthReply(deviceHealth));

			// Act / Assert
			assertThat(malcolmDevice.getDeviceHealth(), is(deviceHealth));

			// Assert: check the expected message has been sent
			verify(malcolmConnection).send(malcolmDevice, expectedMessage);
		}
	}

	@Test
	public void testIsDeviceBusy() throws Exception {
		// is device busy simply calls getDeviceState and returns !DeviceState.isRest()
		// test some common values, just we test that it returns the value from the connection,
		final DeviceState[] deviceStates = new DeviceState[] {
				DeviceState.READY, DeviceState.ARMED, DeviceState.RUNNING, DeviceState.PAUSED,
				DeviceState.SEEKING, DeviceState.ABORTED, DeviceState.FAULT, DeviceState.DISABLED
		};

		for (int i = 0; i < deviceStates.length; i++) {
			// Arrange: set up mocks
			final DeviceState deviceState = deviceStates[i];
			MalcolmMessage expectedMessage = createExpectedMalcolmMessage(i, Type.GET, ATTRIBUTE_NAME_STATE);
			when(malcolmConnection.send(malcolmDevice, expectedMessage)).thenReturn(
					createExpectedMalcolmStateReply(deviceState));

			// Act / Assert
			assertThat(malcolmDevice.isDeviceBusy(), is(!deviceState.isRestState()));

			// Assert: check the expected message has been sent
			verify(malcolmConnection).send(malcolmDevice, expectedMessage);
		}
	}

	@Test
	public void testValidateNoScan() throws Exception {
		// tests validating a malcolm device outside of a scan. A default point generator and fileDir should be used
		testValidate(null, null);
	}

	@Test
	public void testValidate() throws Exception {
		testValidate(createCompoundModel(), OUTPUT_DIR);
	}

	public void testValidate(CompoundModel compoundModel, String fileDir) throws Exception {
		// Arrange
		final MalcolmModel malcolmModel = createMalcolmModel();

		// create the expected EpicsMalcolmModel that should be sent to malcolm
		final IPointGenerator<CompoundModel> pointGen = compoundModel == null ? null : pointGenService.createCompoundGenerator(compoundModel);
		final EpicsMalcolmModel expectedEpicsMalcolmModel = createExpectedEpicsMalcolmModel(pointGen, compoundModel, fileDir, getExpectedMalcolmDetectorInfos());

		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		MalcolmMessage expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));

		// create the expected 'configure' message to get the default detectors
		MalcolmMessage expectedGetConfigureMessage = createExpectedMalcolmMessage(id++, Type.GET, MalcolmMethod.CONFIGURE.toString());
		when(malcolmConnection.send(malcolmDevice, expectedGetConfigureMessage)).thenReturn(
				createExpectedMalcolmGetConfigureReply());

		// create the expected validate message and configure the mock connection to reply as expected
		// note: the mock connection layer needs to reply as validate calls validateWithReturn internally
		MalcolmMessage expectedValidateMessage = createExpectedCallMessage(id++, MalcolmMethod.VALIDATE, expectedEpicsMalcolmModel);
		when(malcolmConnection.send(malcolmDevice, expectedValidateMessage)).thenReturn(
				createExpectedMalcolmOkReply(createExpectedMalcolmConfigureValidateReturnValue(expectedEpicsMalcolmModel)));

		// Act
		// pointGenerator and fileDir would be set by ScanProcess in a real scan
		malcolmDevice.setPointGenerator(pointGen);
		malcolmDevice.setOutputDir(fileDir);

		malcolmDevice.validate(malcolmModel);

		// Assert
		verify(malcolmConnection).send(malcolmDevice, expectedValidateMessage);

		// Arrange, now with an error response
		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));

		// create the expected validate message and configure the mock connection to reply as expected
		expectedValidateMessage = createExpectedCallMessage(id++, MalcolmMethod.VALIDATE, expectedEpicsMalcolmModel);
		final String errorMessage = "Invalid model";
		when(malcolmConnection.send(malcolmDevice, expectedValidateMessage)).thenReturn(createExpectedMalcolmErrorReply(errorMessage));

		// Act / Assert , this time an error should occur
		try {
			malcolmDevice.validate(malcolmModel);
			fail("A validation exception was expected");
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is(equalTo("org.eclipse.scanning.api.malcolm.MalcolmDeviceException: " + STANDARD_MALCOLM_ERROR_STR + errorMessage)));
		}

		// check that the malcolm connection received the expected validate message
		verify(malcolmConnection).send(malcolmDevice, expectedValidateMessage);
	}

	@Test
	public void testValidateWithReturnNoScan() throws Exception {
		testValidateWithReturn(false, null, null);
	}

	@Test
	public void testValidateWithReturn() throws Exception {
		testValidateWithReturn(false, createCompoundModel(), OUTPUT_DIR);
	}

	@Test
	public void testValidateWithReturnModified() throws Exception {
		testValidateWithReturn(true, createCompoundModel(), OUTPUT_DIR);
	}

	public void testValidateWithReturn(boolean modified, CompoundModel compoundModel, String fileDir) throws Exception {
		// Arrange
		final MalcolmModel malcolmModel = createMalcolmModel();

		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		final MalcolmMessage expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));

		// create the expected 'configure' message to get the default detectors
		final MalcolmMessage expectedGetConfigureMessage = createExpectedMalcolmMessage(id++, Type.GET, MalcolmMethod.CONFIGURE.toString());
		when(malcolmConnection.send(malcolmDevice, expectedGetConfigureMessage)).thenReturn(
				createExpectedMalcolmGetConfigureReply());

		// create the expected EpicsMalcolmModel that should be sent to malcolm
		final List<MalcolmDetectorInfo> expectedDetectorInfos = getExpectedMalcolmDetectorInfos();
		if (modified) {
			expectedDetectorInfos.get(2).setExposureTime(0.025);
			expectedDetectorInfos.get(2).setFramesPerStep(4);
		}
		final IPointGenerator<CompoundModel> pointGen = compoundModel == null ? null : pointGenService.createCompoundGenerator(compoundModel);
		final EpicsMalcolmModel expectedEpicsMalcolmModel = createExpectedEpicsMalcolmModel(pointGen, compoundModel, fileDir, expectedDetectorInfos);

		// create the expected validate message and configure the mock connection to reply as expected
		final MalcolmMessage expectedValidateMessage = createExpectedCallMessage(id++, MalcolmMethod.VALIDATE, expectedEpicsMalcolmModel);
		when(malcolmConnection.send(malcolmDevice, expectedValidateMessage)).thenReturn(
				createExpectedMalcolmOkReply(createExpectedMalcolmConfigureValidateReturnValue(expectedEpicsMalcolmModel)));

		// Act
		// pointGenerator and fileDir would be set by ScanProcess in a real scan
		malcolmDevice.setPointGenerator(pointGen);
		malcolmDevice.setOutputDir(fileDir);

		final IMalcolmModel result = malcolmDevice.validateWithReturn(malcolmModel);

		// Assert
		final IMalcolmModel expectedValidateReturnModel = createExpectedValidateReturnModel(malcolmModel, pointGen);
		if (modified) {
			expectedValidateReturnModel.getDetectorModels().get(2).setExposureTime(0.025);
			expectedValidateReturnModel.getDetectorModels().get(2).setFramesPerStep(4);
		}
		assertThat(result, is(equalTo(expectedValidateReturnModel)));
		verify(malcolmConnection).send(malcolmDevice, expectedValidateMessage);
	}

	private IMalcolmModel createExpectedValidateReturnModel(MalcolmModel malcolmModel, IPointGenerator<CompoundModel> pointGen) {
		// the value expected
		MalcolmModel model = new MalcolmModel(malcolmModel);
		model.setAxesToMove(pointGen != null ? pointGen.getNames() : Collections.emptyList());
		return model;
	}

	private LinkedHashMap<String, Object> createExpectedMalcolmConfigureValidateReturnValue(EpicsMalcolmModel epicsMalcolmModel) {
		// the value to be returned over the mocked epics connection layer
		final LinkedHashMap<String, Object> expectedValue = new LinkedHashMap<>();
		expectedValue.put(FIELD_NAME_GENERATOR, epicsMalcolmModel.getGenerator());
		expectedValue.put(FIELD_NAME_AXES_TO_MOVE, epicsMalcolmModel.getAxesToMove());
		expectedValue.put(FIELD_NAME_FILE_DIR, epicsMalcolmModel.getFileDir());
		expectedValue.put(FIELD_NAME_FILE_TEMPLATE, epicsMalcolmModel.getFileTemplate());
		expectedValue.put(FIELD_NAME_DETECTORS, epicsMalcolmModel.getDetectors());
		return expectedValue;
	}

	@Test
	public void testInitialize() throws Exception {
		initializeMalcolmDevice();
	}

	@Test
	public void testConfigure() throws Exception {
		testConfigure(false, createCompoundModel());
	}

	@Test
	public void testConfigureModified() throws Exception {
		testConfigure(true, createCompoundModel());
	}

	@Test
	public void testConfigureBreakpoints() throws Exception {
		final InterpolatedMultiScanModel multiScanModel = new InterpolatedMultiScanModel();
		multiScanModel.setContinuous(true);
		final AxialStepModel mainScanModel = new AxialStepModel("theta", 0.0, 180.0, 10.0);
		 // darks and flats should be (step / 2) before the start of the main scan, and the same after
		final double posBeforeMainScan = mainScanModel.getStart() - mainScanModel.getStep() / 2;
		final double posAfterMainScan = mainScanModel.getStop() + mainScanModel.getStep() / 2;
		multiScanModel.addModel(createStaticAxialModel("theta", posBeforeMainScan, 3));
		multiScanModel.addModel(mainScanModel);
		multiScanModel.addModel(createStaticAxialModel("theta", posAfterMainScan, 3));

		multiScanModel.addInterpolationPosition(new Scalar<String>("portshutter", "Closed"));
		multiScanModel.addInterpolationPosition(new Scalar<String>("portshutter", "Open"));
		multiScanModel.addInterpolationPosition(new Scalar<String>("portshutter", "Closed"));
		final CompoundModel compoundModel = new CompoundModel(Arrays.asList(multiScanModel));

		testConfigure(false, compoundModel);
	}

	public void testConfigure(boolean modified, CompoundModel compoundModel) throws Exception {
		initializeMalcolmDevice();

		// Arrange
		final MalcolmModel malcolmModel = createMalcolmModel();
		final String fileDir = OUTPUT_DIR;

		// create the expected abort, reset and configure message and configure the mock connection to reply as expected
		final MalcolmMessage expectedAbortMessage = createExpectedCallMessage(id++, MalcolmMethod.ABORT, null);
		final MalcolmMessage expectedResetMessage = createExpectedCallMessage(id++, MalcolmMethod.RESET, null);

		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		final MalcolmMessage expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute(getAxesToMove(compoundModel))));

		// create the expected 'configure' message to get the default detectors
		MalcolmMessage expectedGetConfigureMessage = createExpectedMalcolmMessage(id++, Type.GET, MalcolmMethod.CONFIGURE.toString());
		when(malcolmConnection.send(malcolmDevice, expectedGetConfigureMessage)).thenReturn(
				createExpectedMalcolmGetConfigureReply());

		// create the expected EpicsMalcolmModel that should be sent to malcolm
		final List<MalcolmDetectorInfo> detectorInfos = getExpectedMalcolmDetectorInfos();
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);
		final EpicsMalcolmModel expectedSentEpicsMalcolmModel = createExpectedEpicsMalcolmModel(pointGen, compoundModel, fileDir, detectorInfos);

		// create the expected configure message and configure the mock connection to reply as expected
		when(malcolmConnection.send(malcolmDevice, expectedAbortMessage)).thenReturn(createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedResetMessage)).thenReturn(createExpectedMalcolmOkReply(null));
		final MalcolmMessage expectedConfigureMessage = createExpectedCallMessage(id++, MalcolmMethod.CONFIGURE, expectedSentEpicsMalcolmModel);
		final IPointGenerator<CompoundModel> expectedReceivedPointGen = modified ? createLineGenerator() : pointGen;
		final EpicsMalcolmModel expectedReceivedEpicsMalcolmModel = createExpectedEpicsMalcolmModel(expectedReceivedPointGen, compoundModel, fileDir, detectorInfos);
		when(malcolmConnection.send(malcolmDevice, expectedConfigureMessage)).thenReturn(createExpectedMalcolmOkReply(
				createExpectedMalcolmConfigureValidateReturnValue(expectedReceivedEpicsMalcolmModel)));

		// Act
		// pointGenerator and fileDir would be set by ScanProcess in a real scan
		ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setFilePath(OUTPUT_DIR + ".nxs");
		((AbstractMalcolmDevice) malcolmDevice).configureScan(scanModel);

		malcolmDevice.configure(malcolmModel);

		// Assert - check that the expected messages were sent on the connection layer
		verify(malcolmConnection).send(malcolmDevice, expectedAbortMessage);
		verify(malcolmConnection).send(malcolmDevice, expectedResetMessage);
		verify(malcolmConnection).send(malcolmDevice, expectedConfigureMessage);

		// test duration of pointGen's model has been set to exposure time of malcolm model
		assertThat(pointGen.getModel().getDuration(), is(equalTo(malcolmModel.getExposureTime())));
		assertThat(scanModel.getPointGenerator(), is(equalTo(expectedReceivedPointGen)));
	}

	private String[] getAxesToMove(CompoundModel compoundModel) {
		final List<IScanPointGeneratorModel> models = compoundModel.getModels();
		final List<String> scannableNames = models.get(models.size() - 1).getScannableNames();
		return scannableNames.toArray(new String[scannableNames.size()]);
	}

	private IPointGenerator<CompoundModel> createLineGenerator() throws Exception {
		final TwoAxisLinePointsModel lineModel = new TwoAxisLinePointsModel();
		lineModel.setPoints(18);
		lineModel.setBoundingLine(new BoundingLine(0, 0, 1, 1));

		CompoundModel compoundModel = new CompoundModel(lineModel);
		compoundModel.setDuration(0.1);
		compoundModel.setMutators(Collections.emptyList());
		return pointGenService.createCompoundGenerator(compoundModel);
	}

	private void testCall(MalcolmCall malcolmCall, MalcolmMethod method, Object params) throws Exception {
		// Arrange
		MalcolmMessage expectedRunMessage = createExpectedCallMessage(id++, method, params);
		when(malcolmConnection.send(malcolmDevice, expectedRunMessage)).thenReturn(createExpectedMalcolmOkReply(null));

		// Act
		malcolmCall.call(malcolmDevice);

		// Assert
		verify(malcolmConnection).send(malcolmDevice, expectedRunMessage);

		// Arrange, this time with an error message
		final String errorMessage = "Could not " + method; // e.g. 'Could not run'
		expectedRunMessage = createExpectedCallMessage(id++, method, params);
		when(malcolmConnection.send(malcolmDevice, expectedRunMessage)).thenReturn(createExpectedMalcolmErrorReply(errorMessage));

		// Act / Assert
		try {
			malcolmCall.call(malcolmDevice);
			fail("An exception was expected");
		} catch (MalcolmDeviceException e) {
			assertThat(e.getMessage(), is(equalTo(STANDARD_MALCOLM_ERROR_STR + errorMessage)));
		}
		verify(malcolmConnection).send(malcolmDevice, expectedRunMessage);
	}

	@Test
	public void testRun() throws Exception {
		testCall(IMalcolmDevice::run, MalcolmMethod.RUN, null);
	}

	@Test
	public void testRunWithPosition() throws Exception {
		testCall(malc -> malc.run(new Scalar<>("outer", 2, 0.5)), MalcolmMethod.RUN, null);
	}

	@Test
	public void testAbortRun() throws Exception {
		// set up a WaitingAnswer for the run method
		final MalcolmMessage expectedRunMessage = createExpectedCallMessage(id++, MalcolmMethod.RUN, null);
		final WaitingAnswer<MalcolmMessage> runAnswer = new WaitingAnswer<>(
				createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedRunMessage)).thenAnswer(runAnswer);

		// run the malcolm device in a new thread
		final AtomicReference<Exception> exception = new AtomicReference<>();
		final AtomicBoolean runReturned = new AtomicBoolean(false);
		final AtomicBoolean interruptedFlagSet = new AtomicBoolean(false);
		final Thread runThread = new Thread(() -> {
			try {
				malcolmDevice.run();
			} catch (ScanningException | InterruptedException | TimeoutException | ExecutionException e) {
				exception.set(e);
			} finally {
				runReturned.set(true);
				interruptedFlagSet.set(Thread.currentThread().isInterrupted());
			}
		});
		runThread.start();

		// wait until malcolm has called run on the connection
		runAnswer.waitUntilCalled();

		// interrupt the thread
		runThread.interrupt();

		// give some time for malcolm to return if its going to
		Thread.sleep(2000);

		// check that malcolm.run() hasn't returned and no exception has been thrown
		assertThat(runReturned.get(), is(false));
		assertThat(exception.get(), is(nullValue()));

		// call abort on the malcolm device while the run method is waiting
		final MalcolmMessage expectedAbortMessage = createExpectedCallMessage(id++, MalcolmMethod.ABORT, null);
		when(malcolmConnection.send(malcolmDevice, expectedAbortMessage)).thenReturn(createExpectedMalcolmOkReply(null));

		malcolmDevice.abort();

		// release the run method (i.e. simulate malcolm run method return)
		runAnswer.resume();

		// give some time for malcolm to return if its going to
		Thread.sleep(2000);

		assertThat(runReturned.get(), is(true));
		assertThat(exception.get(), is(nullValue()));
		assertThat(interruptedFlagSet.get(), is(true));

		verify(malcolmConnection).send(malcolmDevice, expectedRunMessage);
	}

	@Test
	public void testSeek() throws Exception {
		final int seekToStepNum = 31;
		LinkedHashMap<String, Integer> expectedSeekParams = new LinkedHashMap<>();
		expectedSeekParams.put("completedSteps", seekToStepNum);

		testCall(malc -> malc.seek(seekToStepNum), MalcolmMethod.PAUSE, expectedSeekParams);
	}

	@Test
	public void testAbort() throws Exception {
		testCall(IMalcolmDevice::abort, MalcolmMethod.ABORT, null);
	}

	@Test
	public void testDisable() throws Exception {
		testCall(IMalcolmDevice::disable, MalcolmMethod.DISABLE, null);
	}

	@Test
	public void testReset() throws Exception {
		// Arrange
		MalcolmMessage expectedResetMessage = createExpectedCallMessage(id++, MalcolmMethod.RESET, null);
		when(malcolmConnection.send(malcolmDevice, expectedResetMessage)).thenReturn(createExpectedMalcolmOkReply(null));

		// Act
		malcolmDevice.reset();

		// Assert
		verify(malcolmConnection).send(malcolmDevice, expectedResetMessage);
	}

	@Test
	public void testPause() throws Exception {
		testCall(IMalcolmDevice::pause, MalcolmMethod.PAUSE, null);
	}

	@Test
	public void testResume() throws Exception {
		testCall(IMalcolmDevice::resume, MalcolmMethod.RESUME, null);
	}

	@Test
	public void testDispose() throws Exception {
		initializeMalcolmDevice();

		// Act
		malcolmDevice.dispose();

		// Assert: note id of unsubscribe message should be same as original subscribe message for the same endpoint
		verify(malcolmConnection).unsubscribe(eq(malcolmDevice),
				eq(createExpectedMalcolmMessage(1, Type.UNSUBSCRIBE, null)), same(stateChangeListener));
		verify(malcolmConnection).unsubscribe(eq(malcolmDevice),
				eq(createExpectedMalcolmMessage(2, Type.UNSUBSCRIBE, null)), same(scanEventListener));
		// TODO IMalcolmConnection currently offers no unsubscribeFromConnectionStateChange method
		assertThat(malcolmDevice.isAlive(), is(false));
	}

	@Test
	public void testGetAvailableAxes() throws Exception {
		// Arrange: set up mocks
		MalcolmMessage expectedMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		String[] axesNames = new String[] { "stage_x", "stage_y" };
		StringArrayAttribute availableAxes = new StringArrayAttribute(axesNames);
		when(malcolmConnection.send(malcolmDevice, expectedMessage))
				.thenReturn(createExpectedMalcolmOkReply(availableAxes));

		// Act / Assert
		assertThat(malcolmDevice.getAvailableAxes(), contains(axesNames[0], axesNames[1]));
	}

	@Test
	public void testGetDatasets() throws Exception {
		// Arrange: set up mocks
		MalcolmMessage expectedMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_DATASETS);
		MalcolmTable datasetsTable = new MalcolmTable(); // TODO complete table?
		TableAttribute datasetsAttr = new TableAttribute();
		datasetsAttr.setValue(datasetsTable);
		datasetsAttr.setName(MalcolmConstants.ATTRIBUTE_NAME_DATASETS);
		when(malcolmConnection.send(malcolmDevice, expectedMessage))
				.thenReturn(createExpectedMalcolmOkReply(datasetsAttr));

		// Act / Assert
		assertThat(malcolmDevice.getDatasets(), is(sameInstance(datasetsTable)));
	}

	@Test
	public void testGetMalcolmDetectorInfos() throws Exception {
		// Arrange
		initializeMalcolmDevice();

		// create the expected 'configure' message to get the default detectors
		MalcolmMessage expectedGetConfigureMessage = createExpectedMalcolmMessage(id++, Type.GET, MalcolmMethod.CONFIGURE.toString());
		when(malcolmConnection.send(malcolmDevice, expectedGetConfigureMessage)).thenReturn(
				createExpectedMalcolmGetConfigureReply());

		// Act
		final List<MalcolmDetectorInfo> detectorInfos = malcolmDevice.getDetectorInfos();

		// Assert
		assertThat(detectorInfos, is(equalTo(getExpectedMalcolmDetectorInfos())));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetDeviceInformation() throws Exception {
		final IMalcolmModel model = createMalcolmModel();
		malcolmDevice.setModel(model);

		DeviceInformation<IMalcolmModel> deviceInfo = new DeviceInformation<>("malcolm");
		deviceInfo.setActivated(false);
		deviceInfo.setAlive(true);
		deviceInfo.setAvailableAxes(asList("x", "y"));
		deviceInfo.setBusy(false);
		deviceInfo.setDescription("Mock malcolm device");
		deviceInfo.setDeviceRole(DeviceRole.MALCOLM);
		deviceInfo.setHealth("fault");
		deviceInfo.setIcon("icon.png");
		deviceInfo.setLabel("Mock Malcolm");
		deviceInfo.setLevel(2);
		deviceInfo.setModel(model);
		deviceInfo.setState(DeviceState.ABORTED);
		((AbstractRunnableDevice<IMalcolmModel>) malcolmDevice).setDeviceInformation(deviceInfo);

		// Arrange: set up mocks
		initializeMalcolmDevice();

		// create replies for the mock connection for the messages that MalcolmDevice sends to populate the DeviceInformation object
		// create the expected 'configure' message to get the default detectors
		final MalcolmMessage expectedGetConfigureMessage = createExpectedMalcolmMessage(id++, Type.GET, MalcolmMethod.CONFIGURE.toString());
		final MalcolmMessage expectedGetConfigureReply = createExpectedMalcolmGetConfigureReply();
		when(malcolmConnection.send(malcolmDevice, expectedGetConfigureMessage)).thenReturn(expectedGetConfigureReply);
		final MalcolmMessage expectedGetDeviceStateMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_STATE);
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage)).thenReturn(
				createExpectedMalcolmStateReply(DeviceState.READY));
		final MalcolmMessage expectedGetDeviceHealthMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_HEALTH);
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceHealthMessage)).thenReturn(
				createExpectedMalcolmHealthReply("ok"));
		final MalcolmMessage expectedGetDeviceStateMessage2 = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_STATE);
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage2)).thenReturn(
				createExpectedMalcolmStateReply(DeviceState.READY));
		final MalcolmMessage expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));
		// create the expected meta message used to get the malcolm version
		final StringArrayAttribute metaFieldValue = new StringArrayAttribute();
		metaFieldValue.setTags(new String[] { "version:pymalcolm:4.2" });
		final MalcolmMessage expectedGetMetaMessage = createExpectedMalcolmMessage(id++, Type.GET, FIELD_NAME_META);
		when(malcolmConnection.send(malcolmDevice, expectedGetMetaMessage)).thenReturn(
				createExpectedMalcolmOkReply(metaFieldValue));
		final MalcolmMessage expectedGetConfigureMessage2 = createExpectedMalcolmMessage(id++, Type.GET, MalcolmMethod.CONFIGURE.toString());
		when(malcolmConnection.send(malcolmDevice, expectedGetConfigureMessage2)).thenReturn(expectedGetConfigureReply);

		// Act
		deviceInfo = ((AbstractRunnableDevice<IMalcolmModel>) malcolmDevice).getDeviceInformation();

		// Assert
		assertThat(deviceInfo, is(notNullValue()));
		assertThat(deviceInfo.getAvailableAxes(), is(equalTo(asList("stage_x", "stage_y"))));
		assertThat(deviceInfo.getDescription(), is(equalTo("Mock malcolm device")));
		assertThat(deviceInfo.getDeviceRole(), is(DeviceRole.MALCOLM));
		assertThat(deviceInfo.getHealth(), is(equalTo("ok")));
		assertThat(deviceInfo.getIcon(), is(equalTo("icon.png")));
		assertThat(deviceInfo.getLabel(), is(equalTo("Mock Malcolm")));
		assertThat(deviceInfo.getLevel(), is(1));
		assertThat(deviceInfo.getModel(), is(equalTo(model)));
		assertThat(deviceInfo.getName(), is(equalTo("malcolm")));
		assertThat(deviceInfo.getState(), is(DeviceState.READY));
		assertThat(deviceInfo.getMalcolmDetectorInfos(), is(equalTo(getExpectedMalcolmDetectorInfos())));
	}

}

