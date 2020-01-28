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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ConsecutiveMultiModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.points.CompoundGenerator;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.mutators.RandomOffsetMutator;
import org.junit.Test;

public class ConsecutiveTest {

	private IPointGeneratorService service = new PointGeneratorService();

	@Test
	// A single model wrapped should return the same scan
	public void singleModel() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm);
		IPointGenerator<?> lsg = service.createGenerator(lsm);
		Iterator<IPosition> cmi = service.createGenerator(cmm).iterator();
		equalIterators(cmi, true, lsg.iterator());
	}

	@Test
	public void twoModels() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(20);
		talpm.setxAxisName("stage_x");
		talpm.setyAxisName("stage_y");
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.setContinuous(false);
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		IPointGenerator<?> lsg = service.createGenerator(lsm);
		IPointGenerator<?> talpg = service.createGenerator(talpm);
		Iterator<IPosition> cmi = service.createGenerator(cmm).iterator();
		equalIterators(cmi, true, lsg.iterator(), talpg.iterator());
	}

	/*
	 * Can mutate but as mutation implementation uses index of point, only first scan in ConsecutiveModel will match
	 * positions Must check others against a suitable scan with same positions but different indices
	 */
	@Test
	public void mutation() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("stage_x");
		talpm.setyAxisName("stage_y");
		// Gives talpm points but offset by 40 indices
		TwoAxisLinePointsModel talpmExtended = new TwoAxisLinePointsModel();
		talpmExtended.setBoundingLine(new BoundingLine(-10, -10, 20, 20));
		talpmExtended.setPoints(80);
		talpmExtended.setxAxisName("stage_x");
		talpmExtended.setyAxisName("stage_y");
		Map<String, Double> offsets = new HashMap<>();
		offsets.put("stage_x", 0.33);
		IMutator rom = new RandomOffsetMutator(444, Arrays.asList("stage_x"), offsets);
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.setContinuous(false);
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		IPointGenerator<?> lsg = service.createGenerator(lsm, Collections.emptyList(), Arrays.asList(rom));
		Iterator<IPosition> talpiExtended = service
				.createGenerator(talpmExtended, Collections.emptyList(), Arrays.asList(rom)).iterator();
		Iterator<IPosition> cmi = service.createGenerator(cmm, Collections.emptyList(), Arrays.asList(rom)).iterator();
		for (int i = 0; i < 40; i++) {
			talpiExtended.next();
		}
		// use respectIndexStep = false as talpiExtended already offset by 40
		equalIterators(cmi, false, lsg.iterator(), talpiExtended);
	}

	@Test(expected = GeneratorException.class)
	public void differentAxis() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("stage_x");
		talpm.setyAxisName("stage_z");
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void differentAxes() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("stage_p");
		talpm.setyAxisName("stage_z");
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void alternatingModel() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setAlternating(true);
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void alternatingWithNonAlternatingModel() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setAlternating(true);
		TwoAxisLissajousModel lsm2 = new TwoAxisLissajousModel();
		lsm2.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(lsm2);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void invalidModel() throws GeneratorException {
		// Lissajous requires boundingbox to be valid
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm);
		service.createGenerator(cmm);
	}

	@Test
	public void compound() throws GeneratorException {
		TwoAxisLissajousModel lsm1 = new TwoAxisLissajousModel();
		lsm1.setPoints(30);
		lsm1.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		double[] energies = { 5, 6, 7 };
		AxialArrayModel aam = new AxialArrayModel(energies);
		aam.setName("energy");
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm1);
		cmm.addModel(lsm1);
		cmm.setContinuous(false);
		CompoundModel cm = new CompoundModel(aam, cmm);
		CompoundGenerator cg = (CompoundGenerator) service.createCompoundGenerator(cm);
		IPointGenerator<?> lj1g = service.createGenerator(lsm1);
		assertEquals(180, cg.size());
		assertArrayEquals(new int[] { 3, 60 }, cg.getShape());
		int j = 0;
		int offset = 0;
		Iterator<IPosition> cmi = cg.iterator();
		for (double energy : energies) {
			int i = 0;
			for (Iterator<IPosition> it : new Iterator[] { lj1g.iterator(), lj1g.iterator() }) {
				while (it.hasNext()) {
					IPosition next = cmi.next();
					IPosition itNext = it.next();
					assertEquals(j * 60 + i, next.getStepIndex());
					assertEquals(energy, next.get("energy"));
					assertEquals(itNext.get("stage_x"), next.get("stage_x"));
					assertEquals(itNext.get("stage_y"), next.get("stage_y"));
					assertEquals(itNext.getStepIndex(), next.getStepIndex() - offset);
					i++;
				}
				offset += 30;
			}
			j++;
		}
		if (cmi.hasNext())
			fail("Iterator too long!");
	}

	@Test
	public void compoundWithRegion() throws GeneratorException {
		TwoAxisLissajousModel lsm1 = new TwoAxisLissajousModel();
		lsm1.setPoints(30);
		lsm1.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		double[] energies = { 5, 6, 7 };
		AxialArrayModel aam = new AxialArrayModel(energies);
		aam.setName("energy");
		ConsecutiveMultiModel cmm = new ConsecutiveMultiModel();
		cmm.addModel(lsm1);
		cmm.addModel(lsm1);
		cmm.setContinuous(false);
		IROI roi = new CircularROI(3, 5, 5);
		ScanRegion sr = new ScanRegion(roi, "stage_x", "stage_y");
		CompoundModel cm = new CompoundModel(aam, cmm);
		cm.setRegions(Arrays.asList(sr));
		CompoundGenerator cg = (CompoundGenerator) service.createCompoundGenerator(cm);
		IPointGenerator<?> lj1g = service.createGenerator(lsm1, roi);
		// 3* for each energy in energies, 2* because consecutived to each other
		assertEquals(6 * lj1g.size(), cg.size());
		assertArrayEquals(new int[] { 3, lj1g.size() * 2 }, cg.getShape());
		int j = 0;
		int offset = 0;
		Iterator<IPosition> cmi = cg.iterator();
		for (double energy : energies) {
			int i = 0;
			for (Iterator<IPosition> it : new Iterator[] { lj1g.iterator(), lj1g.iterator() }) {
				while (it.hasNext()) {
					IPosition next = cmi.next();
					IPosition itNext = it.next();
					assertEquals(j * 2 * lj1g.size() + i, next.getStepIndex());
					assertEquals(energy, next.get("energy"));
					assertEquals(itNext.get("stage_x"), next.get("stage_x"));
					assertEquals(itNext.get("stage_y"), next.get("stage_y"));
					assertEquals(itNext.getStepIndex(), next.getStepIndex() - offset);
					i++;
				}
				offset += lj1g.size();
			}
			j++;
		}
		if (cmi.hasNext())
			fail("Iterator too long!");
	}

	// Consecutive Iterator is equivalent to every other iterator in order
	// Use respectIndexStep = false for instances where injecting an already offset iterator, e.g. Mutators
	// Package, static so can be gotten from MultiModelTest
	static void equalIterators(Iterator<IPosition> consecutiveIt, boolean respectIndexStep,
			Iterator<IPosition>... others) {
		int indexOffset = 0;
		for (Iterator<IPosition> it : others) {
			int indexPoint = 0;
			while (it.hasNext()) {
				IPosition consecPos = consecutiveIt.next();
				IPosition posTwo = it.next();
				// Cannot just use equals as indices different
				for (String dimension : consecPos.getNames()) {
					assertEquals(posTwo.getValue(dimension), consecPos.getValue(dimension), 0.00001);
				}
				assertEquals(posTwo.getStepIndex(), consecPos.getStepIndex() - indexOffset);
				indexPoint++;
			}
			if (respectIndexStep) {
				indexOffset += indexPoint;
			}
		}
		if (consecutiveIt.hasNext())
			fail("Iterator too long!");
	}
}
