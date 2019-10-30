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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionEventListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionStateListener;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.malcolm.core.Services;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public abstract class AbstractMalcolmDeviceTest {

	protected IRunnableDeviceService runnableDeviceService;
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
	protected BeanCollectingAnswer<MalcolmEvent> malcolmBeanCaptor;

	protected int id = 0;

	@Before
	public void setUp() throws Exception {
		ServiceTestHelper.setupServices();
		this.runnableDeviceService = ServiceTestHelper.getRunnableDeviceService();

		pointGenService = new PointGeneratorService();
		new ServiceHolder().setGeneratorService(pointGenService);
		new Services().setPointGeneratorService(pointGenService);

		when(malcolmConnection.getMessageGenerator()).thenReturn(new MalcolmMessageGenerator());
		malcolmDevice = new MalcolmDevice("malcolm", malcolmConnection, runnableDeviceService);

		malcolmBeanCaptor = BeanCollectingAnswer.forClass(MalcolmEvent.class, MalcolmEvent::copy);
		doAnswer(malcolmBeanCaptor).when(malcolmEventListener).eventPerformed(any(MalcolmEvent.class));
	}

	@After
	public void tearDown() throws Exception {
		malcolmDevice.dispose();
		malcolmConnection.disconnect();
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
		verify(malcolmConnection, timeout(250)) // add timeout as call made in different thread
				.subscribeToConnectionStateChange(eq(malcolmDevice), connectionStateListenerCaptor.capture());
		connectionChangeListener = connectionStateListenerCaptor.getValue();
		assertThat(connectionChangeListener, is(notNullValue()));

		assertThat(malcolmDevice.isAlive(), is(true));
		verify(malcolmConnection, timeout(250)).send(malcolmDevice, expectedGetDeviceStateMessage2);
		verify(malcolmEventListener, timeout(250)).eventPerformed(any(MalcolmEvent.class)); // argument checked using custom captor below
		final MalcolmEvent event = malcolmBeanCaptor.getValue();
		assertThat(event, is(equalTo(createExpectedMalcolmEvent(DeviceState.READY, DeviceState.OFFLINE, "connected to " + malcolmDevice.getName()))));
		verifyNoMoreInteractions(malcolmEventListener);
//		verifyNoMoreInteractions(malcolmConnection); // This doesn't work, not sure why
	}

	protected MalcolmModel createMalcolmModel() {
		final MalcolmModel malcolmModel = new MalcolmModel();
		malcolmModel.setExposureTime(0.1);
		return malcolmModel;
	}

	protected IPointGenerator<?> createPointGenerator() throws Exception {
		final GridModel gridModel = new GridModel("stage_x", "stage_y", 10, 10);
		gridModel.setBoundingBox(new BoundingBox(0, 0, 1, 1));

		final IPointGenerator<GridModel> gridGen = pointGenService.createGenerator(gridModel);
		return pointGenService.createCompoundGenerator(gridGen);
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

	protected MalcolmMessage createExpectedMalcolmOkReply(Object value) {
		final MalcolmMessage msg = new MalcolmMessage();
		msg.setValue(value);
		return msg;
	}

	protected MalcolmMessage createExpectedMalcolmStateReply(DeviceState deviceState) {
		final ChoiceAttribute stateAttr = new ChoiceAttribute();
		stateAttr.setName("state");
		stateAttr.setLabel("state");
		stateAttr.setValue(deviceState.toString());
		return createExpectedMalcolmOkReply(stateAttr);
	}

	protected MalcolmMessage createExpectedMalcolmHealthReply(String health) {
		final StringAttribute healthAttr = new StringAttribute();
		healthAttr.setName("health");
		healthAttr.setLabel("health");
		healthAttr.setValue(health);
		return createExpectedMalcolmOkReply(healthAttr);
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

}
