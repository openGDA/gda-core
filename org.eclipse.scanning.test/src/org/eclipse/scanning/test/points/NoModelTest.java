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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.points.NoModelGenerator;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.mutators.RandomOffsetMutator;
import org.junit.Test;

public class NoModelTest {

	private IPointGeneratorService pgs = new PointGeneratorService();

	@Test
	public void testLissajousNoROI() throws GeneratorException {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-10);
		box.setyAxisStart(5);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		TwoAxisLissajousModel model = new TwoAxisLissajousModel();
		model.setBoundingBox(box);
		AbstractGenerator<TwoAxisLissajousModel> gen = (AbstractGenerator<TwoAxisLissajousModel>) pgs.createGenerator(model);
		NoModelGenerator nmg = new NoModelGenerator(gen.createPythonPointGenerator());

		compareIterators(gen.iterator(), nmg.iterator());
	}

	@Test
	public void testGridWithROI() throws GeneratorException {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-1);
		box.setyAxisStart(-2);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel();
		model.setBoundingBox(box);

		IROI roi = new CircularROI();
		AbstractGenerator<CompoundModel> gen = (AbstractGenerator<CompoundModel>) pgs.createGenerator(model, roi);
		NoModelGenerator nmg = new NoModelGenerator(gen.createPythonPointGenerator());

		compareIterators(gen.iterator(), nmg.iterator());
	}

	@Test
	public void testMutatedSpiral() throws GeneratorException {
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-1);
		box.setyAxisStart(-2);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		Map<String, Double> offsets = new HashMap<>();
		offsets.put("x", 0.05);
		RandomOffsetMutator rom = new RandomOffsetMutator(12, Arrays.asList("x"), offsets);
		TwoAxisSpiralModel sm = new TwoAxisSpiralModel("x", "y", 1, box);
		AbstractGenerator<CompoundModel> gen = (AbstractGenerator<CompoundModel>) pgs.createGenerator(sm, new ArrayList<>(), Arrays.asList(rom));
		NoModelGenerator nmg = new NoModelGenerator(gen.createPythonPointGenerator());
		compareIterators(gen.iterator(), nmg.iterator());
	}

	@Test
	public void withRegionsAndMutatorAndNonstandardDuration() throws GeneratorException {
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-1);
		box.setyAxisStart(-2);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		IROI roi1 = new CircularROI();
		IROI roi2 = new CircularROI(1, 1, 1);
		List<IROI> roiList = Arrays.asList(roi1, roi2);

		Map<String, Double> offsets = new HashMap<>();
		offsets.put("x", 0.05);
		RandomOffsetMutator rom = new RandomOffsetMutator(12, Arrays.asList("x"), offsets);
		TwoAxisSpiralModel sm = new TwoAxisSpiralModel("x", "y", 1, box);

		AbstractGenerator<CompoundModel> gen = (AbstractGenerator<CompoundModel>) pgs.createGenerator(sm, roiList, Arrays.asList(rom), 3);

		NoModelGenerator nmg = new NoModelGenerator(gen.createPythonPointGenerator());
		compareIterators(gen.iterator(), nmg.iterator());
	}

	@Test
	public void ableToBeCompounded() throws GeneratorException {
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-1);
		box.setyAxisStart(-2);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		IROI roi1 = new CircularROI();
		IROI roi2 = new CircularROI(1, 1, 1);
		List<IROI> roiList = Arrays.asList(roi1, roi2);

		Map<String, Double> offsets = new HashMap<>();
		offsets.put("x", 0.05);
		RandomOffsetMutator rom = new RandomOffsetMutator(12, Arrays.asList("x"), offsets);
		TwoAxisSpiralModel sm = new TwoAxisSpiralModel("x", "y", 1, box);

		AbstractGenerator<CompoundModel> gen = (AbstractGenerator<CompoundModel>) pgs.createGenerator(sm, roiList, Arrays.asList(rom));

		NoModelGenerator nmg = new NoModelGenerator(gen.createPythonPointGenerator());

		AxialStepModel asm = new AxialStepModel("z", 0, 1, 0.08);
		IPointGenerator<AxialStepModel> asg = pgs.createGenerator(asm);

		CompoundModel cModel1 = new CompoundModel();
		cModel1.addData(sm, roiList);
		cModel1.addModel(asm);
		cModel1.addMutators(Arrays.asList(rom));

		gen = (AbstractGenerator<CompoundModel>) pgs.createCompoundGenerator(cModel1);
		AbstractGenerator<CompoundModel> gen2 = (AbstractGenerator<CompoundModel>) pgs.createCompoundGenerator(nmg, asg);
		assertArrayEquals(gen.getShape(), gen2.getShape());

		compareIterators(gen.iterator(), gen2.iterator());
	}

	@Test(expected = RuntimeException.class)
	public void cannotValidateModels() {
		NoModelGenerator nmg = new NoModelGenerator(null);
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		nmg.validate(lsm);
	}

	@Test(expected = ModelValidationException.class)
	public void cannotReturnModel() throws GeneratorException {
		BoundingLine bl = new BoundingLine(0, 0, 4, 5);
		TwoAxisLinePointsModel lpm = new TwoAxisLinePointsModel();
		lpm.setBoundingLine(bl);

		AbstractGenerator<TwoAxisLinePointsModel> gen = (AbstractGenerator<TwoAxisLinePointsModel>) pgs.createGenerator(lpm);
		NoModelGenerator nmg = new NoModelGenerator(gen.createPythonPointGenerator());

		nmg.getModel();
	}

	/*
	 * NoModelGenerator fills gap where ambiguity in which method could have been sent to Malcolm
	 * No way of retrieving which of above scans produced PPointGenerator which have identical values of all fields
	 */
	@Test
	public void equivalentScans() throws GeneratorException {
		BoundingBox box = new BoundingBox(0, 0, 10, 10);
		TwoAxisGridStepModel stepModel = new TwoAxisGridStepModel("x", "y");
		stepModel.setxAxisStep(1);
		stepModel.setyAxisStep(1);
		stepModel.setBoundingBox(box);
		TwoAxisGridPointsModel pointsModel = new TwoAxisGridPointsModel("x", "y", 10, 10);
		pointsModel.setBoundingBox(box);

		AbstractGenerator<TwoAxisGridStepModel> stepGen = (AbstractGenerator<TwoAxisGridStepModel>) pgs.createGenerator(stepModel);
		AbstractGenerator<TwoAxisGridPointsModel> pointsGen = (AbstractGenerator<TwoAxisGridPointsModel>) pgs.createGenerator(pointsModel);

		NoModelGenerator stepWithoutModel = new NoModelGenerator(stepGen.createPythonPointGenerator());
		NoModelGenerator pointsWithoutModel = new NoModelGenerator(pointsGen.createPythonPointGenerator());

		assertEquals(stepWithoutModel, pointsWithoutModel);
	}

	private void compareIterators(ScanPointIterator creator, ScanPointIterator created) {
		while (creator.hasNext()) {
			assertEquals(creator.next(), created.next());
		}
		if (created.hasNext()) fail();
	}

}
