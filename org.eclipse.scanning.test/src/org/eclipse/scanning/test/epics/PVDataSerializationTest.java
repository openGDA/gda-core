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
package org.eclipse.scanning.test.epics;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_ENABLE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_EXPOSURE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_MRI;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_TABLE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.connector.epics.MalcolmEpicsV4Connection;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.mutators.RandomOffsetMutator;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for serialisation into EPICS V4 structures for transmission over PVAccess
 * @author Matt Taylor
 *
 */
public class PVDataSerializationTest {

	private MalcolmEpicsV4Connection connectorService;

	private static final IPointGeneratorService pgService = new PointGeneratorService();

	private FieldCreate fieldCreate = FieldFactory.getFieldCreate();

	private PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

	@BeforeAll
	public static void setUpClass() {
		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setValidatorService(new ValidatorService());
		serviceHolder.setPointGeneratorService(pgService);
	}

	@BeforeEach
	public void setUp() {
		connectorService = new MalcolmEpicsV4Connection();
		fieldCreate = FieldFactory.getFieldCreate();
		pvDataCreate = PVDataFactory.getPVDataCreate();
	}

	@Test
	public void testArrayGenerator() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();

		AxialArrayModel stepModel = new AxialArrayModel();
		stepModel.setName("x");
		stepModel.setPositions(new double [] {1, 2, 3, 4});
		stepModel.setContinuous(false);
		stepModel.setAlternating(true);
		IPointGenerator<CompoundModel> scan = pgService.createGenerator(stepModel, regions);

		// Create the expected PVStructure
		Structure expectedGeneratorsStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				add("alternate", ScalarType.pvBoolean).
				addArray("units", ScalarType.pvString).
				addArray("points", ScalarType.pvDouble).
				setId("scanpointgenerator:generator/ArrayGenerator:1.0").
				createStructure();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				add("delay_after", ScalarType.pvDouble).
				add("continuous", ScalarType.pvBoolean).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedGeneratorsPVStructure = pvDataCreate.createPVStructure(expectedGeneratorsStructure);
		PVStringArray nameVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "axes");
		String[] axes = new String[] {"x"};
		nameVal.put(0, axes.length, axes, 0);
		PVStringArray unitsVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "units");
		String[] units = new String[] {"mm"};
		unitsVal.put(0, units.length, units, 0);
		PVDoubleArray pointsVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "points");
		double[] points = new double[] {1, 2, 3, 4};
		pointsVal.put(0, points.length, points, 0);
		PVBoolean adVal = expectedGeneratorsPVStructure.getSubField(PVBoolean.class, "alternate");
		adVal.put(true);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVDouble durationVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "duration");
		durationVal.put(-1);
		PVDouble delay_afterVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "delay_after");
		delay_afterVal.put(0);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "generators");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedGeneratorsPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure(), pvStructure.getStructure());
		assertEquals(expectedCompGenPVStructure, pvStructure);
	}

	@Test
	public void testCircularROI() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		regions.add(new CircularROI(2, 6, 7));

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions);

		// Create the expected PVStructure
		Union union = fieldCreate.createVariantUnion();

		Structure expectedCircularRoiStructure = fieldCreate.createFieldBuilder().
				addArray("centre", ScalarType.pvDouble).
				add("radius", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/CircularROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = fieldCreate.createFieldBuilder().
				setId("scanpointgenerator:excluder/ROIExcluder:1.0").
				addArray("axes", ScalarType.pvString).
				addArray("rois", union).
				createStructure();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				add("duration", ScalarType.pvDouble).
				addArray("mutators", union).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedExcluderPVStructure = pvDataCreate.createPVStructure(expectedExcluderStructure);
		PVStringArray scannablesVal = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables = new String[] {"stage_x", "stage_y"};
		scannablesVal.put(0, scannables.length, scannables, 0);

		PVStructure expectedROIPVStructure = pvDataCreate.createPVStructure(expectedCircularRoiStructure);
		PVUnionArray rois = expectedExcluderPVStructure.getSubField(PVUnionArray.class, "rois");

		PVDoubleArray centreVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "centre");
		double[] centre = new double[] {6, 7};
		centreVal.put(0, centre.length, centre, 0);
		PVDouble radiusVal = expectedROIPVStructure.getSubField(PVDouble.class, "radius");
		radiusVal.put(2);

		PVUnion[] roiArray = new PVUnion[1];
		roiArray[0] = pvDataCreate.createPVUnion(union);
		roiArray[0].set(expectedROIPVStructure);
		rois.put(0, roiArray.length, roiArray, 0);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVUnionArray excluders = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "excluders");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedExcluderPVStructure);

		excluders.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("excluders"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("excluders"), pvStructure.getSubField("excluders"));
	}

	@Test
	public void testEllipticalROI() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		EllipticalROI eRoi = new EllipticalROI();
		eRoi.setPoint(3, 4);
		eRoi.setAngle(1.5);
		eRoi.setSemiAxes(new double[]{7, 8});
		regions.add(eRoi);

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions);

		// Create the expected PVStructure
		Union union = fieldCreate.createVariantUnion();

		Structure expectedEllipticalRoiStructure = fieldCreate.createFieldBuilder().
				addArray("semiaxes", ScalarType.pvDouble).
				addArray("centre", ScalarType.pvDouble).
				add("angle", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/EllipticalROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("rois", union).
				setId("scanpointgenerator:excluder/ROIExcluder:1.0").
				createStructure();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedExcluderPVStructure = pvDataCreate.createPVStructure(expectedExcluderStructure);
		PVStringArray scannablesVal = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables = new String[] {"stage_x", "stage_y"};
		scannablesVal.put(0, scannables.length, scannables, 0);

		PVStructure expectedROIPVStructure = pvDataCreate.createPVStructure(expectedEllipticalRoiStructure);
		PVUnionArray rois = expectedExcluderPVStructure.getSubField(PVUnionArray.class, "rois");

		PVDoubleArray semiaxesVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "semiaxes");
		double[] semiaxes = new double[] {7, 8};
		semiaxesVal.put(0, semiaxes.length, semiaxes, 0);
		PVDoubleArray centreVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "centre");
		double[] centre = new double[] {3, 4};
		centreVal.put(0, centre.length, centre, 0);
		PVDouble angleVal = expectedROIPVStructure.getSubField(PVDouble.class, "angle");
		angleVal.put(1.5);

		PVUnion[] roiArray = new PVUnion[1];
		roiArray[0] = pvDataCreate.createPVUnion(union);
		roiArray[0].set(expectedROIPVStructure);
		rois.put(0, roiArray.length, roiArray, 0);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "excluders");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedExcluderPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("excluders"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("excluders"), pvStructure.getSubField("excluders"));
	}

	@Test
	public void testLinearROI() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		LinearROI lRoi = new LinearROI();
		lRoi.setPoint(3, 4);
		lRoi.setLength(18);
		lRoi.setAngle(0.75);
		regions.add(lRoi);

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions);

		// Create the expected PVStructure. Note, Linear ROIs are not supported so should be empty
		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();


		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("excluders"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("excluders"), pvStructure.getSubField("excluders"));
	}

	@Disabled // TODO: Allow Java Generator construction without calling CompoundGenerator.prepare,
	// to allow "empty" scans to be described
	@Test
	public void testPointROI() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		regions.add(new PointROI(new double[]{5, 9.4}));
		regions.add(new CircularROI(2, 6, 7));

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions);

		// Create the expected PVStructure
		Structure expectedPointRoiStructure = fieldCreate.createFieldBuilder().
				addArray("point", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/PointROI:1.0").
				createStructure();

		Structure expectedCircularRoiStructure = fieldCreate.createFieldBuilder().
				addArray("centre", ScalarType.pvDouble).
				add("radius", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/CircularROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				add("roi", expectedPointRoiStructure).
				createStructure();

		Structure expectedCircleExcluderStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				add("roi", expectedCircularRoiStructure).
				createStructure();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedExcluderPVStructure = pvDataCreate.createPVStructure(expectedExcluderStructure);
		PVStringArray scannablesVal = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables = new String[] {"stage_x", "stage_y"};
		scannablesVal.put(0, scannables.length, scannables, 0);

		PVStructure expectedROIPVStructure = expectedExcluderPVStructure.getStructureField("roi");

		PVDoubleArray pointVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "point");
		double[] point = new double[] {5, 9.4};
		pointVal.put(0, point.length, point, 0);

		// Create Expected for Circle too
		PVStructure expectedCircleExcluderPVStructure = pvDataCreate.createPVStructure(expectedCircleExcluderStructure);
		PVStringArray circleScannablesVal = expectedCircleExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] circleScannables = new String[] {"stage_x", "stage_y"};
		circleScannablesVal.put(0, circleScannables.length, circleScannables, 0);

		PVStructure expectedCircleROIPVStructure = expectedCircleExcluderPVStructure.getStructureField("roi");

		PVDoubleArray centreVal = expectedCircleROIPVStructure.getSubField(PVDoubleArray.class, "centre");
		double[] centre = new double[] {6, 7};
		centreVal.put(0, centre.length, centre, 0);
		PVDouble radiusVal = expectedCircleROIPVStructure.getSubField(PVDouble.class, "radius");
		radiusVal.put(2);


		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "excluders");

		PVUnion[] unionArray = new PVUnion[2];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedExcluderPVStructure);
		unionArray[1] = pvDataCreate.createPVUnion(union);
		unionArray[1].set(expectedCircleExcluderPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("excluders"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("excluders"), pvStructure.getSubField("excluders"));
	}

	@Test
	public void testPolygonalROI() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		PolygonalROI diamond = new PolygonalROI(new double[] { 1.5, 0 });
		diamond.insertPoint(new double[] { 3, 1.5 });
		diamond.insertPoint(new double[] { 1.5, 3 });
		diamond.insertPoint(new double[] { 0, 1.5 });
		regions.add(diamond);

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions);

		// Create the expected PVStructure
		Union union = fieldCreate.createVariantUnion();

		Structure expectedRoiStructure = fieldCreate.createFieldBuilder().
				addArray("points_x", ScalarType.pvDouble).
				addArray("points_y", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/PolygonalROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("rois", union).
				setId("scanpointgenerator:excluder/ROIExcluder:1.0").
				createStructure();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedExcluderPVStructure = pvDataCreate.createPVStructure(expectedExcluderStructure);
		PVStringArray scannablesVal = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables = new String[] {"stage_x", "stage_y"};
		scannablesVal.put(0, scannables.length, scannables, 0);

		PVStructure expectedROIPVStructure = pvDataCreate.createPVStructure(expectedRoiStructure);
		PVUnionArray rois = expectedExcluderPVStructure.getSubField(PVUnionArray.class, "rois");

		PVDoubleArray xVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "points_x");
		double[] points_x = new double[] {1.5, 3, 1.5, 0};
		xVal.put(0, points_x.length, points_x, 0);
		PVDoubleArray yVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "points_y");
		double[] points_y = new double[] {0, 1.5, 3, 1.5};
		yVal.put(0, points_y.length, points_y, 0);

		PVUnion[] roiArray = new PVUnion[1];
		roiArray[0] = pvDataCreate.createPVUnion(union);
		roiArray[0].set(expectedROIPVStructure);
		rois.put(0, roiArray.length, roiArray, 0);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "excluders");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedExcluderPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("excluders"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("excluders"), pvStructure.getSubField("excluders"));
	}

	@Test
	public void testRectangularROI() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		RectangularROI rRoi = new RectangularROI();
		rRoi.setPoint(new double[]{7, 3});
		rRoi.setLengths(5, 16);
		rRoi.setAngle(1.2);
		regions.add(rRoi);

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions);

		// Create the expected PVStructure
		Union union = fieldCreate.createVariantUnion();

		Structure expectedCircularRoiStructure = fieldCreate.createFieldBuilder().
				addArray("start", ScalarType.pvDouble).
				add("width", ScalarType.pvDouble).
				add("angle", ScalarType.pvDouble).
				add("height", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/RectangularROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = fieldCreate.createFieldBuilder().
				setId("scanpointgenerator:excluder/ROIExcluder:1.0").
				addArray("axes", ScalarType.pvString).
				addArray("rois", union).
				createStructure();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				add("duration", ScalarType.pvDouble).
				addArray("mutators", union).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedExcluderPVStructure = pvDataCreate.createPVStructure(expectedExcluderStructure);
		PVStringArray scannablesVal = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables = new String[] {"stage_x", "stage_y"};
		scannablesVal.put(0, scannables.length, scannables, 0);

		PVStructure expectedROIPVStructure = pvDataCreate.createPVStructure(expectedCircularRoiStructure);
		PVUnionArray rois = expectedExcluderPVStructure.getSubField(PVUnionArray.class, "rois");

		PVDoubleArray startVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "start");
		double[] start = new double[] {7, 3};
		startVal.put(0, start.length, start, 0);
		PVDouble widthVal = expectedROIPVStructure.getSubField(PVDouble.class, "width");
		widthVal.put(5);
		PVDouble heightVal = expectedROIPVStructure.getSubField(PVDouble.class, "height");
		heightVal.put(16);
		PVDouble angleVal = expectedROIPVStructure.getSubField(PVDouble.class, "angle");
		angleVal.put(1.2);

		PVUnion[] roiUnionArray = new PVUnion[1];
		roiUnionArray[0] = pvDataCreate.createPVUnion(union);
		roiUnionArray[0].set(expectedROIPVStructure);
		rois.put(0, roiUnionArray.length, roiUnionArray, 0);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "excluders");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedExcluderPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("excluders"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("excluders"), pvStructure.getSubField("excluders"));
	}

	@Test
	public void testSectorROI() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		SectorROI sRoi = new SectorROI();
		sRoi.setPoint(new double[]{12, 1});
		sRoi.setRadii(1, 11);
		sRoi.setAngles(0, Math.PI);
		regions.add(sRoi);

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions);

		// Create the expected PVStructure
		Union union = fieldCreate.createVariantUnion();

		Structure expectedRoiStructure = fieldCreate.createFieldBuilder().
				addArray("radii", ScalarType.pvDouble).
				addArray("angles", ScalarType.pvDouble).
				addArray("centre", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/SectorROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("rois", union).
				setId("scanpointgenerator:excluder/ROIExcluder:1.0").
				createStructure();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedExcluderPVStructure = pvDataCreate.createPVStructure(expectedExcluderStructure);
		PVStringArray scannablesVal = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables = new String[] {"stage_x", "stage_y"};
		scannablesVal.put(0, scannables.length, scannables, 0);

		PVStructure expectedROIPVStructure = pvDataCreate.createPVStructure(expectedRoiStructure);
		PVUnionArray rois = expectedExcluderPVStructure.getSubField(PVUnionArray.class, "rois");

		PVDoubleArray centreVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "centre");
		double[] centre = new double[] {12, 1};
		centreVal.put(0, centre.length, centre, 0);
		PVDoubleArray radiiVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "radii");
		double[] radii = new double[] {1, 11};
		radiiVal.put(0, radii.length, radii, 0);
		PVDoubleArray anglesVal = expectedROIPVStructure.getSubField(PVDoubleArray.class, "angles");
		double[] angles = new double[] {0, Math.PI};
		anglesVal.put(0, angles.length, angles, 0);

		PVUnion[] roiArray = new PVUnion[1];
		roiArray[0] = pvDataCreate.createPVUnion(union);
		roiArray[0].set(expectedROIPVStructure);
		rois.put(0, roiArray.length, roiArray, 0);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVDouble durationVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "duration");
		durationVal.put(-1);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "excluders");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedExcluderPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("excluders"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("excluders"), pvStructure.getSubField("excluders"));
	}

	@Test
	public void testRandomOffsetMutator() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		regions.add(new CircularROI(2, 6, 7));

		List<IMutator> mutators = new LinkedList<>();
		List<String> axes = new LinkedList<String>();
		axes.add("stage_x");
		Map<String,Double> offsets = new LinkedHashMap<String, Double>();
		offsets.put("stage_x", 34d);
		RandomOffsetMutator rom = new RandomOffsetMutator(3456, axes, offsets);
		mutators.add(rom);

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gm.setAlternating(true);
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(10);

		IPointGenerator<CompoundModel> scan = pgService.createGenerator(gm, regions, mutators, 2.5f);

		// Create the expected PVStructure
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();

		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

		Structure expectedRandomOffsetMutatorStructure = fieldCreate.createFieldBuilder().
				add("seed", ScalarType.pvInt).
				addArray("axes", ScalarType.pvString).
				addArray("max_offset", ScalarType.pvDouble).
				setId("scanpointgenerator:mutator/RandomOffsetMutator:1.0").
				createStructure();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedMutatorPVStructure = pvDataCreate.createPVStructure(expectedRandomOffsetMutatorStructure);
		PVInt seedVal = expectedMutatorPVStructure.getSubField(PVInt.class, "seed");
		seedVal.put(3456);
		PVStringArray axesVal = expectedMutatorPVStructure.getSubField(PVStringArray.class, "axes");
		String[] axesStr = new String[] {"stage_x"};
		axesVal.put(0, axesStr.length, axesStr, 0);

		PVDoubleArray maxOffsetPVfield = expectedMutatorPVStructure.getSubField(PVDoubleArray.class, "max_offset");
		double[] expectedMaxoffset = new double[] {34};
		maxOffsetPVfield.put(0, expectedMaxoffset.length, expectedMaxoffset, 0);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "mutators");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedMutatorPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure().getField("mutators"), pvStructure.getStructure().getField("excluders"));
		assertEquals(expectedCompGenPVStructure.getSubField("mutators"), pvStructure.getSubField("mutators"));
	}

	@Test
	public void testLineGenerator() throws Exception {

		// Create test generator
		AxialStepModel stepModel = new AxialStepModel("x", 3, 4, 0.25);
		stepModel.setAlternating(true);
		stepModel.setContinuous(false);
		IPointGenerator<CompoundModel> scan = pgService.createCompoundGenerator(new CompoundModel(stepModel));

		// Create the expected PVStructure
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();

		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

		Structure expectedGeneratorsStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("start", ScalarType.pvDouble).
				add("alternate", ScalarType.pvBoolean).
				addArray("units", ScalarType.pvString).
				addArray("stop", ScalarType.pvDouble).
				add("size", ScalarType.pvInt).
				setId("scanpointgenerator:generator/LineGenerator:1.0").
				createStructure();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				add("delay_after", ScalarType.pvDouble).
				add("continuous", ScalarType.pvBoolean).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedGeneratorsPVStructure = pvDataCreate.createPVStructure(expectedGeneratorsStructure);
		PVStringArray nameVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "axes");
		String[] name = new String[] {"x"};
		nameVal.put(0, name.length, name, 0);
		PVStringArray unitsVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "units");
		String[] units = new String[] {"mm"};
		unitsVal.put(0, units.length, units, 0);
		PVDoubleArray startVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "start");
		double[] start = new double[] {3};
		startVal.put(0, start.length, start, 0);
		PVDoubleArray stopVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "stop");
		double[] stop = new double[] {4};
		stopVal.put(0, stop.length, stop, 0);
		PVInt numVal = expectedGeneratorsPVStructure.getSubField(PVInt.class, "size");
		numVal.put(5);
		PVBoolean adVal = expectedGeneratorsPVStructure.getSubField(PVBoolean.class, "alternate");
		adVal.put(true);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVDouble durationVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "duration");
		durationVal.put(-1);
		PVDouble delay_afterVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "delay_after");
		delay_afterVal.put(0);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "generators");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedGeneratorsPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure(), pvStructure.getStructure());
		assertEquals(expectedCompGenPVStructure, pvStructure);
	}

	@Test
	public void testLissajousGenerator() throws Exception {

		// Create test generator
		TwoAxisLissajousModel lissajousModel = new TwoAxisLissajousModel();
		lissajousModel.setBoundingBox(new BoundingBox(0, -5, 10, 6));
		lissajousModel.setPoints(20);
		lissajousModel.setyAxisName("san");
		lissajousModel.setxAxisName("fan");
		lissajousModel.setContinuous(false);
		lissajousModel.setAlternating(true);
		IPointGenerator<CompoundModel> scan = pgService.createCompoundGenerator(new CompoundModel(lissajousModel));

		// Create the expected PVStructure
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();

		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

		//TODO: Not used?
		Structure expectedBoxStructure = fieldCreate.createFieldBuilder().
				addArray("centre", ScalarType.pvDouble).
				add("width", ScalarType.pvDouble).
				add("height", ScalarType.pvDouble).
				createStructure();

		Structure expectedGeneratorsStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				add("lobes", ScalarType.pvInt).
				addArray("centre", ScalarType.pvDouble).
				add("alternate", ScalarType.pvBoolean).
				addArray("units", ScalarType.pvString).
				add("size", ScalarType.pvInt).
				addArray("span", ScalarType.pvDouble).
				setId("scanpointgenerator:generator/LissajousGenerator:1.0").
				createStructure();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				add("delay_after", ScalarType.pvDouble).
				add("continuous", ScalarType.pvBoolean).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedGeneratorsPVStructure = pvDataCreate.createPVStructure(expectedGeneratorsStructure);
		PVStringArray nameVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "axes");
		String[] name = new String[] {"fan", "san"};
		nameVal.put(0, name.length, name, 0);
		PVStringArray unitsVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "units");
		String[] units = new String[] {"mm", "mm"};
		unitsVal.put(0, units.length, units, 0);
		PVInt numPointsVal = expectedGeneratorsPVStructure.getSubField(PVInt.class, "size");
		numPointsVal.put(20);
		PVInt numLobesVal = expectedGeneratorsPVStructure.getSubField(PVInt.class, "lobes");
		numLobesVal.put(4);

		PVDoubleArray centreVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "centre");
		double[] centre = new double[] {5.0, -2};
		centreVal.put(0, centre.length, centre, 0);
		PVDoubleArray spanVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "span");
		double[] span = new double[] {10, 6};
		spanVal.put(0, span.length, span, 0);
		PVBoolean altVal = expectedGeneratorsPVStructure.getSubField(PVBoolean.class, "alternate");
		altVal.put(true);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVDouble durationVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "duration");
		durationVal.put(-1);
		PVDouble delayVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "delay_after");
		delayVal.put(0);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "generators");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedGeneratorsPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure(), pvStructure.getStructure());
		assertEquals(expectedCompGenPVStructure, pvStructure);
	}

	@Test
	public void testSpiralGenerator() throws Exception {

		// Create test generator
		IPointGenerator<CompoundModel> scan = pgService.createCompoundGenerator(
				new CompoundModel(new TwoAxisSpiralModel("x", "y", 2, new BoundingBox(0, 5, 2, 4))));

		// Create the expected PVStructure
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();

		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

		Structure expectedGeneratorsStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("centre", ScalarType.pvDouble).
				add("scale", ScalarType.pvDouble).
				add("alternate", ScalarType.pvBoolean).
				addArray("units", ScalarType.pvString).
				add("radius", ScalarType.pvDouble).
				setId("scanpointgenerator:generator/SpiralGenerator:1.0").
				createStructure();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				add("delay_after", ScalarType.pvDouble).
				add("continuous", ScalarType.pvBoolean).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedGeneratorsPVStructure = pvDataCreate.createPVStructure(expectedGeneratorsStructure);
		PVStringArray nameVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "axes");
		String[] name = new String[] {"x", "y"};
		nameVal.put(0, name.length, name, 0);
		PVStringArray unitsVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "units");
		String[] units = new String[] {"mm", "mm"};
		unitsVal.put(0, units.length, units, 0);
		PVDoubleArray centreVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "centre");
		double[] centre = new double[] {1, 7};
		centreVal.put(0, centre.length, centre, 0);
		PVDouble scaleVal = expectedGeneratorsPVStructure.getSubField(PVDouble.class, "scale");
		scaleVal.put(2);
		PVDouble radiusVal = expectedGeneratorsPVStructure.getSubField(PVDouble.class, "radius");
		radiusVal.put(2.23606797749979);
		PVBoolean adVal = expectedGeneratorsPVStructure.getSubField(PVBoolean.class, "alternate");
		adVal.put(false);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVDouble durationVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "duration");
		durationVal.put(-1);
		PVDouble delay_afterVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "delay_after");
		delay_afterVal.put(0);
		PVBoolean contVal = expectedCompGenPVStructure.getSubField(PVBoolean.class, "continuous");
		contVal.put(true);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "generators");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedGeneratorsPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure(), pvStructure.getStructure());
		assertEquals(expectedCompGenPVStructure, pvStructure);
	}

	@Test
	public void testSingularLineGenerator() throws Exception {

		// Create test generator
		List<IROI> regions = new LinkedList<>();

		AxialStepModel stepModel = new AxialStepModel("x", 3, 4, 0.25);
		stepModel.setContinuous(false);
		IPointGenerator<CompoundModel> scan = pgService.createGenerator(stepModel, regions);

		// Create the expected PVStructure
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();

		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

		Structure expectedGeneratorsStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("start", ScalarType.pvDouble).
				add("alternate", ScalarType.pvBoolean).
				addArray("units", ScalarType.pvString).
				addArray("stop", ScalarType.pvDouble).
				add("size", ScalarType.pvInt).
				setId("scanpointgenerator:generator/LineGenerator:1.0").
				createStructure();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				add("delay_after", ScalarType.pvDouble).
				add("continuous", ScalarType.pvBoolean).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		PVStructure expectedGeneratorsPVStructure = pvDataCreate.createPVStructure(expectedGeneratorsStructure);
		PVStringArray nameVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "axes");
		String[] name = new String[] {"x"};
		nameVal.put(0, name.length, name, 0);
		PVStringArray unitsVal = expectedGeneratorsPVStructure.getSubField(PVStringArray.class, "units");
		String[] units = new String[] {"mm"};
		unitsVal.put(0, units.length, units, 0);
		PVDoubleArray startVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "start");
		double[] start = new double[] {3};
		startVal.put(0, start.length, start, 0);
		PVDoubleArray stopVal = expectedGeneratorsPVStructure.getSubField(PVDoubleArray.class, "stop");
		double[] stop = new double[] {4};
		stopVal.put(0, stop.length, stop, 0);
		PVInt numVal = expectedGeneratorsPVStructure.getSubField(PVInt.class, "size");
		numVal.put(5);
		PVBoolean adVal = expectedGeneratorsPVStructure.getSubField(PVBoolean.class, "alternate");
		adVal.put(false);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVDouble durationVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "duration");
		durationVal.put(-1);
		PVDouble delay_afterVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "delay_after");
		delay_afterVal.put(0);
		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "generators");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedGeneratorsPVStructure);

		generators.put(0, unionArray.length, unionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(scan);

		assertEquals(expectedCompGenPVStructure.getStructure(), pvStructure.getStructure());
		assertEquals(expectedCompGenPVStructure, pvStructure);
	}

	@Test
	public void testFullCompoundGenerator() throws Exception {

		// This test will not behave as expected if either rectangular region has angle == 0
		// This is due to the LineGenerators being "trimmed" in this case by CompoundGenerator

		// Create test generator
		List<IROI> regions = new LinkedList<>();
		RectangularROI rRoi1 = new RectangularROI();
		rRoi1.setPoint(new double[]{2, 1});
		rRoi1.setLengths(5, 16);
		rRoi1.setAngle(Math.PI / 2.0);
		regions.add(rRoi1);
		RectangularROI rRoi2 = new RectangularROI();
		rRoi2.setPoint(new double[]{-2, 2});
		rRoi2.setLengths(9, 16);
		rRoi2.setAngle(0);
		regions.add(rRoi2);

		List<IMutator> mutators = new LinkedList<>();
		Map<String, Double> offsets = new HashMap<String, Double>();
		offsets.put("stage_x", 0.5);
		mutators.add(new RandomOffsetMutator(112, Arrays.asList(new String[] {"stage_x"}), offsets));

		TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gridModel.setAlternating(true);
		gridModel.setContinuous(false);
		gridModel.setyAxisPoints(5);
		gridModel.setxAxisPoints(10);

		IPointGenerator<CompoundModel> pointGen = pgService.createGenerator(gridModel, regions, mutators, 1.5f);

		// Create the expected PVStructure
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();

		PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

		Union union = fieldCreate.createVariantUnion();

		Structure expectedCircularRoiStructure = fieldCreate.createFieldBuilder().
				addArray("start", ScalarType.pvDouble).
				add("width", ScalarType.pvDouble).
				add("angle", ScalarType.pvDouble).
				add("height", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/RectangularROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("rois", union).
				setId("scanpointgenerator:excluder/ROIExcluder:1.0").
				createStructure();

		Structure expectedRandomOffsetMutatorStructure = fieldCreate.createFieldBuilder().
				add("seed", ScalarType.pvInt).
				addArray("axes", ScalarType.pvString).
				addArray("max_offset", ScalarType.pvDouble).
				setId("scanpointgenerator:mutator/RandomOffsetMutator:1.0").
				createStructure();

		Structure expectedLineGeneratorsStructure = fieldCreate.createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("start", ScalarType.pvDouble).
				add("alternate", ScalarType.pvBoolean).
				addArray("units", ScalarType.pvString).
				addArray("stop", ScalarType.pvDouble).
				add("size", ScalarType.pvInt).
				setId("scanpointgenerator:generator/LineGenerator:1.0").
				createStructure();

		Structure expectedCompGenStructure = fieldCreate.createFieldBuilder().
				addArray("mutators", union).
				add("duration", ScalarType.pvDouble).
				add("delay_after", ScalarType.pvDouble).
				add("continuous", ScalarType.pvBoolean).
				addArray("generators", union).
				addArray("excluders", union).
				setId("scanpointgenerator:generator/CompoundGenerator:1.0").
				createStructure();

		// Excluders
		PVStructure expectedROI1PVStructure = pvDataCreate.createPVStructure(expectedCircularRoiStructure);
		PVDoubleArray startVal1 = expectedROI1PVStructure.getSubField(PVDoubleArray.class, "start");
		double[] start1 = new double[] {2, 1};
		startVal1.put(0, start1.length, start1, 0);
		PVDouble widthVal1 = expectedROI1PVStructure.getSubField(PVDouble.class, "width");
		widthVal1.put(5);
		PVDouble heightVal1 = expectedROI1PVStructure.getSubField(PVDouble.class, "height");
		heightVal1.put(16);
		PVDouble angleVal1 = expectedROI1PVStructure.getSubField(PVDouble.class, "angle");
		angleVal1.put(Math.PI / 2.0);

		PVStructure expectedROI2PVStructure = pvDataCreate.createPVStructure(expectedCircularRoiStructure);
		PVDoubleArray startVal2 = expectedROI2PVStructure.getSubField(PVDoubleArray.class, "start");
		double[] start2 = new double[] {-2, 2};
		startVal2.put(0, start2.length, start2, 0);
		PVDouble widthVal2 = expectedROI2PVStructure.getSubField(PVDouble.class, "width");
		widthVal2.put(9);
		PVDouble heightVal2 = expectedROI2PVStructure.getSubField(PVDouble.class, "height");
		heightVal2.put(16);
		PVDouble angleVal2 = expectedROI2PVStructure.getSubField(PVDouble.class, "angle");
		angleVal2.put(0);

		PVStructure expectedExcluderPVStructure = pvDataCreate.createPVStructure(expectedExcluderStructure);
		PVStringArray scannables1Val = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables1 = new String[] {"stage_x", "stage_y"};
		scannables1Val.put(0, scannables1.length, scannables1, 0);
		PVUnionArray rois = expectedExcluderPVStructure.getSubField(PVUnionArray.class, "rois");
		PVUnion[] roiArray = new PVUnion[2];
		roiArray[0] = pvDataCreate.createPVUnion(union);
		roiArray[0].set(expectedROI1PVStructure);
		roiArray[1] = pvDataCreate.createPVUnion(union);
		roiArray[1].set(expectedROI2PVStructure);
		rois.put(0, roiArray.length, roiArray, 0);

		// Mutators
		PVStructure expectedMutatorPVStructure = pvDataCreate.createPVStructure(expectedRandomOffsetMutatorStructure);
		PVDoubleArray expectedOffsetPVStructure = expectedMutatorPVStructure.getSubField(PVDoubleArray.class, "max_offset");
		double[] offsetArray = new double[] {0.5};
		expectedOffsetPVStructure.put(0, offsetArray.length, offsetArray, 0);
		PVInt seedVal = expectedMutatorPVStructure.getSubField(PVInt.class, "seed");
		seedVal.put(112);
		PVStringArray axesVal = expectedMutatorPVStructure.getSubField(PVStringArray.class, "axes");
		String[] axes = new String[] {"stage_x"};
		axesVal.put(0, axes.length, axes, 0);

		// Generators
		PVStructure expectedGeneratorsPVStructure1 = pvDataCreate.createPVStructure(expectedLineGeneratorsStructure);
		PVStringArray nameVal1 = expectedGeneratorsPVStructure1.getSubField(PVStringArray.class, "axes");
		String[] name1 = new String[] {"stage_y"};
		nameVal1.put(0, name1.length, name1, 0);
		PVStringArray unitsVal1 = expectedGeneratorsPVStructure1.getSubField(PVStringArray.class, "units");
		String[] units1 = new String[] {"mm"};
		unitsVal1.put(0, units1.length, units1, 0);
		PVDoubleArray gstartVal1 = expectedGeneratorsPVStructure1.getSubField(PVDoubleArray.class, "start");
		double[] gstart1 = new double[] {1};
		gstartVal1.put(0, gstart1.length, gstart1, 0);
		PVDoubleArray stopVal1 = expectedGeneratorsPVStructure1.getSubField(PVDoubleArray.class, "stop");
		double[] stop1 = new double[] {18};
		stopVal1.put(0, stop1.length, stop1, 0);
		PVInt numVal1 = expectedGeneratorsPVStructure1.getSubField(PVInt.class, "size");
		numVal1.put(5);
		PVBoolean adVal1 = expectedGeneratorsPVStructure1.getSubField(PVBoolean.class, "alternate");
		adVal1.put(true);

		PVStructure expectedGeneratorsPVStructure2 = pvDataCreate.createPVStructure(expectedLineGeneratorsStructure);
		PVStringArray nameVal = expectedGeneratorsPVStructure2.getSubField(PVStringArray.class, "axes");
		String[] name = new String[] {"stage_x"};
		nameVal.put(0, name.length, name, 0);
		PVStringArray unitsVal = expectedGeneratorsPVStructure2.getSubField(PVStringArray.class, "units");
		String[] units2 = new String[] {"mm"};
		unitsVal.put(0, units2.length, units2, 0);
		PVDoubleArray startVal = expectedGeneratorsPVStructure2.getSubField(PVDoubleArray.class, "start");
		double[] start = new double[] {-14};
		startVal.put(0, start.length, start, 0);
		PVDoubleArray stopVal = expectedGeneratorsPVStructure2.getSubField(PVDoubleArray.class, "stop");
		double[] stop = new double[] {7};
		stopVal.put(0, stop.length, stop, 0);
		PVInt numVal = expectedGeneratorsPVStructure2.getSubField(PVInt.class, "size");
		numVal.put(10);
		PVBoolean adVal2 = expectedGeneratorsPVStructure2.getSubField(PVBoolean.class, "alternate");
		adVal2.put(true);

		PVStructure expectedCompGenPVStructure = pvDataCreate.createPVStructure(expectedCompGenStructure);
		PVDouble durationVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "duration");
		durationVal.put(1.5);
		PVDouble delayVal = expectedCompGenPVStructure.getSubField(PVDouble.class, "delay_after");
		delayVal.put(0);
		PVUnionArray excluders = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "excluders");

		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvDataCreate.createPVUnion(union);
		unionArray[0].set(expectedExcluderPVStructure);

		excluders.put(0, unionArray.length, unionArray, 0);

		PVUnionArray mutatorsPVArray = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "mutators");

		PVUnion[] mutUnionArray = new PVUnion[1];
		mutUnionArray[0] = pvDataCreate.createPVUnion(union);
		mutUnionArray[0].set(expectedMutatorPVStructure);

		mutatorsPVArray.put(0, mutUnionArray.length, mutUnionArray, 0);

		PVUnionArray generators = expectedCompGenPVStructure.getSubField(PVUnionArray.class, "generators");

		PVUnion[] genUunionArray = new PVUnion[2];
		genUunionArray[0] = pvDataCreate.createPVUnion(union);
		genUunionArray[0].set(expectedGeneratorsPVStructure1);
		genUunionArray[1] = pvDataCreate.createPVUnion(union);
		genUunionArray[1].set(expectedGeneratorsPVStructure2);

		generators.put(0, genUunionArray.length, genUunionArray, 0);

		// Marshal and check against expected
		PVStructure pvStructure = connectorService.pvMarshal(pointGen);

		assertEquals(expectedCompGenPVStructure.getStructure(), pvStructure.getStructure());
		assertEquals(expectedCompGenPVStructure, pvStructure);

		final IPointGenerator<?> actualPointGen = connectorService.pvUnmarshal(pvStructure, IPointGenerator.class);
//		assertEquals(pointGen, deserialized); // Note: we can't directly compare point generators for equality
		assertEquals(actualPointGen.size(), pointGen.size());
		assertEquals(actualPointGen.getRank(), pointGen.getRank());
		assertArrayEquals(actualPointGen.getShape(), pointGen.getShape());
		assertEquals(actualPointGen.getNames(), pointGen.getNames());
		final Iterator<IPosition> actualPointGenIter = actualPointGen.iterator();
		final Iterator<IPosition> pointGenIter = pointGen.iterator();
		while (pointGenIter.hasNext()) {
			assertTrue(actualPointGenIter.hasNext());
			assertEquals(actualPointGenIter.next(), pointGenIter.next());
		}
		assertFalse(actualPointGenIter.hasNext());
	}

	@Test
	public void testMalcolmTable() throws Exception {
		// test pv-serialization and deserialization of a malcolm table

		final boolean[] enabledArray = new boolean[] { true, true, false };
		final String[] nameArray = new String[] { "diffraction", "izero", "load" };
		final String[] mriArray = new String[] { "ws256-ML-DET-01", "ws256-ML-DET-02", "ws256-ML-DET-03" };
		final double[] exposureArray = new double[] { 0.01, 0.004, 0.0025 };
		final int[] framesPerStepArray = new int[] { 1, 2, 3 };

		// create the malcolm table to serialialize
		final LinkedHashMap<String, Class<?>> tableTypesMap = new LinkedHashMap<>();
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_ENABLE, Boolean.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_NAME, String.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_MRI, String.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_EXPOSURE, Double.class);
		tableTypesMap.put(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, Integer.class);

		final List<Boolean> enabledList = new ArrayList<>(enabledArray.length); // no
		for (int i = 0; i < enabledArray.length; i++) enabledList.add(enabledArray[i]);

		final int numDetectors = 3;
		final LinkedHashMap<String, List<?>> tableData = new LinkedHashMap<>(numDetectors);
		tableData.put(DETECTORS_TABLE_COLUMN_ENABLE, enabledList);
		tableData.put(DETECTORS_TABLE_COLUMN_NAME, Arrays.stream(nameArray).collect(toList()));
		tableData.put(DETECTORS_TABLE_COLUMN_MRI, Arrays.stream(mriArray).collect(toList()));
		tableData.put(DETECTORS_TABLE_COLUMN_EXPOSURE, Arrays.stream(exposureArray).boxed().collect(toList()));
		tableData.put(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, Arrays.stream(framesPerStepArray).boxed().collect(toList()));

		final MalcolmTable detectorsTable = new MalcolmTable(tableData, tableTypesMap);

		// create expected pv structure
		final Structure expectedTableStructure = fieldCreate.createFieldBuilder()
				.addArray(DETECTORS_TABLE_COLUMN_ENABLE, ScalarType.pvBoolean)
				.addArray(DETECTORS_TABLE_COLUMN_NAME, ScalarType.pvString)
				.addArray(DETECTORS_TABLE_COLUMN_MRI, ScalarType.pvString)
				.addArray(DETECTORS_TABLE_COLUMN_EXPOSURE, ScalarType.pvDouble)
				.addArray(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, ScalarType.pvInt)
				.setId(TYPE_ID_TABLE)
				.createStructure();

		final PVStructure expectedPVStructure = pvDataCreate.createPVStructure(expectedTableStructure);
		expectedPVStructure.getSubField(PVBooleanArray.class, DETECTORS_TABLE_COLUMN_ENABLE).put(0, enabledArray.length, enabledArray, 0);
		expectedPVStructure.getSubField(PVStringArray.class, DETECTORS_TABLE_COLUMN_NAME).put(0, nameArray.length, nameArray, 0);
		expectedPVStructure.getSubField(PVStringArray.class, DETECTORS_TABLE_COLUMN_MRI).put(0, mriArray.length, mriArray, 0);
		expectedPVStructure.getSubField(PVDoubleArray.class, DETECTORS_TABLE_COLUMN_EXPOSURE).put(0, exposureArray.length, exposureArray, 0);
		expectedPVStructure.getSubField(PVIntArray.class, DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP).put(0, framesPerStepArray.length, framesPerStepArray, 0);

		// perform the serialization
		final PVStructure pvStructure = connectorService.pvMarshal(detectorsTable);

		assertEquals(expectedPVStructure, pvStructure);

		final MalcolmTable unmarshalled = connectorService.pvUnmarshal(pvStructure, MalcolmTable.class);
		assertEquals(detectorsTable, unmarshalled);
	}

}
