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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel.Orientation;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.points.AbstractScanPointGenerator;
import org.junit.jupiter.api.Test;

public class EnforcedShapeTest extends AbstractGeneratorTest {

	/**
	 * A grid that steps right up to the edge of a boundingbox. The returned bounding box should be
	 * the same as the input bounding box, and all positions should be the same.
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void gridThatFitsTest() throws GeneratorException {
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
		// 0 -> 5 inclusive in steps of 0.2
		expectedPointsModel.setxAxisPoints(26);
		// in steps of 1
		expectedPointsModel.setyAxisPoints(6);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel, null, null);
	}

	/**
	 * A grid for which its steps do not fit neatly up to the edge of its boundingbox.
	 * Should return a model with a different boundingbox, but that produces the same positions.
	 *
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void gridThatDoesntFitTest() throws GeneratorException {
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
		// 0 -> 4.8 inclusive in steps of 0.3
		expectedPointsModel.setxAxisPoints(17);
		// 0 -> 3.4 in 1.7
		expectedPointsModel.setyAxisPoints(3);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel, null, null);
	}

	/**
	 * A grid for which its steps are too long to fit into the bounding box.
	 *
	 * Should return a model with the boundingBox in that axis adjusted to allow appropriate positions and bounds according to model's boundsToFit setting
	 *
	 * a. boundsToFit: box length in axis same
	 * b. !boundsToFit: box length in axis adjusted to the length of the step, box moved so that centre of box consistent
	 *
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void gridStepLongerThanAxisOfBox() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, 1, 1);
		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(false);
		// Expected positions = (0, 0), (0, 0.6), bounds (-2, 0), (2, 0.6)
		stepModel.setxAxisStep(4);
		stepModel.setyAxisStep(0.6);
		stepModel.setName("notRaster");
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(new BoundingBox(0, 0, 4, 0.6));
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(false);
		expectedPointsModel.setxAxisPoints(1);
		expectedPointsModel.setyAxisPoints(2);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);

		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel,
				new MapPosition(Map.of("xAxisName", -2.0, "yAxisName", 0.0)),
				new MapPosition(Map.of("xAxisName", 2.0, "yAxisName", 0.6)));

		// alternating
		// Expected positions = (0, 0), (0, 0.6), bounds (-2, 0), (-2, 0.6)
		stepModel.setAlternating(true);
		expectedPointsModel.setAlternating(true);
		actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel,
				new MapPosition(Map.of("xAxisName", -2.0, "yAxisName", 0.0)),
				// Even number of points in outer axis, so bounds are coming backwards
				new MapPosition(Map.of("xAxisName", -2.0, "yAxisName", 0.6)));


		stepModel.setAlternating(false);
		expectedPointsModel.setAlternating(false);

		// Expected positions = (0, 0), (0, 0.6), bounds (0, -0.3), (0, 0.9)
		expectedPointsModel.setOrientation(Orientation.VERTICAL);
		stepModel.setOrientation(Orientation.VERTICAL);

		actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);

		assertEquals(expectedPointsModel, actualPointsModel);

		testPoints(stepModel, actualPointsModel,
				new MapPosition(Map.of("xAxisName", 0.0, "yAxisName", -0.3)),
				new MapPosition(Map.of("xAxisName", 0.0, "yAxisName", 0.9)));

		// And boundsToFit
		stepModel.setBoundsToFit(true);
		expectedPointsModel.setBoundsToFit(true);
		// Expected position = (0.5, 0.3), bounds (0.5, 0.0), (0.5, 0.6)
		expectedPointsModel.setyAxisPoints(1);
		expectedPointsModel.getBoundingBox().setyAxisLength(0.6);
		expectedPointsModel.getBoundingBox().setxAxisLength(1);
		actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);


		assertEquals(expectedPointsModel, actualPointsModel);

		testPoints(stepModel, actualPointsModel,
				new MapPosition(Map.of("xAxisName", 0.5, "yAxisName", 0.0)),
				new MapPosition(Map.of("xAxisName", 0.5, "yAxisName", 0.6)));
	}

	/**
	 * A grid for which its steps and box are heading in the "negative" direction and steps right up to the edge of boundingbox.
	 * The returned bounding box should be the same as the input bounding box, and all positions should be the same.
	 *
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void gridNegativeStepsTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, -5, -5);
		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		stepModel.setxAxisStep(-1);
		stepModel.setyAxisStep(-0.2);
		stepModel.setName("notRaster");
		stepModel.setBoundsToFit(true);
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(box);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(true);
		expectedPointsModel.setBoundsToFit(true);
		// -0.5 -> -4.5
		expectedPointsModel.setxAxisPoints(5);
		// -0.1 -> -4.9
		expectedPointsModel.setyAxisPoints(25);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel, null, null);
	}

	/**
	 * A grid for which its steps and box are heading in the "negative" direction.
	 * One axis is not stepping up to the edge of the bounding box.
	 * The other axis is too short for its step length.
	 *
	 * Expected:
	 * for not stepping up to edge of bounding box: trim the length
	 * for axis too short for step length:
	 *  a. When boundsFit: step length = axis length, position middle
	 *  b. when notBoundsFit: axis length = step length, position middle
	 *
	 * @throws GeneratorException
	 */
	@Test
	public void gridMixedTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, -1.7, -5);
		stepModel.setBoundingBox(box);
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		stepModel.setxAxisStep(-0.3);
		stepModel.setyAxisStep(-12);
		stepModel.setName("notRaster");
		// -0.15, -0.45, -0.75, -1.05, -1.35, ¬1.65 -> bounds don't fit
		// 2.5, bounds of 0 -> -5
		stepModel.setBoundsToFit(true);
		stepModel.setxAxisUnits("microns");
		stepModel.setyAxisUnits("Astronomical Units");

		final BoundingBox shrunkBox = new BoundingBox(0, 0, -1.5, -5);
		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(shrunkBox);
		expectedPointsModel.setContinuous(false);
		expectedPointsModel.setAlternating(true);
		expectedPointsModel.setBoundsToFit(true);
		// -0.15, -0.45, -0.75, -1.05, -1.35
		expectedPointsModel.setxAxisPoints(5);
		// Step > Length && isBoundsFit -> centre of box (-2.5): bounds 0, -5
		expectedPointsModel.setyAxisPoints(1);
		expectedPointsModel.setName("notRaster");
		expectedPointsModel.setxAxisUnits("microns");
		expectedPointsModel.setyAxisUnits("Astronomical Units");

		TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);

		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel, null, null);

		List<IPosition> actualPositions = service.createGenerator(stepModel).createPoints();
		List<Point> expectedPositions = IntStream.range(0, 5).mapToObj(i ->
			new Point("xAxisName", i, -0.15 + i * -0.3, "yAxisName", 0, -2.5, i, true)).collect(Collectors.toList());
		assertEquals(expectedPositions, actualPositions);

		stepModel.setBoundsToFit(false);
		expectedPointsModel.setBoundsToFit(false);
		// Step > Length && !isBoundsFit -> start of line (0): bounds 6, -6
		expectedPointsModel.getBoundingBox().setyAxisLength(-12);
		// 0, -0.3, -0.6, -0.9, -1.2, -1.5
		expectedPointsModel.setxAxisPoints(6);

		actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);

		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel, null, null);

		actualPositions = service.createGenerator(stepModel).createPoints();
		expectedPositions = IntStream.range(0, 6).mapToObj(i ->
			new Point("xAxisName", i, 0 + i * -0.3, "yAxisName", 0, 0, i, true)).collect(Collectors.toList());
		assertEquals(expectedPositions, actualPositions);

	}

	/**
	 * A Step model stepping in the opposite direction to its box length will give a non-physical number of points (and create an exception at validation)
	 * @throws GeneratorException
	 */
	@Test(expected = GeneratorException.class)
	public void gridWrongDirectionTest() throws GeneratorException {
		final TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("xAxisName", "yAxisName");
		final BoundingBox box = new BoundingBox(0, 0, 5, 5);
		stepModel.setBoundingBox(box);
		stepModel.setxAxisStep(-0.3);
		// All other values default, i.e. yAxisStep = 1
		final BoundingBox shrunkBox = new BoundingBox(0, 0, 4.8, 5);
		final TwoAxisGridPointsModel expectedPointsModel = new TwoAxisGridPointsModel("xAxisName", "yAxisName");
		expectedPointsModel.setBoundingBox(shrunkBox);
		// 5, 4.7, 4.4, 4.1, 3.8, 3.5, 3.2, 2.9, 2.6, 2.3, 2, 1.7, 1.4, 1.1, 0.8, 0.5, 0.2
		expectedPointsModel.setxAxisPoints(-17);
		expectedPointsModel.setyAxisPoints(6);

		final TwoAxisGridPointsModel actualPointsModel = AbstractTwoAxisGridModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		service.createGenerator(actualPointsModel);
	}

	/**
	 * A line that steps right up to the end of its boundingLine. The returned bounding line should be
	 * the same as the input bounding line, and all positions should be the same.
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void lineThatFitsTest() throws GeneratorException {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		// Starting at 0, length 5
		final BoundingLine line = new BoundingLine(0, 0, 3, 4);
		stepModel.setBoundingLine(line);
		stepModel.setAlternating(true);
		stepModel.setContinuous(false);
		stepModel.setBoundsToFit(true);
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
		expectedPointsModel.setBoundsToFit(true);
		expectedPointsModel.setName("Diagonal");
		expectedPointsModel.setxAxisName("Temperature");
		expectedPointsModel.setxAxisUnits("Farenheit");
		expectedPointsModel.setyAxisName("Energy");
		expectedPointsModel.setyAxisUnits("Horsepower");
		expectedPointsModel.setPoints(10);

		final TwoAxisLinePointsModel actualPointsModel = AbstractBoundingLineModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel, null, null);
	}

	/**
	 * A line that doesn't step up to the end of its boundingLine. The returned bounding line should be trimmed
	 * so that all positions are be the same.
	 * Continuous/Alternating/name/units set just to have non default values for those fields.
	 */
	@Test
	public void lineThatDoesntFitsTest() throws GeneratorException {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		// Starting at 0, length 5
		final BoundingLine line = new BoundingLine(0, 0, 3, 4);
		stepModel.setBoundingLine(line);
		stepModel.setAlternating(true);
		stepModel.setContinuous(false);
		stepModel.setBoundsToFit(true);
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
		expectedPointsModel.setBoundsToFit(true);
		expectedPointsModel.setName("Diagonal");
		expectedPointsModel.setxAxisName("Temperature");
		expectedPointsModel.setxAxisUnits("Farenheit");
		expectedPointsModel.setyAxisName("Energy");
		expectedPointsModel.setyAxisUnits("Horsepower");
		expectedPointsModel.setPoints(7);

		final TwoAxisLinePointsModel actualPointsModel = AbstractBoundingLineModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		testPoints(stepModel, actualPointsModel,
				new MapPosition(Map.of("Temperature", 0.0, "Energy", 0.0)),
				new MapPosition(Map.of("Temperature", 2.94, "Energy", 3.92)));
	}

	/**
	 * A line that is too short for its step length.
	 * A single point should be placed on the line.
	 * For Stepped lines:
	 * a. !boundsToFit: a single point is placed at the start of the line, with bounds of +- 1/2 step length
	 * b. boundsToFit: a single point is place at the centre of the line, with bounds of +- 1/2 step length
	 *
	 * For Point lines:
	 * a. !boundsToFit: a single point is placed at the start of the line, with bounds of +- 1/2 line length
	 * b. boundsFit: a single point is placed at the centre of the line, with bounds of +- 1/2 line length
	 *
	 * Therefore, line length has to be adjusted but either start or centre has to remain the same.
	 * @throws GeneratorException
	 */
	@Test
	public void lineTooSmallForAnyPoints() throws GeneratorException {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		// Starting at 0, length 13
		final BoundingLine line = new BoundingLine(0, 0, 5, 12);
		stepModel.setBoundingLine(line);
		stepModel.setAlternating(true);
		stepModel.setContinuous(true);
		// Not "Step"
		stepModel.setName("Diagonal");
		stepModel.setxAxisName("Temperature");
		stepModel.setxAxisUnits("Farenheit");
		stepModel.setyAxisName("Energy");
		stepModel.setyAxisUnits("Horsepower");
		// notBoundsFit: single point at 0, 0 with bounds at -+19/2 {cos(angle), sin(angle)}
		stepModel.setStep(19);

		final BoundingLine copyLine = new BoundingLine();
		copyLine.setLength(19);
		copyLine.setAngle(line.getAngle());

		final TwoAxisLinePointsModel expectedPointsModel = new TwoAxisLinePointsModel();
		expectedPointsModel.setBoundingLine(copyLine);
		expectedPointsModel.setAlternating(true);
		expectedPointsModel.setContinuous(true);
		expectedPointsModel.setName("Diagonal");
		expectedPointsModel.setxAxisName("Temperature");
		expectedPointsModel.setxAxisUnits("Farenheit");
		expectedPointsModel.setyAxisName("Energy");
		expectedPointsModel.setyAxisUnits("Horsepower");
		expectedPointsModel.setPoints(1);

		final TwoAxisLinePointsModel actualPointsModel = AbstractBoundingLineModel.enforceShape(stepModel);

		assertEquals(expectedPointsModel, actualPointsModel);

		testPoints(stepModel, actualPointsModel,
				new MapPosition(Map.of("Temperature", -9.5 * Math.cos(line.getAngle()), "Energy", -9.5 * Math.sin(line.getAngle()))),
				new MapPosition(Map.of("Temperature", 9.5 * Math.cos(line.getAngle()), "Energy", 9.5 * Math.sin(line.getAngle()))));

		AbstractScanPointGenerator<TwoAxisLineStepModel> gen = ((AbstractScanPointGenerator<TwoAxisLineStepModel>) service.createGenerator(stepModel));
		IPosition expectedPosition = new Point("Temperature", 0, 0, "Energy", 0, 0, 0, true);
		Iterator<IPosition> it = gen.iterator();
		assertEquals(expectedPosition, it.next());
		assertFalse(it.hasNext());
	}

	/**
	 * LineModels will except with a negative step length, so this shouldn't ever be attempted, but for consistency
	 * @throws GeneratorException
	 */

	@Test(expected = GeneratorException.class)
	public void lineWrongDirectionTest() throws GeneratorException {
		final TwoAxisLineStepModel stepModel = new TwoAxisLineStepModel();
		final BoundingLine line = new BoundingLine(0, 0, 3, 4); // length 5
		stepModel.setBoundingLine(line);
		stepModel.setStep(-1); // 5, 4, 3, 2, 1, 0
		// All other values default

		final TwoAxisLinePointsModel expectedPointsModel = new TwoAxisLinePointsModel();
		final BoundingLine lineCopy = new BoundingLine(0, 0, 3, 4);
		expectedPointsModel.setBoundingLine(lineCopy);
		expectedPointsModel.setPoints(-6);

		final TwoAxisLinePointsModel actualPointsModel = AbstractBoundingLineModel.enforceShape(stepModel);
		assertEquals(expectedPointsModel, actualPointsModel);
		service.createGenerator(actualPointsModel);
	}

	/**
	 * Points of a PointsModel should lie where the points of the stepmodel that created it do, and should produce
	 * only the same steps, no additional ones.
	 */
	private void testPoints(IScanPointGeneratorModel stepModel, IScanPointGeneratorModel actualPointsModel, MapPosition initialBounds, MapPosition finalBounds) throws GeneratorException {
		AbstractScanPointGenerator<?> expectedGen = (AbstractScanPointGenerator<?>) service.createGenerator(stepModel);
		AbstractScanPointGenerator<?> actualGen = (AbstractScanPointGenerator<?>) service.createGenerator(actualPointsModel);
		final List<IPosition> stepIt = expectedGen.createPoints();
		final List<IPosition> pointIt = actualGen.createPoints();

		assertEquals(stepIt, pointIt);

		assertThat(expectedGen.initialBounds(), is(equalTo(actualGen.initialBounds())));
		assertThat(expectedGen.finalBounds(), is(equalTo(actualGen.finalBounds())));
		expectedGen.createPoints();
		if (initialBounds != null && finalBounds != null) {
			IPosition actualFinal = expectedGen.finalBounds();
			IPosition actualInitial = expectedGen.initialBounds();
			for (String axis : initialBounds.getNames()) {
				assertThat((double) actualInitial.get(axis), is(closeTo((Double) initialBounds.get(axis), 1e-10)));
				assertThat((double) actualFinal.get(axis), is(closeTo((Double) finalBounds.get(axis), 1e-10)));
			}
		}
	}

}
