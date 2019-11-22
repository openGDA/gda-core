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


import static gda.mscan.element.AreaScanpath.AXIS_POINTS;
import static gda.mscan.element.AreaScanpath.AXIS_STEP;
import static gda.mscan.element.AreaScanpath.GRID_POINTS;
import static gda.mscan.element.AreaScanpath.GRID_STEP;
import static gda.mscan.element.AreaScanpath.LINE_POINTS;
import static gda.mscan.element.AreaScanpath.LINE_STEP;
import static gda.mscan.element.AreaScanpath.LISSAJOUS;
import static gda.mscan.element.AreaScanpath.SINGLE_POINT;
import static gda.mscan.element.AreaScanpath.SPIRAL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
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

	private final String MUTATOR_NOT_SUPPORTED_BY = "The %s mutator is not supported by ";
	private final String ALL_POSITIVE_ERROR = " path requires all positive parameters";
	private final String ALL_INTEGER_ERROR = " path requires all integer parameters";
	private final String ONE_POSITIVE_ERROR = " path requires that parameter %s is positive";
	private final String ONE_INTEGER_ERROR = " path requires that parameter %s is an integer";

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
		correctLengthPathData.put(GRID_POINTS, new Double[] {3.0, 3.0});
		correctLengthPathData.put(GRID_STEP, new Double[] {4.0, 4.0});
		correctLengthPathData.put(SPIRAL, new Double[] {5.0});
		correctLengthPathData.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0});
		correctLengthPathData.put(LINE_POINTS, new Double[] {6.0});
		correctLengthPathData.put(LINE_STEP, new Double[] {6.0});
		correctLengthPathData.put(SINGLE_POINT, new Double[] {6.0, 6.0});
		correctLengthPathData.put(AXIS_STEP, new Double[] {5.0});
		correctLengthPathData.put(AXIS_POINTS, new Double[] {5.0});
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
		assertThat(GRID_POINTS.valueCount(), is(2));
		assertThat(GRID_STEP.valueCount(), is(2));
		assertThat(SPIRAL.valueCount(), is(1));
		assertThat(LISSAJOUS.valueCount(), is(3));
		assertThat(LINE_POINTS.valueCount(), is(1));
		assertThat(LINE_STEP.valueCount(), is(1));
		assertThat(SINGLE_POINT.valueCount(), is(2));
		assertThat(AXIS_POINTS.valueCount(), is(1));
		assertThat(AXIS_STEP.valueCount(), is(1));
	}

	@Test
	public void checkModelTypes() throws Exception {
		assertTrue(GRID_POINTS.modelType().equals(TwoAxisGridPointsModel.class));
		assertTrue(GRID_STEP.modelType().equals(TwoAxisGridStepModel.class));
		assertTrue(SPIRAL.modelType().equals(TwoAxisSpiralModel.class));
		assertTrue(LISSAJOUS.modelType().equals(TwoAxisLissajousModel.class));
		assertTrue(LINE_POINTS.modelType().equals(TwoAxisLinePointsModel.class));
		assertTrue(LINE_STEP.modelType().equals(TwoAxisLineStepModel.class));
		assertTrue(SINGLE_POINT.modelType().equals(TwoAxisPointSingleModel.class));
		assertTrue(AXIS_POINTS.modelType().equals(AxialStepModel.class));
		assertTrue(AXIS_STEP.modelType().equals(AxialStepModel.class));
	}

	@Test
	public void checkContinuousSupport() throws Exception {
		assertTrue(GRID_POINTS.supports(Mutator.CONTINUOUS));
		assertTrue(GRID_STEP.supports(Mutator.CONTINUOUS));
		assertTrue(SPIRAL.supports(Mutator.CONTINUOUS));
		assertTrue(LISSAJOUS.supports(Mutator.CONTINUOUS));
		assertTrue(LINE_POINTS.supports(Mutator.CONTINUOUS));
		assertTrue(LINE_STEP.supports(Mutator.CONTINUOUS));
		assertFalse(SINGLE_POINT.supports(Mutator.CONTINUOUS));
		assertTrue(AXIS_POINTS.supports(Mutator.CONTINUOUS));
		assertTrue(AXIS_STEP.supports(Mutator.CONTINUOUS));
	}

	@Test
	public void checkAlternatingSupport() throws Exception {
		assertTrue(GRID_POINTS.supports(Mutator.ALTERNATING));
		assertTrue(GRID_STEP.supports(Mutator.ALTERNATING));
		assertTrue(SPIRAL.supports(Mutator.ALTERNATING));
		assertTrue(LISSAJOUS.supports(Mutator.ALTERNATING));
		assertTrue(LINE_POINTS.supports(Mutator.ALTERNATING));
		assertTrue(LINE_STEP.supports(Mutator.ALTERNATING));
		assertFalse(SINGLE_POINT.supports(Mutator.ALTERNATING));
		assertTrue(AXIS_POINTS.supports(Mutator.ALTERNATING));
		assertTrue(AXIS_STEP.supports(Mutator.ALTERNATING));
	}

	@Test
	public void checkRandomOffsetSupport() throws Exception {
		assertTrue(GRID_POINTS.supports(Mutator.RANDOM_OFFSET));
		assertFalse(GRID_STEP.supports(Mutator.RANDOM_OFFSET));
		assertFalse(SPIRAL.supports(Mutator.RANDOM_OFFSET));
		assertFalse(LISSAJOUS.supports(Mutator.RANDOM_OFFSET));
		assertFalse(LINE_POINTS.supports(Mutator.RANDOM_OFFSET));
		assertFalse(LINE_STEP.supports(Mutator.RANDOM_OFFSET));
		assertFalse(SINGLE_POINT.supports(Mutator.RANDOM_OFFSET));
		assertFalse(AXIS_POINTS.supports(Mutator.RANDOM_OFFSET));
		assertFalse(AXIS_STEP.supports(Mutator.RANDOM_OFFSET));
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
		tooMany.put(GRID_POINTS, new Double[] {3.0, 3.0, 3.0});
		tooMany.put(GRID_STEP, new Double[] {4.0, 4.0, 4.0});
		tooMany.put(SPIRAL, new Double[] {5.0, 5.0});
		tooMany.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0, 6.0, 6.0});
		tooMany.put(LINE_POINTS, new Double[] {3.0, 3.0});
		tooMany.put(LINE_STEP, new Double[] {3.0, 3.0});
		tooMany.put(SINGLE_POINT, new Double[] {6.0, 6.0, 6.0});
		tooMany.put(AXIS_POINTS, new Double[] {3.0, 3.0});
		tooMany.put(AXIS_STEP, new Double[] {3.0, 3.0});

		assertCreatingAllInstancesFailsIfWrongNoOfParams(tooMany, blankArray);
	}

	@Test
	public void createModelRejectsTooFewPathParamsForAllInstances() throws Exception {
		Map<AreaScanpath, Double[]> tooFew = new EnumMap<>(AreaScanpath.class);
		tooFew.put(GRID_POINTS, new Double[] {3.0});
		tooFew.put(GRID_STEP, new Double[] {4.0});
		tooFew.put(SPIRAL, blankArray);
		tooFew.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0});
		tooFew.put(LINE_POINTS, blankArray);
		tooFew.put(LINE_STEP, blankArray);
		tooFew.put(SINGLE_POINT, new Double[] {6.0});
		tooFew.put(AXIS_POINTS, blankArray);
		tooFew.put(AXIS_STEP, blankArray);

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
			List<Scannable> scannableList = path.equals(AXIS_STEP) || path.equals(AXIS_POINTS) ? axialScannables : scannables;

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
		exception.expectMessage(TwoAxisGridPointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(-5, 6);
		GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForGrid() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisGridPointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(0, 6);
		GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForTwoDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisLinePointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(-5);
		LINE_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForTwoDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisLinePointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(0);
		LINE_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForOneDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("AxialPointsModel" + String.format(ONE_POSITIVE_ERROR, 2));
		pathParams = Arrays.asList(2, 3, -5);
		AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForOneDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("AxialPointsModel" + String.format(ONE_POSITIVE_ERROR, 2));
		pathParams = Arrays.asList(2, 3, 0);
		AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForGrid() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisGridPointsModel.class.getSimpleName() + ALL_INTEGER_ERROR);
		pathParams = Arrays.asList(5, 6.2);
		GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForTwoDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisLinePointsModel.class.getSimpleName() + ALL_INTEGER_ERROR);
		pathParams = Arrays.asList(6.2);
		LINE_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForOneDEqualSpacing() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("AxialPointsModel" + String.format(ONE_INTEGER_ERROR, 2));
		pathParams = Arrays.asList(2, 3, 6.2);
		AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelCreatesCorrectModelForGrid() throws Exception {
		pathParams = Arrays.asList(5, 6);
		IScanPathModel model = GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisGridPointsModel.class)));
		TwoAxisGridPointsModel gModel = (TwoAxisGridPointsModel)model;
		assertThat(gModel.getScannableNames(), contains("name1", "name2"));
		assertThat(gModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(gModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(gModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(gModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(gModel.getxAxisPoints(), is(5));
		assertThat(gModel.getyAxisPoints(), is(6));
		assertThat(gModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(gModel.isAlternating(), is(false));
		assertThat(gModel.isContinuous(), is(false));
	}

	@Test
	public void createModelCreatesCorrectModelForGridWithRandomOffsetMutator() throws Exception {
		pathParams = Arrays.asList(5, 6);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		mutators.put(Mutator.CONTINUOUS, Arrays.asList(blankArray));
		IScanPathModel model = GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisGridPointsRandomOffsetModel.class)));
		TwoAxisGridPointsRandomOffsetModel gModel = (TwoAxisGridPointsRandomOffsetModel)model;
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
		assertThat(gModel.isContinuous(), is(true));
	}

	@Test
	public void createModelCreatesCorrectModelForTwoDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(5);
		mutators.put(Mutator.CONTINUOUS, Arrays.asList(blankArray));
		IScanPathModel model = LINE_POINTS.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisLinePointsModel.class)));
		TwoAxisLinePointsModel eModel = (TwoAxisLinePointsModel)model;
		assertThat(eModel.getScannableNames(), contains("name1", "name2"));
		assertThat(eModel.getBoundingLine().getxStart(), is(1.0));
		assertThat(eModel.getBoundingLine().getyStart(), is(2.0));
		assertThat(eModel.getBoundingLine().getLength(), is(5.0));
		assertThat(Math.rint(Math.toDegrees(eModel.getBoundingLine().getAngle())), is(53.0));
		assertThat(eModel.getPoints(), is(5));
		assertThat(eModel.isContinuous(), is(true));
	}

	@Test
	public void createModelCreatesCorrectModelForOneDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(-2, 2, 5);
		IScanPathModel model = AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(AxialStepModel.class)));
		AxialStepModel eModel = (AxialStepModel)model;
		assertThat(eModel.getScannableNames(), contains("name1"));
		assertThat(eModel.getStart(), is(-2.0));
		assertThat(eModel.getStop(), is(2.0));
		assertThat(eModel.getStep(), is(1.0));
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForRaster() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisGridStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(-5.2, 6.1);
		GRID_STEP.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForRaster() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisGridStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(-5.2,0);
		GRID_STEP.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeStepValueForTwoDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisLineStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(-5.2);
		LINE_STEP.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroStepValueForTwoDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(TwoAxisLineStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR);
		pathParams = Arrays.asList(0);
		LINE_STEP.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsNegativeStepValueForOneDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(AxialStepModel.class.getSimpleName() + String.format(ONE_POSITIVE_ERROR, 2));
		pathParams = Arrays.asList(2, 3, -5.2);
		AXIS_STEP.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsZeroStepValueForOneDStep() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(AxialStepModel.class.getSimpleName() + String.format(ONE_POSITIVE_ERROR, 2));
		pathParams = Arrays.asList(2, 3, 0);
		AXIS_STEP.createModel(axialScannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForRaster() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(String.format(MUTATOR_NOT_SUPPORTED_BY, "random offset")+TwoAxisGridStepModel.class.getSimpleName());
		pathParams = Arrays.asList(-5.2, 6.1);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		GRID_STEP.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelCreatesCorrectModelForRaster() throws Exception {
		pathParams = Arrays.asList(0.5, 6.5);
		mutators.put(Mutator.ALTERNATING, new ArrayList<>());
		IScanPathModel model = GRID_STEP.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisGridStepModel.class)));
		TwoAxisGridStepModel rModel = (TwoAxisGridStepModel)model;
		assertThat(rModel.getScannableNames(), contains("name1", "name2"));
		assertThat(rModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(rModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(rModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(rModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(rModel.getxAxisStep(), is(0.5));
		assertThat(rModel.getyAxisStep(), is(6.5));
		assertThat(rModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(rModel.isAlternating(), is(true));
		assertThat(rModel.isContinuous(), is(false));
	}

	@Test
	public void createModelCreatesCorrectModelForTwoDStep() throws Exception {
		pathParams = Arrays.asList(0.5);
		IScanPathModel model = LINE_STEP.createModel(scannables, pathParams, blineParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisLineStepModel.class)));
		TwoAxisLineStepModel sModel = (TwoAxisLineStepModel)model;
		assertThat(sModel.getScannableNames(), contains("name1", "name2"));
		assertThat(sModel.getBoundingLine().getxStart(), is(1.0));
		assertThat(sModel.getBoundingLine().getyStart(), is(2.0));
		assertThat(sModel.getBoundingLine().getLength(), is(5.0));
		assertThat(Math.rint(Math.toDegrees(sModel.getBoundingLine().getAngle())), is(53.0));
		assertThat(sModel.getStep(), is(0.5));
		assertThat(sModel.isContinuous(), is(false));
	}

	@Test
	public void createModelCreatesCorrectModelForOneDStep() throws Exception {
		pathParams = Arrays.asList(2, 3, 0.5);
		IScanPathModel model = AXIS_STEP.createModel(axialScannables, pathParams, blineParams, mutators);
		assertThat(model, is(instanceOf(AxialStepModel.class)));
		AxialStepModel sModel = (AxialStepModel)model;
		assertThat(sModel.getScannableNames(), contains("name1"));
		assertThat(sModel.getStart(), is(2.0));
		assertThat(sModel.getStop(), is(3.0));
		assertThat(sModel.getStep(), is(0.5));
	}

	@Test
	public void createModelCreatesCorrectModelForSpiral() throws Exception {
		pathParams = Arrays.asList(5.0);
		mutators.put(Mutator.CONTINUOUS, Arrays.asList(blankArray));
		IScanPathModel model = SPIRAL.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisSpiralModel.class)));
		TwoAxisSpiralModel sModel = (TwoAxisSpiralModel)model;
		assertThat(sModel.getScannableNames(), contains("name1", "name2"));
		assertThat(sModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(sModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(sModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(sModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(sModel.getScale(), is(5.0));
		assertThat(sModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(sModel.isContinuous(), is(true));
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForSpiral() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(String.format(MUTATOR_NOT_SUPPORTED_BY, "random offset")+TwoAxisSpiralModel.class.getSimpleName());
		pathParams = Arrays.asList(5.0);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		SPIRAL.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelsCorrectModelForLissajous() throws Exception {
		pathParams = Arrays.asList(5, 6.0, 7.0);
		IScanPathModel model = LISSAJOUS.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisLissajousModel.class)));
		TwoAxisLissajousModel lModel = (TwoAxisLissajousModel)model;
		assertThat(lModel.getScannableNames(), contains("name1", "name2"));
		assertThat(lModel.getBoundingBox().getxAxisStart(), is(1.0));
		assertThat(lModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(lModel.getBoundingBox().getxAxisLength(), is(3.0));
		assertThat(lModel.getBoundingBox().getyAxisLength(), is(4.0));
		assertThat(lModel.getPoints(), is(5));
		assertThat(lModel.getA(), is(6.0));
		assertThat(lModel.getB(), is(7.0));
		assertThat(lModel.getBoundingBox().getyAxisStart(), is(2.0));
		assertThat(lModel.isContinuous(), is(false));
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForLissajous() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(String.format(MUTATOR_NOT_SUPPORTED_BY, "random offset")+TwoAxisLissajousModel.class.getSimpleName());
		pathParams = Arrays.asList(5.0, 6.0, 7.0);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		LISSAJOUS.createModel(scannables, pathParams, bboxParams, mutators);
	}

	@Test
	public void createModelCreatesCorrectModelForSinglePoint() throws Exception {
		pathParams = Arrays.asList(5.1, 6.2);
		IScanPathModel model = SINGLE_POINT.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(TwoAxisPointSingleModel.class)));
		TwoAxisPointSingleModel eModel = (TwoAxisPointSingleModel)model;
		assertThat(eModel.getScannableNames(), contains("name1", "name2"));
		assertThat(eModel.getX(), is(5.1));
		assertThat(eModel.getY(), is(6.2));
	}
}
