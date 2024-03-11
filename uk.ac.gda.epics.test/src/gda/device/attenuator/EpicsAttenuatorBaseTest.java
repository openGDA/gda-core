/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.device.attenuator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class EpicsAttenuatorBaseTest {

	private EpicsController mockEpicsController;

	class TestAttenuator extends EpicsAttenuatorBase {

		private boolean[] desired;
		private boolean[] actual;

		public TestAttenuator(EpicsController controller, boolean[] desired, boolean[] actual) {
			super(controller);
			this.desired = desired;
			this.actual = actual;
			useCurrentEnergy = mock(Channel.class);
			desiredTransmission = mock(Channel.class);
			change = mock(Channel.class);
			actualTransmission = mock(Channel.class);
		}

		public Channel getUseCurrentEnergyChannel() {
			return useCurrentEnergy;
		}
		public Channel getDesiredTransmissionChannel() {
			return desiredTransmission;
		}
		public Channel getChangeChannel() {
			return change;
		}

		@Override
		public boolean[] getFilterPositions() throws DeviceException {
			return actual;
		}

		@Override
		public boolean[] getDesiredFilterPositions() throws DeviceException {
			return desired;
		}

		@Override
		public int getNumberFilters() throws DeviceException {
			return 0;
		}

		@Override
		public String[] getFilterNames() throws DeviceException {
			return null;
		}

	}

	@Before
	public void setup() {
		mockEpicsController = mock(EpicsController.class);
	}

	private TestAttenuator testAttenuatorWithValidFilters() {
		boolean[] desired = {true};
		boolean[] actual = {true};
		return new TestAttenuator(mockEpicsController, desired, actual);
	}


	@Test
	public void testWhenSetTransmissionThenDesiredEnergySet() throws DeviceException, CAException, InterruptedException {
		var attenuator = testAttenuatorWithValidFilters();
		attenuator.setTransmission(20);
		verify(mockEpicsController).caput(attenuator.getDesiredTransmissionChannel(), 20.0d);
	}

	@Test
	public void testWhenSetTransmissionThenUseCurrentEnergySet() throws DeviceException, CAException, InterruptedException {
		var attenuator = testAttenuatorWithValidFilters();
		attenuator.setTransmission(20);
		verify(mockEpicsController).caput(attenuator.getUseCurrentEnergyChannel(), 1);
	}

	@Test
	public void testWhenSetTransmissionThenChangeSet() throws DeviceException, CAException, InterruptedException, TimeoutException {
		var attenuator = testAttenuatorWithValidFilters();
		attenuator.setTransmission(20);
		verify(mockEpicsController).caputWait(attenuator.getChangeChannel(), 1);
	}

	@Test
	public void testGivenDeviceGoesReadyWhenSetTransmissionThenReturnsExpectedTransmission() throws DeviceException, CAException, InterruptedException, TimeoutException {
		double expectedTransmission = 34.5;
		var attenuator = testAttenuatorWithValidFilters();
		when(mockEpicsController.cagetDouble(attenuator.actualTransmission)).thenReturn(expectedTransmission);
		var measuredTransmission = attenuator.setTransmission(20);
		assertEquals(expectedTransmission, measuredTransmission, 0.1);
	}

	@Test
	public void testGivenDeviceDoesNotGoReadyWhenSetTransmissionThenAllExpectedPVsSetButThrowsTimeout() throws CAException, InterruptedException, TimeoutException {
		mockEpicsController = mock(EpicsController.class);
		double expectedTransmission = 34.5;
		boolean[] desired = {false};
		boolean[] actual = {true};
		LocalProperties.set("gda.px.attenuator.timeout", "100");
		var attenuator = new TestAttenuator(mockEpicsController, desired, actual);
		when(mockEpicsController.cagetDouble(attenuator.actualTransmission)).thenReturn(expectedTransmission);
		assertThrows(DeviceException.class, () -> attenuator.setTransmission(20));
		verify(mockEpicsController).caput(attenuator.getUseCurrentEnergyChannel(), 1);
		verify(mockEpicsController).caput(attenuator.getDesiredTransmissionChannel(), 20.0d);
		verify(mockEpicsController).caputWait(attenuator.getChangeChannel(), 1);
	}

}
