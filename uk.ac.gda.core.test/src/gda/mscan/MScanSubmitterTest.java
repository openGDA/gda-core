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


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.device.Detector;
import gda.device.Monitor;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.mscan.element.AreaScanpath;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;
import gda.mscan.processor.AreaScanpathElementProcessor;
import gda.mscan.processor.IClauseElementProcessor;
import gda.mscan.processor.IRunnableDeviceDetectorElementProcessor;
import gda.mscan.processor.MutatorElementProcessor;
import gda.mscan.processor.NumberElementProcessor;
import gda.mscan.processor.RegionShapeElementProcessor;
import gda.mscan.processor.ScannableElementProcessor;
import gda.mscan.processor.ScannableGroupElementProcessor;


@RunWith(MockitoJUnitRunner.class)
public class MScanSubmitterTest {

	private MScanSubmitter builder;

	@Mock
	private ScanClausesResolver resolver;
	@Mock
	private ScannableMotor scannable;
	@Mock
	private ScannableMotor anotherScannable;
	@Mock
	private ScannableGroup scannableGrp;
	@Mock
	private Detector detector;
	@Mock
	private Monitor monitor;
	@Mock
	IRunnableDevice<Object> runnableDevice;
	@Mock
	IRunnableDevice<IDetectorModel> detectorRunnableDevice;
	@Mock
	IDetectorModel detectorModel;
	@Mock
	private IEventService eventService;
	@Mock
	private IRunnableDeviceService runnableDeviceService;
	@Mock
	private IEventConnectorService connectorService;
	@Mock
	private ISubmitter<StatusBean> submitter;
	@Mock
	private MScanSubmitter.ResolverFactory resolverFactory;
	@Captor
	private ArgumentCaptor<List<IClauseElementProcessor>> captor;
	@Rule
	public final ExpectedException exception = ExpectedException.none();


	@Before
	public void setUp() throws Exception {
		builder = new MScanSubmitter(eventService, runnableDeviceService, resolverFactory);
		when(eventService.getEventConnectorService()).thenReturn(connectorService);
		when(connectorService.marshal(any())).thenReturn("");
		when(eventService.createSubmitter(any(), any())).thenReturn(submitter);

		when(resolverFactory.getResolver(captor.capture())).thenReturn(resolver);
		when(detector.getName()).thenReturn("Detector");
		when(runnableDeviceService.getRunnableDevice("Detector")).thenReturn(runnableDevice);
		when(runnableDevice.getModel()).thenReturn(detectorModel);
		when(detectorRunnableDevice.getModel()).thenReturn(detectorModel);
		when(detectorRunnableDevice.getRole()).thenReturn(DeviceRole.HARDWARE);
	}

	@Test
	public void scanArrayCannotBeNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("The scan request array is null");
		builder.buildAndSubmitBlockingScanRequest(null);
	}

	@Test
	public void scanArrayCannotBeEmpty() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(startsWith("You must specify at least one argument in your mscan command"));
		builder.buildAndSubmitBlockingScanRequest(new Object[0]);
	}

	@Test
	public void scanArrayMustStartWithAScannable() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(startsWith("You must specify at least one argument in your mscan command"));
		Object[] arr = {1};
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void rejectsEmptyResponseFromClauseResolver() throws Exception {
		Object[] arr = {scannable};
		when(resolver.resolveScanClauses()).thenReturn(Arrays.asList());
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("clause resolution returned an empty or invalid list of processors by clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void rejectsResponseContainingNullEntriesFromClauseResolver() throws Exception {
		Object[] arr = {scannable};
		List<List<IClauseElementProcessor>> ret = new ArrayList<>();
		ret.add(null);
		when(resolver.resolveScanClauses()).thenReturn(ret);
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("clause resolution returned an empty or invalid list of processors by clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void rejectsResponseWithEmptyClauseFromClauseResolver() throws Exception {
		Object[] arr = {scannable};
		when(resolver.resolveScanClauses()).thenReturn(Arrays.asList(Arrays.asList()));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("clause resolution returned an empty or invalid processor list for a clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void rejectsResponseWithClauseContainingNullFromClauseResolver() throws Exception {
		Object[] arr = {scannable};
		List<IClauseElementProcessor> ret = new ArrayList<>();
		ret.add(null);
		when(resolver.resolveScanClauses()).thenReturn(Arrays.asList(ret));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("clause resolution returned an empty or invalid processor list for a clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void firstElementCannotCurrentlyBeADetector() throws Exception {
		Object[] arr = {detector};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(detector))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined - SPEC style scans not yet supported");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void firstElementCannotCurrentlyBeAMonitor() throws Exception {
		Object[] arr = {monitor};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(monitor))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined - SPEC style scans not yet supported");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void canMakeScannableProcessors() throws Exception {
		Object[] arr = {scannable};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined - SPEC style scans not yet supported");
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(1));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			throw e;
		}
	}

	@Test
	public void canMakesScannableGroupProcessors() throws Exception {
		Object[] arr = {scannableGrp};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannableGrp))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined - SPEC style scans not yet supported");
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(1));
			assertThat(processors.get(0), instanceOf(ScannableGroupElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannableGrp));
			throw e;
		}
	}

	@Test
	public void canMakeRunnableDeviceProcessors() throws Exception {
		Object[] arr = {scannable, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable)),
						Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined - SPEC style scans not yet supported");
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(1), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(detectorRunnableDevice));
			throw e;
		}
	}


	@Test
	public void canMakeAreaScanpathProcessors() throws Exception {
		Object[] arr = {scannable, AreaScanpath.GRID};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(AreaScanpathElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(AreaScanpath.GRID));
			throw e;
		}
	}

	@Test
	public void canMakeRoiProcessors() throws Exception {
		Object[] arr = {scannable, RegionShape.CIRCLE};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(RegionShape.CIRCLE));
			throw e;
		}
	}

	@Test
	public void canMakeMutatorProcessors() throws Exception {
		Object[] arr = {scannable, Mutator.ALTERNATING};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(MutatorElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(Mutator.ALTERNATING));
			throw e;
		}
	}

	@Test
	public void canMakeNumericProcessorsFromInteger() throws Exception {
		Object[] arr = {scannable, 1};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(NumberElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(1));
			throw e;
		}
	}

	@Test
	public void canMakeNumericProcessorsFromDouble() throws Exception {
		Object[] arr = {scannable, (double)1.75};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(NumberElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(1.75));
			throw e;
		}
	}

	@Test
	public void rejectsTypeWithNoMappedProcessor() throws Exception {
		Object[] arr = {scannable, "FAIL"};
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Your command contains an invalid argument at position 1");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void createsCorrectProcessorListForPlausibleScan() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, 2, 3, 5.5, AreaScanpath.GRID, 1, 1,
										detector,monitor, detectorRunnableDevice, 0.5, scannable};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new NumberElementProcessor(5.5),
											new AreaScanpathElementProcessor(AreaScanpath.GRID),
											new NumberElementProcessor(1),
											new NumberElementProcessor(1)),
							  Arrays.asList(new ScannableElementProcessor(detector)),
							  Arrays.asList(new ScannableElementProcessor(monitor)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice),
											new NumberElementProcessor(0.5)),
							  Arrays.asList(new ScannableElementProcessor(scannable))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(14));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(anotherScannable));
		assertThat(processors.get(2), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(RegionShape.CIRCLE));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(2));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(3));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(5.5));
		assertThat(processors.get(6), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(AreaScanpath.GRID));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(1));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(1));
		assertThat(processors.get(9), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(9).getElement(), is(detector));
		assertThat(processors.get(10), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(10).getElement(), is(monitor));
		assertThat(processors.get(11), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(11).getElement(), is(detectorRunnableDevice));
		assertThat(processors.get(12), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(12).getElement(), is(0.5));
		assertThat(processors.get(13), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(13).getElement(), is(scannable));
	}

	@Test
	public void createsCorrectProcessorListForPointScanWithShorthandSyntax() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.POINT, 2, 3, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.POINT),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new AreaScanpathElementProcessor(AreaScanpath.SINGLE_POINT),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(9));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(anotherScannable));
		assertThat(processors.get(2), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(RegionShape.POINT));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(2));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(3));
		assertThat(processors.get(5), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(AreaScanpath.SINGLE_POINT));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(2));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(3));
		assertThat(processors.get(8), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(detectorRunnableDevice));
	}

	@Test
	public void createsCorrectProcessorListForPointScanWithFullSyntax() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.POINT, 2, 3, RegionShape.POINT, 2, 3, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.POINT),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new AreaScanpathElementProcessor(AreaScanpath.SINGLE_POINT),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(9));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(anotherScannable));
		assertThat(processors.get(2), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(RegionShape.POINT));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(2));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(3));
		assertThat(processors.get(5), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(AreaScanpath.SINGLE_POINT));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(2));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(3));
		assertThat(processors.get(8), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(detectorRunnableDevice));
	}

	@Test
	public void createsCorrectProcessorListForAxialPointsScanWithFullSyntax() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, 2, 3, AreaScanpath.ONE_AXIS_NO_OF_POINTS, 20, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new AreaScanpathElementProcessor(AreaScanpath.ONE_AXIS_NO_OF_POINTS),
											new NumberElementProcessor(20)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(2));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(3));
		assertThat(processors.get(4), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(AreaScanpath.ONE_AXIS_NO_OF_POINTS));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(20));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
	}

	@Test
	public void createsCorrectProcessorListForAxialPointsScanWithAliasedSyntax() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, 2, 3, AreaScanpath.TWO_AXIS_NO_OF_POINTS, 20, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new AreaScanpathElementProcessor(AreaScanpath.ONE_AXIS_NO_OF_POINTS),
											new NumberElementProcessor(20)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(2));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(3));
		assertThat(processors.get(4), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(AreaScanpath.ONE_AXIS_NO_OF_POINTS));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(20));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
	}

	@Test
	public void createsCorrectProcessorListForAxialStepScanWithFullSyntax() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, 2, 3, AreaScanpath.ONE_AXIS_STEP, 0.2, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new AreaScanpathElementProcessor(AreaScanpath.ONE_AXIS_STEP),
											new NumberElementProcessor(0.2)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(2));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(3));
		assertThat(processors.get(4), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(AreaScanpath.ONE_AXIS_STEP));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(0.2));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
	}

	@Test
	public void createsCorrectProcessorListForAxialStepScanWithAliasedSyntax() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, 2, 3, AreaScanpath.TWO_AXIS_STEP, 0.2, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new AreaScanpathElementProcessor(AreaScanpath.ONE_AXIS_STEP),
											new NumberElementProcessor(0.2)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(2));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(3));
		assertThat(processors.get(4), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(AreaScanpath.ONE_AXIS_STEP));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(0.2));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
	}

	@Test
	public void willFailWithoutRunnableDeviceMatchingDetector() throws Exception {
		when(runnableDeviceService.getRunnableDevice("Detector")).thenReturn(null);
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, 2, 3, 5.5, AreaScanpath.GRID, 1, 1, detector, 0.5};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new NumberElementProcessor(5.5),
											new AreaScanpathElementProcessor(AreaScanpath.GRID),
											new NumberElementProcessor(1),
											new NumberElementProcessor(1)),
							  Arrays.asList(new ScannableElementProcessor(detector),
											new NumberElementProcessor(0.5))));
		exception.expect(ScanningException.class);
		exception.expectMessage(startsWith("Could not get detector"));
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void willFailWithTooManyDetectorParameters() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, 2, 3, 5.5, AreaScanpath.GRID, 1, 1, detector, 0.5, 1.7};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new NumberElementProcessor(5.5),
											new AreaScanpathElementProcessor(AreaScanpath.GRID),
											new NumberElementProcessor(1),
											new NumberElementProcessor(1)),
							  Arrays.asList(new ScannableElementProcessor(detector),
											new NumberElementProcessor(0.5),
											new NumberElementProcessor(1.7))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("too many elements in Detector clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void willFailWithWrongTypeOfDetectorParameters() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, 2, 3, 5.5, AreaScanpath.GRID, 1, 1, detector, AreaScanpath.GRID};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new NumberElementProcessor(5.5),
											new AreaScanpathElementProcessor(AreaScanpath.GRID),
											new NumberElementProcessor(1),
											new NumberElementProcessor(1)),
							  Arrays.asList(new ScannableElementProcessor(detector),
									  new AreaScanpathElementProcessor(AreaScanpath.GRID))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("2nd element of unexpected type in Detector clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void willFailWithMonitorParameters() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, 2, 3, 5.5, AreaScanpath.GRID, 1, 1, monitor, 0.5};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new NumberElementProcessor(5.5),
											new AreaScanpathElementProcessor(AreaScanpath.GRID),
											new NumberElementProcessor(1),
											new NumberElementProcessor(1)),
							  Arrays.asList(new ScannableElementProcessor(monitor),
											new NumberElementProcessor(0.5))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("too many elements in Monitor clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}
}
