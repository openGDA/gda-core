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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MalcolmDeviceEventTest extends AbstractMalcolmDeviceTest {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initializeMalcolmDevice();
	}

	private MalcolmMessage createStateChangeMessage(DeviceState newState) {
		final MalcolmMessage message = new MalcolmMessage();
		message.setEndpoint("state");
		message.setMessage("State changed to " + newState);
		message.setType(Type.UPDATE);

		final ChoiceAttribute stateAttribute = new ChoiceAttribute();
		stateAttribute.setName("state");
		stateAttribute.setValue(newState.toString());
		message.setValue(stateAttribute);
		return message;
	}

	private MalcolmMessage createScanEventMessage(int stepNum) {
		final MalcolmMessage message = new MalcolmMessage();
		message.setMessage("Start of point " + stepNum);
		message.setType(Type.UPDATE);

		NumberAttribute pointAttribute = new NumberAttribute();
		pointAttribute.setName("value");
		pointAttribute.setValue(stepNum);
		message.setValue(pointAttribute);

		return message;
	}

	@Test
	public void testReceiveStateChange() throws Exception {
		// Arrange
		DeviceState oldState = DeviceState.READY;
		DeviceState newState = DeviceState.CONFIGURING;
		MalcolmMessage message = createStateChangeMessage(newState);

		// Act
		stateChangeListener.eventPerformed(message);

		// Assert
		verify(malcolmEventListener, times(2)).eventPerformed(any(MalcolmEvent.class));
		assertThat(malcolmBeanCaptor.getValue(), is(equalTo(
				createExpectedMalcolmEventBean(newState, oldState, "State changed to " + newState))));

		// Now call again. This time, previousState should be CONFIGURING
		// Arrange
		oldState = newState;
		newState = DeviceState.ARMED;
		message = createStateChangeMessage(newState);

		// Act
		stateChangeListener.eventPerformed(message);

		// Assert
		verify(malcolmEventListener, times(3)).eventPerformed(any(MalcolmEvent.class));
		assertThat(malcolmBeanCaptor.getValue(), is(equalTo(
				createExpectedMalcolmEventBean(newState, oldState, "State changed to " + newState))));
	}

	@Test
	public void testReceiveScanEvent() throws Exception {
		// Arrange
		// A very simply unlimited position iterator
		final Iterator<IPosition> positionIterator = new Iterator<IPosition>() {

			private int next = 0;

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public IPosition next() {
				IPosition pos = new Scalar<>("x", next, next);
				next++;
				return pos;
			}
		};

		// Create a mock SubscanModerator whose getInnerIterable method returns an iterable whose
		// iterator method returns this position iterate. Set this on the MalcolmDevice using scanPoint()
		// method, normally called at the start of each outer point due to its @ScanPoint annotation
		final Iterable<IPosition> positionIterable = () -> positionIterator;
		final SubscanModerator subscanModerator = mock(SubscanModerator.class);
		when(subscanModerator.getInnerIterable()).thenReturn(positionIterable);
		((MalcolmDevice<?>) malcolmDevice).scanPoint(subscanModerator);

		// create a mock position listener and add it to the malcolm device
		final IPositionListener positionListener = mock(IPositionListener.class);
		((MalcolmDevice<?>) malcolmDevice).addPositionListener(positionListener);

		// Act: create and send the first message to the malcolm device
		Thread.sleep(MalcolmDevice.POSITION_COMPLETE_INTERVAL); // sleep for the position complete frequency, so the next event should fire
		int stepNum = 0;
		scanEventListener.eventPerformed(createScanEventMessage(stepNum));

		// Assert: check that the listener received the correct message
		ArgumentCaptor<PositionEvent> positionCaptor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(positionListener).positionPerformed(new PositionEvent(new Scalar<>("x", stepNum, stepNum), malcolmDevice));

		verifyNoMoreInteractions(positionListener);

		// Act again: send another message to the malcolm device. This should not cause a position event
		// to be fired, due to throttling by the MalcolmDevice
		stepNum = 5;
		scanEventListener.eventPerformed(createScanEventMessage(stepNum));
		verifyNoMoreInteractions(positionListener); //

		// sleep for the position complete frequency, so the next event should fire
		Thread.sleep(MalcolmDevice.POSITION_COMPLETE_INTERVAL);
		stepNum = 10;
		scanEventListener.eventPerformed(createScanEventMessage(stepNum));
		verify(positionListener).positionPerformed(new PositionEvent(new Scalar<>("x", stepNum, stepNum), malcolmDevice));
		verifyNoMoreInteractions(positionListener);
		// Note: no assertions are made of the published ScanBean as MalcolmDevice shouldn't be updating and
		// publishing this bean in the first place, and this code will be removed (TODO remove this comment when done)
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConnectionStateChange() throws Exception {
		// Arrange
		assertThat(malcolmDevice.isAlive(), is(true));

		// Act
		connectionChangeListener.connectionStateChanged(false);

		// Assert
		assertThat(malcolmDevice.isAlive(), is(false));
		// verify that a state change event is received, from ARMED to OFFLINE
		verify(malcolmEventListener, timeout(100).times(2)).eventPerformed(any(MalcolmEvent.class));
		assertThat(malcolmBeanCaptor.getValue(), is(equalTo(createExpectedMalcolmEventBean(
				DeviceState.OFFLINE, DeviceState.READY, "disconnected from " + malcolmDevice.getName()))));

		// Arrange
		final MalcolmMessage expectedGetDeviceStateMessage = createExpectedMalcolmMessage(id++, Type.GET, "state");
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage)).thenReturn(createExpectedMalcolmOkReply("ready"));

		// Act
		connectionChangeListener.connectionStateChanged(true);
		assertThat(malcolmDevice.isAlive(), is(true));

		// Assert
		verify(malcolmConnection, timeout(100)) // wait with timeout as invocation happens in a another thread
				.send(malcolmDevice, expectedGetDeviceStateMessage);
		verify(malcolmEventListener, times(3)).eventPerformed(any(MalcolmEvent.class));
		assertThat(malcolmBeanCaptor.getValue(), is(equalTo(createExpectedMalcolmEventBean(
				DeviceState.READY, DeviceState.OFFLINE, "connected to " + malcolmDevice.getName()))));
	}

}
