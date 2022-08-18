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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.PySerializable;
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.mutators.RandomOffsetMutator;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.python.core.PyDictionary;
import org.python.core.PyList;

public class CompoundTest {

	private static final IPointGeneratorService pointGeneratorService = new PointGeneratorService();

	@BeforeAll
	public static void beforeClass() {
		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setPointGeneratorService(pointGeneratorService);
		serviceHolder.setValidatorService(new ValidatorService());
	}

	@Test(expected=ModelValidationException.class)
	public void testCompoundCompoundException() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel();
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setBoundingBox(box);

		// Throws exception because compoundedCompound contains 2 models in axis "Position"
		CompoundModel cModel = new CompoundModel(new AxialStepModel("Position", 1, 4, 0.6));
		CompoundModel compoundedCompound = new CompoundModel(cModel, cModel.getModels().get(0));
		pointGeneratorService.createCompoundGenerator(compoundedCompound);
	}

	@Test(expected=GeneratorException.class)
	public void testDuplicateAxisNameException() throws Exception {

		IPointGenerator<AxialStepModel> pos1 = pointGeneratorService.createGenerator(new AxialStepModel("Position", 1, 4, 0.6));
		IPointGenerator<AxialStepModel> pos2 = pointGeneratorService.createGenerator(new AxialStepModel("Position", 1, 4, 0.6));
		pointGeneratorService.createCompoundGenerator(Arrays.asList(pos1, pos2));
	}

	@Test(expected=ModelValidationException.class)
	public void testDuplicateAxisNameExceptionForModel() throws Exception {

		CompoundModel cModel = new CompoundModel(new AxialStepModel("Position", 1, 4, 0.6));
		cModel.addModel(cModel.getModels().get(0));
		pointGeneratorService.createCompoundGenerator(cModel);
	}

	@Test
	public void testIteratedSize() throws Exception {

		IPointGenerator<AxialStepModel> temp = pointGeneratorService.createGenerator(new AxialStepModel("Temperature", 290,295,1));
		assertEquals(6, temp.size());
		assertEquals(1, temp.getRank());
		assertArrayEquals(new int[] { 6 }, temp.getShape());

		IPointGenerator<AxialStepModel> pos = pointGeneratorService.createGenerator(new AxialStepModel("Position", 1,4, 0.6));
		assertEquals(6, pos.size());
		assertEquals(1, pos.getRank());
		assertArrayEquals(new int[] { 6 }, temp.getShape());

		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(Arrays.asList(temp, pos));
		assertTrue(scan.iterator().next()!=null);
		assertEquals(36, scan.size());
		assertEquals(2, scan.getRank());
		assertArrayEquals(new int[] { 6, 6 }, scan.getShape());

		Iterator<IPosition> it = scan.iterator();
		int size = scan.size();
		int sz=0;
		while(it.hasNext()) {
			it.next();
			sz++;
			if (sz>size) throw new Exception("Iterator grew too large!");
		}
	}

	@Test
	public void testSimpleCompoundStep2Step() throws Exception {

		IPointGenerator<AxialStepModel> temp = pointGeneratorService.createGenerator(new AxialStepModel("Temperature", 290,295,1));
		assertEquals(6, temp.size());
		assertEquals(1, temp.getRank());
		assertArrayEquals(new int[] { 6 }, temp.getShape());

		IPointGenerator<AxialStepModel> pos = pointGeneratorService.createGenerator(new AxialStepModel("Position", 1,4, 0.6));
		assertEquals(6, pos.size());
		assertEquals(1, pos.getRank());
		assertArrayEquals(new int[] { 6 }, temp.getShape());

		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(Arrays.asList(temp, pos));
		assertEquals(36, scan.size());
		assertEquals(2, scan.getRank());
		assertArrayEquals(new int[] { 6, 6 }, scan.getShape());

		final List<IPosition> points = scan.createPoints();

		// 290K
		assertEquals(290.0, points.get(0).get("Temperature"));
		assertEquals(1.0,   points.get(0).get("Position"));
		assertEquals(290.0, points.get(1).get("Temperature"));
		assertEquals(1.6, points.get(1).get("Position"));
		assertEquals(290.0, points.get(2).get("Temperature"));
		assertEquals(2.2, points.get(2).get("Position"));

		// 291K
		assertEquals(291.0, points.get(6).get("Temperature"));
		assertEquals(1.0,   points.get(6).get("Position"));
		assertEquals(291.0, points.get(7).get("Temperature"));
		assertEquals(1.6, points.get(7).get("Position"));
		assertEquals(291.0, points.get(8).get("Temperature"));
		assertEquals(2.2, points.get(8).get("Position"));

		// 295K
		assertEquals(295.0, points.get(30).get("Temperature"));
		assertEquals(1.0,   points.get(30).get("Position"));
		assertEquals(295.0, points.get(31).get("Temperature"));
		assertEquals(1.6, points.get(31).get("Position"));
		assertEquals(295.0, points.get(32).get("Temperature"));
		assertEquals(2.2, points.get(32).get("Position"));

        GeneratorUtil.testGeneratorPoints(scan);
	}

	@Test
	public void testSimpleToDict() throws Exception {

		IPointGenerator<AxialStepModel> temp = pointGeneratorService.createGenerator(new AxialStepModel("Temperature", 290, 295, 1));
		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(Arrays.asList(temp));

		Map<?,?> dict = ((PySerializable)scan).toDict();

		PyList gens = (PyList) dict.get("generators");
		PyDictionary line1 = (PyDictionary) gens.get(0);

		assertEquals("Temperature", ((List<String>) line1.get("axes")).get(0));
		assertEquals("mm", ((List<String>) line1.get("units")).get(0));
		assertEquals(290.0, (double) ((PyList) line1.get("start")).get(0), 1E-10);
		assertEquals(295.0, (double) ((PyList) line1.get("stop")).get(0), 1E-10);
		assertEquals(6, (int) line1.get("size"));

		PyList excluders = (PyList) dict.get("excluders");
		PyList mutators = (PyList) dict.get("mutators");
		assertEquals(new PyList(), excluders);
		assertEquals(new PyList(), mutators);
	}

	@Test
	public void testNestedToDict() throws Exception {

		AxialStepModel temp = new AxialStepModel("Temperature", 290, 295, 1);
		AxialStepModel pos = new AxialStepModel("Position", 1, 4, 0.6);
		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(new CompoundModel(temp, pos));

		Map<?,?> dict = ((PySerializable)scan).toDict();

		PyList gens = (PyList) dict.get("generators");
		PyDictionary line1 = (PyDictionary) gens.get(0);
		PyDictionary line2 = (PyDictionary) gens.get(1);

		assertEquals("Temperature", ((List<String>) line1.get("axes")).get(0));
		assertEquals("mm", ((List<String>) line1.get("units")).get(0));
		assertEquals(290.0, (double) ((PyList) line1.get("start")).get(0), 1E-10);
		assertEquals(295.0, (double) ((PyList) line1.get("stop")).get(0), 1E-10);
		assertEquals(6, (int) line1.get("size"));

		assertEquals("Position", ((List<String>) line2.get("axes")).get(0));
		assertEquals("mm", ((List<String>) line2.get("units")).get(0));
		assertEquals(1.0, (double) ((PyList) line2.get("start")).get(0), 1E-10);
		assertEquals(4.0, (double) ((PyList) line2.get("stop")).get(0), 1E-10);
		assertEquals(6, (int) line2.get("size"));

		PyList excluders = (PyList) dict.get("excluders");
		PyList mutators = (PyList) dict.get("mutators");
		assertEquals(new PyList(), excluders);
		assertEquals(new PyList(), mutators);
	}

	@Test
	public void testGridContinuousToDict() throws Exception {
		IPointGenerator<AxialStepModel> temp = pointGeneratorService.createGenerator(new AxialStepModel("Temperature", 290,300,1));
		assertEquals(11, temp.size());
		assertEquals(1, temp.getRank());
		assertArrayEquals(new int[] { 11 }, temp.getShape());

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setBoundingBox(box);
		model.setContinuous(false);

		// Create a compound generator from the grid model and check it has the continous flag set to true
		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(new CompoundModel(model));

		Map<?,?> dict = ((PySerializable) scan).toDict();
		assertEquals(false, dict.get("continuous"));

		// set continuous to false and create another compound generator
		model.setContinuous(true);
		scan = pointGeneratorService.createCompoundGenerator(new CompoundModel(model));

		dict = ((PySerializable) scan).toDict();
		assertEquals(true, dict.get("continuous"));
	}

	@Test
	public void testSimpleCompoundStep3Step() throws Exception {

		IPointGenerator<AxialStepModel> temp = pointGeneratorService.createGenerator(new AxialStepModel("Temperature", 290,295,1));
		assertEquals(6, temp.size());
		assertEquals(1, temp.getRank());
		assertArrayEquals(new int[] { 6 }, temp.getShape());

		IPointGenerator<AxialStepModel> y = pointGeneratorService.createGenerator(new AxialStepModel("Y", 11, 14, 0.6));
		assertEquals(6, y.size());
		assertEquals(1, y.getRank());
		assertArrayEquals(new int[] { 6 }, y.getShape());

		IPointGenerator<AxialStepModel> x = pointGeneratorService.createGenerator(new AxialStepModel("X", 1, 4, 0.6));
		assertEquals(6, x.size());
		assertEquals(1, x.getRank());
		assertArrayEquals(new int[] { 6 }, x.getShape());

		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(Arrays.asList(temp, y, x));
		assertEquals(216, scan.size());
		assertEquals(3, scan.getRank());
		assertArrayEquals(new int[] { 6, 6, 6 }, scan.getShape());

		final List<IPosition> points = scan.createPoints();

		// 290K
		assertEquals(290.0, points.get(0).get("Temperature"));
		assertEquals(11.0,  points.get(0).get("Y"));
		assertEquals(1.0,   points.get(0).get("X"));
		assertEquals(290.0, points.get(1).get("Temperature"));
		assertEquals(11.0,  points.get(1).get("Y"));
		assertEquals(1.6, points.get(1).get("X"));
		assertEquals(290.0, points.get(2).get("Temperature"));
		assertEquals(11.0,  points.get(2).get("Y"));
		assertEquals(2.2, points.get(2).get("X"));

		// 291K
		assertEquals(291.0, points.get(36).get("Temperature"));
		assertEquals(11.0,  points.get(36).get("Y"));
		assertEquals(1.0,   points.get(36).get("X"));
		assertEquals(291.0, points.get(37).get("Temperature"));
		assertEquals(11.0,  points.get(37).get("Y"));
		assertEquals(1.6, points.get(37).get("X"));
		assertEquals(291.0, points.get(38).get("Temperature"));
		assertEquals(11.0,  points.get(38).get("Y"));
		assertEquals(2.2, points.get(38).get("X"));

		// 295K
		assertEquals(295.0, points.get(180).get("Temperature"));
		assertEquals(11.0,  points.get(180).get("Y"));
		assertEquals(1.0,   points.get(180).get("X"));
		assertEquals(295.0, points.get(181).get("Temperature"));
		assertEquals(11.0,  points.get(181).get("Y"));
		assertEquals(1.6, points.get(181).get("X"));
		assertEquals(295.0, points.get(182).get("Temperature"));
		assertEquals(11.0,  points.get(182).get("Y"));
		assertEquals(2.2, points.get(182).get("X"));

        GeneratorUtil.testGeneratorPoints(scan);
	}

	@Test
	public void testSimpleCompoundGrid() throws Exception {

		IPointGenerator<AxialStepModel> temp = pointGeneratorService.createGenerator(new AxialStepModel("Temperature", 290,300,1));
		assertEquals(11, temp.size());
		assertEquals(1, temp.getRank());
		assertArrayEquals(new int[] { 11 }, temp.getShape());

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> grid = pointGeneratorService.createGenerator(model);
		assertEquals(400, grid.size());
		assertEquals(2, grid.getRank());
		assertArrayEquals(new int[] { 20, 20 }, grid.getShape());

		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(Arrays.asList(temp, grid));
		assertEquals(4400, scan.size());
		assertEquals(3, scan.getRank());
		assertArrayEquals(new int[] { 11, 20, 20 }, scan.getShape());

		List<IPosition> points = scan.createPoints();

		List<IPosition> first400 = new ArrayList<>(400);

		// The first 400 should be T=290
		for (int i = 0; i < 400; i++) {
			assertEquals(290.0, points.get(i).get("Temperature"));
			first400.add(points.get(i));
		}
		checkPoints(first400);

		for (int i = 400; i < 800; i++) {
			assertEquals(291.0, points.get(i).get("Temperature"));
		}
		for (int i = 4399; i >= 4000; i--) {
			assertEquals(300.0, points.get(i).get("Temperature"));
		}
        GeneratorUtil.testGeneratorPoints(scan);
	}

	@Test
	public void testSimpleCompoundGridWithCircularRegion() throws Exception {
		IPointGenerator<AxialStepModel> temp = pointGeneratorService.createGenerator(new AxialStepModel("Temperature", 290,300,1));
		final int expectedOuterSize = 11;
		assertEquals(expectedOuterSize, temp.size());
		assertEquals(1, temp.getRank());
		assertArrayEquals(new int[] { expectedOuterSize }, temp.getShape());

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setBoundingBox(box);

		IROI region = new CircularROI(2, 1, 1);
		IPointGenerator<CompoundModel> grid = pointGeneratorService.createGenerator(model, region);

		final int expectedInnerSize = 276;
		assertEquals(expectedInnerSize, grid.size());
		assertEquals(1, grid.getRank());
		assertArrayEquals(new int[] { expectedInnerSize }, grid.getShape());

		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(Arrays.asList(temp, grid));
		final int expectedScanSize = expectedOuterSize * expectedInnerSize;
		assertEquals(expectedScanSize, scan.size());
		assertEquals(2, scan.getRank());
		assertArrayEquals(new int[] { expectedOuterSize, expectedInnerSize }, scan.getShape());

		List<IPosition> points = scan.createPoints();

		// The first 400 should be T=290
		for (int i = 0; i < expectedInnerSize; i++) {
			assertEquals("i = " + i, 290.0, points.get(i).get("Temperature"));
		}
		for (int i = expectedInnerSize, max = expectedInnerSize * 2; i < max; i++) {
			assertEquals(291.0, points.get(i).get("Temperature"));
		}
		for (int i = expectedScanSize - 1, min = expectedScanSize - expectedInnerSize; i >= min; i--) {
			assertEquals(300.0, points.get(i).get("Temperature"));
		}
        GeneratorUtil.testGeneratorPoints(scan, expectedOuterSize, expectedInnerSize);
	}


	@Test
	public void testGridCompoundGrid() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model1 = new TwoAxisGridPointsModel();
		model1.setyAxisPoints(5);
		model1.setxAxisPoints(5);
		model1.setBoundingBox(box);
		model1.setxAxisName("x");
		model1.setyAxisName("y");

		IPointGenerator<TwoAxisGridPointsModel> grid1 = pointGeneratorService.createGenerator(model1);
		assertEquals(25, grid1.size());
		assertEquals(2, grid1.getRank());
		assertArrayEquals(new int[] { 5, 5 }, grid1.getShape());

		TwoAxisGridPointsModel model2 = new TwoAxisGridPointsModel();
		model2.setyAxisPoints(5);
		model2.setxAxisPoints(5);
		model2.setBoundingBox(box);
		model2.setxAxisName("x2");
		model2.setyAxisName("y2");

		IPointGenerator<TwoAxisGridPointsModel> grid2 = pointGeneratorService.createGenerator(model2);
		assertEquals(25, grid2.size());
		assertEquals(2, grid2.getRank());
		assertArrayEquals(new int[] { 5, 5 }, grid2.getShape());

		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(Arrays.asList(grid1, grid2));
		assertEquals(625, scan.size());
		assertEquals(4, scan.getRank());
		assertArrayEquals(new int[] { 5, 5, 5, 5 }, scan.getShape());

        GeneratorUtil.testGeneratorPoints(scan);
	}

	private void checkPoints(List<IPosition> pointList) {
		// Check correct number of points
		assertEquals(20 * 20, pointList.size());

		final double start = 0;
		final double step = 3.0/19;

		// Check some random points are correct
		assertEquals(start, (Double)pointList.get(0).get("x"), 1e-8);
		assertEquals(start, (Double)pointList.get(0).get("y"), 1e-8);

		assertEquals(start + 3 * step, (Double)pointList.get(3).get("x"), 1e-8);
		assertEquals(start + 0.0, (Double)pointList.get(3).get("y"), 1e-8);

		assertEquals(start + 2 * step, (Double)pointList.get(22).get("x"), 1e-8);
		assertEquals(start + 1 * step, (Double)pointList.get(22).get("y"), 1e-8);

		assertEquals(start + 10 * step, (Double)pointList.get(350).get("x"), 1e-8);
		assertEquals(start + 17 * step, (Double)pointList.get(350).get("y"), 1e-8);

	}

	@Test
	public void testNestedNeXus() throws Exception {

		int[] size = {10,8,5};

		// Create scan points for a grid and make a generator
		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("xNex");
		gmodel.setxAxisPoints(size[size.length-2]);
		gmodel.setyAxisName("yNex");
		gmodel.setyAxisPoints(size[size.length-1]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		// Add grid model last so innermost
		CompoundModel cModel = new CompoundModel();

		// We add the outer scans, if any
		if (size.length > 2) {
			for (int dim = 0; dim < size.length -2; dim++) {
				cModel.addModel(new AxialStepModel("neXusScannable"+dim, 10,20,11/size[dim]));
			}
		}
		cModel.addModel(gmodel);

		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(cModel);


		final IPosition pos = gen.iterator().next();
		assertEquals(size.length, pos.size());

	}

	@Test
	public void testMultiAroundGrid() throws Exception {

		final AxialMultiStepModel mmodel = new AxialMultiStepModel();
		mmodel.setName("energy");
		mmodel.addModel(new AxialStepModel("energy", 10000,20000,10000)); // 2 points

		assertEquals(2, pointGeneratorService.createGenerator(mmodel).size());


		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y", 5, 5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		IPointGenerator<CompoundModel> scan = pointGeneratorService.createCompoundGenerator(new CompoundModel(mmodel, gmodel));

		assertEquals(50, scan.size());
	}

	@Test
	public void testCompoundingCompoundGenerator() throws GeneratorException {
		// Compound a CompoundGenerator with more generators, making same points as compound generator of constituent generators
		AxialStepModel xmodel = new AxialStepModel("x", 0, 10, 1);
		AxialStepModel ymodel = new AxialStepModel("y", 0, 5, 1);
		CompoundModel firstCompounded = new CompoundModel(xmodel, ymodel);
		IPointGenerator<CompoundModel> firstCompound = pointGeneratorService.createCompoundGenerator(firstCompounded);

		AxialStepModel zmodel = new AxialStepModel("z", 0, 15, 5);
		CompoundModel allInOne = new CompoundModel(xmodel, ymodel, zmodel);
		IPointGenerator<AxialStepModel> compounder = pointGeneratorService.createGenerator(zmodel);
		IPointGenerator<CompoundModel> secondCompound = pointGeneratorService.createCompoundGenerator(Arrays.asList(firstCompound, compounder));

		IPointGenerator<CompoundModel> result = pointGeneratorService.createCompoundGenerator(allInOne);

		assertArrayEquals(result.getShape(), secondCompound.getShape());
		Iterator<IPosition> one = result.iterator();
		Iterator<IPosition> two = secondCompound.iterator();
		while(one.hasNext()) {
			assertEquals(one.next(), two.next());
		}
		if (two.hasNext()) {
			fail();
		}
	}

	@Test
	public void testCompoundingCompoundGeneratorWithMutatorAndRegions() throws GeneratorException {
		AxialStepModel xmodel = new AxialStepModel("x", 0, 10, 1);
		AxialStepModel ymodel = new AxialStepModel("y", 0, 5, 1);
		ScanRegion region = new ScanRegion(new EllipticalROI(5, 3, 5, 5, 0), "x", "y");
		CompoundModel firstCompounded = new CompoundModel(xmodel, ymodel);
		firstCompounded.addRegions(Arrays.asList(region));

		int seed = 12;
		List<String> axes = Arrays.asList("x");
		Map<String, Double> offset = new HashMap<>();
		List<IMutator> muts = Arrays.asList(new RandomOffsetMutator(seed, axes, offset));
		offset.put("x", 0.07);
		firstCompounded.addMutators(muts);
		IPointGenerator<CompoundModel> firstCompound = pointGeneratorService.createCompoundGenerator(firstCompounded);

		AxialStepModel zmodel = new AxialStepModel("z", 0, 15, 5);
		CompoundModel allInOne = new CompoundModel(xmodel, ymodel, zmodel);
		allInOne.addMutators(muts);
		allInOne.addRegions(Arrays.asList(region));
		IPointGenerator<AxialStepModel> compounder = pointGeneratorService.createGenerator(zmodel);
		IPointGenerator<CompoundModel> secondCompound = pointGeneratorService.createCompoundGenerator(Arrays.asList(firstCompound, compounder));

		IPointGenerator<CompoundModel> result = pointGeneratorService.createCompoundGenerator(allInOne);

		assertArrayEquals(result.getShape(), secondCompound.getShape());
		Iterator<IPosition> one = result.iterator();
		Iterator<IPosition> two = secondCompound.iterator();
		while(one.hasNext()) {
			assertEquals(one.next(), two.next());
		}
		if (two.hasNext()) {
			fail();
		}
	}

	@Test
	public void pyRoiFromScanRegion() throws GeneratorException {
		CompoundModel cModel = new CompoundModel();

		TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setBoundingBox(new BoundingBox(3, 7, 2, 6));
		gridModel.setxAxisName("x");
		gridModel.setyAxisName("y");

		cModel.addModel(gridModel);

		ScanRegion sr = new ScanRegion();
		// Non-overlapping region, 3<x<5, 7<y<13
		sr.setRoi(new RectangularROI(3, 7, 2, 6, 0));
		sr.setScannables(Arrays.asList("x", "y"));

		cModel.setRegions(Arrays.asList(sr));

		List<IPosition> allPositions = pointGeneratorService.createCompoundGenerator(cModel).createPoints();
		assertEquals(25, allPositions.size());
	}

	@Test(expected = ModelValidationException.class)
	public void regionForWrongAxis() throws GeneratorException {
		CompoundModel cModel = new CompoundModel();

		TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setBoundingBox(new BoundingBox(3, 7, 2, 6));
		gridModel.setxAxisName("x");
		gridModel.setyAxisName("y");

		cModel.addModel(gridModel);

		ScanRegion sr = new ScanRegion();
		// Non-overlapping region, 3<x<5, 7<y<13
		sr.setRoi(new RectangularROI(3, 7, 2, 6, 0));
		sr.setScannables(Arrays.asList("x", "z"));

		cModel.setRegions(Arrays.asList(sr));

		pointGeneratorService.createCompoundGenerator(cModel).createPoints();
	}

	@Test(expected = ModelValidationException.class)
	public void regionForWrongAxes() throws GeneratorException {
		CompoundModel cModel = new CompoundModel();

		TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setBoundingBox(new BoundingBox(3, 7, 2, 6));
		gridModel.setxAxisName("x");
		gridModel.setyAxisName("y");

		cModel.addModel(gridModel);

		ScanRegion sr = new ScanRegion();
		// Non-overlapping region, 3<x<5, 7<y<13
		sr.setRoi(new RectangularROI(3, 7, 2, 6, 0));
		sr.setScannables(Arrays.asList("p", "z"));

		cModel.setRegions(Arrays.asList(sr));

		pointGeneratorService.createCompoundGenerator(cModel).createPoints();
	}

}
