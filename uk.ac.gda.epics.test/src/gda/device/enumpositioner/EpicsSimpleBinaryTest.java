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

package gda.device.enumpositioner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_STS_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

public class EpicsSimpleBinaryTest {

	private final String POSITIONER_NAME = "TestPositioner";
	private final String PV_NAME = "BLXXI-POS-01:POS";
	private final String PV_NAME_WITH_COLON = "BLXXI-POS-01:POS:";

	private EpicsSimpleBinary positioner;

	private EpicsController controller;
	private Channel controlChannel;
	private EpicsChannelManager channelManager;
	private IObserver observer;

	@Before
	public void setUp() throws Exception {
		observer = mock(IObserver.class);

		// Allow injection of mock Epics controller-related classes
		final Class<EpicsSimpleBinary> positionerClass = EpicsSimpleBinary.class;
		final Field controllerField = positionerClass.getDeclaredField("controller");
		controllerField.setAccessible(true);
		final Field channelManagerField = positionerClass.getDeclaredField("channelManager");
		channelManagerField.setAccessible(true);

		// Create positioner with mock Epics support
		controller = mock(EpicsController.class);
		controlChannel = mock(Channel.class);
		channelManager = mock(EpicsChannelManager.class);
		when(channelManager.createChannel(anyString(), any(MonitorListener.class), any(MonitorType.class), anyBoolean())).thenReturn(controlChannel);
		when(controller.cagetLabels(any(Channel.class))).thenReturn(new String[] { "Open", "Close" });

		positioner = new EpicsSimpleBinary();
		positioner.setName(POSITIONER_NAME);
		positioner.addIObserver(observer);

		// When channelManager.creationPhaseCompleted is called, pretend the channel used by EpicsSimpleBinary becomes
		// connected immediately, and call EpicsSimpleBinary.initializationCompleted
		Mockito.doAnswer(invocation -> {
			final Method initializationCompletedMethod = EpicsSimpleBinary.class.getDeclaredMethod("initializationCompleted");
			initializationCompletedMethod.setAccessible(true);
			initializationCompletedMethod.invoke(positioner);
			return null;
		}).when(channelManager).creationPhaseCompleted();

		controllerField.set(positioner, controller);
		channelManagerField.set(positioner, channelManager);
	}

	@Test(expected = FactoryException.class)
	public void testConfigureNoEpicsInfoSet() throws Exception {
		positioner.configure();
	}

	@Test
	public void testConfigureWithPvName() throws Exception {
		positioner.setPvName(PV_NAME_WITH_COLON);
		positioner.configure();

		// If PV name is set, configure() preserves trailing colon
		verify(channelManager).createChannel(eq(PV_NAME_WITH_COLON), any(MonitorListener.class), eq(MonitorType.STS), eq(false));
		verify(channelManager).tryInitialize(100);
		checkPositionerConfigured();
	}

	/**
	 * No matter how the positioner is initialised, we expect configure() to:<br>
	 * - set the configured flag<br>
	 * - set a single input name (the name of the positioner)<br>
	 * - set output format to be a string
	 */
	private void checkPositionerConfigured() {
		assertTrue(positioner.isConfigured());

		final String[] inputNames = positioner.getInputNames();
		assertEquals(1, inputNames.length);
		assertEquals(POSITIONER_NAME, inputNames[0]);

		final String[] outputFormat = positioner.getOutputFormat();
		assertEquals(1, outputFormat.length);
		assertEquals("%s", outputFormat[0]);
	}

	private void configureWithPv() throws Exception {
		positioner.setPvName(PV_NAME);
		positioner.configure();
	}

	@Test
	public void testGetStatus() throws Exception {
		// getStatus() always returns IDLE
		assertEquals(EnumPositionerStatus.IDLE, positioner.getStatus());
	}

	@Test
	public void testIsBusy() throws Exception {
		// isBusy() always returns false
		assertFalse(positioner.isBusy());
	}

	@Test(expected = DeviceException.class)
	public void testRawAsynchronousMoveToNotConfigured() throws Exception {
		positioner.rawAsynchronousMoveTo("Open");
	}

	@Test(expected = DeviceException.class)
	public void testRawAsynchronousMoveToReadOnly() throws Exception {
		configureWithPv();
		positioner.setReadonly(true);
		positioner.rawAsynchronousMoveTo("Open");
	}

	@Test(expected = DeviceException.class)
	public void testRawAsynchronousMoveToInvalidPosition() throws Exception {
		configureWithPv();
		positioner.rawAsynchronousMoveTo("InvalidPos");
	}

	@Test
	public void testRawAsynchronousMoveToValidPositionSucceeds() throws Exception {
		// caput() succeeds
		rawAsynchronousMoveToValidPosition(CAStatus.NORMAL);
	}

	@Test
	public void testRawAsynchronousMoveToValidPositionFails() throws Exception {
		// caput() fails: no exception thrown
		rawAsynchronousMoveToValidPosition(CAStatus.PUTFAIL);
	}

	/**
	 * Simulate calling rawAsynchronousMoveTo() followed by firing a PutEvent with the given status
	 *
	 * @param status
	 *            The status of the PutEvent to be fired
	 * @throws Exception
	 */
	private void rawAsynchronousMoveToValidPosition(CAStatus status) throws Exception {
		configureWithPv();
		positioner.rawAsynchronousMoveTo("Open");

		final ArgumentCaptor<PutListener> putListenerCaptor = ArgumentCaptor.forClass(PutListener.class);
		verify(controller).caput(eq(controlChannel), eq("Open"), putListenerCaptor.capture());

		putListenerCaptor.getValue().putCompleted(new PutEvent(controlChannel, DBRType.STRING, 1, status));
	}

	@Test
	public void testMonitorChangedEnum() throws Exception {
		final DBR_Enum dbrEnum = new DBR_Enum();
		final int dbrValue = dbrEnum.getEnumValue()[0];
		testMonitorChanged(dbrEnum, dbrValue);
	}

	@Test
	public void testMonitorChangedStsEnum() throws Exception {
		final DBR_STS_Enum dbrStsEnum = new DBR_STS_Enum();
		final int dbrValue = dbrStsEnum.getEnumValue()[0];
		testMonitorChanged(dbrStsEnum, dbrValue);
	}

	/**
	 * Test the result of firing a MonitorEvent:<br>
	 * - capture the MonitorListener that is created when the control channel is created<br>
	 * - fire a MonitorEvent on this listener<br>
	 * - check that the positioner updates its observer with the appropriate event type
	 *
	 * @param dbr
	 *            Type of the event fired
	 * @param expectedValue
	 *            The value that the positioner should report to its observers (= the ordinal value of the event type)
	 * @throws Exception
	 */
	private void testMonitorChanged(DBR dbr, int expectedValue) throws Exception {
		configureWithPv();

		final ArgumentCaptor<MonitorListener> monitorListenerCaptor = ArgumentCaptor.forClass(MonitorListener.class);
		verify(channelManager).createChannel(eq(PV_NAME), monitorListenerCaptor.capture(), eq(MonitorType.STS), eq(false));

		monitorListenerCaptor.getValue().monitorChanged(new MonitorEvent(controlChannel, dbr, CAStatus.NORMAL));
		verify(observer).update(positioner, expectedValue);
	}

	/**
	 * An event other than DBR_Enum or DBR_STS_Enum is an error and will not cause the observer to be called
	 *
	 * @throws Exception
	 */
	@Test
	public void testMonitorChangedInvalidEventType() throws Exception {
		configureWithPv();
		reset(observer); // configuration may have sent events to observer, so clear them here

		final ArgumentCaptor<MonitorListener> monitorListenerCaptor = ArgumentCaptor.forClass(MonitorListener.class);
		verify(channelManager).createChannel(eq(PV_NAME), monitorListenerCaptor.capture(), eq(MonitorType.STS), eq(false));

		monitorListenerCaptor.getValue().monitorChanged(new MonitorEvent(controlChannel, new DBR_Byte(), CAStatus.NORMAL));
		verify(observer, never()).update(any(Object.class), any(Object.class));
	}

	@Test
	public void testGetPositions() throws Exception {
		configureWithPv();
		final List<String> expectedPositions = Arrays.asList("Open", "Close");
		assertEquals(expectedPositions, positioner.getPositionsList());
	}

	@Test
	public void testGetPositionsEpicsFails() throws Exception {
		when(controller.cagetLabels(any(Channel.class))).thenThrow(new CAException("Exception getting positions"));
		configureWithPv();
		assertTrue(positioner.getPositionsList().isEmpty());
	}

	@Test
	public void testSetPositionsArray() throws Exception {
		final String[] positions = new String[] { "On", "Off" };
		positioner.setPositions(positions);
		assertTrue(Arrays.equals(positions, positioner.getPositions()));
	}

	@Test
	public void testSetPositionsList() {
		final List<String> positions = Arrays.asList("On", "Off");
		positioner.setPositions(positions);
		assertEquals(positions, positioner.getPositionsList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPositionsTooFewElements() {
		positioner.setPositions(Arrays.asList("On"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPositionsTooManyElements() {
		positioner.setPositions(Arrays.asList("On", "Off", "Intermediate"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPositionsNullElement() {
		positioner.setPositions(Arrays.asList("On", null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPositionsEmptyElement() {
		positioner.setPositions(Arrays.asList("On", ""));
	}

	@Test
	public void testGetPosition() throws Exception {
		configureWithPv();
		when(controller.cagetString(any(Channel.class))).thenReturn("Open");
		assertEquals("Open", positioner.getPosition());
	}

	@Test
	public void testCheckPositionValid() throws Exception {
		configureWithPv();
		assertNull(positioner.checkPositionValid("Open"));
	}

	@Test
	public void testCheckPositionValidNotString() throws Exception {
		configureWithPv();
		assertEquals("position not a string", positioner.checkPositionValid(42));
	}

	@Test
	public void testCheckPositionValidWrongValue() throws Exception {
		configureWithPv();
		assertEquals("InvalidPos not in array of acceptable strings", positioner.checkPositionValid("InvalidPos"));
	}
}
