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

package gda.device.zebra;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class LogicGateConfigurationTest {

	@Test
	public void testGivenNoInputWhenSourceProvidedThenThrowError() {
		assertThrows(IllegalArgumentException.class, () -> new LogicGateConfiguration.Builder().source(1));
	}

	@Test
	public void testGivenNoInputWhenInvertProvidedThenThrowError() {
		assertThrows(IllegalArgumentException.class, () -> new LogicGateConfiguration.Builder().invert());
	}

	@Test
	public void testWhenInputLessThan1ProvidedThenThrowError() {
		assertThrows(IllegalArgumentException.class, () -> new LogicGateConfiguration.Builder().input(0));
	}

	@Test
	public void testWhenInputGreaterThan4ProvidedThenThrowError() {
		assertThrows(IllegalArgumentException.class, () -> new LogicGateConfiguration.Builder().input(5));
	}

	@Test
	public void testGivenInputWhenSourceLessThen0ProvidedThenThrowError() {
		assertThrows(IllegalArgumentException.class, () -> new LogicGateConfiguration.Builder().input(1).source(-1));
	}

	@Test
	public void testGivenInputWhenSourceGreaterThan63ProvidedThenThrowError() {
		assertThrows(IllegalArgumentException.class, () -> new LogicGateConfiguration.Builder().input(1).source(64));
	}

	@Test
	public void testGivenInputOf1WhenLogicGateConfigInspectedThenGivesExpectedOutput() {
		LogicGateConfiguration configuration = new LogicGateConfiguration.Builder().input(1).build();

		assertArrayEquals(configuration.getUse(), new boolean[] {true, false, false, false});
		assertArrayEquals(configuration.getSources(), new int[] {0, 0, 0, 0});
		assertArrayEquals(configuration.getInvert(), new boolean[] {false, false, false, false});
	}

	@Test
	public void testGivenInputOf2AndSourceOf37WhenLogicGateConfigInspectedThenGivesExpectedOutput() {
		LogicGateConfiguration configuration = new LogicGateConfiguration.Builder().input(2).source(37).build();

		assertArrayEquals(configuration.getUse(), new boolean[] {false, true, false, false});
		assertArrayEquals(configuration.getSources(), new int[] {0, 37, 0, 0});
		assertArrayEquals(configuration.getInvert(), new boolean[] {false, false, false, false});
	}

	@Test
	public void testGivenMultipleInputsAndSourcesWhenLogicGateConfigInspectedThenGivesExpectedOutput() {
		LogicGateConfiguration configuration = new LogicGateConfiguration.Builder().input(1).source(13).input(4).source(56).invert().build();

		assertArrayEquals(configuration.getUse(), new boolean[] {true, false, false, true});
		assertArrayEquals(configuration.getSources(), new int[] {13, 0, 0, 56});
		assertArrayEquals(configuration.getInvert(), new boolean[] {false, false, false, true});
	}

}
