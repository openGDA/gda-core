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
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.HEALTH_ENDPOINT;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.STANDARD_MALCOLM_ERROR_STR;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.STATE_ENDPOINT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.malcolm.core.MalcolmDevice.EpicsMalcolmModel;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * A test class for testing a real {@link MalcolmDevice}, but with a mock {@link IMalcolmConnectorService}.
 */
public class MalcolmDeviceTest extends AbstractMalcolmDeviceTest {

	@FunctionalInterface
	public interface MalcolmCall {
		void call(IMalcolmDevice<?> malcolmDevice) throws Exception;
	}


	private static final String FILE_DIR = "/path/to/ixx-1234";
	private static final String FILE_TEMPLATE = "ixx-1234-%s.h5";

	private EpicsMalcolmModel createExpectedEpicsMalcolmModel(IPointGenerator<?> pointGen) {
		final List<String> axesToMove = Arrays.asList("stage_y", "stage_x");
		return new EpicsMalcolmModel(FILE_DIR, FILE_TEMPLATE, axesToMove, pointGen);
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
			MalcolmMessage expectedMessage = createExpectedMalcolmMessage(i, Type.GET, STATE_ENDPOINT);
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
			MalcolmMessage expectedMessage = createExpectedMalcolmMessage(i, Type.GET, HEALTH_ENDPOINT);
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
			MalcolmMessage expectedMessage = createExpectedMalcolmMessage(i, Type.GET, STATE_ENDPOINT);
			when(malcolmConnection.send(malcolmDevice, expectedMessage)).thenReturn(
					createExpectedMalcolmStateReply(deviceState));

			// Act / Assert
			assertThat(malcolmDevice.isDeviceBusy(), is(!deviceState.isRestState()));

			// Assert: check the expected message has been sent
			verify(malcolmConnection).send(malcolmDevice, expectedMessage);
		}
	}

	@Test
	public void testValidate() throws Exception {
		// Arrange
		final MalcolmModel malcolmModel = createMalcolmModel();
		final IPointGenerator<?> pointGen = createPointGenerator();

		// create the expected EpicsMalcolmModel that should be sent to malcolm
		final EpicsMalcolmModel expectedMalcolmModel = createExpectedEpicsMalcolmModel(pointGen);

		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		MalcolmMessage expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));
		// create the expected validate message and configure the mock connection to reply as expected
		MalcolmMessage expectedValidateMessage = createExpectedCallMessage(id++, MalcolmMethod.VALIDATE, expectedMalcolmModel);
		when(malcolmConnection.send(malcolmDevice, expectedValidateMessage)).thenReturn(createExpectedMalcolmOkReply(null));

		// Act
		// pointGenerator and fileDir would be set by ScanProcess in a real scan
		malcolmDevice.setPointGenerator(pointGen);
		malcolmDevice.setFileDir(FILE_DIR);

		malcolmDevice.validate(malcolmModel);

		// Assert
		verify(malcolmConnection).send(malcolmDevice, expectedValidateMessage);

		// Arrange, now with an error response
		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));
		// create the expected validate message and configure the mock connection to reply as expected
		expectedValidateMessage = createExpectedCallMessage(id++, MalcolmMethod.VALIDATE, expectedMalcolmModel);
		final String errorMessage = "Invalid model";
		when(malcolmConnection.send(malcolmDevice, expectedValidateMessage)).thenReturn(createExpectedMalcolmErrorReply(errorMessage));

		// Act / Assert , this time an error should occur
		try {
			malcolmDevice.validate(malcolmModel);
			fail("A validation exception was expected");
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is(equalTo("org.eclipse.scanning.api.ValidationException: " + STANDARD_MALCOLM_ERROR_STR + errorMessage)));
		}

		verify(malcolmConnection).send(malcolmDevice, expectedValidateMessage);
	}

	@Test
	public void testValidateWithReturn() throws Exception {
		// TODO: validateWithReturn should really return a MalcolmModel, with the exposureTime updated if necessary. See JIRA ticket DAQ-1437.

		// Arrange
		final MalcolmModel malcolmModel = createMalcolmModel();
		final IPointGenerator<?> pointGen = createPointGenerator();

		// create the expected EpicsMalcolmModel that should be sent to malcolm
		EpicsMalcolmModel expectedEpicsMalcolmModel = createExpectedEpicsMalcolmModel(pointGen);

		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		MalcolmMessage expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));
		// create the expected validate message and configure the mock connection to reply as expected
		MalcolmMessage expectedValidateMessage = createExpectedCallMessage(id++, MalcolmMethod.VALIDATE, expectedEpicsMalcolmModel);
		when(malcolmConnection.send(malcolmDevice, expectedValidateMessage)).thenReturn(
				createExpectedMalcolmValidateReturnReply(expectedEpicsMalcolmModel));

		// Act
		// pointGenerator and fileDir would be set by ScanProcess in a real scan
		malcolmDevice.setPointGenerator(pointGen);
		malcolmDevice.setFileDir(FILE_DIR);

		EpicsMalcolmModel result = (EpicsMalcolmModel) malcolmDevice.validateWithReturn(malcolmModel);

		// Assert
		assertThat(result, equalTo(expectedEpicsMalcolmModel));
		verify(malcolmConnection).send(malcolmDevice, expectedValidateMessage);

		// Arrange, now with a modified model
		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));
		// create the expected validate message and configure the mock connection to reply as expected
		expectedValidateMessage = createExpectedCallMessage(id++, MalcolmMethod.VALIDATE, expectedEpicsMalcolmModel);
		final IPointGenerator<?> modifiedPointGen = createPointGenerator();
		final EpicsMalcolmModel modifiedEpicsMalcolmModel = createExpectedEpicsMalcolmModel(modifiedPointGen);
		final MalcolmMessage expectedValidateResponse = createExpectedMalcolmValidateReturnReply(modifiedEpicsMalcolmModel);
		when(malcolmConnection.send(malcolmDevice, expectedValidateMessage)).thenReturn(expectedValidateResponse);

		// Act, this time an error should occur
		result = (EpicsMalcolmModel) malcolmDevice.validateWithReturn(malcolmModel);
		assertThat(result, equalTo(modifiedEpicsMalcolmModel));
		verify(malcolmConnection).send(malcolmDevice, expectedValidateMessage);
	}

	@Test
	public void testInitialize() throws Exception {
		initializeMalcolmDevice();
	}

	@Test
	public void testConfigure() throws Exception {
		initializeMalcolmDevice();

		// Arrange
		final MalcolmModel malcolmModel = createMalcolmModel();
		final IPointGenerator<?> pointGen = createPointGenerator();

		// create the expected EpicsMalcolmModel that should be sent to malcolm
		final EpicsMalcolmModel expectedMalcolmModel = createExpectedEpicsMalcolmModel(pointGen);

		// create the expected reset and configure message and configure the mock connection to reply as expected
		final MalcolmMessage expectedResetMessage = createExpectedCallMessage(id++, MalcolmMethod.RESET, null);
		// create the expected message to get the simultaneous axes (required to configure the malcolm device) and reply as expected
		MalcolmMessage expectedGetSimultaneousAxesMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		when(malcolmConnection.send(malcolmDevice, expectedGetSimultaneousAxesMessage)).thenReturn(
				createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y")));
		// create the expected validate message and configure the mock connection to reply as expected
		final MalcolmMessage expectedConfigureMessage = createExpectedCallMessage(id++, MalcolmMethod.CONFIGURE, expectedMalcolmModel);
		when(malcolmConnection.send(malcolmDevice, expectedResetMessage)).thenReturn(createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedConfigureMessage)).thenReturn(createExpectedMalcolmOkReply(null));

		// Act
		// pointGenerator and fileDir would be set by ScanProcess in a real scan
		malcolmDevice.setPointGenerator(pointGen);
		malcolmDevice.setFileDir(FILE_DIR);

		malcolmDevice.configure(malcolmModel);

		// Assert
		verify(malcolmConnection).send(malcolmDevice, expectedResetMessage);
		verify(malcolmConnection).send(malcolmDevice, expectedConfigureMessage);
		// test duration of pointGen's model has been set to exposure time of malcolm model
		assertThat(((CompoundModel<?>) pointGen.getModel()).getDuration(), is(equalTo(malcolmModel.getExposureTime())));
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
		testCall(IMalcolmDevice::reset, MalcolmMethod.RESET, null);
	}

	@Test
	public void testPause() throws Exception {
		testCall(IMalcolmDevice::pause, MalcolmMethod.PAUSE, null);
	}

	@Test
	public void testResume() throws Exception {
		testCall(IMalcolmDevice::resume, MalcolmMethod.RESUME, null);
	}

	@SuppressWarnings("unchecked")
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
		assertThat(malcolmDevice.getDatasets(), is(Matchers.sameInstance(datasetsTable)));
	}

	@Test
	public void testIsNewMalcolm() throws Exception {
		// Arrange
		MalcolmMessage expectedMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		String[] axesNames = new String[] { "stage_x", "stage_y" };
		StringArrayAttribute availableAxes = new StringArrayAttribute(axesNames);
		when(malcolmConnection.send(malcolmDevice, expectedMessage))
				.thenReturn(createExpectedMalcolmOkReply(availableAxes));

		// Act / Assert
		assertTrue(malcolmDevice.isNewMalcolmVersion());
	}

}

