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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
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
import gda.mscan.element.ScanDataConsumer;


@RunWith(MockitoJUnitRunner.class)
public class ClausesContextTest {

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
	private IRunnableDevice<Object> runnableDevice;

	@Mock
	private IDetectorModel model;

	@Mock
	private DummyMonitor monitor;

	@Mock
	private IRunnableDeviceService runnableDeviceService;

	@Mock
	private CompoundModel scanModel;

	@Mock
	private IROI roi;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ClausesContext clausesContext;
	private ClausesContext unvalidatedClauseContext;    // For test cases where we want to use the getters before
	                                                   // completing the whole context to confirm metadata was set

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		clausesContext = new ClausesContext(runnableDeviceService);
		unvalidatedClauseContext = new ClausesContext(runnableDeviceService).withoutPathClauseValidation();
		when(runnableDeviceService.getRunnableDevice("present")).thenReturn(runnableDevice);
		when(runnableDevice.getModel()).thenReturn(model);
		when(runnableDevice.getName()).thenReturn("detector");
		when(model.getName()).thenReturn("grid");
	}

	/**
	 * Test methods for {@link gda.mscan.ClausesContext#addScannable(gda.device.Scannable)}.
	 */

	@Test
	public void scannableToAddMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied Scannable was null");
		clausesContext.addScannable(null);
	}

	@Test
	public void cannotAddDetectorsToScanPathClause() {
		expectUnsupportedOperationWithMessageContents("DummyDetector", "cannot be present");
		clausesContext.addScannable(detector);
	}

	@Test
	public void cannotAddMonitorsToScanpathClause() {
		expectUnsupportedOperationWithMessageContents("DummyMonitor", "cannot be present");
		clausesContext.addScannable(monitor);
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
	 * Test methods for {@link gda.mscan.ClausesContext#setRegionShape(gda.mscan.element.RegionShape)}.
	 */

	@Test
	public void regionShapeToSetMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied RegionShape was null");
		prepareForRegionShapeTest(clausesContext, false);
		clausesContext.setRegionShape(null);
	}

	@Test
	public void regionShaperegionShapeCannotBeSetIfNoScannables() {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		clausesContext.setRegionShape(RegionShape.RECTANGLE);
	}

	@Test
	public void regionShapeCannotBeSetTwiceBecauseRegionShapeParamsWillHaveBeenInitialised() {
		expectIllegalStateWithMessageContents("RegionShape must be the specified before any parameters");
		prepareForRegionShapeTest(clausesContext, false);
		clausesContext.setRegionShape(RegionShape.RECTANGLE);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
	}

	@Test
	public void regionShapeCannotBeSetIfAreaScanpathAlreadyHasBeen() {
		expectUnsupportedOperationWithMessageContents("RegionShape must be set before AreaScanpath");
		prepareForRegionShapeTest(clausesContext, false);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
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
	 * Test methods for {@link gda.mscan.ClausesContext#setAreaScanpath(gda.mscan.element.AreaScanpath)}.
	 */

	@Test
	public void areaScanpathToSetMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied AreaScanpath was null");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(null);
	}

	@Test
	public void areaScanpathCannotBeSetIfNoScannables() {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
	}

	@Test
	public void twoAxisAreaScanpathCannotBeSetIfOnlyOneScannable() {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		clausesContext.addScannable(scannable);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
	}

	@Test
	public void oneAxisAreaScanpathCannotBeSetIfTwoScannables() {
		expectUnsupportedOperationWithMessageContents("requires 1 scannables");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setAreaScanpath(AreaScanpath.AXIS_POINTS);
	}

	@Test
	public void areaScanpathCannotBeSetTwice() {
		expectUnsupportedOperationWithMessageContents("AreaScanpath already set");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_STEP);
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
	 * Test methods for {@link gda.mscan.ClausesContext#addMutator(gda.mscan.element.Mutator)}.
	 */

	@Test
	public void mutatorToAddMustNotBeNull() {
		expectIllegalArgumentWithMessageContents("The supplied Mutator was null");
		clausesContext.addMutator(null);
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
		prepareForMutatorTest(clausesContext);
		clausesContext.addMutator(Mutator.ALTERNATING);
		clausesContext.addParam(2);
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
	public void addMutatorSetsMetadataAndStoresRandomOffsetGridMutatorCorrectlyForMinNoOfParameters() throws Exception {
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
		prepareForMutatorTest(clausesContext);
		clausesContext.addMutator(Mutator.RANDOM_OFFSET);
		clausesContext.validateAndAdjustPathClause();
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
		prepareForMutatorTest(clausesContext, path, params);
		clausesContext.addMutator(mutator);
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
	 * Test method for {@link gda.mscan.ClausesContext#addParam(java.lang.Number)}.
	 */

	@Test
	public void paramToAddMustNotBeNull() {
		prepareForNumericParamTest(clausesContext, RegionShape.CENTRED_RECTANGLE);
		expectIllegalArgumentWithMessageContents("The supplied Number was null");
		clausesContext.addParam(null);
	}

	@Test
	public void cannotAddParamsIfNoScannables() throws Exception {
		expectIllegalStateWithMessageContents("at least 1 scannable");
		clausesContext.addParam(2);
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
		prepareForAreaScanpathTest(clausesContext, Extent.AXIAL);
		clausesContext.setAreaScanpath(AreaScanpath.AXIS_POINTS);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getPathParams(), contains(20));
		assertThat(clausesContext.getModelPathParams(), contains(1,3,20));
	}

	@Test
	public void getBoundsReturnsSyntheticListForAxialScans() throws Exception {
		prepareForAreaScanpathTest(clausesContext, Extent.AXIAL);
		clausesContext.setAreaScanpath(AreaScanpath.AXIS_POINTS);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getShapeParams(), contains(1,3));
		assertThat(clausesContext.getBounds(), contains(1,1,3,3));
	}

	@Test
	public void getModelPathParamsReturnsSameAsGetPathParamsFor2AxisScan() throws Exception {
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getPathParams(), contains(20));
		assertThat(clausesContext.getModelPathParams(), contains(20));
	}

	@Test
	public void getBoundsReturnsSameAsGetShapeParamsFor2AxisScan() throws Exception {
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getShapeParams(), contains(1,3,3,5));
		assertThat(clausesContext.getBounds(), contains(1,3,3,5));
	}


	@Test
	public void cannotSetAreaScanpathOnceDefaultedBecauseItsParamsWillHaveStartedToFill() throws Exception {
		expectIllegalStateWithMessageContents("specified before its parameters");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		assertThat(clausesContext.addParam(2), is(true));
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
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
	 * Test method for {@link gda.mscan.ClausesContext#paramsFull()}.+
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
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		assertThat(clausesContext.addParam(0), is(true));
		assertThat(clausesContext.paramsFull(), is(false));
		assertThat(clausesContext.addParam(3), is(true));
		assertThat(clausesContext.paramsFull(), is(true));
		clausesContext.addParam(7);
	}
	@Test
	public void paramsForAreaScanpathWithSetAreaScanpathCannotBeFilledBeyondTheBound() throws Exception {
		expectIllegalStateWithMessageContents("has already been supplied");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		assertThat(clausesContext.addParam(3), is(true));
		assertThat(clausesContext.paramsFull(), is(true));
		clausesContext.addParam(7);
	}

	@Test
	public void contextValuesCannotBeUsedIfNotValidated() throws Exception {
		expectUnsupportedOperationWithMessageContents("must be validated");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addParam(1);
		clausesContext.getScannables();
	}

	// Post completion validation tests

	@Test
	public void validateRejectsContextsWithoutScannables() throws Exception {
		expectIllegalStateWithMessageContents("required number of Scannables");
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithoutBothRegionShapeAndAreaScanpath() throws Exception {
		expectIllegalStateWithMessageContents("both RegionShape and AreaScanpath");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathFor2DShape() throws Exception {
		expectIllegalStateWithMessageContents("cannot be combined with");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(AreaScanpath.LINE_POINTS);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathFor1AxisPath() throws Exception {
		expectUnsupportedOperationWithMessageContents("requires 2 scannables");
		prepareForAreaScanpathTest(clausesContext, Extent.AXIAL);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.addParam(2);
		clausesContext.addParam(2);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathForLine() throws Exception {
		expectIllegalStateWithMessageContents("cannot be combined with");
		prepareForNumericParamTest(clausesContext, RegionShape.LINE);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithIncompatibleRegionShapeAndAreaScanpathForPoint() throws Exception {
		expectIllegalStateWithMessageContents("cannot be combined with");
		prepareForNumericParamTest(clausesContext, RegionShape.POINT);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.setAreaScanpath(AreaScanpath.LINE_POINTS);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithTooFewAreaScanpathParams() throws Exception {
		expectIllegalStateWithMessageContents("correct no of params");
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.addParam(3);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithTooFewAreaScanpathParamsForUnboundedRegionShape() throws Exception {
		expectIllegalStateWithMessageContents("correct no of params");
		prepareForNumericParamTest(clausesContext, RegionShape.POLYGON);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		clausesContext.addParam(7);
		clausesContext.addParam(8);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.addParam(3);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsContextsWithTooFewAreaScanpathParamsForLineRegionShape() throws Exception {
		expectIllegalStateWithMessageContents("correct no of params");
		prepareForNumericParamTest(clausesContext, RegionShape.LINE);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		clausesContext.setAreaScanpath(AreaScanpath.LINE_STEP);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void validateRejectsOddNumberOfParamsForPolygonalRegionShape() throws Exception {
		expectIllegalStateWithMessageContents("even number of params");
		prepareForNumericParamTest(clausesContext, RegionShape.POLYGON);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		clausesContext.addParam(7);
		clausesContext.addParam(8);
		clausesContext.addParam(9);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.validateAndAdjustPathClause();
	}

	@Test
	public void correctlyFullyCompletedContextWithBoundedRegionShapeValidates() {
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void correctlyFullyCompletedContextWithUnboundedRegionShapeValidates() {
		prepareForNumericParamTest(clausesContext, RegionShape.POLYGON);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(4);
		clausesContext.addParam(6);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.addParam(2);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void correctlyFullyCompletedContextWithLineRegionShapeValidates() {
		prepareForNumericParamTest(clausesContext, RegionShape.LINE);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(4);
		clausesContext.setAreaScanpath(AreaScanpath.LINE_POINTS);
		clausesContext.addParam(13);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}
	@Test
	public void correctlyFullyCompletedContextWithPointRegionShapeValidates() {
		prepareForNumericParamTest(clausesContext, RegionShape.POINT);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.setAreaScanpath(AreaScanpath.SINGLE_POINT);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedRegionShapeValidates() {
		prepareForRegionShapeTest(clausesContext, false);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(4);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedAreaScanpathValidates() {
		prepareForAreaScanpathTest(clausesContext, Extent.AREA);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedRegionShapeAndAreaScanpathValidates() {
		prepareForRegionShapeTest(clausesContext, false);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(4);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void defaultingOfAreaScanpathCannotBeDetectedWithUnboundedRegionShape() {
		expectIllegalStateWithMessageContents("both RegionShape and AreaScanpath");
		prepareForNumericParamTest(clausesContext, RegionShape.POLYGON);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(4);
		clausesContext.addParam(6);
		clausesContext.addParam(3);
		// and now lets attempt to default the AreaScanpath by not specifying it
		clausesContext.addParam(3);
		clausesContext.addParam(2);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void addDetectorByNameRejectsScannableDetectorsWithoutACorrespondingRunnableDevice() throws Exception {
		thrown.expect(ScanningException.class);
		expectMessageContents("Could not get detector for name");
		when(runnableDeviceService.getRunnableDevice(anyString())).thenReturn(null);
		clausesContext.addDetector("missing", 0.1);
	}

	@Test
	public void addDetectorByNameRejectsNullName() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("The supplied detector name String was null");
		clausesContext.addDetector((String)null, 0.1);
	}

	@Test
	public void addDetectorRejectsRunnableDeviceWiithNoModel() throws Exception {
		thrown.expect(ScanningException.class);
		expectMessageContents("Could not get model for detector");
		when(runnableDevice.getModel()).thenReturn(null);
		clausesContext.addDetector(runnableDevice, 0.1);
	}

	@Test
	public void addDetectorRejectsNullRunnableDevice() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("The supplied IRunnableDevice was null");
		clausesContext.addDetector((IRunnableDevice<IDetectorModel>)null, 0.1);
	}

	@Test
	public void addDetectorByNameWithoutExposureSucceedsWithNoExposureSetting() throws Exception {
		clausesContext.addDetector("present", 0);
		verify(model, never()).setExposureTime(anyDouble());
		assertThat(clausesContext.isDetectorClauseSeen(), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
		assertThat(clausesContext.getDetectorMap(), hasEntry("detector", model));
	}

	@Test
	public void addDetectorWithoutExposureSucceedsWithNoExposureSetting() throws Exception {
		clausesContext.addDetector(runnableDevice,  0);
		verify(model, never()).setExposureTime(anyDouble());
		assertThat(clausesContext.isDetectorClauseSeen(), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
		assertThat(clausesContext.getDetectorMap(), hasEntry("detector", model));
	}

	@Test
	public void addDetectorByNameWithExposureSucceedsWithExposureSetting() throws Exception {
		clausesContext.addDetector("present", 0.1);
		verify(model, times(1)).setExposureTime(0.1);
		assertThat(clausesContext.isDetectorClauseSeen(), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
		assertThat(clausesContext.getDetectorMap(), hasEntry("detector", model));
	}

	@Test
	public void addDetectorWithExposureSucceedsWithExposureSetting() throws Exception {
		clausesContext.addDetector(runnableDevice,  0.1);
		verify(model, times(1)).setExposureTime(0.1);
		assertThat(clausesContext.isDetectorClauseSeen(), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
		assertThat(clausesContext.getDetectorMap(), hasEntry("detector", model));
	}

	@Test
	public void addMonitorByNameRejectsNullName() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("The supplied monitor name String was null");
		clausesContext.addMonitor((String)null, false);
	}

	@Test
	public void addMonitorSucceeds() throws Exception {
		clausesContext.addMonitor("present", false);
		assertThat(clausesContext.isDetectorClauseSeen(), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
		assertThat(clausesContext.getMonitorsPerPoint().contains("present"), is(true));
	}

	@Test
	public void addMonitorUsingScannableReadoutSucceeds() throws Exception {
		clausesContext.addMonitor("present", true);
		assertThat(clausesContext.isDetectorClauseSeen(), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
		assertThat(clausesContext.getMonitorsPerPoint().contains("present"), is(true));
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsNullScanModel() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("The supplied CompoundModel was null");
		clausesContext.addPathDefinitionToCompoundModel(null);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsUnpopulatedContext() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("scan must have the required number of Scannables");
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsContextWithoutRegionShape() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("scan must have both RegionShape and AreaScanpath");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsContextWithoutAreaScanpath() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("scan must have both RegionShape and AreaScanpath");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForRegionShape() throws Exception {
		thrown.expect(UnsupportedOperationException.class);
		expectMessageContents("not enough parameters for the RegionShape");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooManyParamsForRegionShape() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("AreaScanpath must be specified before its parameters");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForAreaScanpath() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("correct no of params for RegionShape and Scanpath");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}


	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForAreaScanpathForRegionShapeWithNoFixedValueCount()
			throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("correct no of params for RegionShape and Scanpath");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.POLYGON);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsOddNoOfParamsForAreaScanpathForRegionShapeWithNoFixedValueCount()
			throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("Polygon requires an even number of param");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.POLYGON);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addParam(1);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooManyParamsForAreaScanpath() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("params for the Spiral has already been supplied");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsPointWithNonMatchingAreaScanpath() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("POINT cannot be combined with SPIRAL");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.POINT);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.SPIRAL);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsPointWithNonMatchingShapeAndPathParams() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("RegionShape and Scanpath parameters must match");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.POINT);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.SINGLE_POINT);
		clausesContext.addParam(1);
		clausesContext.addParam(2);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForMutator() throws Exception {
		thrown.expect(UnsupportedOperationException.class);
		expectMessageContents("Too few parameters supplied for");
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.addParam(1);
		clausesContext.addParam(2);
		clausesContext.addMutator(Mutator.RANDOM_OFFSET);
		clausesContext.addPathDefinitionToCompoundModel(scanModel);
	}

	@Test
	public void addPathDefinitionToCompoundModelSucceedsForCompliantContext() throws Exception {
		CompoundModel compoundModel = new CompoundModel();
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(2);
		clausesContext.addParam(3);
		clausesContext.setAreaScanpath(AreaScanpath.GRID_POINTS);
		clausesContext.addParam(1);
		clausesContext.addParam(2);
		clausesContext.addPathDefinitionToCompoundModel(compoundModel);
		assertThat(clausesContext.isScanPathSeen(), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
		assertThat(compoundModel.getModels().get(0), instanceOf(TwoAxisGridPointsModel.class));
		assertThat(((TwoAxisGridPointsModel)compoundModel.getModels().get(0)).getxAxisPoints(), is(1));
		assertThat(((TwoAxisGridPointsModel)compoundModel.getModels().get(0)).getyAxisPoints(), is(2));
		assertThat(compoundModel.getRegions().iterator().next().getRoi(), instanceOf(CircularROI.class));
		assertThat(((CircularROI)compoundModel.getRegions().iterator().next().getRoi()).getCentre()[0], is(1.0));
		assertThat(((CircularROI)compoundModel.getRegions().iterator().next().getRoi()).getCentre()[1], is(2.0));
		assertThat(((CircularROI)compoundModel.getRegions().iterator().next().getRoi()).getRadius(), is(3.0));
		assertNull(compoundModel.getMutators());
	}

	@Test
	public void templatesCanBeSuccessfullySet() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.TEMPLATE, "fil fal fol");
		assertThat(clausesContext.getTemplates().size(), is(3));
		assertThat(clausesContext.getTemplates().contains("fil"), is(true));
		assertThat(clausesContext.getTemplates().contains("fal"), is(true));
		assertThat(clausesContext.getTemplates().contains("fol"), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void processorsCanBeSuccessfullySet() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil fol two::fal");
		assertThat(clausesContext.getProcessorRequest().getRequest().size(), is(2));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("one"), is(true));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("two"), is(true));
		assertThat(((List<String>)clausesContext.getProcessorRequest().getRequest().get("one")).get(0), is("fil"));
		assertThat(((List<String>)clausesContext.getProcessorRequest().getRequest().get("one")).get(1), is("fol"));
		assertThat(((List<String>)clausesContext.getProcessorRequest().getRequest().get("two")).get(0), is("fal"));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void templateSettingDoesNotBlockProcessorSetting() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.TEMPLATE, "fil fal fol");
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil two::fal");
		assertThat(clausesContext.getTemplates().size(), is(3));
		assertThat(clausesContext.getTemplates().contains("fil"), is(true));
		assertThat(clausesContext.getTemplates().contains("fal"), is(true));
		assertThat(clausesContext.getTemplates().contains("fol"), is(true));
		assertThat(clausesContext.getProcessorRequest().getRequest().size(), is(2));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("one"), is(true));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("two"), is(true));
		assertThat(((List<String>)clausesContext.getProcessorRequest().getRequest().get("one")).get(0), is("fil"));
		assertThat(((List<String>)clausesContext.getProcessorRequest().getRequest().get("two")).get(0), is("fal"));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void processorSettingDoesNotBlockTemplateSetting() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil two::fal");
		clausesContext.addScanDataConsumer(ScanDataConsumer.TEMPLATE, "fil fal fol");
		assertThat(clausesContext.getTemplates().size(), is(3));
		assertThat(clausesContext.getTemplates().contains("fil"), is(true));
		assertThat(clausesContext.getTemplates().contains("fal"), is(true));
		assertThat(clausesContext.getTemplates().contains("fol"), is(true));
		assertThat(clausesContext.getProcessorRequest().getRequest().size(), is(2));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("one"), is(true));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("two"), is(true));
		assertThat(((List<String>)clausesContext.getProcessorRequest().getRequest().get("one")).get(0), is("fil"));
		assertThat(((List<String>)clausesContext.getProcessorRequest().getRequest().get("two")).get(0), is("fal"));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

	@Test
	public void templateConsumerIsRejectedIfContextNotAcceptingTemplates() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("Templates have already been set");
		clausesContext.addScanDataConsumer(ScanDataConsumer.TEMPLATE, "fil");
		clausesContext.addScanDataConsumer(ScanDataConsumer.TEMPLATE, "fal");
	}

	@Test
	public void processorConsumerIsRejectedIfContextNotAcceptingProcessors() throws Exception {
		thrown.expect(IllegalStateException.class);
		expectMessageContents("Processors have already been set");
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil fol");
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "two::fal");
	}

	@Test
	public void processorConsumerIsRejectedIfNoSpaceBetweenDeclarations() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("Incorrect processor specification");
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil::fol fel two::fal");
	}

	@Test
	public void processorConsumerIsRejectedIfNoSeparatorSequenceInDeclaration() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("No processor app specified");
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "onefil fel two::fal");
	}

	@Test
	public void processorConsumerIsRejectedIfAppNameIsRepeated() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("App names may not be repeated");
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil one::fel two::fal");
	}

	@Test
	public void processorConsumerIsRejectedIfEmptyConfigStringIsSupplied() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		expectMessageContents("No processor app specified");
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "");
	}

	private void prepareForMutatorTest(final ClausesContext context) {
		prepareForMutatorTest(context, AreaScanpath.GRID_POINTS, 3, 3);
	}

	private void prepareForMutatorTest(final ClausesContext context, final AreaScanpath path, final Number... params) {
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

	private void prepareForAreaScanpathTest(final ClausesContext context, final Extent extent) {
		prepareForRegionShapeTest(context, extent == Extent.AXIAL);
		context.setRegionShape(lookup.get(extent));
		context.addParam(1);
		context.addParam(3);
		if (extent == Extent.LINEAR || extent == Extent.AREA) {
			context.addParam(3);
			context.addParam(5);
		}
	}

	private void prepareForRegionShapeTest(final ClausesContext context, final boolean singleAxis) {
		context.addScannable(scannable);
		if (singleAxis == false) {
			context.addScannable(scannable);
		}
	}

	private void prepareForNumericParamTest(final ClausesContext context, RegionShape regionShape) {
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
