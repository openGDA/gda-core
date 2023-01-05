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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test different scan ranks after compounds are created.
 *
 * @author Matthew Gerring
 *
 */
class ScanRankTest extends AbstractGeneratorTest {

	static Object[] data() {
		return IntStream.range(0, 6).mapToObj(Integer::valueOf).toArray();
	}

	private int polygonSize = 180;
	private int circularSize = 276;

	@ParameterizedTest(name = "nestCount: {0}")
	@MethodSource("data")
	void testRankLine(int nestCount) throws Exception {
		final LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

		final TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
		model.setPoints(10);
		model.setxAxisName("x");
		model.setyAxisName("y");

		// Get the point list
		final CompoundModel cModel = new CompoundModel();

		for (int i = 0; i < nestCount; i++) {
			cModel.addModel(new AxialStepModel("T"+(nestCount - 1 - i), 290, 300, 1));
		}

		cModel.addData(model,  Arrays.asList(roi));

		var generator = pointGeneratorService.createCompoundGenerator(cModel);
		checkGenerator(generator, nestCount, 10);
	}

	@ParameterizedTest(name = "nestCount {0}")
	@MethodSource("data")
	void testRankSpiral(int nestCount) throws Exception {
		final BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		final TwoAxisSpiralModel model = new TwoAxisSpiralModel("x", "y");
		model.setBoundingBox(box);

		// Get the point list
		final CompoundModel cModel = new CompoundModel();
		for (int i = 0; i < nestCount; i++) {
			cModel.addModel(new AxialStepModel("T"+(nestCount -1 -i), 290, 300, 1));
		}
		cModel.addModel(model);

		var gen = pointGeneratorService.createCompoundGenerator(cModel);
		checkGenerator(gen, nestCount, 15);
	}

	@ParameterizedTest(name = "nestCount {0}")
	@MethodSource("data")
	void testRankGrid(int nestCount) throws Exception {
		var gen = createGridGenerator(nestCount, null);
		checkGenerator(gen, nestCount, 20, 20);
	}

	@ParameterizedTest(name = "nestCount {0}")
	@MethodSource("data")
	void testRankGridWithCircularRegion(int nestCount) throws Exception {
		final IROI region = new CircularROI(2, 1, 1);
		var gen = createGridGenerator(nestCount, region);
		checkGenerator(gen, nestCount, circularSize);
	}

	@ParameterizedTest(name = "nestCount {0}")
	@MethodSource("data")
	void testRankGridWithPolygonRegion(int nestCount) throws Exception {
		final PolygonalROI diamond = new PolygonalROI(new double[] { 1.5, 0 });
		diamond.insertPoint(new double[] { 3, 1.5 });
		diamond.insertPoint(new double[] { 1.5, 3 });
		diamond.insertPoint(new double[] { 0, 1.5 });

		var gen = createGridGenerator(nestCount, diamond);
		checkGenerator(gen, nestCount, polygonSize);
	}

	private int[] getExpectedShape(int nestCount, int... innerShape) {
		return Stream.concat(Collections.nCopies(nestCount, 11).stream(),
				Arrays.stream(innerShape).mapToObj(Integer::new)).
				mapToInt(Integer::valueOf).toArray();
	}

	private void checkGenerator(IPointGenerator<CompoundModel> gen, int nestCount, int... innerShape) {
		final int innerSize = Arrays.stream(innerShape).reduce(1, (x, y) -> Math.multiplyExact(x, y));
		final int expectedSize = innerSize * (int) Math.pow(11, nestCount);
		final int expectedScanRank = nestCount + innerShape.length;
		final int[] expectedShape = getExpectedShape(nestCount, innerShape);
		final List<List<String>> dimensionNames = gen.getDimensionNames();
		final AbstractPosition firstPos = (AbstractPosition) gen.getFirstPoint();

		assertThat(gen.size(), is(expectedSize));
		assertThat(gen.getRank(), is(expectedScanRank));
		assertThat(gen.getShape(), is(equalTo(expectedShape)));

		assertThat(firstPos.getDimensionNames(), is(equalTo(dimensionNames)));

		assertThat("Unexpected scan rank for pos " + firstPos, firstPos.getScanRank(), is(expectedScanRank));
		for (int i = 0; i < nestCount; i++) {
			var expectedDimensionNames = List.of("T" + (nestCount - 1 - i));
			assertThat(firstPos.getDimensionNames(i), is(equalTo(expectedDimensionNames)));
			assertThat(dimensionNames.get(i), is(equalTo(expectedDimensionNames)));
		}
		if (nestCount > 0) {
			if (innerShape.length == 1) {
				// Grid models have ordered slow, fast (even when squashed), non-grid models have x, y.
				var expectedAxisOrdering = (!(gen.getModel().getModels().get(expectedScanRank - 1) instanceof AbstractTwoAxisGridModel grid) || grid.isVerticalOrientation()) ? List.of("x", "y") : List.of("y", "x");
				assertThat(firstPos.getDimensionNames(expectedScanRank - 1), is(equalTo(expectedAxisOrdering)));
				assertThat(dimensionNames.get(expectedScanRank - 1), is(equalTo(expectedAxisOrdering)));
			} else {
				assertThat(firstPos.getDimensionNames(expectedScanRank - 1), contains("x"));
				assertThat(firstPos.getDimensionNames(expectedScanRank - 2), contains("y"));
				assertThat(dimensionNames.get(expectedScanRank - 1), contains("x"));
				assertThat(dimensionNames.get(expectedScanRank - 2), contains("y"));
			}
		}

	}

	private IPointGenerator<CompoundModel> createGridGenerator(int nestCount, IROI region) throws Exception {
		final BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		final TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setBoundingBox(box);

		final CompoundModel cModel = new CompoundModel();
		for (int i = 0; i < nestCount; i++) {
			cModel.addModel(new AxialStepModel("T"+(nestCount -1 -i), 290, 300, 1));
		}
		cModel.addData(model, region == null ? Collections.<IROI>emptyList() : Arrays.<IROI>asList(region));

		return pointGeneratorService.createCompoundGenerator(cModel);
	}

}
