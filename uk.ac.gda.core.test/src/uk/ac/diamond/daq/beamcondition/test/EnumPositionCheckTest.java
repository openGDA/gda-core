/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.beamcondition.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import gda.device.EnumPositioner;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.beamcondition.EnumPositionCheck;


@RunWith(PowerMockRunner.class)
public class EnumPositionCheckTest {
	EnumPositioner scannable;

	private EnumPositionCheck condition;
	private ObservableComponent observable = new ObservableComponent();

	@Before
	public void setup() throws Exception {
		condition = new EnumPositionCheck();

		scannable = mock(EnumPositioner.class);
		when(scannable.getName()).thenReturn("scannable");
		when(scannable.getPosition()).thenReturn("Closed");

		doAnswer(i -> {
			observable.addIObserver(i.getArgumentAt(0, IObserver.class));
			return null;
		}).when(scannable).addIObserver(eq(condition));
		doAnswer(i -> {
			observable.deleteIObserver(i.getArgumentAt(0, IObserver.class));
			return null;
		}).when(scannable).deleteIObserver(eq(condition));

		condition.setPositioner(scannable);
	}

	@Test
	public void testNoLimits() throws Exception {
		observable.notifyIObservers(scannable, null);
		assertThat("Beam should be on with no limits", condition.beamOn());
	}

	@Test
	public void testScannableIsObserved() throws Exception {
		verify(scannable).addIObserver(condition);
	}

	@Test
	public void testScannableIsUnobservedWhenNewScannableSet() throws Exception {
		verify(scannable).addIObserver(condition);
		EnumPositioner secondScannable = mock(EnumPositioner.class);
		condition.setPositioner(secondScannable);
		verify(scannable).deleteIObserver(condition);
		verify(secondScannable).addIObserver(condition);
	}

	@Test
	public void testConditionName() throws Exception {
		assertThat("Condition name incorrect", condition.getName(), equalTo("PositionCheck(scannable (no restrictions))"));
		condition.setAllowedPositions("Open");
		assertThat("Condition name incorrect", condition.getName(), equalTo("PositionCheck(scannable is at Open)"));
		condition.setAllowedPositions("Open", "Opening");
		assertThat("Condition name incorrect", condition.getName(), equalTo("PositionCheck(scannable is at one of {Open, Opening})"));
		condition.setRestrictedPositions("Closed");
		assertThat("Condition name incorrect", condition.getName(), equalTo("PositionCheck(scannable is not at Closed)"));
		condition.setRestrictedPositions("Closed", "Closing");
		assertThat("Condition name incorrect", condition.getName(), equalTo("PositionCheck(scannable is not at one of {Closed, Closing})"));
		condition.setPositioner(null);
		assertThat("Condition name incorrect", condition.getName(), equalTo("PositionCheck(??? is not at one of {Closed, Closing})"));
	}

	@Test
	public void testUpdateTriggersPositionCheck() throws Exception {
		condition.setAllowedPositions("Open");

		when(scannable.getPosition()).thenReturn("Closed");
		observable.notifyIObservers(scannable, null);
		assertThat("Beam should be off", not(condition.beamOn()));

		when(scannable.getPosition()).thenReturn("Open");
		observable.notifyIObservers(scannable, null);
		assertThat("Beam should be on", condition.beamOn());
	}

	@Test
	public void testNullScannableIsntObserved() throws Exception {
		condition.setPositioner(null); // Shouldn't throw NPE
		verify(scannable).deleteIObserver(condition);
	}

	@Test
	public void testAllowedPositions() throws Exception {
		when(scannable.getPosition()).thenReturn("Open");
		condition.setAllowedPositions("Open");
		assertThat("Beam should be on", condition.beamOn());
		condition.setAllowedPositions("Closed");
		assertThat("Beam should be off", not(condition.beamOn()));
	}

	@Test
	public void testRestrictedPositions() throws Exception {
		when(scannable.getPosition()).thenReturn("Open");
		condition.setRestrictedPositions("Closed");
		assertThat("Beam should be on", condition.beamOn());
		condition.setRestrictedPositions("Closed");
		assertThat("Beam should be off", not(condition.beamOn()));

		condition.setRestrictedPositions();
		assertThat("Beam should be on when there are no restrictions", condition.beamOn());
	}

	@Test
	public void testNullScannablePositionIsUnknown() throws Exception {
		condition.setPositioner(null);
		condition.update(null, null);
		assertThat("Beam should be on when there is no positioner", condition.beamOn());
	}

	@Test
	public void testAllowedFactoryMethod() throws Exception {
		when(scannable.getPosition()).thenReturn("Open");
		EnumPositionCheck check = EnumPositionCheck.isAt(scannable, "Open");
		assertThat("Beam should be on", check.beamOn());
		when(scannable.getPosition()).thenReturn("Closed");
		assertThat("Beam should be off", not(check.beamOn()));
	}

	@Test
	public void testRestrictedFactoryMethod() throws Exception {
		when(scannable.getPosition()).thenReturn("Open");
		EnumPositionCheck check = EnumPositionCheck.isNotAt(scannable, "Closed");
		assertThat("Beam should be on", check.beamOn());
		when(scannable.getPosition()).thenReturn("Closed");
		assertThat("Beam should be off", not(check.beamOn()));
	}
}
