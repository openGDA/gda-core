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
import java.util.NoSuchElementException;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ConcurrentMultiModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.mutators.RandomOffsetMutator;
import org.junit.Test;

public class ConcurrentTest {

	private IPointGeneratorService service = new PointGeneratorService();

	@Test
	// A single model wrapped should return the same scan
	public void singleModel() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(50);
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		IPointGenerator<?> lsg = service.createGenerator(lsm);
		IPointGenerator<?> cmg = service.createGenerator(cmm);
		assertEquals(50, cmg.size());
		assertArrayEquals(new int[] { 50 }, cmg.getShape());
		equalIterators(cmg.iterator(), lsg.iterator());
	}

	@Test
	public void twoModels() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("z");
		talpm.setyAxisName("p");
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		IPointGenerator<?> lsg = service.createGenerator(lsm);
		IPointGenerator<?> talpg = service.createGenerator(talpm);
		IPointGenerator<?> cmg = service.createGenerator(cmm);
		assertEquals(40, cmg.size());
		assertArrayEquals(new int[] { 40 }, cmg.getShape());
		equalIterators(cmg.iterator(), lsg.iterator(), talpg.iterator());
	}

	/*
	 * Can mutate on axes from any component scan: although 2 identical mutators on X (e.g. one on a ConcurrentModel and
	 * one on a Compound model that wraps it in another scan) would be reduced to 1 instance, a mutator on X and another
	 * on (X,Y) would be treated as two unique (and therefore additive) mutators.
	 */
	@Test
	public void mutation() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("z");
		talpm.setyAxisName("p");
		Map<String, Double> offsets = new HashMap<>();
		offsets.put("p", 0.1);
		offsets.put("stage_x", 0.33);
		IMutator rom = new RandomOffsetMutator(444, Arrays.asList("p", "stage_x"), offsets);
		Map<String, Double> pOffsets = new HashMap<>();
		pOffsets.put("p", 0.1);
		IMutator pRom = new RandomOffsetMutator(444, Arrays.asList("p"), pOffsets);
		Map<String, Double> stage_xOffsets = new HashMap<>();
		stage_xOffsets.put("stage_x", 0.33);
		IMutator stage_xRom = new RandomOffsetMutator(444, Arrays.asList("stage_x"), stage_xOffsets);
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		IPointGenerator<?> lsg = service.createGenerator(lsm, Collections.emptyList(), Arrays.asList(stage_xRom));
		IPointGenerator<?> talpg = service.createGenerator(talpm, Collections.emptyList(), Arrays.asList(pRom));
		IPointGenerator<?> cmg = service.createGenerator(cmm, Collections.emptyList(), Arrays.asList(rom));
		equalIterators(cmg.iterator(), lsg.iterator(), talpg.iterator());
	}

	@Test
	public void canCompound() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("z");
		talpm.setyAxisName("p");
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		AxialArrayModel aam = new AxialArrayModel();
		aam.setName("energy");
		double[] energies = new double[] { 0, 1, 2, 6 };
		aam.setPositions(energies);
		CompoundModel cm = new CompoundModel(aam, cmm);
		IPointGenerator<?> lsg = service.createGenerator(lsm);
		IPointGenerator<?> talpg = service.createGenerator(talpm);
		IPointGenerator<?> cmg = service.createCompoundGenerator(cm);
		assertEquals(160, cmg.size());
		assertArrayEquals(new int[] { 4, 40 }, cmg.getShape());
		int i = 0;
		int j = 0;
		Iterator<IPosition> cmi = cmg.iterator();
		for (double energy : energies) {
			Iterator<IPosition> lsi = lsg.iterator();
			Iterator<IPosition> tali = talpg.iterator();
			while (lsi.hasNext()) {
				IPosition indexHolder = new MapPosition();
				indexHolder.setStepIndex(i);
				((MapPosition) indexHolder).put("energy", j, energy);
				indexHolder = indexHolder.compound(lsi.next());
				indexHolder = indexHolder.compound(tali.next());
				assertEquals(indexHolder, cmi.next());
				i++;
			}
			j++;
			if (tali.hasNext())
				fail("Should not have been able to compound scans of different length!");
		}
		if (cmi.hasNext())
			fail("Iterator too long!");
	}

	@Test
	public void canCompoundWithRegion() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("z");
		talpm.setyAxisName("p");
		IROI roi = new CircularROI(3, 5, 5);
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		AxialArrayModel aam = new AxialArrayModel();
		aam.setName("energy");
		double[] energies = new double[] { 0, 1, 2, 6 };
		aam.setPositions(energies);
		CompoundModel cm = new CompoundModel(aam, cmm);
		// Will cut off some points in Lissajous pattern, but connecting generators happens before regions so valid
		cm.addRegions(Arrays.asList(new ScanRegion(roi, "stage_x", "stage_y")));
		IPointGenerator<?> lsg = service.createGenerator(lsm);
		IPointGenerator<?> talpg = service.createGenerator(talpm);
		IPointGenerator<?> cmg = service.createCompoundGenerator(cm);
		int j = 0;
		Iterator<IPosition> cmi = cmg.iterator();
		for (double energy : energies) {
			int i = 0;
			Iterator<IPosition> lsi = lsg.iterator();
			Iterator<IPosition> tali = talpg.iterator();
			while (lsi.hasNext()) {
				IPosition pos = lsi.next();
				IPosition tal = tali.next();
				if (!roi.containsPoint(pos.getValue("stage_x"), pos.getValue("stage_y"))) {
					continue;
				}
				MapPosition indexHolder = new MapPosition();
				indexHolder.setStepIndex(j * 6 + i);
				indexHolder.put("energy", j, energy);
				// index = step index as flattened into line, so cannot just compound
				indexHolder.put("stage_x", i, pos.get("stage_x"));
				indexHolder.put("stage_y", i, pos.get("stage_y"));
				indexHolder.put("z", i, tal.get("z"));
				indexHolder.put("p", i, tal.get("p"));
				assertEquals(indexHolder, cmi.next());
				i++;
			}
			j++;
			if (tali.hasNext())
				fail("Should not have been able to compound scans of different length!");
		}
		if (cmi.hasNext())
			fail("Iterator too long!");
	}

	@Test(expected = GeneratorException.class)
	public void differentSizes() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(39);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("z");
		talpm.setyAxisName("p");
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void twoModelsWithCommonAxis() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("z");
		talpm.setyAxisName("stage_x");
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void twoModelsWithSameAxes() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setPoints(40);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("stage_y");
		talpm.setyAxisName("stage_x");
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void alternatingModel() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setAlternating(true);
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void alternatingWithOtherModel() throws GeneratorException {
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		lsm.setBoundingBox(new BoundingBox(0, 0, 10, 10));
		lsm.setAlternating(true);
		TwoAxisLinePointsModel talpm = new TwoAxisLinePointsModel();
		talpm.setBoundingLine(new BoundingLine(0, 0, 10, 10));
		talpm.setPoints(40);
		talpm.setxAxisName("z");
		talpm.setyAxisName("stage_x");
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		cmm.addModel(talpm);
		service.createGenerator(cmm);
	}

	@Test(expected = GeneratorException.class)
	public void invalidInnerModel() throws GeneratorException {
		// LissajousModel should have boundingbox
		TwoAxisLissajousModel lsm = new TwoAxisLissajousModel();
		ConcurrentMultiModel cmm = new ConcurrentMultiModel();
		cmm.addModel(lsm);
		service.createGenerator(cmm);
	}

	// Concurrent Iterator is equivalent to every other iterator simultaneously
	// Package & static so can get from MultiModelTests
	static void equalIterators(Iterator<IPosition> concurrentIt, Iterator<IPosition>... others) {
		try {
			int stepIndex = 0;
			while (concurrentIt.hasNext()) {
				IPosition concurrentPos = concurrentIt.next();
				IPosition otherPos = new MapPosition();
				// Otherwise default of -1 overwrites
				otherPos.setStepIndex(stepIndex);
				for (Iterator<IPosition> it : others) {
					otherPos = otherPos.compound(it.next());
				}
				assertEquals(otherPos, concurrentPos);
				stepIndex++;
			}
		} catch (NoSuchElementException i) {
			fail("Iterator too long!");
		}
	}
}
