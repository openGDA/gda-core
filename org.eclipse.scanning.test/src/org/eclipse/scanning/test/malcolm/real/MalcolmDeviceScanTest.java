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

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.COMPLETED_STEPS_ENDPOINT;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.POSITION_COMPLETE_INTERVAL;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.STATE_ENDPOINT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Test;
import org.mockito.Mock;

/**
 * A test that uses a {@link MalcolmDevice} in a scan.
 */
public class MalcolmDeviceScanTest extends AbstractMalcolmDeviceTest {

	@Mock
	private IScannableDeviceService scannableDeviceService;

	@Mock
	private IPublisher<ScanBean> publisher;

	private ScanBean scanBean = null;

	private int expectedNumPublishedBeans;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		RunnableDeviceServiceImpl.setDeviceConnectorService(scannableDeviceService);
		new ServiceHolder().setGeneratorService(pointGenService);
		initializeMalcolmDevice();
	}

	private ScanBean createExpectedStateChangeBean(DeviceState deviceState, DeviceState previousState,
			Status status, Status previousStatus) throws Exception {
		if (scanBean == null) {
			scanBean = new ScanBean();
			scanBean.setHostName(InetAddress.getLocalHost().getHostName());
		}

		scanBean.setDeviceName("solstice_scan");

		scanBean.setDeviceState(deviceState);
		scanBean.setPreviousDeviceState(previousState);

		scanBean.setStatus(status);
		scanBean.setPreviousStatus(previousStatus);

		return new ScanBean(scanBean);
	}

	private ScanBean createExpectedCompleteStepsBean(int completedSteps) {
		scanBean.setDeviceName("solstice_scan");

		scanBean.setDeviceState(DeviceState.RUNNING);
		scanBean.setPreviousDeviceState(DeviceState.RUNNING);

		scanBean.setStatus(Status.RUNNING);
		scanBean.setPreviousStatus(Status.RUNNING);

		scanBean.setPoint(completedSteps);
		scanBean.setMessage("Point " + completedSteps + " of " + scanBean.getSize());
		scanBean.setPercentComplete(((double) completedSteps / scanBean.getSize()) * 100);

		return new ScanBean(scanBean);
	}

	@Test
	public void testMalcolmScan() throws Exception {
		// Arrange. Use a special answer that collects
		// creates a answer that collects and clones the beans its called with - TODO move to setUp?
		BeanCollectingAnswer<ScanBean> beanCaptor = BeanCollectingAnswer.forClass(ScanBean.class, ScanBean::new);
		doAnswer(beanCaptor).when(publisher).broadcast(any(ScanBean.class));

		// Act: create and configure the scan
		final IRunnableDevice<ScanModel> scanner = createScan();

		// Assert: verify the correct events have been fired while configuring the scan
		expectedNumPublishedBeans = 2;
		verify(publisher, times(expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		List<ScanBean> beans = beanCaptor.getAllValues();
		// TODO for the moment the assertion below verifies what AcqusitionDevice currently does, so that the test passes
		// Before refactoring, these should be updated to reflect what we think the correct seqeuence is of
		// Status and DeviceState changes should be. Then the code should be refactored and fixed to pass the updated test
		assertThat(beans, hasSize(2));
		assertThat(beans.get(0), is(equalTo(createExpectedStateChangeBean(DeviceState.CONFIGURING, null, Status.SUBMITTED, null))));
		assertThat(beans.get(1), is(equalTo(createExpectedStateChangeBean(DeviceState.ARMED, DeviceState.CONFIGURING, Status.PREPARING, Status.SUBMITTED))));
		// TODO replace with assertThat collection contains?

		// Arrange: set up the malcolm connection to respond to the run message with a WaitingAnswer
		MalcolmMessage expectedRunMessage1 = createExpectedCallMessage(id++, MalcolmMethod.RUN, null);
		WaitingAnswer<MalcolmMessage> runAnswer = new WaitingAnswer<MalcolmMessage>(
				createExpectedMalcolmOkReply(null));
		when(malcolmConnection.send(malcolmDevice, expectedRunMessage1)).thenAnswer(runAnswer);

		// Act
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		scanner.start(null); // runs the scan in a separate thread

		// First inner scan
		runAnswer.waitUntilCalled();
		verify(publisher, times(++expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
		assertThat(beans, hasSize(2));
		ScanBean bean = beanCaptor.getValue();
		scanBean.setSize(200); // AcquisitionDevice has now set scan size and start time in the ScanBean
		scanBean.setStartTime(bean.getStartTime());
		assertThat(bean, is(equalTo(createExpectedStateChangeBean(DeviceState.RUNNING, DeviceState.ARMED, Status.RUNNING, Status.PREPARING))));

		// Send some updates to the completed steps listener and check an updated ScanBean is published
		checkCompletedSteps(beanCaptor);

		// Pause the scan and check the correct messages are sent
		checkPauseAndResumeScan(beanCaptor, scanner);

		// set up the malcolm connection to create a respond to the second run message
		MalcolmMessage expectedRunMessage2 = createExpectedCallMessage(id++, MalcolmMethod.RUN, null);
		when(malcolmConnection.send(malcolmDevice, expectedRunMessage2)).thenAnswer(runAnswer);
		// Resume the Answer, will cause the MalcolmDevice.run method to return and the first inner scan to end
		runAnswer.resume();

		// Second inner scan
		runAnswer.waitUntilCalled();

		runAnswer.resume();

		boolean scanCompleted = scanner.latch(10, TimeUnit.SECONDS);
		assertThat(scanCompleted, is(true));

		// Assert
		verify(malcolmConnection).send(malcolmDevice, expectedRunMessage1);
		verify(malcolmConnection).send(malcolmDevice, expectedRunMessage2);
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
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
		verify(publisher, times(expectedNumPublishedBeans += 2)).broadcast(any(ScanBean.class));
		List<ScanBean> beans = beanCaptor.getAllValues();
		assertThat(beans, hasSize(2));
		// TODO DAQ-1410, ScanBean should only have DeviceState set to PAUSED when the MalcolmDevice sends us
		// a state change with a DeviceState of PAUSED.
		assertThat(beans.get(0), is(equalTo(createExpectedStateChangeBean(
				DeviceState.SEEKING, DeviceState.RUNNING, Status.PAUSED, Status.RUNNING))));
		assertThat(beans.get(1), is(equalTo(createExpectedStateChangeBean(
				DeviceState.PAUSED, DeviceState.SEEKING, Status.PAUSED, Status.RUNNING))));

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
				DeviceState.RUNNING, DeviceState.PAUSED, Status.RESUMED, Status.RUNNING))));

		// send a state change event to say the malcolm device is running, currently this is ignored
		stateChangeListener.eventPerformed(createStateChangeEvent(DeviceState.RUNNING));
		verify(publisher, times(expectedNumPublishedBeans)).broadcast(any(ScanBean.class));
	}

	private MalcolmMessage createPositionEvent(int numSteps) {
		final MalcolmMessage message = new MalcolmMessage();

		final NumberAttribute stepsAttr = new NumberAttribute();
		stepsAttr.setName(COMPLETED_STEPS_ENDPOINT);
		stepsAttr.setValue(numSteps);
		message.setValue(stepsAttr);

		return message;
	}

	private MalcolmMessage createStateChangeEvent(DeviceState state) {
		final MalcolmMessage message = new MalcolmMessage();
		message.setEndpoint(STATE_ENDPOINT);

		final ChoiceAttribute stateAttr = new ChoiceAttribute();
		stateAttr.setName(STATE_ENDPOINT);
		stateAttr.setValue(state.toString());
		message.setValue(stateAttr);

		return message;
	}

	private IRunnableDevice<ScanModel> createScan() throws Exception {
		final GridModel gmodel = new GridModel(); // Note stage_x and stage_y scannables controlled by malcolm
		gmodel.setFastAxisName("stage_x");
		gmodel.setFastAxisPoints(10);
		gmodel.setSlowAxisName("stage_y");
		gmodel.setSlowAxisPoints(10);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		final IPointGenerator<?> gridGen = pointGenService.createGenerator(gmodel);
		final StepModel stepModel = new StepModel("stage_z", 0, 1, 1);
		final IPointGenerator<?> stepGen = pointGenService.createGenerator(stepModel);
		final IPointGenerator<?> gen = pointGenService.createCompoundGenerator(stepGen, gridGen);

		final ScanModel scanModel = new ScanModel();
		scanModel.setPositionIterable(gen);
		scanModel.setDetectors(malcolmDevice);

		// Create and configure the scanner (AcquisitionDevice) this calls some method on MalcolmDevice which in turn
		// call methods in the mocked communication layer, so we need to set up replies for those
		final MalcolmMessage axesToMoveReply = createExpectedMalcolmOkReply(new StringArrayAttribute("stage_x", "stage_y"));
		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES))).thenReturn(axesToMoveReply);
		when(malcolmConnection.send(malcolmDevice, createExpectedMalcolmMessage(id++, Type.GET, ATTRIBUTE_NAME_SIMULTANEOUS_AXES))).thenReturn(axesToMoveReply); // This is called at 2 different points

		return runnableDeviceService.createRunnableDevice(scanModel, publisher);
	}

}
