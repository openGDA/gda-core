/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.mscan.element;


import static gda.mscan.element.AreaScanpath.GRID;
import static gda.mscan.element.AreaScanpath.LISSAJOUS;
import static gda.mscan.element.AreaScanpath.ONE_AXIS_NO_OF_POINTS;
import static gda.mscan.element.AreaScanpath.ONE_AXIS_STEP;
import static gda.mscan.element.AreaScanpath.RASTER;
import static gda.mscan.element.AreaScanpath.SINGLE_POINT;
import static gda.mscan.element.AreaScanpath.SPIRAL;
import static gda.mscan.element.AreaScanpath.TWO_AXIS_NO_OF_POINTS;
import static gda.mscan.element.AreaScanpath.TWO_AXIS_STEP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.device.Scannable;

@RunWith(MockitoJUnitRunner.class)
public class AreaScanpathTest {

	private static Map<AreaScanpath, Double[]> emptyPathData = new EnumMap<>(AreaScanpath.class);
	private static Map<AreaScanpath, Double[]> correctLengthPathData = new EnumMap<>(AreaScanpath.class);
	private static Double[] blankArray = new Double[]{};

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private List<Scannable> scannables;
	private List<Scannable> axialScannables;
	private List<Number> pathParams;
	private List<Number> bboxParams;
	private List<Number> blineParams;
	private Map<Mutator, List<Number>> mutators;

	@Mock
	private Scannable scannable1;

	@Mock
	private Scannable scannable2;

	@BeforeClass
	public static void setupClass() {
		for (AreaScanpath scanpath : AreaScanpath.values()) {
			emptyPathData.put(scanpath, new Double[] {});
		}
		correctLengthPathData.put(GRID, new Double[] {3.0, 3.0});
		correctLengthPathData.put(RASTER, new Double[] {4.0, 4.0});
		correctLengthPathData.put(SPIRAL, new Double[] {5.0});
		correctLengthPathData.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0});
		correctLengthPathData.put(TWO_AXIS_NO_OF_POINTS, new Double[] {6.0});
		correctLengthPathData.put(TWO_AXIS_STEP, new Double[] {6.0});
		correctLengthPathData.put(SINGLE_POINT, new Double[] {6.0, 6.0});
		correctLengthPathData.put(ONE_AXIS_STEP, new Double[] {5.0});
		correctLengthPathData.put(ONE_AXIS_NO_OF_POINTS, new Double[] {5.0});
	}

	@Before
	public void setUp() throws Exception {
		when(scannable1.getName()).thenReturn("name1");
		when(scannable2.getName()).thenReturn("name2");

		scannables = Arrays.asList(scannable1, scannable2);
		axialScannables = Arrays.asList(scannable1);
		pathParams = new ArrayList<>();
		bboxParams = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		blineParams = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		mutators = new EnumMap<>(Mutator.class);
	}

	@Test
	public void checkValueCounts() throws Exception {
		assertThat(GRID.valueCount(), is(2));
		assertThat(RASTER.valueCount(), is(2));
		assertThat(SPIRAL.valueCount(), is(1));
		assertThat(LISSAJOUS.valueCount(), is(3));
		assertThat(TWO_AXIS_NO_OF_POINTS.valueCount(), is(1));
		assertThat(TWO_AXIS_STEP.valueCount(), is(1));
		assertThat(SINGLE_POINT.valueCount(), is(2));
		assertThat(ONE_AXIS_NO_OF_POINTS.valueCount(), is(1));
		assertThat(ONE_AXIS_STEP.valueCount(), is(1));
	}

	@Test
	public void checkModelTypes() throws Exception {
		assertTrue(GRID.modelType().equals(GridModel.class));
		assertTrue(RASTER.modelType().equals(RasterModel.class));
		assertTrue(SPIRAL.modelType().equals(SpiralModel.class));
		assertTrue(LISSAJOUS.modelType().equals(LissajousModel.class));
		assertTrue(TWO_AXIS_NO_OF_POINTS.modelType().equals(OneDEqualSpacingModel.class));
		assertTrue(TWO_AXIS_STEP.modelType().equals(OneDStepModel.class));
		assertTrue(SINGLE_POINT.modelType().equals(SinglePointModel.class));
		assertTrue(ONE_AXIS_NO_OF_POINTS.modelType().equals(StepModel.class));
		assertTrue(ONE_AXIS_STEP.modelType().equals(StepModel.class));
	}

	@Test
	public void createModelRejectsTooManyScannablesForAllInstances() throws Exception {
		scannables = Arrays.asList(scannable1, scannable2, scannable2);
		axialScannables = Arrays.asList(scannable1, scannable2);
		assertCreatingAllInstancesFailsIfWrongNoOfParams(emptyPathData, blankArray);
	}

	@Test
	public void createGridModelRejectsTooFewScannables() throws Exception {
		scannables = Arrays.asList(scannable1);
		axialScannables = Arrays.asList();
		assertCreatingAllInstancesFailsIfWrongNoOfParams(emptyPathData, blankArray);
	}

	@Test
	public void createModelRejectsTooManyPathParamsForAllInstances() throws Exception {
		Map<AreaScanpath, Double[]> tooMany = new EnumMap<>(AreaScanpath.class);
		tooMany.put(GRID, new Double[] {3.0, 3.0, 3.0});
		tooMany.put(RASTER, new Double[] {4.0, 4.0, 4.0});
		tooMany.put(SPIRAL, new Double[] {5.0, 5.0});
		tooMany.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0, 6.0, 6.0});
		tooMany.put(TWO_AXIS_NO_OF_POINTS, new Double[] {3.0, 3.0});
		tooMany.put(TWO_AXIS_STEP, new Double[] {3.0, 3.0});
		tooMany.put(SINGLE_POINT, new Double[] {6.0, 6.0, 6.0});
		tooMany.put(ONE_AXIS_NO_OF_POINTS, new Double[] {3.0, 3.0});
		tooMany.put(ONE_AXIS_STEP, new Double[] {3.0, 3.0});

		assertCreatingAllInstancesFailsIfWrongNoOfParams(tooMany, blankArray);
	}

	@Test
	public void createModelRejectsTooFewPathParamsForAllInstances() throws Exception {
		Map<AreaScanpath, Double[]> tooFew = new EnumMap<>(AreaScanpath.class);
		tooFew.put(GRID, new Double[] {3.0});
		tooFew.put(RASTER, new Double[] {4.0});
		tooFew.put(SPIRAL, blankArray);
		tooFew.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0});
		tooFew.put(TWO_AXIS_NO_OF_POINTS, blankArray);
		tooFew.put(TWO_AXIS_STEP, blankArray);
		tooFew.put(SINGLE_POINT, new Double[] {6.0});
		tooFew.put(ONE_AXIS_NO_OF_POINTS, blankArray);
		tooFew.put(ONE_AXIS_STEP, blankArray);

		assertCreatingAllInstancesFailsIfWrongNoOfParams(tooFew, blankArray);
	}

	@Test
	public void createModelRejectsTooManyBoundingParamsForAllInstances() throws Exception {
		Double[] bboxData = new Double[] {1.0, 2.0, 3.0, 4.0, 5.0};  // Too many path params for rectangle/line (4)
		assertCreatingAllInstancesFailsIfWrongNoOfParams(correctLengthPathData, bboxData);
	}

	@Test
	public void createModelRejectsTooFewBoundingParamsForAllInstances() throws Exception {
		assertCreatingAllInstancesFailsIfWrongNoOfParams(correctLengthPathData, blankArray);
	}

	private void assertCreatingAllInstancesFailsIfWrongNoOfParams(Map<AreaScanpath, Double[]> pathParams,
															Double[] bboxParams) throws Exception {
		for (AreaScanpath path: AreaScanpath.values()) {
			List<Scannable> scannableList = path.equals(ONE_AXIS_STEP) || path.equals(ONE_AXIS_NO_OF_POINTS) ? axialScannables : scannables;

			try {
				path.createModel(scannableList, Arrays.asList(pathParams.get(path)), Arrays.asList(bboxParams), mutators);
				fail("Model created from path " + path + " when it shouldn't be possible");
			} catch (IllegalArgumentException e) {
				// should always go in here
			}
		}
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForGrid() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Grid path requires all positive parameters");
		pathParams = Arrays.asList(-5, 6);
		GRID.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForGrid() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Grid path requires all positive parameters");
		pathParams = Arrays.asList(0, 6);
		GRID.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForTwoDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Two Axis No of Points path requires all positive parameters");
		pathParams = Arrays.asList(-5);
		TWO_AXIS_NO_OF_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForTwoDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Two Axis No of Points path requires all positive parameters");
		pathParams = Arrays.asList(0);
		TWO_AXIS_NO_OF_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForOneDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Axial Step path requires a positive integer no of points");
		pathParams = Arrays.asList(2, 3, -5);
		ONE_AXIS_NO_OF_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForOneDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Axial Step path requires a positive integer no of points");
		pathParams = Arrays.asList(2, 3, 0);
		ONE_AXIS_NO_OF_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForGrid() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Grid path requires integer parameters");
		pathParams = Arrays.asList(5, 6.2);
		GRID.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForTwoDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Two Axis No of Points path requires integer parameters");
		pathParams = Arrays.asList(6.2);
		TWO_AXIS_NO_OF_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForOneDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Axial Step path requires a positive integer no of points");
		pathParams = Arrays.asList(2, 3, 6.2);
		ONE_AXIS_NO_OF_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelCreatesCorrectModelForGrid() throws Exception {
		pathParams = Arrays.asList(5, 6);
		IScanPathModel model = GRID.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(GridModel.class)));
		GridModel gModel = (GridModel)model;
		assertThat(gModel.getScannableNames(), contains("name1", "name2"));
		assertThat(gModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(gModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(gModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(gModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(gModel.getxAxisPoints(), is(5));
		assertThat(gModel.getyAxisPoints(), is(6));
		assertThat(gModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(gModel.isAlternating(), is(false));
	}

	@Test
	public void createModelCreatesCorrectModelForGridWithRandomOffsetMutator() throws Exception {
		pathParams = Arrays.asList(5, 6);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		IScanPathModel model = GRID.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(RandomOffsetGridModel.class)));
		RandomOffsetGridModel gModel = (RandomOffsetGridModel)model;
		assertThat(gModel.getScannableNames(), contains("name1", "name2"));
		assertThat(gModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(gModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(gModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(gModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(gModel.getxAxisPoints(), is(5));
		assertThat(gModel.getyAxisPoints(), is(6));
		assertThat(gModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(gModel.getOffset(), is(20.0));
		assertThat(gModel.getSeed(), is(2));
		assertThat(gModel.isAlternating(), is(false));
	}

	@Test
	public void createModelCreatesCorrectModelForTwoDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(5);
		IScanPathModel model = TWO_AXIS_NO_OF_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(OneDEqualSpacingModel.class)));
		OneDEqualSpacingModel eModel = (OneDEqualSpacingModel)model;
		assertThat(eModel.getScannableNames(), contains("name1", "name2"));
		assertThat(eModel.getBoundingLine().getxStart(), is(1.0));
		assertThat(eModel.getBoundingLine().getyStart(), is(2.0));
		assertThat(eModel.getBoundingLine().getLength(), is(5.0));
		assertThat(Math.rint(Math.toDegrees(eModel.getBoundingLine().getAngle())), is(53.0));
		assertThat(eModel.getPoints(), is(5));
	}

	@Test
	public void createModelCreatesCorrectModelForOneDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(-2, 2, 5);
		IScanPathModel model = ONE_AXIS_NO_OF_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(StepModel.class)));
		StepModel eModel = (StepModel)model;
		assertThat(eModel.getScannableNames(), contains("name1"));
		assertThat(eModel.getStart(), is(-2.0));
		assertThat(eModel.getStop(), is(2.0));
		assertThat(eModel.getStep(), is(1.0));
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForRaster() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Raster path requires all positive parameters");
		pathParams = Arrays.asList(-5.2, 6.1);
		RASTER.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForRaster() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Raster path requires all positive parameters");
		pathParams = Arrays.asList(-5.2,0);
		RASTER.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeStepValueForTwoDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Two Axis Step path requires all positive parameters");
		pathParams = Arrays.asList(-5.2);
		TWO_AXIS_STEP.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroStepValueForTwoDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Two Axis Step path requires all positive parameters");
		pathParams = Arrays.asList(0);
		TWO_AXIS_STEP.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeStepValueForOneDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Axial Step path requires all positive parameters");
		pathParams = Arrays.asList(2, 3, -5.2);
		ONE_AXIS_STEP.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroStepValueForOneDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Axial Step path requires all positive parameters");
		pathParams = Arrays.asList(2, 3, 0);
		ONE_AXIS_STEP.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForRaster() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Only Grid Model supports Random Offset paths");
		pathParams = Arrays.asList(-5.2, 6.1);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		RASTER.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelCreatesCorrectModelForRaster() throws Exception {
		pathParams = Arrays.asList(0.5, 6.5);
		mutators.put(Mutator.ALTERNATING, new ArrayList<>());
		IScanPathModel model = RASTER.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(RasterModel.class)));
		RasterModel rModel = (RasterModel)model;
		assertThat(rModel.getScannableNames(), contains("name1", "name2"));
		assertThat(rModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(rModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(rModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(rModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(rModel.getxAxisStep(), is(0.5));
		assertThat(rModel.getyAxisStep(), is(6.5));
		assertThat(rModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(rModel.isAlternating(), is(true));
	}

	@Test
	public void createModelCreatesCorrectModelForTwoDStep() throws Exception {
		pathParams = Arrays.asList(0.5);
		IScanPathModel model = TWO_AXIS_STEP.createModel(scannables, pathParams, blineParams, mutators);
		assertThat(model, is(instanceOf(OneDStepModel.class)));
		OneDStepModel sModel = (OneDStepModel)model;
		assertThat(sModel.getScannableNames(), contains("name1", "name2"));
		assertThat(sModel.getBoundingLine().getxStart(), is(1.0));
		assertThat(sModel.getBoundingLine().getyStart(), is(2.0));
		assertThat(sModel.getBoundingLine().getLength(), is(5.0));
		assertThat(Math.rint(Math.toDegrees(sModel.getBoundingLine().getAngle())), is(53.0));
		assertThat(sModel.getStep(), is(0.5));
	}

	@Test
	public void createModelCreatesCorrectModelForOneDStep() throws Exception {
		pathParams = Arrays.asList(2, 3, 0.5);
		IScanPathModel model = ONE_AXIS_STEP.createModel(axialScannables, pathParams, blineParams, mutators);
		assertThat(model, is(instanceOf(StepModel.class)));
		StepModel sModel = (StepModel)model;
		assertThat(sModel.getScannableNames(), contains("name1"));
		assertThat(sModel.getStart(), is(2.0));
		assertThat(sModel.getStop(), is(3.0));
		assertThat(sModel.getStep(), is(0.5));
	}

	@Test
	public void createModelCreatesCorrectModelForSpiral() throws Exception {
		pathParams = Arrays.asList(5.0);
		IScanPathModel model = SPIRAL.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(SpiralModel.class)));
		SpiralModel sModel = (SpiralModel)model;
		assertThat(sModel.getScannableNames(), contains("name1", "name2"));
		assertThat(sModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(sModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(sModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(sModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(sModel.getScale(), is(5.0));
		assertThat(sModel.getBoundingBox().getyAxisStart(), is(2.0));
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForSpiral() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Only Grid Model supports Random Offset paths");
		pathParams = Arrays.asList(5.0);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		SPIRAL.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelsCorrectModelForLissajous() throws Exception {
		pathParams = Arrays.asList(5, 6.0, 7.0);
		IScanPathModel model = LISSAJOUS.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(LissajousModel.class)));
		LissajousModel lModel = (LissajousModel)model;
		assertThat(lModel.getScannableNames(), contains("name1", "name2"));
		assertThat(lModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(lModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(lModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(lModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(lModel.getPoints(), is(5));
		assertThat(lModel.getA(), is(6.0));
		assertThat(lModel.getB(), is(7.0));
		assertThat(lModel.getBoundingBox().getyAxisStart(), is(2.0));
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForLissajous() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Only Grid Model supports Random Offset paths");
		pathParams = Arrays.asList(5.0, 6.0, 7.0);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		LISSAJOUS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelCreatesCorrectModelForSinglePoint() throws Exception {
		pathParams = Arrays.asList(5.1, 6.2);
		IScanPathModel model = SINGLE_POINT.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(SinglePointModel.class)));
		SinglePointModel eModel = (SinglePointModel)model;
		assertThat(eModel.getScannableNames(), contains("name1", "name2"));
		assertThat(eModel.getX(), is(5.1));
		assertThat(eModel.getY(), is(6.2));
	}
}
