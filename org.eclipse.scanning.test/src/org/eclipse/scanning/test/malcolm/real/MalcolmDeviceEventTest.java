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

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_STATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MalcolmDeviceEventTest extends AbstractMalcolmDeviceTest {

	@BeforeEach
	@Override
	public void setUpMalcolmDevice() throws Exception {
		super.setUpMalcolmDevice();
		initializeMalcolmDevice();
	}

	private MalcolmMessage createStateChangeMessage(DeviceState newState) {
		final MalcolmMessage message = new MalcolmMessage();
		message.setEndpoint(ATTRIBUTE_NAME_STATE);
		message.setMessage("State changed to " + newState);
		message.setType(Type.UPDATE);

		final ChoiceAttribute stateAttribute = new ChoiceAttribute();
		stateAttribute.setName(ATTRIBUTE_NAME_STATE);
		stateAttribute.setValue(newState.toString());
		message.setValue(stateAttribute);
		return message;
	}

	private MalcolmMessage createScanEventMessage(int stepNum) {
		final MalcolmMessage message = new MalcolmMessage();
		message.setMessage("Start of point " + stepNum);
		message.setType(Type.UPDATE);

		final NumberAttribute pointAttribute = new NumberAttribute();
		pointAttribute.setName("value");
		pointAttribute.setValue(stepNum);
		message.setValue(pointAttribute);

		return message;
	}

	@Test
	void testReceiveStateChange() {
		// Arrange
		DeviceState oldState = DeviceState.READY;
		DeviceState newState = DeviceState.CONFIGURING;
		MalcolmMessage message = createStateChangeMessage(newState);

		// Act
		stateChangeListener.eventPerformed(message);

		// Assert
		verify(malcolmEventListener).eventPerformed(
				createExpectedMalcolmEvent(newState, oldState, "State changed to " + newState));

		// Now call again. This time, previousState should be CONFIGURING
		// Arrange
		oldState = newState;
		newState = DeviceState.ARMED;
		message = createStateChangeMessage(newState);

		// Act
		stateChangeListener.eventPerformed(message);

		// Assert
		verify(malcolmEventListener).eventPerformed(
				createExpectedMalcolmEvent(newState, oldState, "State changed to " + newState));
	}

	@Test
	void testReceiveScanEvent() throws Exception {
		// Arrange
		// create a mock position listener and add it to the malcolm device

		// Act: create and send the first message to the malcolm device
		Thread.sleep(MalcolmDevice.POSITION_COMPLETE_INTERVAL); // sleep for the position complete frequency, so the next event should fire
		int stepNum = 0;
		scanEventListener.eventPerformed(createScanEventMessage(stepNum));

		// Assert: check that the listener received the correct message
		verify(malcolmEventListener).eventPerformed(createExpectedMalcolmEvent(stepNum));
		verifyNoMoreInteractions(malcolmEventListener);

		// Act again: send another message to the malcolm device. This should not cause a position event
		// to be fired, due to throttling by the MalcolmDevice
		stepNum = 5;
		scanEventListener.eventPerformed(createScanEventMessage(stepNum));
		verifyNoMoreInteractions(malcolmEventListener);

		// sleep for the position complete frequency, so the next event should fire
		Thread.sleep(MalcolmDevice.POSITION_COMPLETE_INTERVAL);
		stepNum = 10;
		scanEventListener.eventPerformed(createScanEventMessage(stepNum));
		verify(malcolmEventListener, times(3)).eventPerformed(any(MalcolmEvent.class));
		verifyNoMoreInteractions(malcolmEventListener);
	}

	@Test
	void testConnectionStateChange() throws Exception {
		// Arrange
		assertThat(malcolmDevice.isAlive(), is(true));

		// Act
		connectionChangeListener.connectionStateChanged(false);

		// Assert
		assertThat(malcolmDevice.isAlive(), is(false));
		// verify that a state change event is received, from ARMED to OFFLINE
		verify(malcolmEventListener, timeout(1000)).eventPerformed(
				createExpectedMalcolmEvent(DeviceState.OFFLINE, DeviceState.READY, "disconnected from " + malcolmDevice.getName()));

		// Arrange
		final MalcolmMessage expectedGetDeviceStateMessage = createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_STATE);
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage)).thenReturn(
				createExpectedMalcolmStateReply(DeviceState.READY));

		// Act
		connectionChangeListener.connectionStateChanged(true);
		assertThat(malcolmDevice.isAlive(), is(true));

		// Assert
		verify(malcolmConnection, timeout(1000)) // wait with timeout as invocation happens in a another thread
				.send(malcolmDevice, expectedGetDeviceStateMessage);
		verify(malcolmEventListener, timeout(1000).times(2)).eventPerformed(
				createExpectedMalcolmEvent(DeviceState.READY, DeviceState.OFFLINE, "connected to " + malcolmDevice.getName()));
	}

}
