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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the mapping and scaling functionalities of the FiveCircle class.
 */
class FiveCircleTest {
	private FiveCircle transform = new FiveCircle();

	@BeforeEach
	void setup() {
		transform.setFirstAngle(ReferenceGeometry.MU);

		transform.setFirstAngleValue(10.0);

		transform.setReferenceAnglesToBeamlineMotors(Map.of(
				ReferenceGeometry.DELTA, "motor1",
				ReferenceGeometry.NU, "motor3",
				ReferenceGeometry.CHI, "motor4",
				ReferenceGeometry.PHI, "motor2",
				ReferenceGeometry.ETA, "motor5"
				));

		transform.setReferenceScaling(Map.of(
				ReferenceGeometry.CHI, Map.of(
						AngleScaling.ADD, -180.0,
						AngleScaling.MULT, 10.0
						)
				));
	}

	@Test
	void testGetReferenceGeometry() {
		Map<String, Double> beamline = Map.of(
				"motor1", 10.0,
				"motor2", 20.0,
				"motor3", 30.0,
				"motor4", 40.0,
				"motor5", 50.0
				);

		Map<ReferenceGeometry, Double> reference = transform.getReferenceGeometry(beamline);

		assertThat(10.0, is(closeTo(reference.get(ReferenceGeometry.MU), 1e-16)));
		assertThat(beamline.get("motor1"), is(closeTo(reference.get(ReferenceGeometry.DELTA), 1e-16)));
		assertThat(beamline.get("motor3"), is(closeTo(reference.get(ReferenceGeometry.NU), 1e-16)));
		assertThat(beamline.get("motor5"), is(closeTo(reference.get(ReferenceGeometry.ETA), 1e-16)));
		assertThat(beamline.get("motor4")* 10.0 + -180.0, is(closeTo(reference.get(ReferenceGeometry.CHI), 1e-16)));
		assertThat(beamline.get("motor2"), is(closeTo(reference.get(ReferenceGeometry.PHI), 1e-16)));
	}

	@Test
	void testGetReferenceGeometryWrongMotors() {
		Map<String, Double> reference = Map.of(
				"motor1", 10.0,
				"motor2", 20.0,
				"motor5", 30.0,
				"motor4", 40.0,
				"obviously wrong motor name", 50.0
				);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transform.getReferenceGeometry(reference));
		String message = exception.getMessage();

		assertThat(message, is(equalTo("Beamline position non-valid, check motor names and number of motors.")));
	}

	@Test
	void testGetReferenceGeometryTooManyMotors() {
		Map<String, Double> reference = Map.of(
				"motor1", 10.0,
				"motor2", 20.0,
				"motor3", 30.0,
				"motor4", 40.0,
				"motor5", 50.0,
				"motor6", 60.0
				);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transform.getReferenceGeometry(reference));
		String message = exception.getMessage();

		assertThat(message, is(equalTo("Beamline position non-valid, check motor names and number of motors.")));
	}

	@Test
	void testGetBeamlineGeometry() {
		Map<ReferenceGeometry, Double> reference = Map.of(
				ReferenceGeometry.MU, 10.0,
				ReferenceGeometry.DELTA, 20.0,
				ReferenceGeometry.NU, 30.0,
				ReferenceGeometry.ETA, 40.0,
				ReferenceGeometry.CHI, 50.0,
				ReferenceGeometry.PHI, 60.0
				);

		Map<String, Double> beamline = transform.getBeamlineGeometry(reference);

		assertThat(reference.get(ReferenceGeometry.DELTA), is(closeTo(beamline.get("motor1"), 1e-16)));
		assertThat(reference.get(ReferenceGeometry.PHI), is(closeTo(beamline.get("motor2"), 1e-16)));
		assertThat(reference.get(ReferenceGeometry.NU), is(closeTo(beamline.get("motor3"), 1e-16)));
		assertThat((reference.get(ReferenceGeometry.CHI) + 180.0)/10.0, is(closeTo(beamline.get("motor4"), 1e-16)));
		assertThat(reference.get(ReferenceGeometry.ETA), is(closeTo(beamline.get("motor5"), 1e-16)));
	}

	@Test
	void testGetBeamlineGeometryWrongFixedAngles() {
		Map<ReferenceGeometry, Double> reference = Map.of(
				ReferenceGeometry.MU, 100.0,
				ReferenceGeometry.DELTA, 20.0,
				ReferenceGeometry.NU, 30.0,
				ReferenceGeometry.ETA, 40.0,
				ReferenceGeometry.CHI, 50.0,
				ReferenceGeometry.PHI, 60.0
				);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transform.getBeamlineGeometry(reference));
		String message = exception.getMessage();

		assertThat(message, is(equalTo("Reference position non-valid, check fixed angle values.")));
	}

	@Test
	void testDefaultConstraints() {
		Map<String, Double> cons = transform.getDefaultConstraints();
		assertThat(10.0, is(closeTo(cons.get("mu"), 1e-16)));
		assertThat(1, is(equalTo(cons.size())));
	}
}
