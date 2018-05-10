/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.OverlapGridModel;
import org.junit.Test;

public class OverlapGridPathTest extends AbstractGeneratorTest {

	@Test
	public void rectangularRoi() throws GeneratorException {
		// square roi of 2x5
		OverlapGridModel model = createTestModel(2.0, 5.0);

		// square beam of 0.3x0.1
		model.setBeamSize(new Object[] {0.3, 0.1});

		// 55% beam overlap
		model.setOverlap(0.55);

		IPointGenerator<OverlapGridModel> gen = service.createGenerator(model);

		// 2 dimensions
		assertEquals(2, gen.getRank());

		// number of points in first dimension:
		// 1) beam size of 0.3 with 55% overlap -> 0.3*0.45 = 0.135 step size
		// 2) 2 length / 0.135 = 14 rounded down
		// 3) + 1 because there is a point at the origin = 15
		int pointsInFirstDimension = 15;

		// number of points in second dimension:
		// 1) beam size of 0.1 with 55% overlap -> 0.1*0.45 = 0.045 step size
		// 2) 5 length / 0.045 = 111 rounded down
		// 3) + 1 because there is a point at the origin = 112
		int pointsInSecondDimension = 112;

		assertArrayEquals(
				new int[] {pointsInSecondDimension, pointsInFirstDimension},
				gen.getShape());

		int numberOfPoints = pointsInFirstDimension * pointsInSecondDimension;

		List<IPosition> points = gen.createPoints();
		assertEquals(numberOfPoints, points.size());
	}

	@Test(expected=ModelValidationException.class)
	public void zeroXBeamSizeThrows() throws Exception {
		OverlapGridModel model = createTestModel();
		model.setXBeamSize(0);
		service.createGenerator(model).validate(model);
	}

	@Test(expected=ModelValidationException.class)
	public void zeroYBeamSizeThrows() throws Exception {
		OverlapGridModel model = createTestModel();
		model.setYBeamSize(0);
		service.createGenerator(model).validate(model);
	}

	@Test(expected=ModelValidationException.class)
	public void negativeOverlapThrows() throws Exception {
		OverlapGridModel model = createTestModel();
		model.setOverlap(-53.6);
		service.createGenerator(model).validate(model);
	}

	@Test(expected=ModelValidationException.class)
	public void overlapGreaterThanOneThrows() throws Exception {
		OverlapGridModel model = createTestModel();
		model.setOverlap(1.0);
		service.createGenerator(model).validate(model);
	}

	private OverlapGridModel createTestModel() {
		return createTestModel(1.0, 1.0);
	}

	/**
	 * @param length of bounding box
	 * @param width of bounding box
	 * @return
	 */
	private OverlapGridModel createTestModel(double length, double width) {
		OverlapGridModel model = new OverlapGridModel();
		model.setBoundingBox(new BoundingBox(0, 0, length, width));
		return model;
	}

}
