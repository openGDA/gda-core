/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.points;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.junit.jupiter.api.Test;

/**
 * Actually tests any scalar scan
 *
 * @author Matthew Gerring
 *
 */
public class StepTest extends AbstractGeneratorTest {

	/**
	 * General tests that behaviour is consistent
	 * not boundsFit:
	 *  point N = Start + N * Step
	 *  maximum N = M + 1: Length / Step >= M - 0.01
	 *
	 * boundsFit:
	 *  point N = Start + (N + 0.5) * Min(Step, Length)
	 *  maximum N = M
	 *
	 * @throws GeneratorException
	 */
	@Test
	public void testSizes() throws GeneratorException {

		//Expected behaviour for integer steps
		AxialStepModel model = new AxialStepModel("Temperature", 290,300,1);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 290.5, 291.5, 292.5, 293.5, 294.5, 295.5, 296.5, 297.5, 298.5, 299.5);

		// Behaviour consistent when translated in axis
		model = new AxialStepModel("Temperature", 0,10,1);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5);

		// Including across 0
		model = new AxialStepModel("Temperature", -1, 9, 1);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, -0.5, 0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5);

		//Behaviour consistent for non-integer steps
		model = new AxialStepModel("Temperature", 0,3, 0.9);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 4);
		checkSequence(gen, 0, 0.9, 1.8, 2.7);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 3);
		checkSequence(gen, 0.45, 1.35, 2.25);

		// Behaviour consistent for non-integer limits
		model = new AxialStepModel("Temperature", 1.1 ,4.5, 1);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 4);
		checkSequence(gen, 1.1, 2.1, 3.1, 4.1);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 3);
		checkSequence(gen, 1.6, 2.6, 3.6);

		//Behaviour consistent for non-integer limits that fit non-int steps
		model = new AxialStepModel("Temperature", 0.1, 2.1, 0.2);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		checkSequence(gen, 0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 1.7, 1.9, 2.1);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		checkSequence(gen, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0);

		// Behaviour consistent for non-integer limits that do not fit non-int steps
		model = new AxialStepModel("Temperature", 0.1, 2.1, 0.3);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 7);
		checkSequence(gen, 0.1, 0.4, 0.7, 1.0, 1.3, 1.6, 1.9);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 6);
		checkSequence(gen, 0.25, 0.55, 0.85, 1.15, 1.45, 1.75);
	}

	/**
	 * Tests that behaviour is consistent when the generator runs from larger to smaller numbers: Step < 0, Start > Stop
	 * @throws GeneratorException
	 */
	@Test
	public void testDirectionSmaller() throws GeneratorException {
		AxialStepModel model = new AxialStepModel("Temperature", 4, 1, -0.5);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 7);
		double[] points = new double[7];
		for (int i = 0; i < 7; i++) {
			points[i] = 4 - 0.5 * i;
		}
		checkSequence(gen, points);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 6);
		points = new double[6];
		for (int i = 0; i < 6; i++) {
			points[i] = 3.75 - 0.5 * i;
		}
		checkSequence(gen, points);

		//Including across 0
		model = new AxialStepModel("Temperature", 4, -1, -0.5);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);
		points = new double[11];
		for (int i = 0; i < 11; i++) {
			points[i] = 4 - 0.5 * i;
		}
		checkSequence(gen, points);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 10);
		points = new double[10];
		for (int i = 0; i < 10; i++) {
			points[i] = 3.75 - 0.5 * i;
		}
		checkSequence(gen, points);

		// Including non-integers
		model = new AxialStepModel("Temperature", 4.1, -1.2, -0.3);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 18);
		points = new double[18];
		for (int i = 0; i < 18; i++) {
			points[i] = 4.1 - 0.3 * i;
		}
		checkSequence(gen, points);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 17);
		points = new double[17];
		for (int i = 0; i < 17; i++) {
			points[i] = 3.95 - 0.3 * i;
		}
		checkSequence(gen, points);
	}

	/**
	 * Test behaviour when step > length
	 * @throws GeneratorException
	 */
	@Test
	public void testTooLargeStep() throws GeneratorException {

		AxialStepModel model = new AxialStepModel("Temperature", 0, 3, 5);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", -2.5, 2.5);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 1.5);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, 3);
	}

	@Test
	public void testWithZeroLength() throws GeneratorException {
		AxialStepModel model = new AxialStepModel("Temperature", 0, 0, 1);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", -0.5, 0.5);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, 0);
	}

	@Test
	public void testWithZeroLengthAndNegativeStep() throws GeneratorException {
		AxialStepModel model = new AxialStepModel("Temperature", 0, 0, -1);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", 0.5, -0.5);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, 0);
	}

	@Test
	public void testWithZeroStepAndLength() throws GeneratorException {
		AxialStepModel model = new AxialStepModel("Temperature", 0, 0, 0);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", 0, 0);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 0);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, 0);
	}

	/**
	 * Test behaviour when step > 0.5 * length
	 * @throws GeneratorException
	 */
	@Test
	public void testStepAlmostTooLarge() throws GeneratorException {

		AxialStepModel model = new AxialStepModel("Temperature", 0, 3, 2);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 2);
		checkSequence(gen, 0.0, 2.0);
		// Bounds Start +- Step/2
		checkBounds(gen, "Temperature", -1, 3);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 1);
		checkSequence(gen, 1.0);
		// Bounds Start, Stop
		checkBounds(gen, "Temperature", 0, 2);
	}

	/**
	 * Ensure an exception is thrown during validation if the step and length are in opposing directions
	 * @throws GeneratorException
	 */
	@Test
	public void testMisdirectedStepGenPoints() {
		assertThrows(GeneratorException.class,
				() -> service.createGenerator(new AxialStepModel("Temperature", 290, 300, -1)));

	}

	/**
	 * Ensure an exception is thrown during validation if the step and length are in opposing directions
	 * @throws GeneratorException
	 */
	@Test
	public void testMisdirectedLengthGenPoints() {
		assertThrows(GeneratorException.class,
				() -> service.createGenerator(new AxialStepModel("Temperature", 300, 290, 1)));
	}

	/**
	 * Ensure that point is included if within 1% of Stop, but not if outside
	 * @throws GeneratorException
	 */
	@Test
	public void testTolerance() throws GeneratorException {

		// within the 1% of step size tolerance
		AxialStepModel model = new AxialStepModel("Temperature", 0.0, 2.0, 0.667);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 4);
		checkSequence(gen, 0, 0.667, 0.667*2, 0.667*3);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 3);
		checkSequence(gen, 0.667/2, 3 * 0.667/2, 5 * 0.667/2);

		// outside the 1% of step size tolerance
		model = new AxialStepModel("Temperature", 0.0, 2.0, 0.67);
		gen = service.createGenerator(model);
		checkSequence(gen, 0, 0.67, 0.67*2);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 2);
		checkSequence(gen, 0.67/2, 3 * 0.67/2);

		// For negatives
		// within the 1% of step size tolerance
		model = new AxialStepModel("Temperature", 0.0, -2.0, -0.667);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 4);
		checkSequence(gen, 0, -0.667, -0.667 * 2, -0.667 * 3);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 3);
		checkSequence(gen, -0.667 / 2, -3 * 0.667 / 2, -5 * 0.667 / 2);

		// outside the 1% of step size tolerance
		model = new AxialStepModel("Temperature", 0.0, -2.0, -0.67);
		gen = service.createGenerator(model);
		checkSequence(gen, 0, -0.67, -0.67 * 2);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 2);
		checkSequence(gen, -0.67 / 2, -3 * 0.67 / 2);
	}

	@Test
	public void testPerfectSequence() throws GeneratorException {
		// Test cases where start, stop, step result in an integer number of points.

		AxialStepModel model = new AxialStepModel("Temperature", 290,300,1);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		checkSequence(gen, 290.0, 291.0, 292.0, 293.0, 294.0, 295.0, 296.0, 297.0, 298.0, 299.0, 300.0);
		GeneratorUtil.testGeneratorPoints(gen, 11);

		model = new AxialStepModel("Temperature", 0,3, 0.6);
		gen = service.createGenerator(model);
		checkSequence(gen, 0d, 0.6, 1.2, 1.8, 2.4, 3.0);
		GeneratorUtil.testGeneratorPoints(gen, 6);

		model = new AxialStepModel("Temperature", 1, 4, 0.6);
		gen = service.createGenerator(model);
		checkSequence(gen, 1.0, 1.6, 2.2, 2.8, 3.4, 4.0);
		GeneratorUtil.testGeneratorPoints(gen, 6);

		model = new AxialStepModel("Temperature", 11, 14, 0.6);
		gen = service.createGenerator(model);
		checkSequence(gen, 11.0, 11.6, 12.2, 12.8, 13.4, 14.0);
		GeneratorUtil.testGeneratorPoints(gen, 6);

		model = new AxialStepModel("Temperature", 1,4, 0.5);
		gen = service.createGenerator(model);
		checkSequence(gen, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0);
		GeneratorUtil.testGeneratorPoints(gen, 7);
	}

	@Test
	public void testImperfectSequence() throws GeneratorException {
		// Test cases where start, stop, step result in an non-integer number of points.

		AxialStepModel model = new AxialStepModel("Temperature", 290, 299.5, 1);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		checkSequence(gen, 290.0, 291.0, 292.0, 293.0, 294.0, 295.0, 296.0, 297.0, 298.0, 299.0);
		GeneratorUtil.testGeneratorPoints(gen, 10);

		model = new AxialStepModel("Temperature", 0, 2.5, 0.6);
		gen = service.createGenerator(model);
		checkSequence(gen, 0d, 0.6, 1.2, 1.8, 2.4);
		GeneratorUtil.testGeneratorPoints(gen, 5);

		model = new AxialStepModel("Temperature", 1, 3, 0.6);
		gen = service.createGenerator(model);
		checkSequence(gen, 1.0, 1.6, 2.2, 2.8);
		GeneratorUtil.testGeneratorPoints(gen, 4);

		model = new AxialStepModel("Temperature", -1, 1, 0.6);
		gen = service.createGenerator(model);
		checkSequence(gen, -1.0, -0.4, 0.2, 0.8);
		GeneratorUtil.testGeneratorPoints(gen, 4);

		model = new AxialStepModel("Temperature", 1, -1, -0.6);
		gen = service.createGenerator(model);
		checkSequence(gen, 1.0, 0.4, -0.2, -0.8);
		GeneratorUtil.testGeneratorPoints(gen, 4);
	}

	/**
	 * Test behaviour consistent over arbitrary scales.
	 * @throws GeneratorException
	 * n.b: floating point errors make testing at pow<0 difficulty
	 */
	@Test
	public void testPowerIndependent() throws GeneratorException {
		for (int power : new int [] { 0, 1, 2, 3, 6, 9}) {
			double scale = Math.pow(10, power);
			AxialStepModel mmodel = new AxialStepModel("Temperature", scale, scale * 2, scale); // 2 points
			IPointGenerator<AxialStepModel> gen = service.createGenerator(mmodel);
			GeneratorUtil.testGeneratorPoints(gen, 2);
			checkSequence(gen, scale, scale * 2);
			// Bounds at Start - Step/2, Stop + Step/2
			checkBounds(gen, "Temperature", scale / 2, 5 * scale / 2);

			mmodel.setBoundsToFit(true);
			gen = service.createGenerator(mmodel);
			GeneratorUtil.testGeneratorPoints(gen, 1);
			checkSequence(gen, scale * 1.5);
			// Bounds at Start, Stop
			checkBounds(gen, "Temperature", scale, scale * 2);
		}
	}

	private void checkSequence(IPointGenerator<?> gen, double... positions) {

		List<IPosition> pos = gen.createPoints();
        for (int i = 0; i < positions.length; i++) {
			assertThat((double) pos.get(i).get("Temperature"), is(closeTo(positions[i], 1e-10)));
		}
	}

}
