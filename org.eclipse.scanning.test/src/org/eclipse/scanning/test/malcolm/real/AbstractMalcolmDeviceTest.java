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

import static java.util.stream.Collectors.toList;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_HEALTH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_STATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_ENABLE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_EXPOSURE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_MRI;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_AXES_TO_MOVE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_DETECTORS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_FILE_DIR;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_FILE_TEMPLATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_GENERATOR;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.FILE_EXTENSION_H5;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionEventListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionStateListener;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethodMeta;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.EpicsMalcolmModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.malcolm.core.Services;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.common.collect.Iterables;

public abstract class AbstractMalcolmDeviceTest {

	protected IScanService scanService;
	protected IPointGeneratorService pointGenService;
	protected IMalcolmDevice malcolmDevice;

	@Mock
	protected IMalcolmConnection malcolmConnection;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	protected IMalcolmConnectionEventListener stateChangeListener;
	protected IMalcolmConnectionEventListener scanEventListener;
	protected IMalcolmConnectionStateListener connectionChangeListener;

	@Mock
	protected IMalcolmEventListener malcolmEventListener;

	protected int id = 0;

	@Before
	public void setUp() throws Exception {
		ServiceTestHelper.setupServices();
		this.scanService = ServiceTestHelper.getScanService();
		pointGenService = ServiceTestHelper.getPointGeneratorService();

		Services services = new Services();
		services.setPointGeneratorService(pointGenService);
		services.setFilePathService(ServiceTestHelper.getFilePathService());

		when(malcolmConnection.getMessageGenerator()).thenReturn(new MalcolmMessageGenerator());
		malcolmDevice = new MalcolmDevice("malcolm", malcolmConnection, scanService);
	}

	@After
	public void tearDown() throws Exception {
		malcolmDevice.dispose();
		malcolmConnection.disconnect();
	}

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

	/**
	 * Initializes the MalcolmDevice, verifying the correct methods are called. This method is here, and simply
	 * called by {@link MalcolmDeviceTest#testInitialize()} as other test methods require an initialized malcolm device.
	 * @throws Exception
	 */
	protected void initializeMalcolmDevice() throws Exception {
		// Arrange: create the expected get device state message and configure the mock to reply
		malcolmDevice.addMalcolmListener(malcolmEventListener);

		// create the expected messages that malcolm should send to the connection layer
		final MalcolmMessage expectedGetDeviceStateMessage = createExpectedMalcolmMessage(id++, Type.GET, "state");
		final MalcolmMessage expectedSubscribeToStateMessage = createExpectedMalcolmMessage(id++, Type.SUBSCRIBE, "state");
		final MalcolmMessage expectedSubscribeCompletedStepsMessage = createExpectedMalcolmMessage(id++, Type.SUBSCRIBE, "completedSteps");
		final MalcolmMessage expectedGetDeviceStateMessage2 = createExpectedMalcolmMessage(id++, Type.GET, "state");

		final DeviceState currentDeviceState = ((MalcolmDevice) malcolmDevice).getLatestDeviceState();
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage)).thenReturn(
				createExpectedMalcolmStateReply(DeviceState.READY));
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage2)).thenReturn(
				createExpectedMalcolmStateReply(DeviceState.READY));

		// Act: call initialize() on the malcolm device
		assertThat(malcolmDevice.isAlive(), is(false));
		malcolmDevice.initialize();

		// Assert: check the expected message have been send
		verify(malcolmConnection).send(malcolmDevice, expectedGetDeviceStateMessage);

		// The listeners that malcolm registers with the connection layer
		ArgumentCaptor<IMalcolmConnectionEventListener> malcolmListeners = ArgumentCaptor.forClass(IMalcolmConnectionEventListener.class);

		// verify that the malcolm device subscribed to the 'state' endpoint
		verify(malcolmConnection).subscribe(eq(malcolmDevice), eq(expectedSubscribeToStateMessage), malcolmListeners.capture());
		stateChangeListener = malcolmListeners.getValue();
		assertThat(stateChangeListener, is(notNullValue()));

		// verify that the malcolm device subscribed to the 'completedSteps' endpoint
		verify(malcolmConnection).subscribe(eq(malcolmDevice), eq(expectedSubscribeCompletedStepsMessage), malcolmListeners.capture());
		scanEventListener = malcolmListeners.getValue();
		assertThat(scanEventListener, is(notNullValue()));

		// verify that the malcolm device subscribed to connection state changes
		ArgumentCaptor<IMalcolmConnectionStateListener> connectionStateListenerCaptor = ArgumentCaptor.forClass(IMalcolmConnectionStateListener.class);
		verify(malcolmConnection, timeout(1000)) // add timeout as call made in different thread
				.subscribeToConnectionStateChange(eq(malcolmDevice), connectionStateListenerCaptor.capture());
		connectionChangeListener = connectionStateListenerCaptor.getValue();
		assertThat(connectionChangeListener, is(notNullValue()));

		assertThat(malcolmDevice.isAlive(), is(true));
		verify(malcolmConnection, timeout(1000)).send(malcolmDevice, expectedGetDeviceStateMessage2);
		verify(malcolmEventListener, timeout(1000)).eventPerformed(
				createExpectedMalcolmEvent(DeviceState.READY, currentDeviceState, "connected to " + malcolmDevice.getName()));
		verifyNoMoreInteractions(malcolmEventListener);
//		verifyNoMoreInteractions(malcolmConnection); // This doesn't work, not sure why
	}

	protected CompoundModel createCompoundModel() {
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel("stage_x", "stage_y", 10, 10);
		gridModel.setBoundingBox(new BoundingBox(0, 0, 1, 1));

		return new CompoundModel(gridModel);
	}

	protected IPointGenerator<CompoundModel> createPointGenerator() throws Exception {
		return pointGenService.createCompoundGenerator(createCompoundModel());
	}

	protected void configureMocksForConfigure(ScanModel scanModel, boolean modified) throws Exception {
		@SuppressWarnings("unchecked")
		final IPointGenerator<CompoundModel> pointGen = (IPointGenerator<CompoundModel>) scanModel.getPointGenerator();

		// create the expected abort, reset and configure message and configure the mock connection to reply as expected
		final MalcolmMessage expectedAbortMessage = createExpectedCallMessage(id++, MalcolmMethod.ABORT, null);
		final MalcolmMessage expectedResetMessage = createExpectedCallMessage(id++, MalcolmMethod.RESET, null);

		final MalcolmMessage axesToMoveReply = createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y"));
		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES))).thenReturn(axesToMoveReply); // called from MalcolmDevice.configure via getConfiguredAxes

		final MalcolmMessage expectedGetConfigureMessage = createExpectedMalcolmMessage(id++, Type.GET, MalcolmMethod.CONFIGURE.toString());
		when(malcolmConnection.send(malcolmDevice, expectedGetConfigureMessage)).thenReturn(createExpectedMalcolmGetConfigureReply());

		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES))).thenReturn(axesToMoveReply); // called from MalcolmDevice.configure via getConfiguredAxes

		// create the EpicsMalcolmModels expected to be sent to malcolm and to be received
		final List<MalcolmDetectorInfo> expectedSentDetectorInfos = getExpectedMalcolmDetectorInfos(false);
		final List<MalcolmDetectorInfo> expectedReceivedDetectorInfos = getExpectedMalcolmDetectorInfos(modified); // causes the mock connector to return modified malcolm detectors
		final String expectedMalcolmOutputDir = FilenameUtils.removeExtension(scanModel.getFilePath());
		final EpicsMalcolmModel expectedSentEpicsMalcolmModel = createExpectedEpicsMalcolmModel(
				pointGen, expectedMalcolmOutputDir, expectedSentDetectorInfos);

		// create the expected configure message and configure the mock connection to reply as expected
		final MalcolmMessage expectedConfigureMessage = createExpectedCallMessage(id++, MalcolmMethod.CONFIGURE, expectedSentEpicsMalcolmModel);
		final EpicsMalcolmModel expectedReceivedEpicsMalcolmModel = createExpectedEpicsMalcolmModel(
				pointGen, expectedMalcolmOutputDir, expectedReceivedDetectorInfos);
		when(malcolmConnection.send(malcolmDevice, expectedAbortMessage)).thenReturn(createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedResetMessage)).thenReturn(createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedConfigureMessage)).thenReturn(createExpectedMalcolmOkReply(
				createExpectedMalcolmConfigureValidateReturnValue(expectedReceivedEpicsMalcolmModel)));
	}

	protected MalcolmMessage createExpectedMalcolmMessage(long id, Type type, String endpoint) {
		final MalcolmMessage msg = new MalcolmMessage();
		msg.setId(id);
		msg.setType(type);
		msg.setEndpoint(endpoint);

		return msg;
	}

	protected MalcolmMessage createExpectedCallMessage(long id, MalcolmMethod method, Object arg) {
		final MalcolmMessage msg = new MalcolmMessage();
		msg.setId(id);
		msg.setType(Type.CALL);
		msg.setMethod(method);
		msg.setArguments(arg);

		return msg;
	}

	protected MalcolmMessage createExpectedMalcolmOkReply(Object returnValue) {
		final MalcolmMessage msg = new MalcolmMessage();
		msg.setValue(returnValue);
		return msg;
	}

	protected MalcolmMessage createExpectedMalcolmStateReply(DeviceState deviceState) {
		final ChoiceAttribute stateAttr = new ChoiceAttribute();
		stateAttr.setName(ATTRIBUTE_NAME_STATE);
		stateAttr.setLabel(ATTRIBUTE_NAME_STATE);
		stateAttr.setValue(deviceState.toString());
		return createExpectedMalcolmOkReply(stateAttr);
	}

	protected MalcolmMessage createExpectedMalcolmHealthReply(String health) {
		final StringAttribute healthAttr = new StringAttribute();
		healthAttr.setName(ATTRIBUTE_NAME_HEALTH);
		healthAttr.setLabel(ATTRIBUTE_NAME_HEALTH);
		healthAttr.setValue(health);
		return createExpectedMalcolmOkReply(healthAttr);
	}

	protected MalcolmMessage createExpectedMalcolmGetConfigureReply() {
		final MalcolmMethodMeta result = new MalcolmMethodMeta(MalcolmMethod.CONFIGURE);
		final Map<String, Object> defaults = new HashMap<>();
		final MalcolmTable detectorsTable = createExpectedDetectorsMalcolmTable(getExpectedDefaultMalcolmDetectorInfos());
		defaults.put(MalcolmConstants.FIELD_NAME_DETECTORS, detectorsTable);
		result.setDefaults(defaults);

		return createExpectedMalcolmOkReply(result);
	}

	protected MalcolmTable createExpectedDetectorsMalcolmTable(List<MalcolmDetectorInfo> detectorInfos) {
		// convert the MalcolmDetectorInfos to a MalcolmTable
		final int numDetectors = detectorInfos.size();
		final List<String> names = new ArrayList<>(numDetectors);
		final List<String> mris = new ArrayList<>(numDetectors);
		final List<Double> exposures = new ArrayList<>(numDetectors);
		final List<Integer> framesPerSteps = new ArrayList<>(numDetectors);
		final List<Boolean> enablements = new ArrayList<>(numDetectors);

		for (MalcolmDetectorInfo info : detectorInfos) {
			names.add(info.getName());
			mris.add(info.getId());
			exposures.add(info.getExposureTime());
			framesPerSteps.add(info.getFramesPerStep());
			enablements.add(info.isEnabled());
		}

		final LinkedHashMap<String, Class<?>> tableTypesMap = new LinkedHashMap<>();
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_ENABLE, Boolean.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_NAME, String.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_MRI, String.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_EXPOSURE, Double.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, Integer.class);

		final LinkedHashMap<String, List<?>> tableData = new LinkedHashMap<>();
		tableData.put(DETECTORS_TABLE_COLUMN_NAME, names);
		tableData.put(DETECTORS_TABLE_COLUMN_MRI, mris);
		tableData.put(DETECTORS_TABLE_COLUMN_EXPOSURE, exposures);
		tableData.put(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, framesPerSteps);
		tableData.put(DETECTORS_TABLE_COLUMN_ENABLE, enablements);

		return new MalcolmTable(tableData, tableTypesMap);
	}

	protected List<MalcolmDetectorInfo> getExpectedMalcolmDetectorInfos(boolean modified) {
		final List<MalcolmDetectorInfo> detectorInfos = getExpectedMalcolmDetectorInfos();
		if (modified) {
			detectorInfos.get(2).setExposureTime(0.025); // reflects the modification that the mock malcolmConnection makes
			detectorInfos.get(2).setFramesPerStep(4);
		}
		return detectorInfos;
	}

	protected List<MalcolmDetectorInfo> getExpectedDefaultMalcolmDetectorInfos() {
		// used to create the MalcolmTable returned by get configure, which returns the
		// defaults values for each detector for this malcolm device,
		// with framesPerStep = 1 and exposureTime = 0.0 (which means use the maximum)
		final List<MalcolmDetectorInfo> detectorInfos = getExpectedMalcolmDetectorInfos();
		for (MalcolmDetectorInfo detInfo : detectorInfos) {
			detInfo.setFramesPerStep(1);
			detInfo.setExposureTime(0.0);
		}
		return detectorInfos;
	}

	protected List<MalcolmDetectorInfo> getExpectedMalcolmDetectorInfos() {
		final List<MalcolmDetectorInfo> detectorInfos = new ArrayList<>();
		detectorInfos.add(new MalcolmDetectorInfo("mri1", "det1", 1, 0.1, true));
		detectorInfos.add(new MalcolmDetectorInfo("mri2", "det2", 2, 0.05, true));
		detectorInfos.add(new MalcolmDetectorInfo("mri3", "det3", 1, 0.1, false));
		detectorInfos.add(new MalcolmDetectorInfo("mri4", "det4", 5, 0.02, true));

		return detectorInfos;
	}

	protected MalcolmMessage createExpectedMalcolmValidateReturnReply(Object rawValue) {
		final MalcolmMessage msg = new MalcolmMessage();
		msg.setRawValue(rawValue);
		return msg;
	}

	protected MalcolmMessage createExpectedMalcolmErrorReply(String errorMessage) {
		final MalcolmMessage msg = new MalcolmMessage();
		msg.setType(Type.ERROR);
		msg.setMessage(errorMessage);
		return msg;
	}

	protected MalcolmEvent createExpectedMalcolmEvent(DeviceState state, DeviceState previousState, String message) {
		return MalcolmEvent.forStateChange(malcolmDevice, state, previousState, message);
	}

	protected MalcolmEvent createExpectedMalcolmEvent(int stepsCompleted) {
		return MalcolmEvent.forStepsCompleted(malcolmDevice, stepsCompleted, "Start of point " + stepsCompleted);
	}

	protected LinkedHashMap<String, Object> createExpectedMalcolmConfigureValidateReturnValue(EpicsMalcolmModel epicsMalcolmModel) {
		// the value to be returned over the mocked epics connection layer
		final LinkedHashMap<String, Object> expectedValue = new LinkedHashMap<>();
		expectedValue.put(FIELD_NAME_GENERATOR, epicsMalcolmModel.getGenerator());
		expectedValue.put(FIELD_NAME_AXES_TO_MOVE, epicsMalcolmModel.getAxesToMove());
		expectedValue.put(FIELD_NAME_FILE_DIR, epicsMalcolmModel.getFileDir());
		expectedValue.put(FIELD_NAME_FILE_TEMPLATE, epicsMalcolmModel.getFileTemplate());
		expectedValue.put(FIELD_NAME_DETECTORS, epicsMalcolmModel.getDetectors());
		return expectedValue;
	}

	protected EpicsMalcolmModel createExpectedEpicsMalcolmModel(IPointGenerator<CompoundModel> pointGen,
			String outputDir, List<MalcolmDetectorInfo> detectorInfos) throws Exception {
		if (outputDir == null) {
			outputDir = Services.getFilePathService().getTempDir();
		}
		final String fileTemplate = Paths.get(outputDir).getFileName().toString() + "-%s." + FILE_EXTENSION_H5;

		// create a copy of the compound model, so that we're not cheating when comparing the
		// expected model with the one actually used
		final CompoundModel model = pointGen == null ? new CompoundModel(new StaticModel()): pointGen.getModel();
		final CompoundModel copiedModel = new CompoundModel(model);
		copiedModel.setDuration(0.1);
		copiedModel.setMutators(Collections.emptyList());
		pointGen = pointGenService.createCompoundGenerator(copiedModel);

		// get the axes of the inner scan (note only pointGen.getNames returns the names in the right order,
		// so we need to use that, then remove the outer axes)
		final List<String> innerScanAxes = Iterables.getLast(model.getModels()).getScannableNames();
		final List<String> axesToMove = pointGen.getNames().stream().filter(innerScanAxes::contains).collect(toList());

		final MalcolmTable detectorsTable = createExpectedDetectorsMalcolmTable(detectorInfos);
		final int[] breakpoints = calculateExpectedBreakpoints(pointGen.getModel());

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

}
