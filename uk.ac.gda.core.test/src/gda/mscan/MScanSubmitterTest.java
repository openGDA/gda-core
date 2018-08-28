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

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
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
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.mscan.element.AreaScanpath;
import gda.mscan.element.Mutator;
import gda.mscan.element.Roi;
import gda.mscan.processor.AreaScanpathElementProcessor;
import gda.mscan.processor.IClauseElementProcessor;
import gda.mscan.processor.MutatorElementProcessor;
import gda.mscan.processor.NumberElementProcessor;
import gda.mscan.processor.RoiElementProcessor;
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
	private ScannableGroup scannableGrp;
	@Mock
	private Detector detector;
	@Mock
	IRunnableDevice<Object> detectorRunnableDevice;
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
		when(runnableDeviceService.getRunnableDevice("Detector")).thenReturn(detectorRunnableDevice);
		when(detectorRunnableDevice.getModel()).thenReturn(detectorModel);
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
	public void builderCanMakeScannableProcessors() throws Exception {
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
			assertThat(processors.get(0).getSource(), is(scannable));
			throw e;
		}
	}

	@Test
	public void builderCanMakesScannableGroupProcessors() throws Exception {
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
			assertThat(processors.get(0).getSource(), is(scannableGrp));
			throw e;
		}
	}

	@Test
	public void builderCanMakeAreaScanpathProcessors() throws Exception {
		Object[] arr = {scannable, AreaScanpath.GRID};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getSource(), is(scannable));
			assertThat(processors.get(1), instanceOf(AreaScanpathElementProcessor.class));
			assertThat(processors.get(1).getSource(), is(AreaScanpath.GRID));
			throw e;
		}
	}

	@Test
	public void builderCanMakeRoiProcessors() throws Exception {
		Object[] arr = {scannable, Roi.CIRCLE};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getSource(), is(scannable));
			assertThat(processors.get(1), instanceOf(RoiElementProcessor.class));
			assertThat(processors.get(1).getSource(), is(Roi.CIRCLE));
			throw e;
		}
	}

	@Test
	public void builderCanMakeMutatorProcessors() throws Exception {
		Object[] arr = {scannable, Mutator.SNAKE};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getSource(), is(scannable));
			assertThat(processors.get(1), instanceOf(MutatorElementProcessor.class));
			assertThat(processors.get(1).getSource(), is(Mutator.SNAKE));
			throw e;
		}
	}

	@Test
	public void builderCanMakeNumericProcessorsFromInteger() throws Exception {
		Object[] arr = {scannable, 1};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getSource(), is(scannable));
			assertThat(processors.get(1), instanceOf(NumberElementProcessor.class));
			assertThat(processors.get(1).getSource(), is(1));
			throw e;
		}
	}

	@Test
	public void builderCanMakeNumericProcessorsFromDouble() throws Exception {
		Object[] arr = {scannable, (double)1.75};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getSource(), is(scannable));
			assertThat(processors.get(1), instanceOf(NumberElementProcessor.class));
			assertThat(processors.get(1).getSource(), is(1.75));
			throw e;
		}
	}

	@Test
	public void builderRejectsTypeWithNoMappedProcessor() throws Exception {
		Object[] arr = {scannable, "FAIL"};
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Your command contains an invalid argument at position 1");
		builder.buildAndSubmitBlockingScanRequest(arr);
	}

	@Test
	public void builderCreatesCorrectProcessorListForPlausibleScan() throws Exception {
		Object[] arr = {scannable, scannable, Roi.CIRCLE, 2, 3, 5.5, AreaScanpath.GRID, 1, 1, detector, 0.5};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(scannable),
											new RoiElementProcessor(Roi.CIRCLE),
											new NumberElementProcessor(2),
											new NumberElementProcessor(3),
											new NumberElementProcessor(5.5),
											new AreaScanpathElementProcessor(AreaScanpath.GRID),
											new NumberElementProcessor(1),
											new NumberElementProcessor(1)),
							  Arrays.asList(new ScannableElementProcessor(detector),
											new NumberElementProcessor(0.5))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(11));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getSource(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getSource(), is(scannable));
		assertThat(processors.get(2), instanceOf(RoiElementProcessor.class));
		assertThat(processors.get(2).getSource(), is(Roi.CIRCLE));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getSource(), is(2));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getSource(), is(3));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getSource(), is(5.5));
		assertThat(processors.get(6), instanceOf(AreaScanpathElementProcessor.class));
		assertThat(processors.get(6).getSource(), is(AreaScanpath.GRID));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getSource(), is(1));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getSource(), is(1));
		assertThat(processors.get(9), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(9).getSource(), is(detector));
		assertThat(processors.get(10), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(10).getSource(), is(0.5));
	}


	@Test
	public void willFailWithoutRunnableDeviceMatchingDetector() throws Exception {
		when(runnableDeviceService.getRunnableDevice("Detector")).thenReturn(null);
		Object[] arr = {scannable, scannable, Roi.CIRCLE, 2, 3, 5.5, AreaScanpath.GRID, 1, 1, detector, 0.5};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(scannable),
											new RoiElementProcessor(Roi.CIRCLE),
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

}
