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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.points.AbstractScanPointGenerator;
import org.eclipse.scanning.points.CompoundGenerator;
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
		AbstractScanPointGenerator<TwoAxisLissajousModel> gen = (AbstractScanPointGenerator<TwoAxisLissajousModel>) pgs.createGenerator(model);
		NoModelGenerator nmg = new NoModelGenerator(gen.getPointGenerator());

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
		AbstractScanPointGenerator<CompoundModel> gen = (AbstractScanPointGenerator<CompoundModel>) pgs.createGenerator(model, roi);
		NoModelGenerator nmg = new NoModelGenerator(gen.getPointGenerator());

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
		AbstractScanPointGenerator<CompoundModel> gen = (AbstractScanPointGenerator<CompoundModel>) pgs.createGenerator(sm, new ArrayList<>(), Arrays.asList(rom));
		NoModelGenerator nmg = new NoModelGenerator(gen.getPointGenerator());
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

		AbstractScanPointGenerator<CompoundModel> gen = (AbstractScanPointGenerator<CompoundModel>) pgs.createGenerator(sm, roiList, Arrays.asList(rom), 3);

		NoModelGenerator nmg = new NoModelGenerator(gen.getPointGenerator());
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

		AbstractScanPointGenerator<CompoundModel> gen = (AbstractScanPointGenerator<CompoundModel>) pgs.createGenerator(sm, roiList, Arrays.asList(rom));

		NoModelGenerator nmg = new NoModelGenerator(gen.getPointGenerator());

		AxialStepModel asm = new AxialStepModel("z", 0, 1, 0.08);
		IPointGenerator<AxialStepModel> asg = pgs.createGenerator(asm);

		CompoundModel cModel1 = new CompoundModel();
		cModel1.addData(sm, roiList);
		cModel1.addModel(asm);
		cModel1.addMutators(Arrays.asList(rom));

		gen = (AbstractScanPointGenerator<CompoundModel>) pgs.createCompoundGenerator(cModel1);
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

		AbstractScanPointGenerator<TwoAxisLinePointsModel> gen = (AbstractScanPointGenerator<TwoAxisLinePointsModel>) pgs.createGenerator(lpm);
		NoModelGenerator nmg = new NoModelGenerator(gen.getPointGenerator());

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

		AbstractScanPointGenerator<TwoAxisGridStepModel> stepGen = (AbstractScanPointGenerator<TwoAxisGridStepModel>) pgs.createGenerator(stepModel);
		AbstractScanPointGenerator<TwoAxisGridPointsModel> pointsGen = (AbstractScanPointGenerator<TwoAxisGridPointsModel>) pgs.createGenerator(pointsModel);

		NoModelGenerator stepWithoutModel = new NoModelGenerator(stepGen.getPointGenerator());
		NoModelGenerator pointsWithoutModel = new NoModelGenerator(pointsGen.getPointGenerator());

		assertEquals(stepWithoutModel, pointsWithoutModel);
	}

	@Test
	public void StaticWithDuration() throws GeneratorException {
		StaticModel staticModel = new StaticModel();
		CompoundModel compoundModel = new CompoundModel(staticModel);
		compoundModel.setDuration(0.1);

		AbstractScanPointGenerator<CompoundModel> stepGen = (AbstractScanPointGenerator<CompoundModel>) pgs.createGenerator(compoundModel);
		NoModelGenerator staticNoModel = new NoModelGenerator(stepGen.getPointGenerator());
		CompoundGenerator genFromGenerators = new CompoundGenerator(new IPointGenerator[] {staticNoModel}, pgs);

		StaticPosition expected = new StaticPosition();
		expected.setExposureTime(0.1);
		expected.setStepIndex(0);
		assertEquals(expected, staticNoModel.getFirstPoint());
		assertEquals(expected, stepGen.getFirstPoint());
		assertEquals(expected, genFromGenerators.getFirstPoint());


	}

	private void compareIterators(Iterator<IPosition> creator, Iterator<IPosition> created) {
		while (creator.hasNext()) {
			assertEquals(creator.next(), created.next());
		}
		if (created.hasNext()) fail();
	}

}
