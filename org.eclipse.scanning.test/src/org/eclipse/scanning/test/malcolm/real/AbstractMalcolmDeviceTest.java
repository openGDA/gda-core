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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
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
	protected IMalcolmDevice<MalcolmModel> malcolmDevice;

	@Mock
	protected IMalcolmConnection malcolmConnection;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	protected IMalcolmListener<MalcolmMessage> stateChangeListener;
	protected IMalcolmListener<MalcolmMessage> scanEventListener;
	protected IMalcolmListener<Boolean> connectionChangeListener;

	protected int id = 0;

	@Before
	public void setUp() throws Exception {
		this.runnableDeviceService = new RunnableDeviceServiceImpl();

		when(malcolmConnection.getMessageGenerator()).thenReturn(new MalcolmMessageGenerator());
		malcolmDevice = new MalcolmDevice<>("malcolm", malcolmConnection, runnableDeviceService, null);
		pointGenService = new PointGeneratorService();
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
		final MalcolmMessage expectedGetDeviceStateMessage = createExpectedMalcolmMessage(id++, Type.GET, "state");
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage)).thenReturn(createExpectedMalcolmOkReply("ready"));

		// Act: call initialize() on the malcolm device
		assertThat(malcolmDevice.isAlive(), is(false));
		malcolmDevice.initialize();

		// Assert: check the expected message have been send
		verify(malcolmConnection).send(malcolmDevice, expectedGetDeviceStateMessage);

		ArgumentCaptor<IMalcolmListener> malcolmListeners = ArgumentCaptor.forClass(IMalcolmListener.class);

		verify(malcolmConnection).subscribe(eq(malcolmDevice), eq(createExpectedMalcolmMessage(id++, Type.SUBSCRIBE, "state")), malcolmListeners.capture());
		stateChangeListener = malcolmListeners.getValue();
		assertThat(stateChangeListener, is(notNullValue()));

		verify(malcolmConnection).subscribe(eq(malcolmDevice), eq(createExpectedMalcolmMessage(id++, Type.SUBSCRIBE, "completedSteps")), malcolmListeners.capture());
		scanEventListener = malcolmListeners.getValue();
		assertThat(scanEventListener, is(notNullValue()));

		Thread.sleep(250); // the call below is made is a different thread
		verify(malcolmConnection).subscribeToConnectionStateChange(eq(malcolmDevice), malcolmListeners.capture());
		connectionChangeListener = malcolmListeners.getValue();
		assertThat(connectionChangeListener, is(notNullValue()));

		assertThat(malcolmDevice.isAlive(), is(true));
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

	protected MalcolmMessage createExpectedMalcolmValdiateReturnReply(Object rawValue) {
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


}
