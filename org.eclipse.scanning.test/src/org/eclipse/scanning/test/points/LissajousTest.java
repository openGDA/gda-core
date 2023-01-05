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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("DAQ-2088 Tests have not been implemented")
class LissajousTest extends AbstractGeneratorTest {

	@Test
	void testLissajousNoROI() throws GeneratorException {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-10);
		box.setyAxisStart(5);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		TwoAxisLissajousModel model = new TwoAxisLissajousModel();
		model.setBoundingBox(box);
		var generator = pointGeneratorService.createGenerator(model);

		// Get the point list
		List<IPosition> pointList = generator.createPoints();

		assertEquals(503, pointList.size());

		// Test a few points
		// TODO check x and y index values - currently these are not tested by AbstractPosition.equals()
		assertEquals(new Point(0, -8.5, 0, 7.0, 0, false), pointList.get(0));
		assertEquals(new Point(100, -9.939837880744866, 100, 6.925069008238842, 100, false), pointList.get(100));
		assertEquals(new Point(300, -7.5128903496577735, 300, 6.775627736273646, 300, false), pointList.get(300));
	}

	@Test
	void testLissajousWithBoundingRectangle() {
		Assert.fail(); // Not yet implemented
	}

	@Test
	void testLissajousWithCircularRegion() {
		Assert.fail(); // Not yet implemented
	}

	@Test
	void testLissajousWithPolygonRegion() {
		Assert.fail(); // Not yet implemented
	}

}
