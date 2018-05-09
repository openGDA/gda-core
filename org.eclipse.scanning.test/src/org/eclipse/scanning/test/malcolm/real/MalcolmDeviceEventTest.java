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
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
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

	@SuppressWarnings("unchecked")
	@Test
	public void testReceiveStateChange() throws Exception {
		// Arrange
		IMalcolmListener<MalcolmEventBean> malcolmListener = mock(IMalcolmListener.class);
		malcolmDevice.addMalcolmListener(malcolmListener);
		DeviceState newState = DeviceState.READY;
		MalcolmMessage message = createStateChangeMessage(newState);

		// Act
		stateChangeListener.eventPerformed(new MalcolmEvent<>(message));

		// Assert
		// Note: no assertions are made of the published ScanBean as MalcolmDevice shouldn't be updating and
		// publishing this bean in the first place, and this code will be removed (TODO remove this comment when done)
		@SuppressWarnings("rawtypes")
		ArgumentCaptor<MalcolmEvent> captor = ArgumentCaptor.forClass(MalcolmEvent.class);
		verify(malcolmListener).eventPerformed(captor.capture());
		MalcolmEventBean bean = (MalcolmEventBean) captor.getValue().getBean();
		assertThat(bean.getPreviousState(), is(nullValue()));
		assertThat(bean.getDeviceState(), is(newState));
		assertThat(bean.getMessage(), is(equalTo(message.getMessage())));

		// Now call again. This time, previousState should be READY
		// Arrange
		final DeviceState oldState = newState;
		newState = DeviceState.ARMED;
		message = createStateChangeMessage(newState);

		// Act
		stateChangeListener.eventPerformed(new MalcolmEvent<>(message));

		// Assert
		verify(malcolmListener, times(2)).eventPerformed(captor.capture());
		bean = (MalcolmEventBean) captor.getValue().getBean();
		assertThat(bean.getPreviousState(), is(oldState));
		assertThat(bean.getDeviceState(), is(newState));
		assertThat(bean.getMessage(), is(equalTo(message.getMessage())));
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
		int stepNum = 0;
		scanEventListener.eventPerformed(new MalcolmEvent<>(createScanEventMessage(stepNum)));

		// Assert: check that the listener received the correct message
		ArgumentCaptor<PositionEvent> captor = ArgumentCaptor.forClass(PositionEvent.class);
		verify(positionListener).positionPerformed(captor.capture());
		PositionEvent event = captor.getValue();
		assertThat(event.getDevice().getName(), is(equalTo(malcolmDevice.getName())));
		assertThat(event.getSource(), is(sameInstance(malcolmDevice)));
		IPosition position = event.getPosition();
		assertThat(position, is(equalTo(new Scalar<>("x", stepNum, stepNum))));
		verifyNoMoreInteractions(positionListener);

		// Act again: send another message to the malcolm device. Note malcolm doesn't notify every position
		stepNum = 5;
		scanEventListener.eventPerformed(new MalcolmEvent<>(createScanEventMessage(stepNum)));
		verifyNoMoreInteractions(positionListener); // We throttle the updates to every 250ms by default, so no update

		// sleep for the position complete frequency, so the next event should fire
		Thread.sleep(MalcolmDevice.POSITION_COMPLETE_INTERVAL + 10);
		stepNum = 10;
		scanEventListener.eventPerformed(new MalcolmEvent<>(createScanEventMessage(stepNum)));
		verify(positionListener, times(2)).positionPerformed(captor.capture());
		event = captor.getValue();
		assertThat(event.getDevice().getName(), is(equalTo(malcolmDevice.getName())));
		assertThat(event.getSource(), is(sameInstance(malcolmDevice)));
		position = event.getPosition();
		assertThat(position, is(equalTo(new Scalar<>("x", stepNum, stepNum))));
		verifyNoMoreInteractions(positionListener);
		// Note: no assertions are made of the published ScanBean as MalcolmDevice shouldn't be updating and
		// publishing this bean in the first place, and this code will be removed (TODO remove this comment when done)
	}

	@Test
	public void testConnectionStateChange() throws Exception {
		assertThat(malcolmDevice.isAlive(), is(true));
		connectionChangeListener.eventPerformed(new MalcolmEvent<>(false));
		assertThat(malcolmDevice.isAlive(), is(false));

		final MalcolmMessage expectedGetDeviceStateMessage = createExpectedMalcolmMessage(id++, Type.GET, "state");
		when(malcolmConnection.send(malcolmDevice, expectedGetDeviceStateMessage)).thenReturn(createExpectedMalcolmOkReply("ready"));
		connectionChangeListener.eventPerformed(new MalcolmEvent<>(true));
		assertThat(malcolmDevice.isAlive(), is(true));
		Thread.sleep(100); // short sleep as the call to get device state happens in a different thread
		verify(malcolmConnection).send(malcolmDevice, expectedGetDeviceStateMessage);
	}

}
