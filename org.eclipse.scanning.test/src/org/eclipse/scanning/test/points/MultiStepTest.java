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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.junit.jupiter.api.Test;

class MultiStepTest extends AbstractGeneratorTest {

	@Test
	void testNoName() {
		AxialMultiStepModel model = new AxialMultiStepModel();

		assertThrows(GeneratorException.class, () ->  pointGeneratorService.createGenerator(model));
	}

	@Test
	void testEmpty() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testSingleForward() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(10, 20, 2);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);

		final int expectedSize = 6;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
		for (int i = 0; i < pointList.size(); i++) {
			assertEquals(new Scalar<>("x", i, 10.0 + (2 * i)), pointList.get(i));
		}
	}

	@Test
	void testSingleForwardStepsWrongDir() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(10.0, 20.0, -1.0);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testSingleBackward() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(20.0, 10.0, -2.0);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
		final int expectedSize = 6;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
		for (int i = 0; i < pointList.size(); i++) {
			assertEquals(new Scalar<>("x", i, 20.0 - (2 * i)), pointList.get(i));
		}
	}

	@Test
	void testSingleBackwardStepsWrongDir() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(20, 10, 2);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testMultipleForward() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(10, 20, 2);
		model.addRange(25, 50, 5);
		model.addRange(100, 500, 50);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
		final int expectedSize = 21; // 6 + 6 + 9
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		for (int i = 0; i < pointList.size(); i++) {
			double expected;
			if (i < 6) expected = 10 + 2 * i;
			else if (i < 12) expected = 25 + 5 * (i - 6);
			else expected = 100 + 50 * (i - 12);
			assertEquals(new Scalar<>("x", i, expected), pointList.get(i));
		}
	}

	@Test
	void testMultipleBackward() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(500, 100, -50);
		model.addRange(50, 25, -5);
		model.addRange(20, 10, -2);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);

		final int expectedSize = 21; // 6 + 6 + 9
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		for (int i = 0; i < pointList.size(); i++) {
			double expected;
			if (i < 9) expected = 500 - 50 * i;
			else if (i < 15) expected = 50 - 5 * (i - 9);
			else expected = 20 - 2 * (i - 15);
			assertEquals(new Scalar<>("x", i, expected), pointList.get(i));
		}
	}

	@Test
	void testForwardNoGap() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(10, 20, 2); // Bounds 9 -> 21, 6 points
		model.addRange(23.5, 103.5, 5); // Bounds 21 -> 106, 17 points

		for (boolean bool : new boolean[] {true, false}) {
			model.setContinuous(bool);
			IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
			GeneratorUtil.testGeneratorPoints(gen);

			final int expectedSize = 23; // 6 + 17
			assertEquals(expectedSize, gen.size());
			assertEquals(1, gen.getRank());
			assertArrayEquals(new int[] { expectedSize }, gen.getShape());

			List<IPosition> pointList = gen.createPoints();
			assertEquals(expectedSize, pointList.size());
			for (int i = 0; i < pointList.size(); i++) {
				double expected;
				if (i < 6) expected = 10 + 2 * i;
				else expected = 23.5 + 5 * (i - 6);
				assertEquals(new Scalar<>("x", i, expected), pointList.get(i));
			}
		}
	}

	@Test
	void testForwardContinuousWithGap() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(0, 20, 2); // Bounds -1 -> 21
		model.addRange(20, 100, 5); // Bounds 17.5 -> 102.5

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testBackwardNoGap() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(100, 20, -5); // Bounds 102.5 -> 17.5 (17 points)
		model.addRange(16.5, 6.5, -2); // Bounds 17.5 -> 5.5 (6 points)

		for (boolean bool : new boolean[]{true, false}){
			model.setContinuous(bool);
			IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
			GeneratorUtil.testGeneratorPoints(gen);

			final int expectedSize = 23; // 6 + 17
			assertEquals(expectedSize, gen.size());
			assertEquals(1, gen.getRank());
			assertArrayEquals(new int[] { expectedSize }, gen.getShape());

			List<IPosition> pointList = gen.createPoints();
			assertEquals(expectedSize, pointList.size());
			for (int i = 0; i < pointList.size(); i++) {
				double expected;
				if (i < 17) expected = 100 - 5 * i;
				else expected = 16.5 - 2 * (i - 17);
				assertEquals(new Scalar<>("x", i, expected), pointList.get(i));
			}
		}
	}

	@Test
	void testBackwardContinuousWithGap() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(100, 20, -5); // Bounds 102.5 -> 17.5
		model.addRange(20, 0, -2); // Bounds 21 -> -1

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testForwardOverlapping() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(10, 20, 2);
		model.addRange(15, 50, 5);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	void testForwardOverlappingContinuous() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(10, 20, 2);
		model.addRange(15, 50, 5);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testBackwardOverlapping() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(50, 20, -5);
		model.addRange(22, 10, -2);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	void testBackwardOverlappingContinuous() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(50, 20, -5);
		model.addRange(22, 10, -2);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testForwardThenBackward() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(10, 20, 2);
		model.addRange(50, 25, -5);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	void testForwardThenBackwardContinuous() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(10, 20, 2);
		model.addRange(20, 10, -2);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	void testForwardThenBackwardContinuousWithGap() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(10, 20, 2);
		model.addRange(25, 15, -2);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testBackwardThenForward() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");

		model.addRange(50, 25, -5);
		model.addRange(10, 20, 2);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	void testBackwardThenForwardContinuous() throws Exception {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(20, 10, -2);
		model.addRange(10, 20, 2);

		IPointGenerator<AxialMultiStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	void testBackwardThenForwardContinuousWithGap() {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		model.addRange(50, 25, -5);
		model.addRange(10, 20, 2);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void alternatingCapable() throws GeneratorException {
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		// Bounds 9-21: 6 positions
		model.addRange(10, 20, 2);
		// Bounds 21-30: 9 positions
		model.addRange(21.5, 29.5, 1);
		model.setAlternating(true);

		for (boolean bool : new boolean[] {true, false}) {
			model.setContinuous(bool);
			// 2 positions: 0, 1
			CompoundModel cModel = new CompoundModel(new AxialStepModel("axis", 0, 1, 1));
			cModel.addModel(model);
			IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(cModel);
			assertEquals(30, gen.size());
			List<IPosition> positions = gen.createPoints();
			assertEquals(30, positions.size());
			for (int i = 0; i < 15; i++) {
				assertEquals(positions.get(i).get("x"), positions.get(29-i).get("x"));
			}
		}

	}

	@Test
	void submodelAlternateIncapable() {
		CompoundModel cModel = new CompoundModel();
		// 2 positions: 0, 1
		cModel.addModel(new AxialStepModel("axis", 0, 1, 1));
		AxialMultiStepModel model = new AxialMultiStepModel();
		model.setName("x");
		model.setContinuous(true);

		// Bounds 9-21: 6 positions
		model.addRange(10, 20, 2);
		model.getModels().get(0).setAlternating(true);
		// Bounds 21-30: 9 positions
		model.addRange(21.5, 30, 1);
		model.setAlternating(true);
		cModel.addModel(model);
		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(cModel));
	}


	@Test
	void testSizeIndependent() throws GeneratorException {


		AxialMultiStepModel mmodel = new AxialMultiStepModel();
		mmodel.setName("energy");
		mmodel.addRange(1,2,1); // 2 points

		assertEquals(2, pointGeneratorService.createGenerator(mmodel).size());


		mmodel = new AxialMultiStepModel();
		mmodel.setName("energy");
		mmodel.addRange(10,20,10); // 2 points

		assertEquals(2, pointGeneratorService.createGenerator(mmodel).size());


		mmodel = new AxialMultiStepModel();
		mmodel.setName("energy");
		mmodel.addRange(100,200,100); // 2 points

		assertEquals(2, pointGeneratorService.createGenerator(mmodel).size());

		mmodel = new AxialMultiStepModel();
		mmodel.setName("energy");
		mmodel.addRange(1000,2000,1000); // 2 points

		assertEquals(2, pointGeneratorService.createGenerator(mmodel).size());


		mmodel = new AxialMultiStepModel();
		mmodel.setName("energy");
		mmodel.addRange(10000,20000,10000); // 2 points

		assertEquals(2, pointGeneratorService.createGenerator(mmodel).size());

	}
}
