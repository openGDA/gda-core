/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.points;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PointsTest extends AbstractGeneratorTest {

	@Test
	void testTranslationInvariant() throws GeneratorException {
		// Expected behaviour for integer range
		var model = new AxialPointsModel("Temperature", 290, 300, 11);
		var<AxialPointsModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300);

		// Behaviour consistent when translated in axis
		model = new AxialPointsModel("Temperature", 0, 10, 11);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		// Including across 0
		model = new AxialPointsModel("Temperature", -1, 9, 11);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
	}

	@Test
	void testTranslationInvariantBoundsFit() throws GeneratorException {
		var model = new AxialPointsModel("Temperature", 290, 300, 10);
		model.setBoundsToFit(true);

		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 290.5, 291.5, 292.5, 293.5, 294.5, 295.5, 296.5, 297.5, 298.5, 299.5);

		// Including across 0
		model = new AxialPointsModel("Temperature", -1, 9, 10);
		model.setBoundsToFit(true);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, -0.5, 0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5);
	}

	@Test
	void testTranslationInvariantNonInt() throws GeneratorException {
		// Behaviour consistent when translated in axis
		var model = new AxialPointsModel("Temperature", 0.4, 10.4, 11);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 0.4, 1.4, 2.4, 3.4, 4.4, 5.4, 6.4, 7.4, 8.4, 9.4, 10.4);
	}

	@Test
	void testTranslationInvariantBoundsFitNonInt() throws GeneratorException {
		var model = new AxialPointsModel("Temperature", 0.4, 10.4, 10);
		model.setBoundsToFit(true);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 0.9, 1.9, 2.9, 3.9, 4.9, 5.9, 6.9, 7.9, 8.9, 9.9);

	}

	@Test
	void testNegativeDirectionNonInt() throws GeneratorException {
		// Behaviour consistent for non-integer range
		var model = new AxialPointsModel("Temperature", 10.4, 0.4, 11);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 10.4, 9.4, 8.4, 7.4, 6.4, 5.4, 4.4, 3.4, 2.4, 1.4, 0.4);
	}

	@Test
	void testNegativeDirectionBoundsFitNonInt() throws GeneratorException {
		// Behaviour consistent for non-integer range
		var model = new AxialPointsModel("Temperature", 10.4, 0.4, 10);
		model.setBoundsToFit(true);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 9.9, 8.9, 7.9, 6.9, 5.9, 4.9, 3.9, 2.9, 1.9, 0.9);

		// Including across 0
		model = new AxialPointsModel("Temperature", 9, -1, 10);
		model.setBoundsToFit(true);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 8.5, 7.5, 6.5, 5.5, 4.5, 3.5, 2.5, 1.5, 0.5, -0.5);
	}

	@Test
	void testNegativeDirection() throws GeneratorException {
		// Expected behaviour for integer range
		var model = new AxialPointsModel("Temperature", 300, 290, 11);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 300, 299, 298, 297, 296, 295, 294, 293, 292, 291, 290);

		// Behaviour consistent when translated in axis
		model = new AxialPointsModel("Temperature", 10, 0, 11);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);

		// Including across 0
		model = new AxialPointsModel("Temperature", 9, -1, 11);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1);

	}

	@Test
	void testNegativeDirectionBoundsFit() throws GeneratorException {
		// With Bounds Fit:
		var model = new AxialPointsModel("Temperature", 300, 290, 10);
		model.setBoundsToFit(true);

		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 299.5, 298.5, 297.5, 296.5, 295.5, 294.5, 293.5, 292.5, 291.5, 290.5);

		// Behaviour consistent when translated in axis
		model = new AxialPointsModel("Temperature", 10, 0, 10);
		model.setBoundsToFit(true);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 9.5, 8.5, 7.5, 6.5, 5.5, 4.5, 3.5, 2.5, 1.5, 0.5);
	}

	@Test
	void testOnePoint() throws GeneratorException {

		var model = new AxialPointsModel("Temperature", 0, 3, 1);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", -1.5, 1.5);

		model.setBoundsToFit(true);
		gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 1.5);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, 3);
	}

	@Test
	void testOnePointBackwards() throws GeneratorException {

		var model = new AxialPointsModel("Temperature", 0, -3, 1);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", 1.5, -1.5);
	}

	@Test
	void testOnePointBackwardsBoundsFit() throws GeneratorException {
		var model = new AxialPointsModel("Temperature", 0, -3, 1);
		model.setBoundsToFit(true);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, -1.5);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, -3);
	}

	@Test
	void testOnePointWithZeroLength() throws GeneratorException {
		var model = new AxialPointsModel("Temperature", 0, 0, 1);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", 0, 0);
	}

	@Test
	void testOnePointWithZeroLengthBoundsFIt() throws GeneratorException {
		var model = new AxialPointsModel("Temperature", 0, 0, 1);
		model.setBoundsToFit(true);
		var gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, 0);
	}

	/**
	 * Test behaviour consistent over arbitrary scales.
	 *
	 * @throws GeneratorException
	 *             n.b: floating point errors make testing at pow<0 difficulty
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3, 6, 9 })
	void testPowerIndependent(int power) throws GeneratorException {
		var scale = Math.pow(10, power);
		var mmodel = new AxialPointsModel("Temperature", scale, scale * 2, 2); // 2 points
		var gen = pointGeneratorService.createGenerator(mmodel);
		GeneratorUtil.testGeneratorPoints(gen, 2);
		checkSequence(gen, scale, scale * 2);
		// Bounds at Start - Step/2, Stop + Step/2
		checkBounds(gen, "Temperature", scale / 2, 5 * scale / 2);

	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3, 6, 9 })
	void testPowerIndependentBoundsFit(int power) throws GeneratorException {
		var scale = Math.pow(10, power);
		var mmodel = new AxialPointsModel("Temperature", scale, scale * 2, 2); // 2 points
		mmodel.setBoundsToFit(true);
		var gen = pointGeneratorService.createGenerator(mmodel);
		gen = pointGeneratorService.createGenerator(mmodel);
		GeneratorUtil.testGeneratorPoints(gen, 2);
		checkSequence(gen, scale * 5 / 4, scale * 7 / 4);
		checkBounds(gen, "Temperature", scale, scale * 2);
	}

	private void checkSequence(IPointGenerator<?> gen, double... positions) {

		List<IPosition> pos = gen.createPoints();
		for (int i = 0; i < positions.length; i++) {
			assertThat((double) pos.get(i).get("Temperature"), is(closeTo(positions[i], 1e-10)));
		}
	}
}
