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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import uk.ac.diamond.daq.diffcalc.gda.AngleScaling;
import uk.ac.diamond.daq.diffcalc.gda.FourCircle;
import uk.ac.diamond.daq.diffcalc.gda.ReferenceGeometry;

/**
 * Test the mapping and scaling functionalities of the FourCircle class.
 */
@TestInstance(Lifecycle.PER_CLASS)
class FourCircleTest {

	FourCircle transform = new FourCircle();

	@BeforeAll
	void setup() {
		transform.setFirstAngle(ReferenceGeometry.MU);
		transform.setSecondAngle(ReferenceGeometry.ETA);

		transform.setFirstAngleValue(10.0);
		transform.setSecondAngleValue(90.0);

		transform.setReferenceAnglesToBeamlineMotors(Map.of(
				ReferenceGeometry.DELTA, "motor1",
				ReferenceGeometry.NU, "motor3",
				ReferenceGeometry.CHI, "motor4",
				ReferenceGeometry.PHI, "motor2"
				));

		transform.setReferenceScaling(Map.of(
				ReferenceGeometry.DELTA, Map.of(
						AngleScaling.ADD, -180.0,
						AngleScaling.MULT, 2.0
						)
				));
	}

	@Test
	void testGetReferenceGeometry() {
		Map<String, Double> beamline = Map.of(
				"motor1", 10.0,
				"motor2", 20.0,
				"motor3", 30.0,
				"motor4", 40.0
				);

		Map<ReferenceGeometry, Double> reference = transform.getReferenceGeometry(beamline);

		assertEquals(10.0, reference.get(ReferenceGeometry.MU));
		assertEquals(-160.0, reference.get(ReferenceGeometry.DELTA));
		assertEquals(30.0, reference.get(ReferenceGeometry.NU));
		assertEquals(90.0, reference.get(ReferenceGeometry.ETA));
		assertEquals(40.0, reference.get(ReferenceGeometry.CHI));
		assertEquals(20.0, reference.get(ReferenceGeometry.PHI));
	}

	@Test
	void testGetReferenceGeometryWrongMotors() {
		Map<String, Double> reference = Map.of(
				"motor1", 10.0,
				"motor2", 20.0,
				"motor5", 30.0,
				"motor4", 40.0
				);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transform.getReferenceGeometry(reference));

		String expectedMessage = "Beamline position non-valid, check motor names and number of motors.";
		String actualMessage = exception.getMessage();

		assertEquals(actualMessage, expectedMessage);
	}

	@Test
	void testGetReferenceGeometryTooManyMotors() {
		Map<String, Double> reference = Map.of(
				"motor1", 10.0,
				"motor2", 20.0,
				"motor3", 30.0,
				"motor4", 40.0,
				"motor5", 50.0
				);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transform.getReferenceGeometry(reference));

		String expectedMessage = "Beamline position non-valid, check motor names and number of motors.";
		String actualMessage = exception.getMessage();

		assertEquals(actualMessage, expectedMessage);
	}

	@Test
	void testGetBeamlineGeometry() {
		Map<ReferenceGeometry, Double> reference = Map.of(
				ReferenceGeometry.MU, 10.0,
				ReferenceGeometry.DELTA, -160.0,
				ReferenceGeometry.NU, 30.0,
				ReferenceGeometry.ETA, 90.0,
				ReferenceGeometry.CHI, 40.0,
				ReferenceGeometry.PHI, 20.0
				);

		Map<String, Double> beamline = transform.getBeamlineGeometry(reference);

		assertEquals(10.0, beamline.get("motor1"));
		assertEquals(20.0, beamline.get("motor2"));
		assertEquals(30.0, beamline.get("motor3"));
		assertEquals(40.0, beamline.get("motor4"));
	}

	@Test
	void testGetBeamlineGeometryWrongFixedAngles() {
		Map<ReferenceGeometry, Double> reference = Map.of(
				ReferenceGeometry.MU, 100.0,
				ReferenceGeometry.DELTA, -160.0,
				ReferenceGeometry.NU, 30.0,
				ReferenceGeometry.ETA, 90.0,
				ReferenceGeometry.CHI, 40.0,
				ReferenceGeometry.PHI, 20.0
				);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transform.getBeamlineGeometry(reference));

		String expectedMessage = "Reference position non-valid, check fixed angle values.";
		String actualMessage = exception.getMessage();

		assertEquals(actualMessage, expectedMessage);
	}

	@Test
	void testDefaultConstraints() {
		Map<String, Double> cons = transform.defaultConstraints();
		assertEquals(10.0, cons.get("mu"));
		assertEquals(90.0, cons.get("eta"));
	}
}
