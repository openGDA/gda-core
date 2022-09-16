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
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import uk.ac.diamond.daq.beamcondition.BeamConditionWrapper;



public class BeamConditionWrapperTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Mock BooleanSupplier function;

	private BeamConditionWrapper condition;

	@BeforeEach
	public void setup() throws Exception {
		condition = new BeamConditionWrapper("condition", function);
	}

	@Test
	public void testConditionCallsFunction() throws Exception {
		when(function.getAsBoolean()).thenReturn(true);
		assertThat("Beam should be on", condition.beamOn());
		verify(function).getAsBoolean();

		reset(function);

		when(function.getAsBoolean()).thenReturn(false);
		assertThat("Beam should be off", not(condition.beamOn()));
		verify(function).getAsBoolean();
	}

	@Test
	public void testResultIsNotCached() throws Exception {
		when(function.getAsBoolean()).thenReturn(true);
		assertThat("Beam should be on", condition.beamOn());
		assertThat("Beam should be on", condition.beamOn());
		assertThat("Beam should be on", condition.beamOn());
		assertThat("Beam should be on", condition.beamOn());
		assertThat("Beam should be on", condition.beamOn());
		verify(function, times(5)).getAsBoolean();
	}

}
