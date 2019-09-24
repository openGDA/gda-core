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
package org.eclipse.scanning.test.scan.preprocess;

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.junit.BeforeClass;
import org.junit.Test;

public class SubscanModeratorTest {

	protected static IPointGeneratorService  gservice;

	@BeforeClass
	public static void setServices() throws Exception {
		gservice    = new PointGeneratorService();
	}

	@Test
	public void testSimpleWrappedScan() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"x", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		IPointGenerator<?> outerPointGen = moderator.getOuterPointGenerator();
		IPointGenerator<?> innerPointGen = moderator.getInnerPointGenerator();

		assertEquals(6, outerPointGen.size());
		assertEquals(6, moderator.getOuterScanSize());
		assertEquals(1, moderator.getInnerModels().size());
		assertEquals(25, moderator.getInnerScanSize());
		assertEquals(25, innerPointGen.size());
		assertEquals(150, moderator.getTotalScanSize());
	}

	@Test
	public void testSimpleWrappedScanSubscanOutside() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(gmodel, new StepModel("T", 290, 300, 2)));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"x", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 150, 150, 1);
		checkModeratedModels(moderator, 2, 0);
	}

	private void checkModeratedScanSizes(SubscanModerator moderator, int expectedTotalSize,
			int expectedOuterSize, int expectedInnerSize) throws GeneratorException {
		assertEquals(expectedTotalSize, moderator.getTotalScanSize());
		assertEquals(expectedOuterSize, moderator.getOuterScanSize());
		assertEquals(expectedOuterSize, moderator.getOuterPointGenerator().size());
		assertEquals(expectedInnerSize, moderator.getInnerScanSize());
		if (expectedInnerSize == 0) {
			assertNull(moderator.getInnerPointGenerator());
		} else {
			assertEquals(expectedInnerSize, moderator.getInnerPointGenerator().size());
		}
	}

	private void checkModeratedModels(SubscanModerator moderator, int outerModels, int innerModels) {
		moderator.getOuterModels();
		assertEquals(innerModels, moderator.getInnerModels() == null ? 0 : moderator.getInnerModels().size());
		assertEquals(outerModels, moderator.getOuterModels() == null ? 0 : moderator.getOuterModels().size());
	}

	@Test
	public void testSubscanOnlyScan() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"x", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 25, 1, 25);
		checkModeratedModels(moderator, 0, 1);
	}

	@Test
	public void testNoSubscanDevice1() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final MandelbrotModel mmodel = new MandelbrotModel();
		final MandelbrotDetector det = new MandelbrotDetector();
		det.setModel(mmodel);

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 25, 25, 0);
		checkModeratedModels(moderator, 1, 0);
	}

	@Test
	public void testNoSubscanDevice2() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final MandelbrotModel mmodel = new MandelbrotModel();
		final MandelbrotDetector det = new MandelbrotDetector();
		det.setModel(mmodel);

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		IPointGenerator<?> moderated = moderator.getOuterPointGenerator();
		checkModeratedScanSizes(moderator, 150, 150, 0);
		checkModeratedModels(moderator, 2, 0);
	}


	@Test
	public void testDifferentAxes1() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"p", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 25, 25, 1);
		checkModeratedModels(moderator, 1, 0);
		checkHasSingleStaticPosition(moderator.getInnerPointGenerator());
	}

	private void checkHasSingleStaticPosition(IPointGenerator<?> pointGen) {
		Iterator<IPosition> iter = pointGen.iterator();
		assertTrue(iter.hasNext());
		IPosition pos = iter.next();
		assertTrue(pos.getNames().isEmpty());
		assertTrue(pos.getValues().isEmpty());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testDifferentAxes2() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"p", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 150, 150, 1);
		checkModeratedModels(moderator, 2, 0);
		checkHasSingleStaticPosition(moderator.getInnerPointGenerator());
	}

	@Test
	public void testEmptyAxes() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("x", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[0]);

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 150, 150, 1);
		checkModeratedModels(moderator, 2, 0);
		checkHasSingleStaticPosition(moderator.getInnerPointGenerator());
	}

	@Test
	public void testDifferentAxes3() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("p", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"x", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 150, 150, 1);
		checkModeratedModels(moderator, 2, 0);
		checkHasSingleStaticPosition(moderator.getInnerPointGenerator());
	}

	@Test
	public void testNestedAxes() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		GridModel gmodel = new GridModel("p", "y");
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(new StepModel("p", 290, 300, 2), gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"p", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);

		IPointGenerator<?> outer = moderator.getOuterPointGenerator();
		assertEquals(1, outer.size());

		assertEquals(0, moderator.getOuterModels().size());
		assertEquals(2, moderator.getInnerModels().size());
		checkHasSingleStaticPosition(moderator.getOuterPointGenerator());
	}

	@Test
	public void testSimpleWrappedScanSpiral() throws Exception {

		CompoundModel cmodel = new CompoundModel();

		SpiralModel gmodel = new SpiralModel("p", "y");
		gmodel.setScale(2d);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), gmodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[]{"p", "y"});

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 24, 6, 4);
		checkModeratedModels(moderator, 1, 1);
	}

	@Test
	public void testStaticScan() throws Exception {
		CompoundModel cmodel = new CompoundModel();

		StaticModel smodel = new StaticModel();

		cmodel.setModels(Arrays.asList(smodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[0]);

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 1, 1, 1);
		checkModeratedModels(moderator, 0, 1);
		checkHasSingleStaticPosition(moderator.getOuterPointGenerator());
		checkHasSingleStaticPosition(moderator.getInnerPointGenerator());
	}

	@Test
	public void testStaticScan2() throws Exception {
		// the malcolm device's axesToMove is not empty
		CompoundModel cmodel = new CompoundModel();

		StaticModel smodel = new StaticModel();

		cmodel.setModels(Arrays.asList(smodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[] { "x", "y" });

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 1, 1, 1);
		checkModeratedModels(moderator, 0, 1);
		checkHasSingleStaticPosition(moderator.getOuterPointGenerator());
		checkHasSingleStaticPosition(moderator.getInnerPointGenerator());
	}

	@Test
	public void testStaticScanWithOuterScan() throws Exception {
		// the malcolm device's axesToMove is not empty
		CompoundModel cmodel = new CompoundModel();

		StaticModel smodel = new StaticModel();

		cmodel.setModels(Arrays.asList(new StepModel("T", 290, 300, 2), smodel));

		IPointGenerator<?> gen = gservice.createCompoundGenerator(cmodel);

		final DummyMalcolmModel tmodel = new DummyMalcolmModel();
		final DummyMalcolmDevice det = new DummyMalcolmDevice();
		det.setModel(tmodel);
		det.setAttributeValue(MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES, new String[] { "x", "y" });

		SubscanModerator moderator = new SubscanModerator(gen, Arrays.asList(det), gservice);
		checkModeratedScanSizes(moderator, 6, 6, 1);
		checkModeratedModels(moderator, 1, 1);
		checkHasSingleStaticPosition(moderator.getInnerPointGenerator());
	}

}
