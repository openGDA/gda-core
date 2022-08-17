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


import static gda.mscan.element.Scanpath.AXIS_ARRAY;
import static gda.mscan.element.Scanpath.AXIS_POINTS;
import static gda.mscan.element.Scanpath.AXIS_STEP;
import static gda.mscan.element.Scanpath.GRID_POINTS;
import static gda.mscan.element.Scanpath.GRID_STEP;
import static gda.mscan.element.Scanpath.LINE_POINTS;
import static gda.mscan.element.Scanpath.LINE_STEP;
import static gda.mscan.element.Scanpath.LISSAJOUS;
import static gda.mscan.element.Scanpath.SINGLE_POINT;
import static gda.mscan.element.Scanpath.SPIRAL;
import static gda.mscan.element.Scanpath.STATIC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel.Orientation;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScanpathTest {

	private static Map<Scanpath, Double[]> emptyPathData = new EnumMap<>(Scanpath.class);
	private static Map<Scanpath, Double[]> correctLengthPathData = new EnumMap<>(Scanpath.class);
	private static Double[] blankArray = new Double[]{};

	private final String MUTATOR_NOT_SUPPORTED_BY = "The %s mutator is not supported by ";
	private final String ALL_POSITIVE_ERROR = " path requires all positive parameters";
	private final String ALL_INTEGER_ERROR = " path requires all integer parameters";
	private final String ONE_POSITIVE_ERROR = " path requires that parameter %s is positive";
	private final String ONE_INTEGER_ERROR = " path requires that parameter %s is an integer";

	private List<Scannable> scannables;
	private List<Scannable> axialScannables;
	private List<Number> pathParams;
	private List<Number> bboxParams;
	private List<Number> blineParams;
	private Map<Mutator, List<Number>> mutators;

	@Mock
	private ScannableMotionUnits scannable1;

	@Mock
	private Scannable scannable2;

	@BeforeAll
	public static void setupClass() {
		for (Scanpath scanpath : Scanpath.values()) {
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
		correctLengthPathData.put(AXIS_ARRAY, new Double[] {5.0, 6.0});
		correctLengthPathData.put(STATIC, new Double[] {1.0});
	}

	@BeforeEach
	public void setUp() throws Exception {
		when(scannable1.getName()).thenReturn("name1");
		when(scannable2.getName()).thenReturn("name2");
		when(scannable1.getUserUnits()).thenReturn("Deg");

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
		assertThat(AXIS_ARRAY.valueCount(), is(2));
		assertThat(STATIC.valueCount(), is(1));
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
		assertTrue(AXIS_POINTS.modelType().equals(AxialPointsModel.class));
		assertTrue(AXIS_ARRAY.modelType().equals(AxialArrayModel.class));
		assertTrue(AXIS_STEP.modelType().equals(AxialStepModel.class));
	}

	@Test
	public void checkOnlyAxialArrayHasUnboundedParams() throws Exception {
		for (Scanpath path : Scanpath.values()) {
			if (path != AXIS_ARRAY) {
				assert(path.hasFixedValueCount());
			} else {
				assertFalse(path.hasFixedValueCount());
			}
		}
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
		assertTrue(AXIS_ARRAY.supports(Mutator.CONTINUOUS));
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
		assertTrue(AXIS_ARRAY.supports(Mutator.ALTERNATING));
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
		assertFalse(AXIS_ARRAY.supports(Mutator.RANDOM_OFFSET));
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
	public void createModelRejectsTooManyPathParamsForAllInstancesExceptAxisArray() throws Exception {
		Map<Scanpath, Double[]> tooMany = new EnumMap<>(Scanpath.class);
		tooMany.put(GRID_POINTS, new Double[] {3.0, 3.0, 3.0});
		tooMany.put(GRID_STEP, new Double[] {4.0, 4.0, 4.0});
		tooMany.put(SPIRAL, new Double[] {5.0, 5.0});
		tooMany.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0, 6.0, 6.0});
		tooMany.put(LINE_POINTS, new Double[] {3.0, 3.0});
		tooMany.put(LINE_STEP, new Double[] {3.0, 3.0});
		tooMany.put(SINGLE_POINT, new Double[] {6.0, 6.0, 6.0});
		tooMany.put(AXIS_POINTS, new Double[] {3.0, 3.0});
		tooMany.put(AXIS_STEP, new Double[] {3.0, 3.0});
		tooMany.put(STATIC, new Double[] {1.0, 1.0});

		assertCreatingAllInstancesFailsIfWrongNoOfParams(tooMany, blankArray, AXIS_ARRAY);
	}

	@Test
	public void createModelRejectsTooFewPathParamsForAllInstancesExceptStatic() throws Exception {
		Map<Scanpath, Double[]> tooFew = new EnumMap<>(Scanpath.class);
		tooFew.put(GRID_POINTS, new Double[] {3.0});
		tooFew.put(GRID_STEP, new Double[] {4.0});
		tooFew.put(SPIRAL, blankArray);
		tooFew.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0});
		tooFew.put(LINE_POINTS, blankArray);
		tooFew.put(LINE_STEP, blankArray);
		tooFew.put(SINGLE_POINT, new Double[] {6.0});
		tooFew.put(AXIS_POINTS, blankArray);
		tooFew.put(AXIS_STEP, blankArray);
		tooFew.put(AXIS_ARRAY, blankArray);
		tooFew.put(STATIC, blankArray);

		assertCreatingAllInstancesFailsIfWrongNoOfParams(tooFew, blankArray, STATIC);
	}

	@Test
	public void createModelRejectsTooManyBoundingParamsForAllInstancesExceptAxialArray() throws Exception {
		Double[] bboxData = new Double[] {1.0, 2.0, 3.0, 4.0, 5.0};  // Too many path params for rectangle/line (4)
		assertCreatingAllInstancesFailsIfWrongNoOfParams(correctLengthPathData, bboxData, AXIS_ARRAY);
	}

	@Test
	public void createModelRejectsTooFewBoundingParamsForAllInstances() throws Exception {
		assertCreatingAllInstancesFailsIfWrongNoOfParams(correctLengthPathData, blankArray, AXIS_ARRAY);
	}

	private void assertCreatingAllInstancesFailsIfWrongNoOfParams(Map<Scanpath, Double[]> pathParams,
															Double[] bboxParams, Scanpath... excluded) throws Exception {
		Scanpath[] values = ArrayUtils.removeElements(Scanpath.values(), excluded);

		for (Scanpath path: values) {

			List<Scannable> scannableList = path.equals(AXIS_STEP) || path.equals(AXIS_POINTS) || path.equals(AXIS_ARRAY) ? axialScannables : scannables;

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
		pathParams = Arrays.asList(-5, 6);
		var e = assertThrows(IllegalArgumentException.class,
				() -> GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisGridPointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR));
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForGrid() throws Exception {
		pathParams = Arrays.asList(0, 6);
		var e = assertThrows(IllegalArgumentException.class,
				() -> GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisGridPointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR));
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForTwoDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(-5);
		var e = assertThrows(IllegalArgumentException.class,
				() -> LINE_POINTS.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisLinePointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR));
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForTwoDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(0);
		var e = assertThrows(IllegalArgumentException.class,
				() -> LINE_POINTS.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisLinePointsModel.class.getSimpleName() + ALL_POSITIVE_ERROR));
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForOneDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(2, 3, -5);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString("AxialPointsModel" + String.format(ONE_POSITIVE_ERROR, 2)));
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForOneDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(2, 3, 0);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString("AxialPointsModel" + String.format(ONE_POSITIVE_ERROR, 2)));
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForGrid() throws Exception {
		pathParams = Arrays.asList(5, 6.2);
		var e = assertThrows(IllegalArgumentException.class,
				() -> GRID_POINTS.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisGridPointsModel.class.getSimpleName() + ALL_INTEGER_ERROR));
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForTwoDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(6.2);
		var e = assertThrows(IllegalArgumentException.class,
				() -> LINE_POINTS.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisLinePointsModel.class.getSimpleName() + ALL_INTEGER_ERROR));
	}

	@Test
	public void createModelRejectsNonIntegerNoOfPointsForOneDEqualSpacing() throws Exception {
		pathParams = Arrays.asList(2, 3, 6.2);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString("AxialPointsModel" + String.format(ONE_INTEGER_ERROR, 2)));
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
		assertThat(gModel.getOrientation(), is(Orientation.HORIZONTAL));
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
		assertThat(gModel.getOrientation(), is(Orientation.HORIZONTAL));
	}

	@Test
	public void createModelCreatesCorrectModelForGridWithVerticalMutator() throws Exception {
		pathParams = Arrays.asList(5, 6);
		mutators.put(Mutator.VERTICAL, Arrays.asList(blankArray));
		mutators.put(Mutator.ALTERNATING, Arrays.asList(blankArray));
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
		assertThat(gModel.getOrientation(), is(Orientation.VERTICAL));
		assertThat(gModel.isAlternating(), is(true));
		assertThat(gModel.isContinuous(), is(false));
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
		assertThat(model, is(instanceOf(AxialPointsModel.class)));
		AxialPointsModel eModel = (AxialPointsModel)model;
		assertThat(eModel.getScannableNames(), contains("name1"));
		assertThat(eModel.getStart(), is(-2.0));
		assertThat(eModel.getStop(), is(2.0));
		assertThat(eModel.getPoints(), is(5));
	}

	@Test
	public void createModelRejectsNegativeNoOfPointsForRaster() throws Exception {
		pathParams = Arrays.asList(-5.2, 6.1);
		var e = assertThrows(IllegalArgumentException.class,
				() -> GRID_STEP.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisGridStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR));
	}

	@Test
	public void createModelRejectsZeroNoOfPointsForRaster() throws Exception {
		pathParams = Arrays.asList(-5.2, 0);
		var e = assertThrows(IllegalArgumentException.class,
				() -> GRID_STEP.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString((TwoAxisGridStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR)));
	}

	@Test
	public void createModelRejectsNegativeStepValueForTwoDStep() throws Exception {
		pathParams = Arrays.asList(-5.2);
		var e = assertThrows(IllegalArgumentException.class,
				() -> LINE_STEP.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisLineStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR));
	}

	@Test
	public void createModelRejectsZeroStepValueForTwoDStep() throws Exception {
		pathParams = Arrays.asList(0);
		var e = assertThrows(IllegalArgumentException.class,
				() -> LINE_STEP.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(TwoAxisLineStepModel.class.getSimpleName() + ALL_POSITIVE_ERROR));
	}

	@Test
	public void createModelRejectsNegativePointsValueForOneDPoints() throws Exception {
		pathParams = Arrays.asList(2, 3, -5);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(AxialPointsModel.class.getSimpleName() + String.format(ONE_POSITIVE_ERROR, 2)));
	}

	@Test
	public void createModelRejectsZeroPointsValueForOneDPoints() throws Exception {
		pathParams = Arrays.asList(2, 3, 0);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(AxialPointsModel.class.getSimpleName() + String.format(ONE_POSITIVE_ERROR, 2)));
	}


	@Test
	public void createModelRejectsNegativeStepValueForOneDStep() throws Exception {
		pathParams = Arrays.asList(2, 3, -5.2);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_STEP.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(AxialStepModel.class.getSimpleName() + String.format(ONE_POSITIVE_ERROR, 2)));
	}

	@Test
	public void createModelRejectsZeroStepValueForOneDStep() throws Exception {
		pathParams = Arrays.asList(2, 3, 0);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_STEP.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(AxialStepModel.class.getSimpleName() + String.format(ONE_POSITIVE_ERROR, 2)));
	}

	@Test
	public void createModelRejectsNonIntStepValueForOneDStep() throws Exception {
		pathParams = Arrays.asList(-2, 2, 5.4);
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_POINTS.createModel(axialScannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(AxialPointsModel.class.getSimpleName() + String.format(ONE_INTEGER_ERROR, 2)));
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForRaster() throws Exception {
		pathParams = Arrays.asList(-5.2, 6.1);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		var e = assertThrows(IllegalArgumentException.class,
				() -> GRID_STEP.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(String.format(MUTATOR_NOT_SUPPORTED_BY, "random offset")+TwoAxisGridStepModel.class.getSimpleName()));
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
		assertThat(rModel.getOrientation(), is(Orientation.HORIZONTAL));
	}

	@Test
	public void createModelCreatesCorrectModelForRasterWithVerticalMutator() throws Exception {
		pathParams = Arrays.asList(0.5, 6.5);
		mutators.put(Mutator.VERTICAL, Arrays.asList(blankArray));
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
		assertThat(rModel.isAlternating(), is(false));
		assertThat(rModel.isContinuous(), is(false));
		assertThat(rModel.getOrientation(), is(Orientation.VERTICAL));
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
		assertThat(sModel.getUnits(), is(equalTo(List.of("Deg", "mm"))));
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
		assertThat(sModel.getUnits(), is(equalTo(List.of("Deg"))));
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
		pathParams = Arrays.asList(5.0);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		var e = assertThrows(IllegalArgumentException.class,
				() -> SPIRAL.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(
				String.format(MUTATOR_NOT_SUPPORTED_BY, "random offset") + TwoAxisSpiralModel.class.getSimpleName()));
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
		pathParams = Arrays.asList(5.0, 6.0, 7.0);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		var e = assertThrows(IllegalArgumentException.class,
				() -> LISSAJOUS.createModel(scannables, pathParams, bboxParams, mutators));
		assertThat(e.getMessage(), containsString(String.format(MUTATOR_NOT_SUPPORTED_BY, "random offset")
				+ TwoAxisLissajousModel.class.getSimpleName()));
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

	@Test
	public void createModelCreatesCorrectModelForAxialArray() throws Exception {
		pathParams = Arrays.asList(0.5, 1,  0.7,  -0.135);
		mutators.put(Mutator.CONTINUOUS, Arrays.asList(blankArray));
		IScanPathModel model = AXIS_ARRAY.createModel(axialScannables, pathParams, Arrays.asList(blankArray), mutators);
		assertThat(model, is(instanceOf(AxialArrayModel.class)));
		AxialArrayModel sModel = (AxialArrayModel)model;
		assertThat(sModel.getScannableNames(), contains("name1"));
		assertThat(sModel.getPositions()[0], is(0.5));
		assertThat(sModel.getPositions()[1], is(1.0));
		assertThat(sModel.getPositions()[2], is(0.7));
		assertThat(sModel.getPositions()[3], is(-0.135));
		assertThat(sModel.isContinuous(), is(true));
		assertThat(sModel.isAlternating(), is(false));
		assertThat(sModel.getUnits(), is(equalTo(List.of("Deg"))));
	}

	@Test
	public void createModelRejectsRandomOffsetMutatorForAxialArray() throws Exception {
		pathParams = Arrays.asList(5.0, 6.0, 7.0);
		mutators.put(Mutator.RANDOM_OFFSET, Arrays.asList(20, 2));
		var e = assertThrows(IllegalArgumentException.class,
				() -> AXIS_ARRAY.createModel(axialScannables, pathParams, Arrays.asList(blankArray), mutators));
		assertThat(e.getMessage(),
				containsString(String.format(MUTATOR_NOT_SUPPORTED_BY, "random offset") + AxialArrayModel.class.getSimpleName()));
	}
}
