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

package uk.ac.gda.server.ncd.timing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gda.device.DeviceException;
import gda.observable.IObserver;
import uk.ac.gda.server.ncd.timing.data.SimpleTimerConfiguration;

public class DefaultTimerControllerTest {
	private static final double exposure = 0.2;
	private static final int numberOfFrames = 10;
	private static final boolean delay = true;
	private static final double delayTime = 0.3;
	private static final double minimumDelay = 0.1;

	private class UpdateObserver implements IObserver {
		private boolean updated = false;
		private boolean called = false;
		private SimpleTimerConfiguration simpleTimerConfiguration;

		private UpdateObserver (SimpleTimerConfiguration simpleTimerConfiguration) {
			this.simpleTimerConfiguration = simpleTimerConfiguration;
		}

		@Override
		public void update(Object source, Object arg) {
			called = true;
			SimpleTimerConfiguration update = (SimpleTimerConfiguration)arg;
			if (update.getNumberOfFrames() == simpleTimerConfiguration.getNumberOfFrames()
					&& update.getExposure() == simpleTimerConfiguration.getExposure()
					&& update.isDelay() == simpleTimerConfiguration.isDelay()
					&& update.getDelayTime() == simpleTimerConfiguration.getDelayTime()) {
				updated = true;
			}
		}

		public boolean isUpdated() {
			return updated;
		}

		public boolean isCalled() {
			return called;
		}
	}

	private DefaultTimerController defaultTimerController;

	@Mock
	private HardwareTimer hardwareTimer;

	@Before
	public void setUp () {
		MockitoAnnotations.initMocks(this);
		
		when(hardwareTimer.getMinimumDelay()).thenReturn(minimumDelay);

		defaultTimerController = new DefaultTimerController(hardwareTimer, exposure, numberOfFrames, delay, delayTime);
	}

	@Test
	public void initialised () {
		SimpleTimerConfiguration simpleTimerConfiguration = defaultTimerController.getLastUsedConfiguration();

		assertTrue("Frames", simpleTimerConfiguration.getNumberOfFrames() == numberOfFrames);
		assertTrue("Exposure", simpleTimerConfiguration.getExposure() == exposure);
		assertTrue("Delay", simpleTimerConfiguration.isDelay() == delay);
		assertTrue("DelayTime", simpleTimerConfiguration.getDelayTime() == delayTime);
	}

	@Test
	public void configureTimer_withDelay () {
		SimpleTimerConfiguration simpleTimerConfiguration = new SimpleTimerConfiguration();
		simpleTimerConfiguration.setNumberOfFrames(2);
		simpleTimerConfiguration.setExposure(20.0);
		simpleTimerConfiguration.setDelay(true);
		simpleTimerConfiguration.setDelayTime(1.0);

		UpdateObserver updateObserver = new UpdateObserver(simpleTimerConfiguration);
		defaultTimerController.addIObserver(updateObserver);


		try {
			defaultTimerController.configureTimer(simpleTimerConfiguration);
			verify(hardwareTimer).configureTimer(2, 20.0, 1.0);
		} catch (DeviceException e) {
			fail("Device exception thrown");
		} catch (HardwareTimerException e) {
			fail("Hardware Timer Exception thrown");
		}

		assertTrue("Observers not updated", updateObserver.isUpdated() && updateObserver.isCalled());
	}

	@Test
	public void configureTimer_withNoDelay () {
		//when(hardwareTimer).thenReturn(value)
		SimpleTimerConfiguration simpleTimerConfiguration = new SimpleTimerConfiguration();
		simpleTimerConfiguration.setNumberOfFrames(2);
		simpleTimerConfiguration.setExposure(20.0);
		simpleTimerConfiguration.setDelay(false);
		simpleTimerConfiguration.setDelayTime(1.0);

		UpdateObserver updateObserver = new UpdateObserver(simpleTimerConfiguration);
		defaultTimerController.addIObserver(updateObserver);

		try {
			defaultTimerController.configureTimer(simpleTimerConfiguration);
			verify(hardwareTimer).configureTimer(2, 20.0, minimumDelay);
		} catch (DeviceException e) {
			fail("Device exception thrown");
		} catch (HardwareTimerException e) {
			fail("Hardware Timer Exception thrown");
		}

		assertTrue("Observers not updated", updateObserver.isUpdated() && updateObserver.isCalled());
	}

	@Test
	public void configureTimer_failed () throws DeviceException, HardwareTimerException {
		doThrow(new DeviceException("duff")).when(hardwareTimer).configureTimer(anyInt(), anyDouble(), anyDouble());

		SimpleTimerConfiguration simpleTimerConfiguration = new SimpleTimerConfiguration();
		simpleTimerConfiguration.setNumberOfFrames(2);
		simpleTimerConfiguration.setExposure(20.0);
		simpleTimerConfiguration.setDelay(false);
		simpleTimerConfiguration.setDelayTime(1.0);

		UpdateObserver updateObserver = new UpdateObserver(simpleTimerConfiguration);
		defaultTimerController.addIObserver(updateObserver);

		assertFalse("Timer configured", defaultTimerController.configureTimer(simpleTimerConfiguration));

		assertFalse("Observers updated", updateObserver.isCalled());
	}
}
