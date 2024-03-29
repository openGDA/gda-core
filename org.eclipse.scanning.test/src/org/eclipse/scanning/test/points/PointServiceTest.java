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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.junit.jupiter.api.Test;

class PointServiceTest extends AbstractGeneratorTest {

	/**
	 * This tests an important feature of the service
	 * that it takes the bounds of all the regions and
	 * then cuts out the regions from those bounds.
	 *
	 * Bounds are not conserved in the IBoundingBoxModel
	 * @throws GeneratorException
	 *
	 */
	@Test
	void testMultipleGenerationsDifferentBoxes() throws Exception {

		IRectangularROI roi1 = new RectangularROI(0,0,5,5,0);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(5);
		model.setxAxisPoints(5);

		pointGeneratorService.setBounds(model, Arrays.asList(roi1)); // Sets the bounding box

		BoundingBox box1 = model.getBoundingBox();
		assertNotNull(box1);
		checkSame(box1, roi1);

		IRectangularROI roi2 = new RectangularROI(10,10,5,5,0);
		pointGeneratorService.setBounds(model, Arrays.asList(roi2)); // Sets the bounding box

		BoundingBox box2 = model.getBoundingBox();
		assertNotNull(box2);
		checkSame(box2, new RectangularROI(0,0,15,15,0));

	}

	private void checkSame(BoundingBox box, IRectangularROI roi) {
		assertEquals(box.getxAxisStart(), roi.getPointX(), 0.00001);
		assertEquals(box.getyAxisStart(), roi.getPointY(), 0.00001);
		assertEquals(box.getxAxisLength(), roi.getLength(0), 0.00001);
		assertEquals(box.getyAxisLength(), roi.getLength(1), 0.00001);
	}

}
