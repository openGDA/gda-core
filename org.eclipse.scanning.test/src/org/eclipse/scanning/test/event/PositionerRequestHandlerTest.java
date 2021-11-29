/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.event;

import static org.eclipse.scanning.api.device.PositionerRequestHandler.CANNOT_CREATE_POSITIONER;
import static org.eclipse.scanning.api.device.PositionerRequestHandler.CANNOT_READ_POSITION;
import static org.eclipse.scanning.api.device.PositionerRequestHandler.CANNOT_SET_POSITION;
import static org.eclipse.scanning.api.device.PositionerRequestHandler.POSITION_NOT_REACHED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.PositionerRequestHandler;
import org.eclipse.scanning.api.event.scan.PositionRequestType;
import org.eclipse.scanning.api.event.scan.PositionerRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PositionerRequestHandlerTest {

	private PositionerRequestHandler handler;

	@Mock
	private IRunnableDeviceService runnableDeviceService;

	@Mock
	private IPositioner positioner;

	@Rule
	public MockitoRule initialiseMocks = MockitoJUnit.rule();

	@Before
	public void setup() {
		handler = new PositionerRequestHandler(runnableDeviceService, null, null);
	}

	// When things go well...

	@Test
	public void getPosition() throws Exception {
		// request to get positions of scannable1 and scannable2
		var request = createGetPositionRequest("s1", "s2");

		// mock answer
		IPosition currentPosition = new MapPosition(Map.of("s1", 5.5, "s2", 4.4));
		when(positioner.getPosition()).thenReturn(currentPosition);

		when(runnableDeviceService.createPositioner("positioner " + request.getUniqueId()))
			.thenReturn(positioner);

		// process request
		var response = handler.process(request);

		// position in response as given by the internal positioner
		assertThat(response.getPosition(), is(equalTo(currentPosition)));
		assertNull(request.getErrorMessage());
	}

	@Test
	public void setPosition() throws Exception {
		var request = createSetPositionRequest(Map.of("scn1", 12.3, "shutter", "open"));
		request.setPosition(request.getPosition());

		when(positioner.setPosition(request.getPosition())).thenReturn(true);
		linkPositioner(positioner, request.getUniqueId());

		handler.process(request);

		verify(positioner).setPosition(request.getPosition());
		assertNull(request.getErrorMessage());
	}

	@Test
	public void abort() throws Exception {
		var request = new PositionerRequest();
		request.setRequestType(PositionRequestType.ABORT);

		linkPositioner(positioner, request.getUniqueId());

		handler.process(request);

		verify(positioner).abort();
		assertNull(request.getErrorMessage());
	}

	@Test
	public void close() throws Exception {
		var request = new PositionerRequest();
		request.setRequestType(PositionRequestType.CLOSE);

		linkPositioner(positioner, request.getUniqueId());

		handler.process(request);

		verify(positioner).close();
		assertNull(request.getErrorMessage());
	}

	@Test
	public void requestIdMapsToUniquePositioner() throws Exception {
		var firstRequest = createSetPositionRequest(Map.of("scn1", 12.3, "shutter", "open"));
		when(positioner.setPosition(firstRequest.getPosition())).thenReturn(true);

		linkPositioner(positioner, firstRequest.getUniqueId());

		var secondRequest = createSetPositionRequest(Map.of("mirror", 5.2));
		IPositioner anotherPositioner = mock(IPositioner.class);
		when(anotherPositioner.setPosition(secondRequest.getPosition())).thenReturn(true);

		linkPositioner(anotherPositioner, secondRequest.getUniqueId());

		handler.process(firstRequest);
		handler.process(secondRequest);

		// send modified first request
		firstRequest.setRequestType(PositionRequestType.ABORT);
		handler.process(firstRequest); // will use the same positioner as before

		verify(positioner).setPosition(firstRequest.getPosition());
		verify(positioner).abort();
		verify(anotherPositioner).setPosition(secondRequest.getPosition());

		assertNull(firstRequest.getErrorMessage());
		assertNull(secondRequest.getErrorMessage());
	}


	// When things do not go well...

	@Test
	public void exceptionWhenCreatingPositioner() throws Exception {
		when(runnableDeviceService.createPositioner(any(String.class))).thenThrow(ScanningException.class);

		var request = createGetPositionRequest("my_motor");
		var response = handler.process(request);
		assertThat(response.getErrorMessage(), containsString(CANNOT_CREATE_POSITIONER));
	}

	@Test
	public void exceptionWhenGettingPosition() throws Exception {
		var request = createGetPositionRequest("scannable1", "scannable2");

		when(positioner.getPosition()).thenThrow(ScanningException.class);
		linkPositioner(positioner, request.getUniqueId());

		var response = handler.process(request);
		assertThat(response.getErrorMessage(), containsString(CANNOT_READ_POSITION));
	}

	@Test
	public void exceptionWhenSettingPosition() throws Exception {
		var request = createSetPositionRequest(Map.of("s1", 0.1, "s2", 0.2));

		when(positioner.setPosition(request.getPosition())).thenThrow(ScanningException.class);
		linkPositioner(positioner, request.getUniqueId());

		var response = handler.process(request);
		assertThat(response.getErrorMessage(), containsString(CANNOT_SET_POSITION));
	}

	@Test
	public void requestedPositionerNotReached() throws Exception {
		var request = createSetPositionRequest(Map.of("s1", 0.1, "s2", 0.2));

		when(positioner.setPosition(request.getPosition())).thenReturn(false);
		linkPositioner(positioner, request.getUniqueId());

		var response = handler.process(request);
		assertThat(response.getErrorMessage(), containsString(POSITION_NOT_REACHED));
	}

	@Test
	public void setPositionRequestWithNoPosition() throws Exception {
		var request = new PositionerRequest();
		request.setRequestType(PositionRequestType.SET);
		request.setPosition(null); // just to make it explicit!

		linkPositioner(positioner, request.getUniqueId());

		var response = handler.process(request);
		assertThat(response.getErrorMessage(), containsString(CANNOT_SET_POSITION));
	}

	@Test
	public void setPositionInterrupted() throws Exception {
		var request = createSetPositionRequest(Map.of("stagex", 300));

		when(positioner.setPosition(request.getPosition())).thenThrow(InterruptedException.class);
		linkPositioner(positioner, request.getUniqueId());
		var response = handler.process(request);
		assertThat(response.getErrorMessage(), containsString(CANNOT_SET_POSITION));
	}

	private PositionerRequest createSetPositionRequest(Map<String, Object> position) {
		var request = new PositionerRequest();
		request.setRequestType(PositionRequestType.SET);
		request.setPosition(new MapPosition(position));
		return request;
	}

	private PositionerRequest createGetPositionRequest(String... scannableNames) {
		var request = new PositionerRequest();
		request.setRequestType(PositionRequestType.GET);

		Map<String, Object> position = Arrays.stream(scannableNames)
				.collect(Collectors.toMap(Function.identity(), name -> 0.0)); // the value is not important

		request.setPosition(new MapPosition(position));

		return request;
	}

	/**
	 * Make the {@link #runnableDeviceService} return the given positioner
	 * when the {@link #handler} processes a request with the given unique id.
	 */
	private void linkPositioner(IPositioner positioner, String id) throws ScanningException {
		when(runnableDeviceService.createPositioner("positioner " + id))
			.thenReturn(positioner);
	}

}
