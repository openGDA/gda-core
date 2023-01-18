/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.points.mutators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.test.points.AbstractGeneratorTest;
import org.junit.jupiter.api.Test;

class FlatteningExcluderTest extends AbstractGeneratorTest {

	@Test
	void testTwoAxisModelsIntoOneDimension() throws GeneratorException {
		var compoundModel = new CompoundModel();
		compoundModel.addModel(new AxialArrayModel("stage_x", 0, 1, 2));
		compoundModel.addModel(new AxialArrayModel("stage_y", 0, 1, 2));

		// Before flattening, we have 2 dimensions, in 1 axis each.
		var generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("stage_x"), List.of("stage_y")), 9);

		// After flattening, we have 1 dimension with 2 axes
		compoundModel.addRegions(List.of(new ScanRegion("Region", List.of("stage_x", "stage_y"))));
		generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("stage_x", "stage_y")), 9);
	}

	@Test
	void testFlattenSingleTwoAxisScan() throws GeneratorException {
		var compoundModel = new CompoundModel();
		compoundModel.addData(new TwoAxisGridPointsModel(), Set.of(new RectangularROI()));

		// Before flattening, we have 2 dimensions, in 1 axis each.
		var generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("stage_y"), List.of("stage_x")), 25);

		// After flattening, we have 1 dimension with 2 axes
		compoundModel.addRegions(List.of(new ScanRegion("region", List.of("stage_y", "stage_x"))));
		generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("stage_y", "stage_x")), 25);
	}

	@Test
	void testAxisModelMergedIntoFlatTwoAxisModel() throws GeneratorException {
		var compoundModel = new CompoundModel();
		compoundModel.addModel(new AxialArrayModel("p", 0, 1, 2));
		var oneDimensionalTwoAxisModel = new TwoAxisLissajousModel();
		oneDimensionalTwoAxisModel.setPoints(5);
		oneDimensionalTwoAxisModel.setBoundingBox(new BoundingBox(0, 0, 1, 1));
		compoundModel.addModel(oneDimensionalTwoAxisModel);

		// Before flattening, we have 1 outer dimension in 1 axis and 1 inner dimension in 2 axes
		var generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("p"), List.of("stage_x", "stage_y")), 15);


		// Flatten in the outer axis and either of the inner axes:
		compoundModel.addRegions(List.of(new ScanRegion("Region", List.of("p", "stage_x"))));
		// Flat dimension still from outer to inner and mapping x, mapping y order
		generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("p", "stage_x", "stage_y")), 15);
	}

	@Test
	void testAxisModelMergedIntoSquareTwoAxisModel() throws GeneratorException {
		var compoundModel = new CompoundModel();
		compoundModel.addModel(new AxialArrayModel("p", 0, 1, 2));
		var twoDimensionalTwoAxisModel = new TwoAxisGridPointsModel();
		twoDimensionalTwoAxisModel.setBoundingBox(new BoundingBox(0, 0, 1, 1));
		compoundModel.addModel(twoDimensionalTwoAxisModel);
		// Before flattening, we have 3 dimensions of 1 axis each
		var generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("p"), List.of("stage_y"), List.of("stage_x")), 75);

		// If we flatten on outer dimension of the grid, we flatten into 2 dimensions
		compoundModel.addRegions(List.of(new ScanRegion("Region", List.of("p", "stage_y"))));
		generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("p", "stage_y"), List.of("stage_x")), 75);

		// If we flatten on inner or both dimension of the grid, we flatten into 2 dimensions
		compoundModel.setRegions(List.of(new ScanRegion("Region", List.of("p", "stage_x"))));
		generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("p", "stage_y", "stage_x")), 75);

		// If we flatten on both dimensions of the grid, we flatten into 1 dimension in fast-slow order, regardless of order in excluder
		compoundModel.setRegions(List.of(new ScanRegion("Region", List.of("p", "stage_x", "stage_y"))));
		generator = pointGeneratorService.createGenerator(compoundModel);
		testAxes(generator, List.of(List.of("p", "stage_y", "stage_x")), 75);
	}

	private void testAxes(IPointGenerator<?> gen, List<List<String>> expectedDimensions, int size) {
		assertThat(gen.getDimensionNames(), is(equalTo(expectedDimensions)));
		assertThat(gen.getFirstPoint().getDimensionNames(), is(equalTo(expectedDimensions)));
		assertThat(gen.size(), is(equalTo(size)));
	}

}
