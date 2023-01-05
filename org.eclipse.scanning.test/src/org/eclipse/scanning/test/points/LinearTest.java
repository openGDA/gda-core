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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.junit.jupiter.api.Test;

class LinearTest extends AbstractGeneratorTest {

	@Test
	void testOneDEqualSpacing() throws Exception {

		BoundingLine line = new BoundingLine();
		line.setxStart(0.0);
		line.setyStart(0.0);
		line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
        final int numPoints = 10;
        model.setPoints(numPoints);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<TwoAxisLinePointsModel> gen = pointGeneratorService.createGenerator(model);
		assertEquals(numPoints, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { numPoints }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();

		assertEquals(numPoints, pointList.size());
        GeneratorUtil.testGeneratorPoints(gen);
		checkLinePoints(gen);
	}

	@Test
	void testIndicesOneDEqualSpacing() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
        final int numPoints = 10;
        model.setPoints(numPoints);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<TwoAxisLinePointsModel> gen = pointGeneratorService.createGenerator(model);
		assertEquals(numPoints, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { numPoints }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();

		assertEquals(numPoints, pointList.size());
        GeneratorUtil.testGeneratorPoints(gen);
		checkLinePoints(gen);
	}


	@Test
	void testOneDEqualSpacingNoROI() throws GeneratorException {

		TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
		final int numPoints = 10;
		model.setPoints(numPoints);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<TwoAxisLinePointsModel> gen = pointGeneratorService.createGenerator(model);
		int expectedSize = 10;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
		checkLinePoints(gen);
	}

	@Test
	void testOneDEqualSpacingNoPoints() {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
        model.setPoints(0);
        model.setBoundingLine(line);

		// Get the point list
		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}


	@Test
	void testOneDStep() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
		//implicit line.setAngle(0);
		line.setLength(Math.hypot(3.0, 3.0)); // 4.24264

        TwoAxisLineStepModel model = new TwoAxisLineStepModel();
        model.setStep(0.3);
        model.setBoundingLine(line);

        final int expectedSize = 15; // 4.2/0.3 + (0,0)
		IPointGenerator<TwoAxisLineStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen,  expectedSize);
		checkLinePoints(gen);
	}

	@Test
	void testOneDStepNoROI() throws GeneratorException {

		TwoAxisLineStepModel model = new TwoAxisLineStepModel();
		model.setStep(1);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<TwoAxisLineStepModel> gen = pointGeneratorService.createGenerator(model);
		GeneratorUtil.testGeneratorPoints(gen,  11);
		checkLinePoints(gen);
	}

	@Test
	void testOneDStepNoStep() {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLineStepModel model = new TwoAxisLineStepModel();
        model.setStep(0);
        model.setBoundingLine(line);

        assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testOneDStepNegativeStep() {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

		TwoAxisLineStepModel model = new TwoAxisLineStepModel();
		model.setStep(-0.3);
		model.setBoundingLine(line);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	void testOneDStepWrongROI() throws Exception {

		try {
			RectangularROI roi = new RectangularROI(new double[]{0,0}, new double[]{3,3});

	        BoundingLine line = new BoundingLine();
	        line.setxStart(0.0);
	        line.setyStart(0.0);
	        line.setLength(Math.hypot(3.0, 3.0));

	        TwoAxisLineStepModel model = new TwoAxisLineStepModel();
	        model.setStep(0);
	        model.setBoundingLine(line);

			// Get the point list
			IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, roi);
	        GeneratorUtil.testGeneratorPoints(gen);
		} catch (ModelValidationException | GeneratorException e) {
			return;
		}
		throw new Exception("testOneDStepWrongROI did not throw an exception as expected!");
	}

	void checkLinePoints(IPointGenerator<?> gen) {

		final AbstractBoundingLineModel model;
		if (gen.getModel() instanceof CompoundModel) {
			model = (AbstractBoundingLineModel) ((CompoundModel) gen.getModel()).getModels().get(0);
		} else {
			model = (AbstractBoundingLineModel) gen.getModel();
		}

		final int points = gen.getShape()[0];
		assertThat(gen.size(), is(equalTo(points)));
		final double xStep;
		final double yStep;
		if (model instanceof TwoAxisLineStepModel) {
			xStep = ((TwoAxisLineStepModel) model).getStep() * Math.cos(model.getBoundingLine().getAngle());
			yStep = ((TwoAxisLineStepModel) model).getStep() * Math.sin(model.getBoundingLine().getAngle());
		} else {
			TwoAxisLinePointsModel pointsModel = (TwoAxisLinePointsModel) model;
			final double length = model.getBoundingLine().getLength();
			final int numSteps = model.isBoundsToFit() ? pointsModel.getPoints(): pointsModel.getPoints() - 1;
			xStep = length * Math.cos(model.getBoundingLine().getAngle()) / numSteps;
			yStep = length * Math.sin(model.getBoundingLine().getAngle()) / numSteps;

		}
		final double xStart = model.isBoundsToFit() ?
				model.getBoundingLine().getxStart() + xStep / 2 : model.getBoundingLine().getxStart();
		final double yStart = model.isBoundsToFit() ?
				model.getBoundingLine().getyStart() + yStep / 2 : model.getBoundingLine().getyStart();
		final String xName = model.getxAxisName();
		final String yName = model.getyAxisName();
		final List<IPosition> expectedPositions = new ArrayList<>();
		for (int index = 0; index < points; index ++) {
			expectedPositions.add(new Point(xName, index, xStart + index * xStep, yName, index, yStart + index * yStep, index, false));
		}
		final Iterator<IPosition> generatedPositions = gen.iterator();
		gen.createPoints();
		for (int i = 0; i < expectedPositions.size(); i++) {
			assertThat(generatedPositions.next(), is(equalTo(expectedPositions.get(i))));
		}
	}


}
