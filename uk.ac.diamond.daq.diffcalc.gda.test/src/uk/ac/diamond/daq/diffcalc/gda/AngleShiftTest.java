/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.diffcalc.gda.AngleShift;

class AngleShiftTest {

	AngleShift shift = new AngleShift(Map.of("motor1", 0.0));

	@Test
	void testCutAngleDefault() {
		double value = shift.cutAngle("motor2", 181.0);
		assertEquals(-179.0, value);
	}

	@Test
	void testCutAngleNamed() {
		double value = shift.cutAngle("motor1", 181.0);
		assertEquals(181.0, value);
	}

	@Test
	void testCutAngles() {
		Map<String, Double> angles = Map.of("motor1", 190.0, "motor2", 190.0);

		Map<String, Double> cutAngles = shift.cutAngles(angles);

		assertEquals(190.0, cutAngles.get("motor1"));
		assertEquals(-170.0, cutAngles.get("motor2"));
	}

}
