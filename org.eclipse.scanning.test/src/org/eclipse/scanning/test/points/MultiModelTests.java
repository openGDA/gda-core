/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.util.Iterator;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.ConcurrentMultiModel;
import org.eclipse.scanning.api.points.models.ConsecutiveMultiModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Test;

public class MultiModelTests {

	private IPointGeneratorService service = new PointGeneratorService();

	@Test
	public void ConsecutiveOfConcurrent() throws GeneratorException {
		AxialArrayModel x = new AxialArrayModel("x");
		x.setPositions(new double[] { 2, 3, 5, 7, 11, 13, 17 });
		AxialStepModel y = new AxialStepModel("y", 0, 6, 1);
		ConcurrentMultiModel concurrent = new ConcurrentMultiModel();
		concurrent.addModel(x);
		concurrent.addModel(y);
		ConsecutiveMultiModel consecutive = new ConsecutiveMultiModel();
		TwoAxisLissajousModel xy = new TwoAxisLissajousModel();
		xy.setxAxisName("x");
		xy.setyAxisName("y");
		xy.setPoints(8);
		xy.setBoundingBox(new BoundingBox(0, 0, 5, 5));
		consecutive.addModel(concurrent);
		consecutive.addModel(xy);
		IPointGenerator<ConsecutiveMultiModel> gen = service.createGenerator(consecutive);
		assertArrayEquals(new int[] { 15 }, gen.getShape());
		Iterator<IPosition> one = service.createGenerator(concurrent).iterator();
		Iterator<IPosition> two = service.createGenerator(xy).iterator();
		ConsecutiveTest.equalIterators(gen.iterator(), true, one, two);
	}

	@Test
	public void ConsecutiveOfConsecutive() throws GeneratorException {
		AxialArrayModel x1 = new AxialArrayModel("x");
		x1.setPositions(new double[] { 2, 3, 5, 7, 11, 13, 17 });
		AxialStepModel x2 = new AxialStepModel("x", 0, 6, 1);
		ConsecutiveMultiModel innerConsecutive = new ConsecutiveMultiModel();
		innerConsecutive.addModel(x1);
		innerConsecutive.addModel(x2);
		ConsecutiveMultiModel outerConsecutive = new ConsecutiveMultiModel();
		AxialStepModel x3 = new AxialStepModel("x", 0, 6, 1);
		outerConsecutive.addModel(innerConsecutive);
		outerConsecutive.addModel(x3);
		IPointGenerator<ConsecutiveMultiModel> gen = service.createGenerator(outerConsecutive);
		Iterator<IPosition> one = service.createGenerator(innerConsecutive).iterator();
		Iterator<IPosition> two = service.createGenerator(x3).iterator();
		ConsecutiveTest.equalIterators(gen.iterator(), true, one, two);
	}

	@Test
	public void ConcurrentOfConcurrent() throws GeneratorException {
		AxialArrayModel x = new AxialArrayModel("x");
		x.setPositions(new double[] { 2, 3, 5, 7, 11, 13, 17 });
		AxialStepModel y = new AxialStepModel("y", 0, 6, 1);
		ConcurrentMultiModel innerConcurrent = new ConcurrentMultiModel();
		innerConcurrent.addModel(x);
		innerConcurrent.addModel(y);
		ConcurrentMultiModel outerConcurrent = new ConcurrentMultiModel();
		TwoAxisLissajousModel pq = new TwoAxisLissajousModel();
		pq.setxAxisName("p");
		pq.setyAxisName("q");
		pq.setPoints(7);
		pq.setBoundingBox(new BoundingBox(0, 0, 5, 5));
		outerConcurrent.addModel(innerConcurrent);
		outerConcurrent.addModel(pq);
		IPointGenerator<ConcurrentMultiModel> gen = service.createGenerator(outerConcurrent);
		Iterator<IPosition> one = service.createGenerator(innerConcurrent).iterator();
		Iterator<IPosition> two = service.createGenerator(pq).iterator();
		ConcurrentTest.equalIterators(gen.iterator(), one, two);
	}

	@Test
	public void ConcurrentOfConsecutive() throws GeneratorException {
		AxialArrayModel x1 = new AxialArrayModel("x");
		x1.setPositions(new double[] { 2, 3, 5, 7, 11, 13, 17 });
		AxialStepModel x2 = new AxialStepModel("x", 0, 6, 1);
		ConsecutiveMultiModel consecutive = new ConsecutiveMultiModel();
		consecutive.addModel(x1);
		consecutive.addModel(x2);
		ConcurrentMultiModel concurrent = new ConcurrentMultiModel();
		// all models with a ConcurrentModel can only be continuous if the concurrent model can be
		// Currently not implemented
		concurrent.setContinuous(false);
		AxialStepModel y = new AxialStepModel("y", 0, 6.5, 0.5);
		concurrent.addModel(consecutive);
		concurrent.addModel(y);
		IPointGenerator<ConcurrentMultiModel> gen = service.createGenerator(concurrent);
		Iterator<IPosition> one = service.createGenerator(consecutive).iterator();
		Iterator<IPosition> two = service.createGenerator(y).iterator();
		ConcurrentTest.equalIterators(gen.iterator(), one, two);
	}
}
