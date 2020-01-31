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

package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

public class EnforcedShapeTest {

	private static final String FAIL_MESSAGE = "Enforced shape model has more points than stepped model";

	private IPointGeneratorService pgs;

	@Before
	public void setUp() {
		pgs = new PointGeneratorService();
	}

	/**
	 * A grid that steps right up to the edge of a boundingbox. The returned bounding box should be
	 * the same as the input bounding box, and all positions should be the same.
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void GridThatFitsTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, 5, 5);

		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		stepModel.setxAxisStep(0.2);
		stepModel.setyAxisStep(1);
		stepModel.setName("notRaster");
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(box);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(true);
		// 0.1 -> 4.9 in steps o 0.2
		expectedPointsModel.setxAxisPoints(25);
		// 0.5 -> 4.5 in steps of
		expectedPointsModel.setyAxisPoints(5);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);
	}

	/**
	 * A grid for which its steps do not fit neatly up to the edge of its boundingbox.
	 * Should return a model with a different boundingbox, but that produces the same positions.
	 *
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void GridThatDoesntFitTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, 5, 5);
		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		stepModel.setxAxisStep(0.3);
		stepModel.setyAxisStep(1.7);
		stepModel.setName("notRaster");
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final BoundingBox shrunkBox = new BoundingBox(0, 0, 4.8, 3.4);
		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(shrunkBox);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(true);
		// 0.15 -> 4.65 in steps of 0.3
		expectedPointsModel.setxAxisPoints(16);
		// 0.85, 2.55 in steps of 1.7
		expectedPointsModel.setyAxisPoints(2);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);
	}

	/**
	 * A grid for which its steps are too long to fit into the bounding box.
	 *
	 * Should return a model with the same boundingbox, that produces a single point in the middle of the box,
	 * which should be the same behaviour as a stepmodel with a step too long.
	 *
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void GridTooSmallForAnyStepsTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, 1, 1);
		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		stepModel.setxAxisStep(4);
		stepModel.setyAxisStep(0.6);
		stepModel.setName("notRaster");
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(box);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(true);
		// 0.5
		expectedPointsModel.setxAxisPoints(1);
		// 0.5
		expectedPointsModel.setyAxisPoints(1);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);
	}

	/**
	 * A grid for which its steps and box are heading in the "negative" direction and steps right up to the edge of boundingbox.
	 * The returned bounding box should be the same as the input bounding box, and all positions should be the same.
	 *
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void GridNegativeStepsTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, -5, -5);
		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		stepModel.setxAxisStep(-1);
		stepModel.setyAxisStep(-0.2);
		stepModel.setName("notRaster");
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(box);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(true);
		// -0.5 -> -4.5
		expectedPointsModel.setxAxisPoints(5);
		// -0.1 -> -4.9
		expectedPointsModel.setyAxisPoints(25);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);
	}

	/**
	 * A grid for which its steps and box are heading in the "negative" direction.
	 * One axis is not stepping up to the edge of the bounding box.
	 * The other axis is too short for its step length.
	 *
	 * The returned bounding box should be the same in the direction where the step length is too long,
	 * shorter in the direction that the step doesn't reach the edge of the box, and should handle negative
	 * box length, step correctly to give the same IPositions.
	 *
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void GridMixedTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, -1.7, -5);
		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		stepModel.setxAxisStep(-0.3);
		stepModel.setyAxisStep(-12);
		stepModel.setName("notRaster");
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final BoundingBox shrunkBox = new BoundingBox(0, 0, -1.5, -5);
		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(shrunkBox);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(true);
		// -0.15 -> -1.35 (-1.65 less than 1/2 step from edge)
		expectedPointsModel.setxAxisPoints(5);
		// -2.5
		expectedPointsModel.setyAxisPoints(1);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);

	}

	/**
	 * A Step model stepping in the opposite direction to its box length would give 0 points (and create an exception at validation)
	 * so we throw an exception rather than allowing an invalid model to be created without warning.
	 */
	@Test(expected = ModelValidationException.class)
	public void GridWrongDirectionTest() {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, 5, 5);
		stepModel.setBoundingBox(box);
		stepModel.setxAxisStep(-0.3);
		// All other values default, i.e. yAxisStep = 1
		AbstractTwoAxisGridModel.enforceShape(stepModel);
	}

	/**
	 * A line that steps right up to the end of its boundingLine. The returned bounding line should be
	 * the same as the input bounding line, and all positions should be the same.
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void LineThatFitsTest() throws GeneratorException {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		// Starting at 0, length 5
		final BoundingLine line = new BoundingLine(0, 0, 3, 4);
		stepModel.setBoundingLine(line);
		stepModel.setAlternating(true);
		stepModel.setContinuous(false);
		// Not "Step"
		stepModel.setName("Diagonal");
		stepModel.setxAxisName("Temperature");
		stepModel.setxAxisUnits("Farenheit");
		stepModel.setyAxisName("Energy");
		stepModel.setyAxisUnits("Horsepower");
		// 0.25 -> 4.75
		stepModel.setStep(0.5);

		final TwoAxisLinePointsModel expectedPointsModel = new TwoAxisLinePointsModel();
		expectedPointsModel.setBoundingLine(line);
		expectedPointsModel.setBoundingLine(line);
		expectedPointsModel.setAlternating(true);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setName("Diagonal");
		expectedPointsModel.setxAxisName("Temperature");
		expectedPointsModel.setxAxisUnits("Farenheit");
		expectedPointsModel.setyAxisName("Energy");
		expectedPointsModel.setyAxisUnits("Horsepower");
		expectedPointsModel.setPoints(10);

		final TwoAxisLinePointsModel actualPointsModel = AbstractBoundingLineModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);
	}

	/**
	 * A line that doesn't step up to the end of its boundingLine. The returned bounding line should be trimmed
	 * so that all positions are be the same.
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void LineThatDoesntFitsTest() throws GeneratorException {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		// Starting at 0, length 5
		final BoundingLine line = new BoundingLine(0, 0, 3, 4);
		stepModel.setBoundingLine(line);
		stepModel.setAlternating(true);
		stepModel.setContinuous(false);
		// Not "Step"
		stepModel.setName("Diagonal");
		stepModel.setxAxisName("Temperature");
		stepModel.setxAxisUnits("Farenheit");
		stepModel.setyAxisName("Energy");
		stepModel.setyAxisUnits("Horsepower");
		// 0.35 -> 4.55
		stepModel.setStep(0.7);

		// => 4.9 long
		final BoundingLine shrunkLine = new BoundingLine(0, 0, 2.94, 3.92);
		final TwoAxisLinePointsModel expectedPointsModel = new TwoAxisLinePointsModel();
		expectedPointsModel.setBoundingLine(shrunkLine);
		expectedPointsModel.setAlternating(true);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setName("Diagonal");
		expectedPointsModel.setxAxisName("Temperature");
		expectedPointsModel.setxAxisUnits("Farenheit");
		expectedPointsModel.setyAxisName("Energy");
		expectedPointsModel.setyAxisUnits("Horsepower");
		expectedPointsModel.setPoints(7);

		final TwoAxisLinePointsModel actualPointsModel = AbstractBoundingLineModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);
	}

	/**
	 * A line that is too short for its step length. The returned bounding line should be
	 * the same as the input bounding line, and there should be one position in the centre,
	 * which should match the behaviour of a step model.
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */

	@Test
	public void LineTooSmallForAnyPoints() throws GeneratorException {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		// Starting at 0, length 13
		final BoundingLine line = new BoundingLine(0, 0, 5, 12);
		stepModel.setBoundingLine(line);
		stepModel.setAlternating(true);
		stepModel.setContinuous(false);
		// Not "Step"
		stepModel.setName("Diagonal");
		stepModel.setxAxisName("Temperature");
		stepModel.setxAxisUnits("Farenheit");
		stepModel.setyAxisName("Energy");
		stepModel.setyAxisUnits("Horsepower");
		stepModel.setStep(9);

		final TwoAxisLinePointsModel expectedPointsModel = new TwoAxisLinePointsModel();
		expectedPointsModel.setBoundingLine(line);
		expectedPointsModel.setAlternating(true);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setName("Diagonal");
		expectedPointsModel.setxAxisName("Temperature");
		expectedPointsModel.setxAxisUnits("Farenheit");
		expectedPointsModel.setyAxisName("Energy");
		expectedPointsModel.setyAxisUnits("Horsepower");
		expectedPointsModel.setPoints(1);

		final TwoAxisLinePointsModel actualPointsModel = AbstractBoundingLineModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel);
	}

	/**
	 * LineModels will except with a negative step length anyway, so this shouldn't ever be attempted,
	 * but to make sure that our points=0 doesn't throw an exception later, we throw it now.
	 */

	@Test(expected = ModelValidationException.class)
	public void LineWrongDirectionTest() {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		final BoundingLine line = new BoundingLine(0, 0, 1, 1);
		stepModel.setBoundingLine(line);
		stepModel.setStep(-0.3);
		// All other values default
		AbstractBoundingLineModel.enforceShape(stepModel);

	}

	/**
	 * Points of a PointsModel should lie where the points of the stepmodel that created it do, and should produce
	 * only the same steps, no additional ones.
	 */
	private void testPoints(IScanPathModel stepModel, IScanPathModel actualPointsModel) throws GeneratorException {
		final Iterator<IPosition> stepIt = pgs.createGenerator(stepModel).iterator();
		final Iterator<IPosition> pointIt = pgs.createGenerator(actualPointsModel).iterator();

		while (stepIt.hasNext()) {
			assertEquals(stepIt.next(), pointIt.next());
		}
		if (pointIt.hasNext()) {
			fail(FAIL_MESSAGE);
		}
	}

}
