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
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MultiModelTest {

	private static final IPointGeneratorService pointGeneratorService = new PointGeneratorService();

	@BeforeAll
	public static void setUpClass() {
		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setValidatorService(new ValidatorService());
		serviceHolder.setPointGeneratorService(pointGeneratorService);
	}

	@Test
	@Disabled("Currently failing because Array([u'x', u'y'] != Array(['x', 'y']); pull request for SPG created since u'x' = u'x'")
	public void ConsecutiveOfConcurrent() throws GeneratorException {
		AxialArrayModel x = new AxialArrayModel("xxxxx");
		// Bounds 2.5 -> 19
		x.setPositions(new double[] { 2, 3, 5, 7, 11, 13, 17 });
		// Bounds -0.5 -> 6.5
		AxialStepModel y = new AxialStepModel("yajhf", 0, 6, 1);
		ConcurrentMultiModel concurrent = new ConcurrentMultiModel();
		concurrent.addModel(x);
		concurrent.addModel(y);
		ConsecutiveMultiModel consecutive = new ConsecutiveMultiModel();
		TwoAxisLissajousModel xy = new TwoAxisLissajousModel();
		xy.setxAxisName("xxxxx");
		xy.setyAxisName("yajhf");
		// Bounds (19, 6.5) -> (24, 11.5)
		xy.setBoundingBox(new BoundingBox(17, 6, 4, 1));
		consecutive.addModel(concurrent);
		consecutive.addModel(xy);
		consecutive.setContinuous(false);
		IPointGenerator<ConsecutiveMultiModel> gen = pointGeneratorService.createGenerator(consecutive);
		// 7 + 503 points = 510 points, 1 dimension in (xxxxx, yajhf)
		assertArrayEquals(new int[] { 510 }, gen.getShape());
		Iterator<IPosition> one = pointGeneratorService.createGenerator(concurrent).iterator();
		Iterator<IPosition> two = pointGeneratorService.createGenerator(xy).iterator();
		ConsecutiveTest.equalIterators(gen.iterator(), true, one, two);
	}

	@Test
	public void testConsecutiveWithTwoAxes() throws GeneratorException {
		ConsecutiveMultiModel consecutive = new ConsecutiveMultiModel();
		TwoAxisLissajousModel xy = new TwoAxisLissajousModel();
		xy.setxAxisName("xxxxx");
		xy.setyAxisName("yajhf");
		xy.setBoundingBox(new BoundingBox(17, 6, 4, 1));
		TwoAxisLissajousModel zp = new TwoAxisLissajousModel();
		zp.setxAxisName("xxxxx");
		zp.setyAxisName("yajhf");
		zp.setBoundingBox(xy.getBoundingBox());
		consecutive.addModel(zp);
		consecutive.addModel(xy);
		pointGeneratorService.createGenerator(consecutive);
	}

	@Test
	public void ConsecutiveOfConsecutive() throws GeneratorException {
		AxialArrayModel x1 = new AxialArrayModel("x");
		// Bounds 1.5 -> 19
		x1.setPositions(new double[] { 2, 3, 5, 7, 11, 13, 17 });
		// Bounds 19 -> 26
		AxialStepModel x2 = new AxialStepModel("x", 19.5, 25.5, 1);
		// => Bounds 1.5 -> 26
		ConsecutiveMultiModel innerConsecutive = new ConsecutiveMultiModel();
		innerConsecutive.addModel(x1);
		innerConsecutive.addModel(x2);
		ConsecutiveMultiModel outerConsecutive = new ConsecutiveMultiModel();
		// Bounds 26 -> 32
		AxialStepModel x3 = new AxialStepModel("x", 26.5, 32, 1);
		// => Bounds 1.5 -> 32
		outerConsecutive.addModel(innerConsecutive);
		outerConsecutive.addModel(x3);
		outerConsecutive.setContinuous(false);
		IPointGenerator<ConsecutiveMultiModel> gen = pointGeneratorService.createGenerator(outerConsecutive);
		Iterator<IPosition> one = pointGeneratorService.createGenerator(innerConsecutive).iterator();
		Iterator<IPosition> two = pointGeneratorService.createGenerator(x3).iterator();
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
		IPointGenerator<ConcurrentMultiModel> gen = pointGeneratorService.createGenerator(outerConcurrent);
		Iterator<IPosition> one = pointGeneratorService.createGenerator(innerConcurrent).iterator();
		Iterator<IPosition> two = pointGeneratorService.createGenerator(pq).iterator();
		ConcurrentTest.equalIterators(gen.iterator(), one, two);
	}

	@Test
	public void ConcurrentOfConsecutive() throws GeneratorException {
		AxialArrayModel x1 = new AxialArrayModel("x");
		// Bounds 2.5 -> 19
		x1.setPositions(new double[] { 2, 3, 5, 7, 11, 13, 17 });
		// Bounds 19 -> 33
		AxialStepModel x2 = new AxialStepModel("x", 20, 32, 2);
		ConsecutiveMultiModel consecutive = new ConsecutiveMultiModel();
		consecutive.addModel(x1);
		consecutive.addModel(x2);
		ConcurrentMultiModel concurrent = new ConcurrentMultiModel();
		AxialStepModel y = new AxialStepModel("y", 0, 6.5, 0.5);
		concurrent.addModel(consecutive);
		concurrent.addModel(y);
		IPointGenerator<ConcurrentMultiModel> gen = pointGeneratorService.createGenerator(concurrent);
		Iterator<IPosition> one = pointGeneratorService.createGenerator(consecutive).iterator();
		Iterator<IPosition> two = pointGeneratorService.createGenerator(y).iterator();
		ConcurrentTest.equalIterators(gen.iterator(), one, two);
	}
}
