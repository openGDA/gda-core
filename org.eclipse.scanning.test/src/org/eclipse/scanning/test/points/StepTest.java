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

import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

/**
 * Actually tests any scalar scan
 *
 * @author Matthew Gerring
 *
 */
public class StepTest {

	private IPointGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new PointGeneratorService();
	}

	@Test
	public void testSizes() throws Exception {

		AxialStepModel model = new AxialStepModel("Temperature", 290,300,1);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		assertEquals(11, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 11 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 0,10,1);
		gen = service.createGenerator(model);
		assertEquals(11, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 11 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 1,11,1);
		gen = service.createGenerator(model);
		assertEquals(11, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 11 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 0,3, 0.9);
		gen = service.createGenerator(model);
		assertEquals(4, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 4 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 1,4, 0.9);
		gen = service.createGenerator(model);
		assertEquals(4, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 4 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 0, 3, 0.8);
		gen = service.createGenerator(model);
		assertEquals(4, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 4 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 1,4, 0.8);
		gen = service.createGenerator(model);
		assertEquals(4, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 4 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 0,3, 0.6);
		gen = service.createGenerator(model);
		assertEquals(6, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 6 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 1,4, 0.6);
		gen = service.createGenerator(model);
		assertEquals(6, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 6 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);

		model = new AxialStepModel("Temperature", 1,4, 0.5);
		gen = service.createGenerator(model);
		assertEquals(7, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 7 }, gen.getShape());
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	public void testDirectionSmaller() throws Exception {
		AxialStepModel model = new AxialStepModel("Temperature", 4, 1, -0.5);
		IPointGenerator<?> gen = service.createGenerator(model);
		assertEquals(7, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 7 }, gen.getShape());

		GeneratorUtil.testGeneratorPoints(gen);
		List<IPosition> points = gen.createPoints();
		assertEquals(7, points.size());
		for (int i = 0; i < points.size(); i++) {
			assertEquals(new Scalar<>("Temperature", i, 4 - 0.5 * i), points.get(i));
		}
	}

	@Test
	public void testTooLargeStep() throws Exception {

		IPointGenerator<AxialStepModel> gen = service.createGenerator(new AxialStepModel("fred", 0, 3, 5));
		assertEquals(1, gen.size());
		assertEquals(0d, gen.iterator().next().get("fred"));
		// TODO Should this throw an exception or do this? Possible to do a size 1 step makes some tests easier to write.
	}

	@Test(expected = GeneratorException.class)
	public void testMisdirectedStepGenSize() throws Exception {

		IPointGenerator<AxialStepModel> gen = service.createGenerator(new AxialStepModel("Temperature", 290, 300, -1));
		gen.size();
	}

	@Test(expected = GeneratorException.class)
	public void testMisdirectedStepGenPoints() throws Exception {

		IPointGenerator<AxialStepModel> gen = service.createGenerator(new AxialStepModel("Temperature", 290, 300, -1));
		gen.createPoints();
	}

	@Test
	public void testTolerance() throws Exception {

		// within the 1% of step size tolerance
		AxialStepModel model = new AxialStepModel("Temperature", 0.0, 2.0, 0.667);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		assertEquals(4, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 4 }, gen.getShape());

		// outside the 1% of step size tolerance
		model = new AxialStepModel("Temperature", 0.0, 2.0, 0.67);
		gen = service.createGenerator(model);
		assertEquals(3, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { 3 }, gen.getShape());
	}

	@Test
	public void testPerfectSequence() throws Exception {
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
	public void testImperfectSequence() throws Exception {
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

	@Test
	public void testSequenceExposureTime() throws Exception {
		AxialStepModel model = new AxialStepModel("Temperature", 290,300,1);
		IPointGenerator<AxialStepModel> gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 11);

		model = new AxialStepModel("Temperature", 0, 3, 0.6);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 6);

		model = new AxialStepModel("Temperature", 1, 4, 0.6);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 6);

		model = new AxialStepModel("Temperature", 11, 14, 0.6);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 6);

		model = new AxialStepModel("Temperature", 1,4, 0.5);
		gen = service.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen, 7);
	}

	@Test
	public void testSizeIndependent() throws GeneratorException {


		AxialStepModel mmodel = new AxialStepModel("energy", 1,2,1); // 2 points
		assertEquals(2, service.createGenerator(mmodel).size());


		mmodel = new AxialStepModel("energy", 10,20,10); // 2 points
		assertEquals(2, service.createGenerator(mmodel).size());


		mmodel = new AxialStepModel("energy", 100,200,100); // 2 points
		assertEquals(2, service.createGenerator(mmodel).size());

		mmodel = new AxialStepModel("energy", 1000,2000,1000); // 2 points
		assertEquals(2, service.createGenerator(mmodel).size());


		mmodel = new AxialStepModel("energy", 10000,20000,10000); // 2 points
		assertEquals(2, service.createGenerator(mmodel).size());

	}

	private void checkSequence(IPointGenerator<AxialStepModel> gen, double... positions) throws Exception {

		Iterator<IPosition> it = gen.iterator();
        for (int i = 0; i < positions.length; i++) {
			double position = positions[i];
			IPosition pos = it.next();
			if (!equalsWithinTolerance(new Double(position), (Number)pos.get("Temperature"), 0.00001)) {
				throw new Exception("Position not equal! "+pos.get("Temperature"));
			}
		}
	}

	private static boolean equalsWithinTolerance(Number foo, Number bar, Number tolerance) {
		final double a = foo.doubleValue();
		final double b = bar.doubleValue();
		final double t = tolerance.doubleValue();
		return t>=Math.abs(a-b);
	}

}