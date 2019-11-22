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

package gda.mscan;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import gda.device.detector.DummyDetector;
import gda.device.monitor.DummyMonitor;
import gda.device.scannable.ScannableMotor;
import gda.mscan.element.AreaScanpath;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;


@RunWith(MockitoJUnitRunner.class)
public class ClauseContextTest {

	private enum Extent {POINT, AXIAL, LINEAR, AREA}
	private Map<Extent, RegionShape> lookup =
			ImmutableMap.<Extent, RegionShape>of(Extent.POINT, RegionShape.POINT,
												Extent.AXIAL, RegionShape.AXIAL,
												Extent.LINEAR, RegionShape.LINE,
												Extent.AREA, RegionShape.RECTANGLE);

	@Mock
	private ScannableMotor scannable;

	@Mock
	private DummyDetector detector;

	@Mock
	private DummyMonitor monitor;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ClauseContext clauseContext;
	private ClauseContext unvalidatedClauseContext;    // For test cases where we want to use the getters before
	                                                   // completing the whole context to confirm metadata was set

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		clauseContext = new ClauseContext();
		unvalidatedClauseContext = new ClauseContext().withoutValidation();
	}

	/**
	 * Test methods for {@link gda.mscan.ClauseContext#addScannable(gda.device.Scannable)}.
	 */

	@Test
	public void scannableToAddMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied Scannable was null");
		clauseContext.addScannable(null);
	}

	@Test
	public void cannotAddDetectorsToClause() {
		expectUnsupportedOperationWithMessageContents("DummyDetector", "cannot be present");
		clauseContext.addScannable(detector);
	}

	@Test
	public void cannotAddMonitorsToClause() {
		expectUnsupportedOperationWithMessageContents("DummyMonitor", "cannot be present");
		clauseContext.addScannable(monitor);
	}

	@Test
	public void cannotAddMoreThanMaximumScannablesPerClause() throws Exception {
		expectUnsupportedOperationWithMessageContents("Too many scannables in scan clause");
		unvalidatedClauseContext.addScannable(scannable);
		assertThat(unvalidatedClauseContext.getScannables().size(), is(1));
		unvalidatedClauseContext.addScannable(scannable);
		assertThat(unvalidatedClauseContext.getScannables().size(), is(2));
		unvalidatedClauseContext.addScannable(scannable);
	}

	@Test
	public void addScannableSetsMetadataAndStoresScannable() throws Exception {
		assertThat(unvalidatedClauseContext.addScannable(scannable), is(true));
		assertThat(unvalidatedClauseContext.getScannables(), contains(scannable));
		assertThat(unvalidatedClauseContext.getScannables().size(), is(1));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("gda.device.Scannable"));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertNull(unvalidatedClauseContext.paramsToFill());
	}

	/**
	 * Test methods for {@link gda.mscan.ClauseContext#setRegionShape(gda.mscan.element.RegionShape)}.
	 */

	@Test
	public void regionShapeToSetMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied RegionShape was null");
		prepareForRegionShapeTest(clauseContext, false);
		clauseContext.setRegionShape(null);
	}

	@Test
	public void regionShaperegionShapeCannotBeSetIfNoScannables() {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		clauseContext.setRegionShape(RegionShape.RECTANGLE);
	}

	@Test
	public void regionShapeCannotBeSetTwiceBecauseRegionShapeParamsWillHaveBeenInitialised() {
		expectIllegalStateWithMessageContents("RegionShape must be the specified before any parameters");
		prepareForRegionShapeTest(clauseContext, false);
		clauseContext.setRegionShape(RegionShape.RECTANGLE);
		clauseContext.setRegionShape(RegionShape.CIRCLE);
	}

	@Test
	public void regionShapeCannotBeSetIfAreaScanpathAlreadyHasBeen() {
		expectUnsupportedOperationWithMessageContents("RegionShape must be set before AreaScanpath");
		prepareForRegionShapeTest(clauseContext, false);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.setRegionShape(RegionShape.CIRCLE);
	}

	@Test
	public void setRegionShapeSetsMetadataAndRegionShapeIfScannablesCorrectlySet() throws Exception {
		prepareForRegionShapeTest(unvalidatedClauseContext, false);
		unvalidatedClauseContext.setRegionShape(RegionShape.CIRCLE);
		assertThat(unvalidatedClauseContext.getRegionShape(), is(RegionShape.CIRCLE));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("gda.mscan.element.RegionShape"));
		assertThat(unvalidatedClauseContext.paramsToFill(), is(unvalidatedClauseContext.getShapeParams()));
		assertThat(unvalidatedClauseContext.getRequiredParamCount(), is(3));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getParamCount(), is(0));
	}

	/**
	 * Test methods for {@link gda.mscan.ClauseContext#setAreaScanpath(gda.mscan.element.AreaScanpath)}.
	 */

	@Test
	public void areaScanpathToSetMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied AreaScanpath was null");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(null);
	}

	@Test
	public void areaScanpathCannotBeSetIfNoScannables() {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
	}

	@Test
	public void twoAxisAreaScanpathCannotBeSetIfOnlyOneScannable() {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		clauseContext.addScannable(scannable);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
	}

	@Test
	public void oneAxisAreaScanpathCannotBeSetIfTwoScannables() {
		expectUnsupportedOperationWithMessageContents("requires 1 scannables");
		clauseContext.addScannable(scannable);
		clauseContext.addScannable(scannable);
		clauseContext.setAreaScanpath(AreaScanpath.AXIS_POINTS);
	}

	@Test
	public void areaScanpathCannotBeSetTwice() {
		expectUnsupportedOperationWithMessageContents("AreaScanpath already set");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_STEP);
	}

	@Test
	public void setAreaScanpathSetsMetadataAndStoresScanpathIfScannablesAndRegionShapeCorrectlySet() throws Exception {
		prepareForAreaScanpathTest(unvalidatedClauseContext, Extent.AREA);
		unvalidatedClauseContext.setAreaScanpath(AreaScanpath.SPIRAL);
		assertThat(unvalidatedClauseContext.getAreaScanpath(), is(AreaScanpath.SPIRAL));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("gda.mscan.element.AreaScanpath"));
		assertThat(unvalidatedClauseContext.paramsToFill(), is(unvalidatedClauseContext.getPathParams()));
		assertThat(unvalidatedClauseContext.getRequiredParamCount(), is(1));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getParamCount(), is(0));
	}

	/**
	 * Test methods for {@link gda.mscan.ClauseContext#addMutator(gda.mscan.element.Mutator)}.
	 */

	@Test
	public void mutatorToAddMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied Mutator was null");
		clauseContext.addMutator(null);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForGrid() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, AreaScanpath.GRID_POINTS, 3, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForRaster() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, AreaScanpath.GRID_STEP, 3, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForLissajous() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, AreaScanpath.LISSAJOUS, 1, 3, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForTwoAxisStep() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, AreaScanpath.LINE_STEP, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForTwoAxisNoOfPoints() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, AreaScanpath.LINE_POINTS, 5);
	}

	@Test
	public void addMutatorRejectsAlternatingMutatorForPoint() throws Exception {
		checkMutatorIsNotSupported(Mutator.ALTERNATING, AreaScanpath.SINGLE_POINT, 1, 2);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForOneAxisStep() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, AreaScanpath.AXIS_STEP, 1);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForOneAxisNoOfPoints() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, AreaScanpath.AXIS_POINTS, 10);
	}

	@Test
	public void addMutatorRejectsParametersForAlternatingMutator() throws Exception {
		expectIllegalStateWithMessageContents("Too many parameters");
		prepareForMutatorTest(clauseContext);
		clauseContext.addMutator(Mutator.ALTERNATING);
		clauseContext.addParam(2);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresRandomOffsetGridMutatorCorrectlyForGrid() throws Exception {
		prepareForMutatorTest(unvalidatedClauseContext);
		unvalidatedClauseContext.addMutator(Mutator.RANDOM_OFFSET);
		unvalidatedClauseContext.addParam(20);
		unvalidatedClauseContext.addParam(2);
		assertThat(unvalidatedClauseContext.getMutatorUses(), hasKey(Mutator.RANDOM_OFFSET));
		assertThat(unvalidatedClauseContext.getMutatorUses().get(Mutator.RANDOM_OFFSET), contains(20, 2));
		assertThat(unvalidatedClauseContext.getMutatorUses().size(), is(1));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("java.lang.Number"));
		assertThat(unvalidatedClauseContext.paramsFull(), is(true));
		assertNotNull(unvalidatedClauseContext.paramsToFill());
	}

	@Test
	public void addMutatorSetsMetadataAndStoresRandomOffsetGridMutatorCorrectlyForMinimumNumberOfParameters() throws Exception {
		prepareForMutatorTest(unvalidatedClauseContext);
		unvalidatedClauseContext.addMutator(Mutator.RANDOM_OFFSET);
		unvalidatedClauseContext.addParam(20);
		assertThat(unvalidatedClauseContext.getMutatorUses(), hasKey(Mutator.RANDOM_OFFSET));
		assertThat(unvalidatedClauseContext.getMutatorUses().get(Mutator.RANDOM_OFFSET), contains(20));
		assertThat(unvalidatedClauseContext.getMutatorUses().size(), is(1));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("java.lang.Number"));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertNotNull(unvalidatedClauseContext.paramsToFill());
	}

	@Test
	public void addMutatorRejectsTooManyParametersForRandomOffsetGridMutator() throws Exception {
		expectIllegalStateWithMessageContents("Too many parameters");
		prepareForMutatorTest(unvalidatedClauseContext);
		unvalidatedClauseContext.addMutator(Mutator.RANDOM_OFFSET);
		unvalidatedClauseContext.addParam(20);
		unvalidatedClauseContext.addParam(2);
		unvalidatedClauseContext.addParam(2);
	}

	@Test
	public void addMutatorRejectsNegativeValuesOfPercentageOffsetForRandomOffsetGridMutator() throws Exception {
		expectIllegalArgumentWithMessageContents("must be positive");
		prepareForMutatorTest(unvalidatedClauseContext);
		unvalidatedClauseContext.addMutator(Mutator.RANDOM_OFFSET);
		unvalidatedClauseContext.addParam(-20);
	}

	@Test
	public void addMutatorRejectsTooFewParametersForRandomOffsetGridMutator() throws Exception {
		expectUnsupportedOperationWithMessageContents("Too few parameters");
		prepareForMutatorTest(clauseContext);
		clauseContext.addMutator(Mutator.RANDOM_OFFSET);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForRaster() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.GRID_STEP, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForSpiral() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.SPIRAL, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForLissajous() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.LISSAJOUS, 1, 2, 3);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForTwoAxisStep() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.LINE_STEP, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForTwoAxisNoOfPoints() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.LINE_POINTS, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorPoint() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.SINGLE_POINT, 1, 2);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForOneAxisStep() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.AXIS_STEP, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForOneAxisNoOfPoints() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, AreaScanpath.AXIS_POINTS, 10);
	}

	@Test
	public void addMutatorRejectsContinuousMutatorPoint() throws Exception {
		checkMutatorIsNotSupported(Mutator.CONTINUOUS, AreaScanpath.SINGLE_POINT, 1, 2);
	}

	private void checkMutatorIsNotSupported(Mutator mutator, AreaScanpath path, Number... params) throws Exception {
		expectUnsupportedOperationWithMessageContents(mutator + " is not supported");
		prepareForMutatorTest(clauseContext, path, params);
		clauseContext.addMutator(mutator);
	}

	private void checkMutatorIsSupported(Mutator mutator, AreaScanpath scanpath, Number... params) {
		prepareForMutatorTest(unvalidatedClauseContext, scanpath, params);
		unvalidatedClauseContext.addMutator(mutator);
		assertThat(unvalidatedClauseContext.getMutatorUses(), hasEntry(mutator, new ArrayList<Number>()));
		assertThat(unvalidatedClauseContext.getMutatorUses().size(), is(1));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("gda.mscan.element.Mutator"));
		assertThat(unvalidatedClauseContext.paramsFull(), is(true));
		assertNotNull(unvalidatedClauseContext.paramsToFill());
	}

	/**
	 * Test method for {@link gda.mscan.ClauseContext#addParam(java.lang.Number)}.
	 */

	@Test
	public void paramToAddMustNotBeNull() {
		prepareForNumericParamTest(clauseContext, RegionShape.CENTRED_RECTANGLE);
		expectIllegalArgumentWithMessageContents("The supplied Number was null");
		clauseContext.addParam(null);
	}

	@Test
	public void cannotAddParamsIfNoScannables() throws Exception {
		expectIllegalStateWithMessageContents("at least 1 scannable");
		clauseContext.addParam(2);
	}

	@Test
	public void regionShapeIsBeDefaultedIfAddParamsWhenNoRegionShapeSetButScannablesAdded() throws Exception {
		prepareForRegionShapeTest(unvalidatedClauseContext, false);
		assertThat(unvalidatedClauseContext.addParam(2), is(true));
		assertThat(unvalidatedClauseContext.getRegionShape(), is(RegionShape.defaultValue()));
		assertThat(unvalidatedClauseContext.getShapeParams(), contains(2));
		assertThat(unvalidatedClauseContext.getParamCount(), is(1));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("java.lang.Number"));
	}

	@Test
	public void areaScanpathIsDefaultedIfAddParamsWhenRegionShapeAndItsParamsSetAndScannablesAdded() throws Exception {
		prepareForAreaScanpathTest(unvalidatedClauseContext, Extent.AREA);
		assertThat(unvalidatedClauseContext.addParam(2), is(true));
		assertThat(unvalidatedClauseContext.getAreaScanpath(), is(AreaScanpath.defaultValue()));
		assertThat(unvalidatedClauseContext.getPathParams(), contains(2));
		assertThat(unvalidatedClauseContext.getParamCount(), is(1));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("java.lang.Number"));
	}

	@Test
	public void getModelPathParamsReturnsCompositeListForAxialScans() throws Exception {
		prepareForAreaScanpathTest(clauseContext, Extent.AXIAL);
		clauseContext.setAreaScanpath(AreaScanpath.AXIS_POINTS);
		clauseContext.addParam(20);
		clauseContext.validateAndAdjust();
		assertThat(clauseContext.getPathParams(), contains(20));
		assertThat(clauseContext.getModelPathParams(), contains(1,3,20));
	}

	@Test
	public void getBoundsReturnsSyntheticListForAxialScans() throws Exception {
		prepareForAreaScanpathTest(clauseContext, Extent.AXIAL);
		clauseContext.setAreaScanpath(AreaScanpath.AXIS_POINTS);
		clauseContext.addParam(20);
		clauseContext.validateAndAdjust();
		assertThat(clauseContext.getShapeParams(), contains(1,3));
		assertThat(clauseContext.getBounds(), contains(1,1,3,3));
	}

	@Test
	public void getModelPathParamsReturnsSameAsGetPathParamsFor2AxisScan() throws Exception {
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clauseContext.addParam(20);
		clauseContext.validateAndAdjust();
		assertThat(clauseContext.getPathParams(), contains(20));
		assertThat(clauseContext.getModelPathParams(), contains(20));
	}

	@Test
	public void getBoundsReturnsSameAsGetShapeParamsFor2AxisScan() throws Exception {
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clauseContext.addParam(20);
		clauseContext.validateAndAdjust();
		assertThat(clauseContext.getShapeParams(), contains(1,3,3,5));
		assertThat(clauseContext.getBounds(), contains(1,3,3,5));
	}


	@Test
	public void cannotSetAreaScanpathOnceDefaultedBecauseItsParamsWillHaveStartedToFill() throws Exception {
		expectIllegalStateWithMessageContents("specified before its parameters");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		assertThat(clauseContext.addParam(2), is(true));
		clauseContext.setAreaScanpath(AreaScanpath.SPIRAL);
	}

	@Test
	public void addParamUpdatesMetadataAndStoresParam() throws Exception {
		prepareForNumericParamTest(unvalidatedClauseContext, RegionShape.RECTANGLE);
		assertThat(unvalidatedClauseContext.addParam(5), is(true));
		assertThat(unvalidatedClauseContext.getShapeParams(), contains(5));
		assertThat(unvalidatedClauseContext.getParamCount(), is(1));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("java.lang.Number"));
	}

	/**
	 * Test method for {@link gda.mscan.ClauseContext#paramsFull()}.+
	 */
	@Test
	public void boundedRegionShapeParamsListCannotBeOverfilledBecauseWillDefaultAreaScanpath() throws Exception {
		prepareForNumericParamTest(unvalidatedClauseContext, RegionShape.CIRCLE);
		assertThat(unvalidatedClauseContext.getRequiredParamCount(), is(3));
		assertThat(unvalidatedClauseContext.addParam(0), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(3), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(4), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(true));
		assertThat(unvalidatedClauseContext.addParam(6), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getPathParams(), contains(6));
		assertThat(unvalidatedClauseContext.getPathParams().size(), is(1));
		assertThat(unvalidatedClauseContext.getAreaScanpath(), is(AreaScanpath.defaultValue()));
	}

	@Test(expected = NoSuchElementException.class)
	public void unboundedRegionShapeParamsListCannotBeOverfilled() throws Exception {
		prepareForNumericParamTest(unvalidatedClauseContext, RegionShape.POLYGON);
		assertThat(unvalidatedClauseContext.getRequiredParamCount(), is(6));
		assertThat(unvalidatedClauseContext.addParam(0), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(3), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(4), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(6), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(7), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(4), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(6), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.addParam(7), is(true));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getPathParams().size(), is(0));
		unvalidatedClauseContext.getAreaScanpath();
	}

	@Test
	public void paramsForAreaScanpathWithDefaultedAreaScanpathCannotBeFilledBeyondTheBound() throws Exception {
		expectIllegalStateWithMessageContents("has already been supplied");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		assertThat(clauseContext.addParam(0), is(true));
		assertThat(clauseContext.paramsFull(), is(false));
		assertThat(clauseContext.addParam(3), is(true));
		assertThat(clauseContext.paramsFull(), is(true));
		clauseContext.addParam(7);
	}
	@Test
	public void paramsForAreaScanpathWithSetAreaScanpathCannotBeFilledBeyondTheBound() throws Exception {
		expectIllegalStateWithMessageContents("has already been supplied");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(AreaScanpath.SPIRAL);
		assertThat(clauseContext.addParam(3), is(true));
		assertThat(clauseContext.paramsFull(), is(true));
		clauseContext.addParam(7);
	}

	@Test
	public void contextValuesCannotBeUsedIfNotValidated() throws Exception {
		expectUnsupportedOperationWithMessageContents("must be validated");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clauseContext.addParam(1);
		clauseContext.getScannables();
	}

	// Post completion validation tests

	@Test
	public void validateRejectsContextsWithoutScannables() throws Exception {
		expectIllegalStateWithMessageContents("required number of Scannables");
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithoutBothRegionShapeAndAreaScanpath() throws Exception {
		expectIllegalStateWithMessageContents("both RegionShape and AreaScanpath");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathFor2DShape() throws Exception {
		expectIllegalStateWithMessageContents("cannot be combined with");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(AreaScanpath.LINE_POINTS);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathFor1AxisPath() throws Exception {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		prepareForAreaScanpathTest(clauseContext, Extent.AXIAL);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.addParam(2);
		clauseContext.addParam(2);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathForLine() throws Exception {
		expectIllegalStateWithMessageContents("cannot be combined with");
		prepareForNumericParamTest(clauseContext, RegionShape.LINE);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(5);
		clauseContext.addParam(6);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathForPoint() throws Exception {
		expectIllegalStateWithMessageContents("cannot be combined with");
		prepareForNumericParamTest(clauseContext, RegionShape.POINT);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.setAreaScanpath(AreaScanpath.LINE_POINTS);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithTooFewAreaScanpathParams() throws Exception {
		expectIllegalStateWithMessageContents("correct no of params");
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.addParam(3);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithTooFewAreaScanpathParamsForUnboundedRegionShape() throws Exception {
		expectIllegalStateWithMessageContents("correct no of params");
		prepareForNumericParamTest(clauseContext, RegionShape.POLYGON);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(5);
		clauseContext.addParam(6);
		clauseContext.addParam(7);
		clauseContext.addParam(8);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.addParam(3);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsContextsWithTooFewAreaScanpathParamsForLineRegionShape() throws Exception {
		expectIllegalStateWithMessageContents("correct no of params");
		prepareForNumericParamTest(clauseContext, RegionShape.LINE);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(5);
		clauseContext.addParam(6);
		clauseContext.setAreaScanpath(AreaScanpath.LINE_STEP);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void validateRejectsOddNumberOfParamsForPolygonalRegionShape() throws Exception {
		expectIllegalStateWithMessageContents("even number of params");
		prepareForNumericParamTest(clauseContext, RegionShape.POLYGON);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(5);
		clauseContext.addParam(6);
		clauseContext.addParam(7);
		clauseContext.addParam(8);
		clauseContext.addParam(9);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.validateAndAdjust();
	}

	@Test
	public void correctlyFullyCompletedContextWithBoundedRegionShapeValidates() {
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}

	@Test
	public void correctlyFullyCompletedContextWithUnboundedRegionShapeValidates() {
		prepareForNumericParamTest(clauseContext, RegionShape.POLYGON);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(4);
		clauseContext.addParam(5);
		clauseContext.addParam(4);
		clauseContext.addParam(6);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.addParam(2);
		clauseContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clauseContext.addParam(3);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}

	@Test
	public void correctlyFullyCompletedContextWithLineRegionShapeValidates() {
		prepareForNumericParamTest(clauseContext, RegionShape.LINE);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(4);
		clauseContext.setAreaScanpath(AreaScanpath.LINE_POINTS);
		clauseContext.addParam(13);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}
	@Test
	public void correctlyFullyCompletedContextWithPointRegionShapeValidates() {
		prepareForNumericParamTest(clauseContext, RegionShape.POINT);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.setAreaScanpath(AreaScanpath.SINGLE_POINT);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedRegionShapeValidates() {
		prepareForRegionShapeTest(clauseContext, false);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(4);
		clauseContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedAreaScanpathValidates() {
		prepareForAreaScanpathTest(clauseContext, Extent.AREA);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedRegionShapeAndAreaScanpathValidates() {
		prepareForRegionShapeTest(clauseContext, false);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(4);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}

	@Test
	public void defaultingOfAreaScanpathCannotBeDetectedWithUnboundedRegionShape() {
		expectIllegalStateWithMessageContents("both RegionShape and AreaScanpath");
		prepareForNumericParamTest(clauseContext, RegionShape.POLYGON);
		clauseContext.addParam(3);
		clauseContext.addParam(3);
		clauseContext.addParam(4);
		clauseContext.addParam(4);
		clauseContext.addParam(5);
		clauseContext.addParam(4);
		clauseContext.addParam(6);
		clauseContext.addParam(3);
		// and now lets attempt to default the AreaScanpath by not specifying it
		clauseContext.addParam(3);
		clauseContext.addParam(2);
		assertThat(clauseContext.validateAndAdjust(), is(true));
	}

	private void prepareForMutatorTest(final ClauseContext context) {
		prepareForMutatorTest(context, AreaScanpath.GRID_POINTS, 3, 3);
	}

	private void prepareForMutatorTest(final ClauseContext context, final AreaScanpath path, final Number... params) {
		Extent extent = Extent.AXIAL;

		if (path.equals(AreaScanpath.SINGLE_POINT)) {
			extent =  Extent.POINT;
		} else if (IBoundingBoxModel.class.isAssignableFrom(path.modelType())) {
			extent = Extent.AREA;
		} else if (IBoundingLineModel.class.isAssignableFrom(path.modelType())) {
			extent = Extent.LINEAR;
		}

		prepareForAreaScanpathTest(context, extent);
		context.setAreaScanpath(path);
		for (Number param : params) {
			context.addParam(param);
		}
	}

	private void prepareForAreaScanpathTest(final ClauseContext context, final Extent extent) {
		prepareForRegionShapeTest(context, extent == Extent.AXIAL);
		context.setRegionShape(lookup.get(extent));
		context.addParam(1);
		context.addParam(3);
		if (extent == Extent.LINEAR || extent == Extent.AREA) {
			context.addParam(3);
			context.addParam(5);
		}
	}

	private void prepareForRegionShapeTest(final ClauseContext context, final boolean singleAxis) {
		context.addScannable(scannable);
		if (singleAxis == false) {
			context.addScannable(scannable);
		}
	}

	private void prepareForNumericParamTest(final ClauseContext context, RegionShape regionShape) {
		prepareForRegionShapeTest(context, false);
		context.setRegionShape(regionShape);
	}

	private void expectUnsupportedOperationWithMessageContents(final String... substrings) {
		thrown.expect(UnsupportedOperationException.class);
		expectMessageContents(substrings);
	}
	private void expectIllegalStateWithMessageContents(final String... substrings) {
		thrown.expect(IllegalStateException.class);
		expectMessageContents(substrings);
	}

	private void expectIllegalArgumentWithMessageContents(final String... substrings) {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents(substrings);
	}

	private void expectMessageContents(final String... substrings) {
		for (String substring : substrings) {
			thrown.expectMessage(substring);
		}
	}
}
