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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import gda.device.Scannable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.beamcondition.ScannableThresholdCheck;


@RunWith(PowerMockRunner.class)
public class ScannableThresholdConditionTest {
	@Mock Scannable scannable;

	private ScannableThresholdCheck condition;
	private ObservableComponent observable = new ObservableComponent();

	@Before
	public void setup() throws Exception {
		when(scannable.getName()).thenReturn("scannable");
		when(scannable.getPosition()).thenReturn(42.0);
		doAnswer(i -> {
			observable.addIObserver(i.getArgumentAt(0, IObserver.class));
			return null;
		}).when(scannable).addIObserver(condition);
		condition = new ScannableThresholdCheck();
		condition.setScannable(scannable);
	}

	@Test
	public void testNoLimits() throws Exception {
		condition.update(scannable, null);
		assertThat("Beam should be on with no limits", condition.beamOn());
	}

	@Test
	public void testJustLowerLimit() throws Exception {
		condition.setLowerLimit(43);
		assertThat("Beam should be off when below lower limit", not(condition.beamOn()));

		condition.setLowerLimit(42);
		assertThat("Beam should be on when at lower limit", condition.beamOn());

		condition.setLowerLimit(41);
		assertThat("Beam should be on when above lower limit", condition.beamOn());
	}

	@Test
	public void testJustUpperLimit() throws Exception {
		condition.setUpperLimit(41);
		assertThat("Beam should be off when above upper limit", not(condition.beamOn()));

		condition.setUpperLimit(42);
		assertThat("Beam should be off when at upper limit", not(condition.beamOn()));

		condition.setUpperLimit(43);
		assertThat("Beam should be on when below upper limit", condition.beamOn());
	}

	@Test
	public void testBothLimits() throws Exception {
		// below both limits
		condition.setLowerLimit(43);
		condition.setUpperLimit(83);
		assertThat("Beam should be off when below both limits", not(condition.beamOn()));

		// equal to lower limit
		condition.setLowerLimit(42);
		condition.setUpperLimit(45);
		assertThat("Beam should be on when at lower limit", condition.beamOn());

		// between limits
		condition.setLowerLimit(41);
		condition.setUpperLimit(43);
		assertThat("Beam should be on when between limits", condition.beamOn());

		// equal to upper limit
		condition.setLowerLimit(41);
		condition.setUpperLimit(42);
		assertThat("Beam should be off when at upper limit", not(condition.beamOn()));

		// above both limits
		condition.setLowerLimit(21);
		condition.setUpperLimit(41);
		assertThat("Beam should be off when above upper limit", not(condition.beamOn()));
	}

	@Test
	public void testScannableIsObserved() throws Exception {
		verify(scannable).addIObserver(condition);
	}

	@Test
	public void testScannableIsUnobservedWhenNewScannableSet() throws Exception {
		verify(scannable).addIObserver(condition);
		Scannable secondScannable = mock(Scannable.class);
		condition.setScannable(secondScannable);
		verify(scannable).deleteIObserver(condition);
		verify(secondScannable).addIObserver(condition);
	}

	@Test
	public void testConditionName() throws Exception {
		assertThat("Condition name incorrect", condition.getName(), equalTo("ScannableThresholdCheck(scannable (no limits))"));
		condition.setLowerLimit(23);
		assertThat("Condition name incorrect", condition.getName(), equalTo("ScannableThresholdCheck(23.0 < scannable)"));
		condition.setUpperLimit(45);
		assertThat("Condition name incorrect", condition.getName(), equalTo("ScannableThresholdCheck(23.0 < scannable < 45.0)"));
		condition.setLowerLimit(Double.NaN);
		assertThat("Condition name incorrect", condition.getName(), equalTo("ScannableThresholdCheck(scannable < 45.0)"));

		condition.setScannable(null);
		assertThat("Condition name incorrect", condition.getName(), equalTo("ScannableThresholdCheck(??? < 45.0)"));
	}

	@Test
	public void testUpdateTriggersPositionCheck() throws Exception {
		condition.setLowerLimit(20);
		condition.setUpperLimit(60);
		assertThat("Beam should be on", condition.beamOn());

		when(scannable.getPosition()).thenReturn(10);
		observable.notifyIObservers(scannable, null);
		assertThat("Beam should be off", not(condition.beamOn()));

		when(scannable.getPosition()).thenReturn(42);
		observable.notifyIObservers(scannable, null);
		assertThat("Beam should be on", condition.beamOn());

		when(scannable.getPosition()).thenReturn(62);
		observable.notifyIObservers(scannable, null);
		assertThat("Beam should be off", not(condition.beamOn()));
	}

	@Test
	public void testNullScannableIsntObserved() throws Exception {
		condition.setScannable(null); // Shouldn't throw NPE
		verify(scannable).deleteIObserver(condition);
	}
}
