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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import gda.device.detector.DummyDetector;
import gda.device.monitor.DummyMonitor;
import gda.device.scannable.ScannableMotor;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;
import gda.mscan.element.ScanDataConsumer;
import gda.mscan.element.Scanpath;


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
	}

	/**
	 * Test methods for {@link gda.mscan.ClausesContext#addScannable(gda.device.Scannable)}.
	 */

	@Test
	public void scannableToAddMustNotBeNull() {
		var e = assertThrows(IllegalArgumentException.class, () -> clausesContext.addScannable(null));
		expectMessageContents(e, "The supplied Scannable was null");
	}

	@Test
	public void cannotAddDetectorsToScanPathClause() {
		var e = assertThrows(UnsupportedOperationException.class, () -> clausesContext.addScannable(detector));
		expectMessageContents(e, "DummyDetector", "cannot be present");
	}

	@Test
	public void cannotAddMonitorsToScanpathClause() {
		var e = assertThrows(UnsupportedOperationException.class, () -> clausesContext.addScannable(monitor));
		expectMessageContents(e, "DummyMonitor", "cannot be present");
	}

	@Test
	public void cannotAddMoreThanMaximumScannablesPerClause() throws Exception {
		unvalidatedClauseContext.addScannable(scannable);
		assertThat(unvalidatedClauseContext.getScannables().size(), is(1));
		unvalidatedClauseContext.addScannable(scannable);
		assertThat(unvalidatedClauseContext.getScannables().size(), is(2));
		var e = assertThrows(UnsupportedOperationException.class, () -> unvalidatedClauseContext.addScannable(scannable));
		expectMessageContents(e, "Too many scannables in scan clause");
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
		prepareForRegionShapeTest(clausesContext, false);
		var e = assertThrows(IllegalArgumentException.class, () -> clausesContext.setRegionShape(null));
		expectMessageContents(e, "The supplied RegionShape was null");
	}

	@Test
	public void regionShaperegionShapeCannotBeSetIfNoScannables() {
		var e = assertThrows(UnsupportedOperationException.class,
				() -> clausesContext.setRegionShape(RegionShape.RECTANGLE));
		expectMessageContents(e, "requires 2 scannables");
	}

	@Test
	public void regionShapeCannotBeSetTwiceBecauseRegionShapeParamsWillHaveBeenInitialised() {
		prepareForRegionShapeTest(clausesContext, false);
		clausesContext.setRegionShape(RegionShape.RECTANGLE);
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.setRegionShape(RegionShape.CIRCLE));
		expectMessageContents(e, "RegionShape must be the specified before any parameters");
	}

	@Test
	public void regionShapeCannotBeSetIfScanpathAlreadyHasBeen() {
		prepareForRegionShapeTest(clausesContext, false);
		// TODO test name probably needs changing
		var e = assertThrows(UnsupportedOperationException.class, () -> clausesContext.setScanpath(Scanpath.GRID_POINTS));
		expectMessageContents(e, "RegionShape must be set before Scanpath");
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
	 * Test methods for {@link gda.mscan.ClausesContext#setScanpath(gda.mscan.element.Scanpath)}.
	 */

	@Test
	public void scanpathToSetMustNotBeNull() {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		var e = assertThrows(IllegalArgumentException.class, () -> clausesContext.setScanpath(null));
		expectMessageContents(e, "The supplied Scanpath was null");
	}

	@Test
	public void scanpathCannotBeSetIfNoScannables() {
		var e = assertThrows(UnsupportedOperationException.class, () -> clausesContext.setScanpath(Scanpath.GRID_POINTS));
		expectMessageContents(e, "requires 2 scannables");
	}

	@Test
	public void twoAxisScanpathCannotBeSetIfOnlyOneScannable() {
		clausesContext.addScannable(scannable);
		var e = assertThrows(UnsupportedOperationException.class,
				() -> clausesContext.setScanpath(Scanpath.GRID_POINTS));
		expectMessageContents(e, "requires 2 scannables");
	}

	@Test
	public void oneAxisScanpathCannotBeSetIfTwoScannables() {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		var e = assertThrows(UnsupportedOperationException.class,
				() -> clausesContext.setScanpath(Scanpath.AXIS_POINTS));
		expectMessageContents(e, "requires 1 scannables");
	}

	@Test
	public void scanpathCannotBeSetTwice() {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setScanpath(Scanpath.GRID_POINTS);
		var e = assertThrows(UnsupportedOperationException.class, () -> clausesContext.setScanpath(Scanpath.GRID_STEP));
		expectMessageContents(e, "Scanpath already set");
	}

	@Test
	public void setScanpathSetsMetadataAndStoresScanpathIfScannablesAndRegionShapeCorrectlySet() throws Exception {
		prepareForScanpathTest(unvalidatedClauseContext, Extent.AREA);
		unvalidatedClauseContext.setScanpath(Scanpath.SPIRAL);
		assertThat(unvalidatedClauseContext.getScanpath(), is(Scanpath.SPIRAL));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("gda.mscan.element.Scanpath"));
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
		var e = assertThrows(IllegalArgumentException.class, () -> clausesContext.addMutator(null));
		expectMessageContents(e, "The supplied Mutator was null");
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForGrid() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, Scanpath.GRID_POINTS, 3, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForRaster() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, Scanpath.GRID_STEP, 3, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForLissajous() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, Scanpath.LISSAJOUS, 1, 3, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForTwoAxisStep() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, Scanpath.LINE_STEP, 3);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForTwoAxisNoOfPoints() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, Scanpath.LINE_POINTS, 5);
	}

	@Test
	public void addMutatorRejectsAlternatingMutatorForPoint() throws Exception {
		checkMutatorIsNotSupported(Mutator.ALTERNATING, Scanpath.SINGLE_POINT, 1, 2);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForOneAxisStep() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, Scanpath.AXIS_STEP, 1);
	}

	@Test
	public void addMutatorSetsMetadataAndStoresAlternatingMutatorCorrectlyForOneAxisNoOfPoints() throws Exception {
		checkMutatorIsSupported(Mutator.ALTERNATING, Scanpath.AXIS_POINTS, 10);
	}

	@Test
	public void addMutatorRejectsParametersForAlternatingMutator() throws Exception {
		prepareForMutatorTest(clausesContext);
		clausesContext.addMutator(Mutator.ALTERNATING);
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.addParam(2));
		expectMessageContents(e, "Too many parameters");
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
		prepareForMutatorTest(unvalidatedClauseContext);
		unvalidatedClauseContext.addMutator(Mutator.RANDOM_OFFSET);
		unvalidatedClauseContext.addParam(20);
		unvalidatedClauseContext.addParam(2);
		var e = assertThrows(IllegalStateException.class, () -> unvalidatedClauseContext.addParam(2));
		expectMessageContents(e, "Too many parameters");
	}

	@Test
	public void addMutatorRejectsNegativeValuesOfPercentageOffsetForRandomOffsetGridMutator() throws Exception {
		prepareForMutatorTest(unvalidatedClauseContext);
		unvalidatedClauseContext.addMutator(Mutator.RANDOM_OFFSET);
		var e = assertThrows(IllegalArgumentException.class, () -> unvalidatedClauseContext.addParam(-20));
		expectMessageContents(e, "must be positive");
	}

	@Test
	public void addMutatorRejectsTooFewParametersForRandomOffsetGridMutator() throws Exception {
		prepareForMutatorTest(clausesContext);
		clausesContext.addMutator(Mutator.RANDOM_OFFSET);
		var e = assertThrows(UnsupportedOperationException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "Too few parameters");
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForRaster() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.GRID_STEP, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForSpiral() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.SPIRAL, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForLissajous() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.LISSAJOUS, 1, 2, 3);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForTwoAxisStep() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.LINE_STEP, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForTwoAxisNoOfPoints() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.LINE_POINTS, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorPoint() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.SINGLE_POINT, 1, 2);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForOneAxisStep() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.AXIS_STEP, 1);
	}

	@Test
	public void addMutatorRejectsRandomOffsetGridMutatorForOneAxisNoOfPoints() throws Exception {
		checkMutatorIsNotSupported(Mutator.RANDOM_OFFSET, Scanpath.AXIS_POINTS, 10);
	}

	@Test
	public void addMutatorRejectsContinuousMutatorPoint() throws Exception {
		checkMutatorIsNotSupported(Mutator.CONTINUOUS, Scanpath.SINGLE_POINT, 1, 2);
	}

	private void checkMutatorIsNotSupported(Mutator mutator, Scanpath path, Number... params) throws Exception {
		prepareForMutatorTest(clausesContext, path, params);
		var e = assertThrows(UnsupportedOperationException.class, () -> clausesContext.addMutator(mutator));
		expectMessageContents(e, mutator + " is not supported");
	}

	private void checkMutatorIsSupported(Mutator mutator, Scanpath scanpath, Number... params) {
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
		var e = assertThrows(IllegalArgumentException.class, () -> clausesContext.addParam(null));
		expectMessageContents(e, "The supplied Number was null");
	}

	@Test
	public void cannotAddParamsIfNoScannables() throws Exception {
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.addParam(2));
		expectMessageContents(e, "at least 1 scannable");
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
	public void scanpathIsDefaultedIfAddParamsWhenRegionShapeAndItsParamsSetAndScannablesAdded() throws Exception {
		prepareForScanpathTest(unvalidatedClauseContext, Extent.AREA);
		assertThat(unvalidatedClauseContext.addParam(2), is(true));
		assertThat(unvalidatedClauseContext.getScanpath(), is(Scanpath.defaultValue()));
		assertThat(unvalidatedClauseContext.getPathParams(), contains(2));
		assertThat(unvalidatedClauseContext.getParamCount(), is(1));
		assertThat(unvalidatedClauseContext.paramsFull(), is(false));
		assertThat(unvalidatedClauseContext.getPreviousType().getTypeName(), is("java.lang.Number"));
	}

	@Test
	public void getModelPathParamsReturnsCompositeListForAxialScansExceptArray() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AXIAL);
		clausesContext.setScanpath(Scanpath.AXIS_POINTS);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getPathParams(), contains(20));
		assertThat(clausesContext.getModelPathParams().size(), is(3));
		assertThat(clausesContext.getModelPathParams(), contains(1,3,20));
	}

	@Test
	public void getModelPathParamsReturnsSuppliedListForAxialArray() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AXIAL);
		clausesContext.setScanpath(Scanpath.AXIS_ARRAY);
		clausesContext.addParam(20);
		clausesContext.addParam(21);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getModelPathParams().size(), is(2));
		assertThat(clausesContext.getModelPathParams(), contains(20, 21));
	}

	@Test
	public void getBoundsReturnsSyntheticListForAxialScans() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AXIAL);
		clausesContext.setScanpath(Scanpath.AXIS_POINTS);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getShapeParams(), contains(1,3));
		assertThat(clausesContext.getBounds(), contains(1,1,3,3));
	}

	@Test
	public void getModelPathParamsReturnsSameAsGetPathParamsFor2AxisScan() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setScanpath(Scanpath.SPIRAL);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getPathParams(), contains(20));
		assertThat(clausesContext.getModelPathParams(), contains(20));
	}

	@Test
	public void getBoundsReturnsSameAsGetShapeParamsFor2AxisScan() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setScanpath(Scanpath.SPIRAL);
		clausesContext.addParam(20);
		clausesContext.validateAndAdjustPathClause();
		assertThat(clausesContext.getShapeParams(), contains(1,3,3,5));
		assertThat(clausesContext.getBounds(), contains(1,3,3,5));
	}


	@Test
	public void cannotSetScanpathOnceDefaultedBecauseItsParamsWillHaveStartedToFill() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		assertThat(clausesContext.addParam(2), is(true));
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.setScanpath(Scanpath.SPIRAL));
		expectMessageContents(e, "specified before its parameters");
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
	public void boundedRegionShapeParamsListCannotBeOverfilledBecauseWillDefaultScanpath() throws Exception {
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
		assertThat(unvalidatedClauseContext.getScanpath(), is(Scanpath.defaultValue()));
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
		unvalidatedClauseContext.getScanpath();
	}
	@Test
	public void unboundedScanpathParamsListCannotBeOverfilled() throws Exception {
		prepareForNumericParamTest(unvalidatedClauseContext, RegionShape.AXIAL);
		unvalidatedClauseContext.addParam(0);
		unvalidatedClauseContext.addParam(3);
		unvalidatedClauseContext.setScanpath(Scanpath.AXIS_ARRAY);
		assertThat(unvalidatedClauseContext.getRequiredParamCount(), is(2));
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
		assertThat(unvalidatedClauseContext.getPathParams().size(), is(8));
	}

	@Test
	public void paramsForScanpathWithDefaultedScanpathCannotBeFilledBeyondTheBound() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		assertThat(clausesContext.addParam(0), is(true));
		assertThat(clausesContext.paramsFull(), is(false));
		assertThat(clausesContext.addParam(3), is(true));
		assertThat(clausesContext.paramsFull(), is(true));
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.addParam(7));
		expectMessageContents(e, "has already been supplied");
	}
	@Test
	public void paramsForScanpathWithSetScanpathCannotBeFilledBeyondTheBound() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setScanpath(Scanpath.SPIRAL);
		assertThat(clausesContext.addParam(3), is(true));
		assertThat(clausesContext.paramsFull(), is(true));
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.addParam(7));
		expectMessageContents(e, "has already been supplied");
	}

	@Test
	public void contextValuesCannotBeUsedIfNotValidated() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setScanpath(Scanpath.SPIRAL);
		clausesContext.addParam(1);
		var e = assertThrows(UnsupportedOperationException.class, clausesContext::getScannables);
		expectMessageContents(e, "must be validated");
	}

	// Post completion validation tests

	@Test
	public void validateRejectsContextsWithoutScannables() throws Exception {
		var e = assertThrows(IllegalStateException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "required number of Scannables");
	}

	@Test
	public void validateRejectsContextsWithoutBothRegionShapeAndScanpath() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		var e = assertThrows(IllegalStateException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "both RegionShape and Scanpath");
	}

	@Test
	public void contextsWithIncompatibleRegionShapeAndScanpathFor2DShapeAreRejected() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.setScanpath(Scanpath.LINE_POINTS));
		expectMessageContents(e, "cannot be combined with");
	}

	@Test
	public void contextsWithIncompatibleRegionShapeAndScanpathFor1AxisPathAreRejected() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AXIAL);
		var e = assertThrows(UnsupportedOperationException.class, () -> clausesContext.setScanpath(Scanpath.GRID_POINTS));
		expectMessageContents(e, "requires 2 scannables");
	}

	@Test
	public void contextsWithIncompatibleRegionShapeAndScanpathForLineAreRejected() throws Exception {
		prepareForNumericParamTest(clausesContext, RegionShape.LINE);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.setScanpath(Scanpath.GRID_POINTS));
		expectMessageContents(e, "cannot be combined with");
	}

	@Test
	public void contextsWithIncompatibleRegionShapeAndScanpathForPointAreRejected() throws Exception {
		prepareForNumericParamTest(clausesContext, RegionShape.POINT);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		var e = assertThrows(IllegalStateException.class, () -> clausesContext.setScanpath(Scanpath.LINE_POINTS));
		expectMessageContents(e, "cannot be combined with");
	}

	@Test
	public void validateRejectsContextsWithTooFewScanpathParams() throws Exception {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.addParam(3);
		var e = assertThrows(IllegalStateException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "correct no of params");
	}

	@Test
	public void validateRejectsContextsWithTooFewScanpathParamsForUnboundedRegionShape() throws Exception {
		prepareForNumericParamTest(clausesContext, RegionShape.POLYGON);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		clausesContext.addParam(7);
		clausesContext.addParam(8);
		clausesContext.setScanpath(Scanpath.GRID_POINTS);
		clausesContext.addParam(3);
		var e = assertThrows(IllegalStateException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "correct no of params");
	}

	@Test
	public void validateRejectsContextsWithTooFewScanpathParamsForLineRegionShape() throws Exception {
		prepareForNumericParamTest(clausesContext, RegionShape.LINE);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		clausesContext.setScanpath(Scanpath.LINE_STEP);
		var e = assertThrows(IllegalStateException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "correct no of params");
	}

	@Test
	public void validateRejectsOddNumberOfParamsForPolygonalRegionShape() throws Exception {
		prepareForNumericParamTest(clausesContext, RegionShape.POLYGON);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(6);
		clausesContext.addParam(7);
		clausesContext.addParam(8);
		clausesContext.addParam(9);
		clausesContext.setScanpath(Scanpath.GRID_POINTS);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		var e = assertThrows(IllegalStateException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "even number of params");
	}

	@Test
	public void correctlyFullyCompletedContextWithBoundedRegionShapeValidates() {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.setScanpath(Scanpath.GRID_POINTS);
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
		clausesContext.setScanpath(Scanpath.SPIRAL);
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
		clausesContext.setScanpath(Scanpath.LINE_POINTS);
		clausesContext.addParam(13);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}
	@Test
	public void correctlyFullyCompletedContextWithPointRegionShapeValidates() {
		prepareForNumericParamTest(clausesContext, RegionShape.POINT);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.setScanpath(Scanpath.SINGLE_POINT);
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
		clausesContext.setScanpath(Scanpath.GRID_POINTS);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedScanpathValidates() {
		prepareForScanpathTest(clausesContext, Extent.AREA);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		assertThat(clausesContext.validateAndAdjustPathClause(), is(true));
	}

	@Test
	public void correctlyCompletedContextWithDefaultedRegionShapeAndScanpathValidates() {
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
	public void defaultingOfScanpathCannotBeDetectedWithUnboundedRegionShape() {
		prepareForNumericParamTest(clausesContext, RegionShape.POLYGON);
		clausesContext.addParam(3);
		clausesContext.addParam(3);
		clausesContext.addParam(4);
		clausesContext.addParam(4);
		clausesContext.addParam(5);
		clausesContext.addParam(4);
		clausesContext.addParam(6);
		clausesContext.addParam(3);
		// and now lets attempt to default the Scanpath by not specifying it
		clausesContext.addParam(3);
		clausesContext.addParam(2);
		var e = assertThrows(IllegalStateException.class, clausesContext::validateAndAdjustPathClause);
		expectMessageContents(e, "both RegionShape and Scanpath");
	}

	@Test
	public void addDetectorByNameRejectsScannableDetectorsWithoutACorrespondingRunnableDevice() throws Exception {
		when(runnableDeviceService.getRunnableDevice(anyString())).thenReturn(null);
		var e = assertThrows(ScanningException.class, () -> clausesContext.addDetector("missing", 0.1));
		expectMessageContents(e, "Could not get detector for name");
	}

	@Test
	public void addDetectorByNameRejectsNullName() throws Exception {
		var e = assertThrows(IllegalArgumentException.class, () -> clausesContext.addDetector((String) null, 0.1));
		expectMessageContents(e, "The supplied detector name String was null");
	}

	@Test
	public void addDetectorRejectsRunnableDeviceWiithNoModel() throws Exception {
		when(runnableDevice.getModel()).thenReturn(null);
		var e = assertThrows(ScanningException.class, () -> clausesContext.addDetector(runnableDevice, 0.1));
		expectMessageContents(e, "Could not get model for detector");
	}

	@Test
	public void addDetectorRejectsNullRunnableDevice() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addDetector((IRunnableDevice<IDetectorModel>) null, 0.1));
		expectMessageContents(e, "The supplied IRunnableDevice was null");
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
		var e = assertThrows(IllegalArgumentException.class, () -> clausesContext.addMonitor((String) null, false));
		expectMessageContents(e, "The supplied monitor name String was null");
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
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(null));
		expectMessageContents(e, "The supplied CompoundModel was null");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsUnpopulatedContext() throws Exception {
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "scan must have the required number of Scannables");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsContextWithoutRegionShape() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "scan must have both RegionShape and Scanpath");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsContextWithoutScanpath() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "scan must have both RegionShape and Scanpath");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForRegionShape() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		var e = assertThrows(UnsupportedOperationException.class,
				() -> clausesContext.setScanpath(Scanpath.SPIRAL));
		expectMessageContents(e, "not enough parameters for the RegionShape");
	}

	@Test
	public void syntaxDefaultingPreventsEntryOfTooManyParamsForRegionShape() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.setScanpath(Scanpath.SPIRAL));
		expectMessageContents(e, "Scanpath must be specified before its parameters");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForScanpath() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setScanpath(Scanpath.SPIRAL);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "correct no of params for RegionShape and Scanpath");
	}


	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForScanpathForRegionShapeWithNoFixedValueCount()
			throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.POLYGON);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setScanpath(Scanpath.SPIRAL);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "correct no of params for RegionShape and Scanpath");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsOddNoOfParamsForScanpathForRegionShapeWithNoFixedValueCount()
			throws Exception {
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
		clausesContext.setScanpath(Scanpath.SPIRAL);
		clausesContext.addParam(1);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "Polygon requires an even number of param");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooManyParamsForScanpath() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setScanpath(Scanpath.SPIRAL);
		clausesContext.addParam(1);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addParam(1));
		expectMessageContents(e, "params for the Spiral has already been supplied");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsPointWithNonMatchingScanpath() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.POINT);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.setScanpath(Scanpath.SPIRAL));
		expectMessageContents(e, "Point cannot be combined with Spiral");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsPointWithNonMatchingShapeAndPathParams() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.POINT);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setScanpath(Scanpath.SINGLE_POINT);
		clausesContext.addParam(1);
		clausesContext.addParam(2);
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "RegionShape and Scanpath parameters must match");
	}

	@Test
	public void addPathDefinitionToCompoundModelRejectsTooFewParamsForMutator() throws Exception {
		clausesContext.addScannable(scannable);
		clausesContext.addScannable(scannable);
		clausesContext.setRegionShape(RegionShape.CIRCLE);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.addParam(1);
		clausesContext.setScanpath(Scanpath.GRID_POINTS);
		clausesContext.addParam(1);
		clausesContext.addParam(2);
		clausesContext.addMutator(Mutator.RANDOM_OFFSET);
		var e = assertThrows(UnsupportedOperationException.class,
				() -> clausesContext.addPathDefinitionToCompoundModel(scanModel));
		expectMessageContents(e, "Too few parameters supplied for");
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
		clausesContext.setScanpath(Scanpath.GRID_POINTS);
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
		assertThat(compoundModel.getMutators(), is(new ArrayList<>()));
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

	@Test
	public void perScanMonitorsCanBeSuccessfullySet() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.PER_SCAN_MONITOR, "fil fal fol");
		assertThat(clausesContext.getPerScanMonitors().size(), is(3));
		assertThat(clausesContext.getPerScanMonitors().contains("fil"), is(true));
		assertThat(clausesContext.getPerScanMonitors().contains("fal"), is(true));
		assertThat(clausesContext.getPerScanMonitors().contains("fol"), is(true));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

	@Test
	public void processorsCanBeSuccessfullySet() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil fol two::fal");
		assertThat(clausesContext.getProcessorRequest().getRequest().size(), is(2));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("one"), is(true));
		assertThat(clausesContext.getProcessorRequest().getRequest().containsKey("two"), is(true));
		assertThat(((List<Object>)clausesContext.getProcessorRequest().getRequest().get("one")).get(0), is("fil"));
		assertThat(((List<Object>)clausesContext.getProcessorRequest().getRequest().get("one")).get(1), is("fol"));
		assertThat(((List<Object>)clausesContext.getProcessorRequest().getRequest().get("two")).get(0), is("fal"));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

	@Test
	public void sampleMetadataCanBeSuccessfullySet() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.SAMPLE, "one::fil fol|two::fal");
		assertNotNull(clausesContext.getSampleMetadata());
		assertThat(clausesContext.getSampleMetadata().getType(), is(MetadataType.SAMPLE));
		assertThat(clausesContext.getSampleMetadata().getFields().size(), is(2));
		assertThat(clausesContext.getSampleMetadata().getFields().get("one"), is("fil fol"));
		assertThat(clausesContext.getSampleMetadata().getFields().get("two"), is("fal"));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

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
		assertThat(((List<Object>)clausesContext.getProcessorRequest().getRequest().get("one")).get(0), is("fil"));
		assertThat(((List<Object>)clausesContext.getProcessorRequest().getRequest().get("two")).get(0), is("fal"));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

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
		assertThat(((List<Object>)clausesContext.getProcessorRequest().getRequest().get("one")).get(0), is("fil"));
		assertThat(((List<Object>)clausesContext.getProcessorRequest().getRequest().get("two")).get(0), is("fal"));
		assertThat(clausesContext.isClauseProcessed(), is(true));
	}

	@Test
	public void templateConsumerIsRejectedIfContextNotAcceptingTemplates() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.TEMPLATE, "fil");
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.TEMPLATE, "fal"));
		expectMessageContents(e, "Templates have already been set");
	}

	@Test
	public void processorConsumerIsRejectedIfContextNotAcceptingProcessors() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil fol");
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "two::fal"));
		expectMessageContents(e, "Processors have already been set");
	}

	@Test
	public void processorConsumerIsRejectedIfContextNotAcceptingPerScanMonitors() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.PER_SCAN_MONITOR, "fil");
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.PER_SCAN_MONITOR, "fal"));
		expectMessageContents(e, "Per Scan Monitors have already been set");
	}

	@Test
	public void sampleMetadataConsumerIsRejectedIfContextNotAcceptingSampleMetadata() throws Exception {
		clausesContext.addScanDataConsumer(ScanDataConsumer.SAMPLE, "one::fil");
		var e = assertThrows(IllegalStateException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.SAMPLE, "two::fal"));
		expectMessageContents(e, "Sample metadata has already been set");
	}

	@Test
	public void processorConsumerIsRejectedIfNoSpaceBetweenDeclarations() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil::fol fel two::fal"));
		expectMessageContents(e, "Incorrect processor specification");
	}

	@Test
	public void processorConsumerIsRejectedIfNoSeparatorSequenceInDeclaration() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "onefil fel two::fal"));
		expectMessageContents(e, "No processor app specified");
	}

	@Test
	public void processorConsumerIsRejectedIfAppNameIsRepeated() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, "one::fil one::fel two::fal"));
		expectMessageContents(e, "App names may not be repeated");
	}

	@Test
	public void processorConsumerIsRejectedIfEmptyConfigStringIsSupplied() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.PROCESSOR, ""));
		expectMessageContents(e, "No processor app specified");
	}

	@Test
	public void sampleMetadataConsumerIsRejectedIfNoPipeBetweenDeclarations() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.SAMPLE, "one::fil fel two::fal"));
		expectMessageContents(e, "Incorrect sample metadata specification");
	}

	@Test
	public void sampleMetadataConsumerIsRejectedIfNoSeparatorSequenceInDeclaration() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.SAMPLE, "onefil fel|two::fal"));
		expectMessageContents(e, "Incorrect sample metadata specification");
	}

	@Test
	public void sampleMetadataConsumerIsRejectedIfEmptyConfigStringIsSupplied() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> clausesContext.addScanDataConsumer(ScanDataConsumer.SAMPLE, ""));
		expectMessageContents(e, "Incorrect sample metadata specification");
	}

	private void prepareForMutatorTest(final ClausesContext context) {
		prepareForMutatorTest(context, Scanpath.GRID_POINTS, 3, 3);
	}

	private void prepareForMutatorTest(final ClausesContext context, final Scanpath path, final Number... params) {
		Extent extent = Extent.AXIAL;

		if (path.equals(Scanpath.SINGLE_POINT)) {
			extent =  Extent.POINT;
		} else if (IBoundingBoxModel.class.isAssignableFrom(path.modelType())) {
			extent = Extent.AREA;
		} else if (IBoundingLineModel.class.isAssignableFrom(path.modelType())) {
			extent = Extent.LINEAR;
		}

		prepareForScanpathTest(context, extent);
		context.setScanpath(path);
		for (Number param : params) {
			context.addParam(param);
		}
	}

	private void prepareForScanpathTest(final ClausesContext context, final Extent extent) {
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
		prepareForRegionShapeTest(context, regionShape.getAxisCount() == 1);
		context.setRegionShape(regionShape);
	}

	private void expectMessageContents(Exception e, String... substrings) {
		for (var substr : substrings) {
			assertThat(e.getMessage(), containsString(substr));
		}
	}
}
