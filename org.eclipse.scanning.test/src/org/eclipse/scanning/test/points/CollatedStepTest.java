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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.junit.Test;

public class CollatedStepTest extends AbstractGeneratorTest {

	@Test
	public void testCollatednessOfStep() throws GeneratorException {
		AxialCollatedStepModel model = new AxialCollatedStepModel(0, 100, 7, "x", "y", "z");
		IPointGenerator<AxialCollatedStepModel> gen = service.createGenerator(model);

		for (IPosition pos : gen.createPoints()) {
			assertEquals(pos.get("x"), pos.get("y"));
			assertEquals(pos.get("x"), pos.get("z"));
		}
	}

	@Test
	public void testStepnessOfCollated() throws GeneratorException {
		AxialCollatedStepModel model = new AxialCollatedStepModel(0, 10, 1, "x", "y", "z");
		IPointGenerator<AxialCollatedStepModel> gen = service.createGenerator(model);

		Iterator<IPosition> it = gen.iterator();
		double position = 0;
		while (it.hasNext()) {
			assertEquals(position++, it.next().get("x"));
		}
	}

	@Test
	public void testNonMatchingUnits() {
		AxialCollatedStepModel model = new AxialCollatedStepModel(0, 10, 1, "x", "y", "z");
		model.setUnits(new ArrayList<>(Arrays.asList("AU", "pm")));
		assertEquals(Arrays.asList("AU", "pm", "mm"), model.getUnits());
	}

	@Test
	public void withRegionInCollatedAxes() throws GeneratorException {
		AxialCollatedStepModel model = new AxialCollatedStepModel(0, 10, 1, "x", "y", "z");
		List<IROI> circ = Arrays.asList(new CircularROI(3, 4.5, 4.5));
		IPointGenerator<CompoundModel> gen = service.createGenerator(model, circ);
		assertEquals(4, gen.size());
	}

	@Test
	public void testGridWrtCompound() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		AxialCollatedStepModel model = new AxialCollatedStepModel(0, 10, 1, "x", "y");
		IPointGenerator<CompoundModel> gen = service.createGenerator(model, roi);
		System.out.println(gen.createPoints().size());


		checkWrtCompound(model, roi, 4); // (0,0,0)->(3,3,3)
	}


}
