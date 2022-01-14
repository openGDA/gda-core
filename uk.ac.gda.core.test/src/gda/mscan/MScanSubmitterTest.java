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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.AbstractBoundingBoxModel;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.sequencer.ScanRequestBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.python.core.PyList;
import org.python.core.PyTuple;

import gda.device.Detector;
import gda.device.Monitor;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.jython.JythonServerFacade;
import gda.mscan.element.Action;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;
import gda.mscan.element.ScanDataConsumer;
import gda.mscan.element.Scanpath;
import gda.mscan.processor.IClauseElementProcessor;
import gda.mscan.processor.IRunnableDeviceDetectorElementProcessor;
import gda.mscan.processor.MutatorElementProcessor;
import gda.mscan.processor.NumberElementProcessor;
import gda.mscan.processor.ReRunFromFileElementProcessor;
import gda.mscan.processor.RegionShapeElementProcessor;
import gda.mscan.processor.ScanDataConsumerElementProcessor;
import gda.mscan.processor.ScannableDetectorElementProcessor;
import gda.mscan.processor.ScannableElementProcessor;
import gda.mscan.processor.ScannableGroupElementProcessor;
import gda.mscan.processor.ScannableMonitorElementProcessor;
import gda.mscan.processor.ScanpathElementProcessor;
import gda.mscan.processor.TokenStringElementProcessor;


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
	private ISubscriber<EventListener> eventSubscriber;
	@Mock
	private IRunnableDeviceService runnableDeviceService;
	@Mock
	private IEventConnectorService connectorService;
	@Mock
	private ISubmitter<StatusBean> submitter;
	@Mock
	private MScanSubmitter.ResolverFactory resolverFactory;
	@Mock
	private JythonServerFacade theFacadeInstance;
	@Captor
	private ArgumentCaptor<List<IClauseElementProcessor>> captor;
	@Captor
	private ArgumentCaptor<ScanBean> beanCaptor;
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private RectangularROI rectangle;
	private CircularROI circle;
	private LinearROI line;
	private PointROI pointRoi;
	private TwoAxisGridPointsModel grid;
	private TwoAxisGridStepModel steppedGrid;
	private TwoAxisPointSingleModel point;
	private TwoAxisLinePointsModel liPoints;
	private TwoAxisLineStepModel liStep;
	private AxialPointsModel axPoints;
	private AxialStepModel axStep;
	private AxialArrayModel axArray;

	private double rectangleX2;
	private double rectangleY2;
	private double positions[] = {2.1, 3.0, 2.0, 2.4};

	private static final String TEST_FILE = "testfiles/gda/mscan/processor/fake.nxs";
	private static final String DETECTOR_NAME = "Detector";
	private static final double EXPOSURE = 0.5;
	private static final int X = 0;
	private static final int Y = 1;

	private CompoundModel getBoundedCompoundModel(AbstractBoundingBoxModel model, IROI roi) {
		IRectangularROI boundingRoi = roi.getBounds();
		model.setBoundingBox(new BoundingBox(boundingRoi.getPointX(), boundingRoi.getPointY(), boundingRoi.getLength(X), boundingRoi.getLength(Y)));
		return getCompoundModel(model, roi);
	}

	private CompoundModel getCompoundModel(IScanPointGeneratorModel model, IROI roi) {
		return new CompoundModel(model, roi);
	}

	@Before
	public void setUp() throws Exception {
		try(final MockedStatic<JythonServerFacade> facade = mockStatic(JythonServerFacade.class)) {
			facade.when(JythonServerFacade::getInstance).thenReturn(theFacadeInstance);

			builder = new MScanSubmitter(eventService, runnableDeviceService, resolverFactory);
			when(eventService.getEventConnectorService()).thenReturn(connectorService);
			when(connectorService.marshal(any())).thenReturn("");
			when(eventService.createSubmitter(any(), any())).thenReturn(submitter);
			when (eventService.createSubscriber(any(), anyString())).thenReturn(eventSubscriber);

			when(resolverFactory.getResolver(captor.capture())).thenReturn(resolver);
			when(detector.getName()).thenReturn(DETECTOR_NAME);
			when(runnableDeviceService.getRunnableDevice(DETECTOR_NAME)).thenReturn(runnableDevice);
			when(runnableDevice.getModel()).thenReturn(detectorModel);
			when(detectorRunnableDevice.getModel()).thenReturn(detectorModel);
			when(monitor.getName()).thenReturn("monitor");
			when(scannable.getName()).thenReturn("stage_x");
			when (scannable.getUserUnits()).thenReturn("mm");
			when(anotherScannable.getName()).thenReturn("stage_y");
			when (anotherScannable.getUserUnits()).thenReturn("mm");

			point = new TwoAxisPointSingleModel();
			point.setX(2.0);
			point.setY(3.0);
			pointRoi = new PointROI(point.getX(), point.getY());

			axPoints = new AxialPointsModel(scannable.getName(), 2, 3, 20);
			axPoints.setContinuous(false);
			axStep = new AxialStepModel(scannable.getName(), 2, 3, 0.2);
			axStep.setContinuous(false);
			axArray = new AxialArrayModel(scannable.getName(), positions);
			axArray.setContinuous(false);
			liPoints = new TwoAxisLinePointsModel();
			liPoints.setPoints(20);
			liStep = new TwoAxisLineStepModel();
			liStep.setStep(0.2);
			line = new LinearROI(new double[]{2, 2}, new double[] {3, 3});

			grid = new TwoAxisGridPointsModel(scannable.getName(), anotherScannable.getName(), 20, 10);
			grid.setContinuous(false);
			steppedGrid = new TwoAxisGridStepModel();
			steppedGrid.setxAxisStep(0.1);
			steppedGrid.setyAxisStep(0.2);
			steppedGrid.setContinuous(false);

			rectangle = new RectangularROI(2, 3, 4, 5, 0);
			rectangleX2 = rectangle.getPointX() + rectangle.getLength(X);
			rectangleY2 = rectangle.getPointY() + rectangle.getLength(Y);
			circle = new CircularROI(5.5, 2, 3);
		}
	}

	@Test
	public void scanArrayCannotBeNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("The scan request array is null");
		builder.buildAndSubmitBlockingScanRequest(null);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void scanArrayCannotBeEmpty() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(startsWith("You must specify at least one argument in your mscan command"));
		builder.buildAndSubmitBlockingScanRequest(new Object[0]);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void scanArrayMustStartWithAScannable() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(startsWith("You must specify at least one argument in your mscan command"));
		Object[] arr = {1};
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void rejectsEmptyResponseFromClauseResolver() throws Exception {
		Object[] arr = {scannable};
		when(resolver.resolveScanClauses()).thenReturn(Arrays.asList());
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("clause resolution returned an empty or invalid list of processors by clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
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
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void rejectsResponseWithEmptyClauseFromClauseResolver() throws Exception {
		Object[] arr = {scannable};
		when(resolver.resolveScanClauses()).thenReturn(Arrays.asList(Arrays.asList()));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("clause resolution returned an empty or invalid processor list for a clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
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
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void firstElementCannotCurrentlyBeADetector() throws Exception {
		Object[] arr = {detector};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableDetectorElementProcessor(detector))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined - SPEC style scans not yet supported");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void firstElementCannotCurrentlyBeAMonitor() throws Exception {
		Object[] arr = {monitor};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableMonitorElementProcessor(monitor))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined - SPEC style scans not yet supported");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
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
	public void canMakeScannableGroupProcessors() throws Exception {
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
	public void canMakeScanpathProcessors() throws Exception {
		Object[] arr = {scannable, Scanpath.GRID_POINTS};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(ScanpathElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(Scanpath.GRID_POINTS));
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
	public void canMakeScanDataConsumerProcessors() throws Exception {
		Object[] arr = {scannable, ScanDataConsumer.TEMPLATE};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(ScanDataConsumerElementProcessor.class));
			assertThat(processors.get(1).getElement(), is(ScanDataConsumer.TEMPLATE));
			throw e;
		}
	}

	@Test
	public void canMakeTokenStringProcessors() throws Exception {
		Object[] arr = {scannable, "1 2 3 4"};
		when(resolver.resolveScanClauses()).thenThrow(new IllegalArgumentException());
		exception.expect(IllegalArgumentException.class);
		try {
			builder.buildAndSubmitBlockingScanRequest(arr);
		} catch (Exception e) {
			List<IClauseElementProcessor> processors = captor.getValue();
			assertThat(processors.size(), is(2));
			assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
			assertThat(processors.get(0).getElement(), is(scannable));
			assertThat(processors.get(1), instanceOf(TokenStringElementProcessor.class));
			assertThat(processors.get(1).getElement(), is("1 2 3 4"));
			throw e;
		}
	}

	@Test
	public void rejectsTypeWithNoMappedProcessor() throws Exception {
		Object[] arr = {scannable, new ScanningException()};
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Your command contains an invalid argument at position 1");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void createsCorrectProcessorListForPlausibleScan() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
										Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(),
										detector, monitor, detectorRunnableDevice, EXPOSURE, scannable,
										ScanDataConsumer.TEMPLATE, "template1 template2"};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableDetectorElementProcessor(detector)),
							  Arrays.asList(new ScannableMonitorElementProcessor(monitor)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice),
											new NumberElementProcessor(EXPOSURE)),
							  Arrays.asList(new ScannableElementProcessor(scannable)),
							  Arrays.asList(new ScanDataConsumerElementProcessor(ScanDataConsumer.TEMPLATE),
									  		new TokenStringElementProcessor("template1 template2"))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(16));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(anotherScannable));
		assertThat(processors.get(2), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(RegionShape.CIRCLE));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(circle.getPointX()));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(circle.getPointY()));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(circle.getRadius()));
		assertThat(processors.get(6), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(Scanpath.GRID_POINTS));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(grid.getxAxisPoints()));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(grid.getyAxisPoints()));
		assertThat(processors.get(9), instanceOf(ScannableDetectorElementProcessor.class));
		assertThat(processors.get(9).getElement(), is(detector));
		assertThat(processors.get(10), instanceOf(ScannableMonitorElementProcessor.class));
		assertThat(processors.get(10).getElement(), is(monitor));
		assertThat(processors.get(11), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(11).getElement(), is(detectorRunnableDevice));
		assertThat(processors.get(12), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(12).getElement(), is(EXPOSURE));
		assertThat(processors.get(13), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(13).getElement(), is(scannable));
		assertThat(processors.get(14), instanceOf(ScanDataConsumerElementProcessor.class));
		assertThat(processors.get(14).getElement(), is(ScanDataConsumer.TEMPLATE));
		assertThat(processors.get(15), instanceOf(TokenStringElementProcessor.class));
		assertThat(processors.get(15).getElement(), is("template1 template2"));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getBoundedCompoundModel(grid, circle))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForPointScanWithShorthandSyntax() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.POINT, point.getX(), point.getY(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.POINT),
											new NumberElementProcessor(point.getX()),
											new NumberElementProcessor(point.getY()),
											new ScanpathElementProcessor(Scanpath.SINGLE_POINT),
											new NumberElementProcessor(point.getX()),
											new NumberElementProcessor(point.getY())),
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
		assertThat(processors.get(3).getElement(), is(point.getX()));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(point.getY()));
		assertThat(processors.get(5), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(Scanpath.SINGLE_POINT));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(point.getX()));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(point.getY()));
		assertThat(processors.get(8), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(point, pointRoi))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForPointScanWithFullSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.POINT, point.getX(), point.getY(),
								RegionShape.POINT, point.getX(), point.getY(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.POINT),
											new NumberElementProcessor(point.getX()),
											new NumberElementProcessor(point.getY()),
											new ScanpathElementProcessor(Scanpath.SINGLE_POINT),
											new NumberElementProcessor(point.getX()),
											new NumberElementProcessor(point.getY())),
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
		assertThat(processors.get(3).getElement(), is(point.getX()));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(point.getY()));
		assertThat(processors.get(5), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(Scanpath.SINGLE_POINT));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(point.getX()));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(point.getY()));
		assertThat(processors.get(8), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(point, pointRoi))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForAxialPointsScanWithFullSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, line.getPointX(), line.getIntEndPoint()[0],
				Scanpath.AXIS_POINTS, axPoints.getPoints(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(line.getPointX()),
											new NumberElementProcessor(line.getIntEndPoint()[0]),
											new ScanpathElementProcessor(Scanpath.AXIS_POINTS),
											new NumberElementProcessor(axPoints.getPoints())),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(line.getPointX()));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(line.getIntEndPoint()[0]));
		assertThat(processors.get(4), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(Scanpath.AXIS_POINTS));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(axPoints.getPoints()));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(axPoints, line))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForAxialPointsScanWithAliasedSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, line.getPointX(), line.getIntEndPoint()[0],
				Scanpath.LINE_POINTS, liPoints.getPoints(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(line.getPointX()),
											new NumberElementProcessor(line.getIntEndPoint()[0]),
											new ScanpathElementProcessor(Scanpath.AXIS_POINTS),
											new NumberElementProcessor(liPoints.getPoints())),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(line.getPointX()));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(line.getIntEndPoint()[0]));
		assertThat(processors.get(4), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(Scanpath.AXIS_POINTS));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(liPoints.getPoints()));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(axPoints, line))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForAxialStepScanWithFullSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, line.getPointX(), line.getIntEndPoint()[0],
				Scanpath.AXIS_STEP, axStep.getStep(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(line.getPointX()),
											new NumberElementProcessor(line.getIntEndPoint()[0]),
											new ScanpathElementProcessor(Scanpath.AXIS_STEP),
											new NumberElementProcessor(axStep.getStep())),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(line.getPointX()));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(line.getIntEndPoint()[0]));
		assertThat(processors.get(4), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(Scanpath.AXIS_STEP));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(axStep.getStep()));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(axStep, line))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForAxialStepScanWithAliasedSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, line.getPointX(), line.getIntEndPoint()[0],
				Scanpath.LINE_STEP, liStep.getStep(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(line.getPointX()),
											new NumberElementProcessor(line.getIntEndPoint()[0]),
											new ScanpathElementProcessor(Scanpath.AXIS_STEP),
											new NumberElementProcessor(liStep.getStep())),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(7));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(line.getPointX()));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(line.getIntEndPoint()[0]));
		assertThat(processors.get(4), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(Scanpath.AXIS_STEP));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(liStep.getStep()));
		assertThat(processors.get(6), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(axStep, line))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForAxialArrayScanWithShorthandSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, Scanpath.AXIS_ARRAY, positions[0], positions[1], positions[2], positions[3],
				detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(positions[2]),
											new NumberElementProcessor(positions[1]),
											new ScanpathElementProcessor(Scanpath.AXIS_ARRAY),
											new NumberElementProcessor(positions[0]),
											new NumberElementProcessor(positions[1]),
											new NumberElementProcessor(positions[2]),
											new NumberElementProcessor(positions[3])),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(10));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(positions[2]));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(positions[1]));
		assertThat(processors.get(4), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(Scanpath.AXIS_ARRAY));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(positions[0]));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(positions[1]));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(positions[2]));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(positions[3]));
		assertThat(processors.get(9), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(9).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(axArray, line))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForAxialArrayScanWithFullSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, RegionShape.AXIAL, positions[2], positions[1], Scanpath.AXIS_ARRAY, positions[0], positions[1], positions[2], positions[3],
				detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(positions[2]),
											new NumberElementProcessor(positions[1]),
											new ScanpathElementProcessor(Scanpath.AXIS_ARRAY),
											new NumberElementProcessor(positions[0]),
											new NumberElementProcessor(positions[1]),
											new NumberElementProcessor(positions[2]),
											new NumberElementProcessor(positions[3])),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(10));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(RegionShape.AXIAL));
		assertThat(processors.get(2), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(positions[2]));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(positions[1]));
		assertThat(processors.get(4), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(Scanpath.AXIS_ARRAY));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(positions[0]));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(positions[1]));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(positions[2]));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(positions[3]));
		assertThat(processors.get(9), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(9).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getCompoundModel(axArray, line))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForRectangularPointsScanWithAliasedSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, anotherScannable,
				RegionShape.RECTANGLE, rectangle.getPointX(), rectangle.getPointY(), rectangleX2, rectangleY2,
				Scanpath.LINE_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.RECTANGLE),
											new NumberElementProcessor(rectangle.getPointX()),
											new NumberElementProcessor(rectangle.getPointY()),
											new NumberElementProcessor(rectangleX2),
											new NumberElementProcessor(rectangleY2),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(11));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(anotherScannable));
		assertThat(processors.get(2), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(RegionShape.RECTANGLE));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(rectangle.getPointX()));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(rectangle.getPointY()));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(rectangleX2));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(rectangleY2));
		assertThat(processors.get(7), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(Scanpath.GRID_POINTS));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(grid.getxAxisPoints()));
		assertThat(processors.get(9), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(9).getElement(), is(grid.getyAxisPoints()));
		assertThat(processors.get(10), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(10).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getBoundedCompoundModel(grid, rectangle))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForRectangularPointsScanWithAliasedSyntaxWithTuplesOrListsAndSubmits() throws Exception {
		// This is equivalent to typing:
		// mscan scannable, anotherScannable, rect ((2,3) [6,8]) line [20,10] detectorRunnableDevice
		PyTuple t1 = PyTuple.fromIterable(new PyList(Arrays.asList(rectangle.getPointX(), rectangle.getPointY())));
		PyList t2 = new PyList(Arrays.asList(rectangleX2, rectangleY2));
		PyTuple t3 = PyTuple.fromIterable(new PyList(Arrays.asList(t1, t2)));
		PyList t4 = new PyList(Arrays.asList(grid.getxAxisPoints(), grid.getyAxisPoints()));

		Object[] arr = {scannable, anotherScannable,
				RegionShape.RECTANGLE, t3, Scanpath.LINE_POINTS, t4, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.RECTANGLE),
											new NumberElementProcessor(rectangle.getPointX()),
											new NumberElementProcessor(rectangle.getPointY()),
											new NumberElementProcessor(rectangleX2),
											new NumberElementProcessor(rectangleY2),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(11));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(anotherScannable));
		assertThat(processors.get(2), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(RegionShape.RECTANGLE));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(rectangle.getPointX()));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(rectangle.getPointY()));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(rectangleX2));
		assertThat(processors.get(6), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(rectangleY2));
		assertThat(processors.get(7), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(Scanpath.GRID_POINTS));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(grid.getxAxisPoints()));
		assertThat(processors.get(9), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(9).getElement(), is(grid.getyAxisPoints()));
		assertThat(processors.get(10), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(10).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getBoundedCompoundModel(grid, rectangle))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForCircularStepScanWithAliasedSyntaxAndSubmits() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
				Scanpath.LINE_STEP, steppedGrid.getxAxisStep(), steppedGrid.getyAxisStep(), detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_STEP),
											new NumberElementProcessor(steppedGrid.getxAxisStep()),
											new NumberElementProcessor(steppedGrid.getyAxisStep())),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(10));
		assertThat(processors.get(0), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(scannable));
		assertThat(processors.get(1), instanceOf(ScannableElementProcessor.class));
		assertThat(processors.get(1).getElement(), is(anotherScannable));
		assertThat(processors.get(2), instanceOf(RegionShapeElementProcessor.class));
		assertThat(processors.get(2).getElement(), is(RegionShape.CIRCLE));
		assertThat(processors.get(3), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(3).getElement(), is(circle.getPointX()));
		assertThat(processors.get(4), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(4).getElement(), is(circle.getPointY()));
		assertThat(processors.get(5), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(5).getElement(), is(circle.getRadius()));
		assertThat(processors.get(6), instanceOf(ScanpathElementProcessor.class));
		assertThat(processors.get(6).getElement(), is(Scanpath.GRID_STEP));
		assertThat(processors.get(7), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(7).getElement(), is(steppedGrid.getxAxisStep()));
		assertThat(processors.get(8), instanceOf(NumberElementProcessor.class));
		assertThat(processors.get(8).getElement(), is(steppedGrid.getyAxisStep()));
		assertThat(processors.get(9), instanceOf(IRunnableDeviceDetectorElementProcessor.class));
		assertThat(processors.get(9).getElement(), is(detectorRunnableDevice));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getBoundedCompoundModel(steppedGrid, circle))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void createsCorrectProcessorListForReRunFromFileAndSubmits() throws Exception {
		Object[] arr = {Action.RERUN, TEST_FILE};
		doNothing().when(theFacadeInstance).runCommand(anyString());
		try(final MockedStatic<ScanRequestBuilder> scanBuilder = mockStatic(ScanRequestBuilder.class)) {
			ScanRequest request = new ScanRequest();
			request.setCompoundModel(getCompoundModel(grid, circle));
			scanBuilder.when(() -> ScanRequestBuilder.buildFromNexusFile(TEST_FILE)).thenReturn(Optional.of(request));
			builder.buildAndSubmitBlockingScanRequest(arr);
		}
		List<IClauseElementProcessor> processors = captor.getValue();
		assertThat(processors.size(), is(1));
		assertThat(processors.get(0), instanceOf(ReRunFromFileElementProcessor.class));
		assertThat(processors.get(0).getElement(), is(TEST_FILE));
		assertThat(processors.get(0).getElementValue(), is(TEST_FILE));
		verify(eventSubscriber).addListener(any(IScanListener.class));
		verify(submitter).blockingSubmit(beanCaptor.capture());
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(getBoundedCompoundModel(grid, circle))));
	}

	@Test
	public void willFailWithNonNumericTuplesOrLists() throws Exception {
		// This is equivalent to typing:
		// mscan scannable, anotherScannable, (rect (2,3) [6,8]) line [20,10] detectorRunnableDevice
		PyTuple t1 = PyTuple.fromIterable(new PyList(Arrays.asList(rectangle.getPointX(), rectangle.getPointY())));
		PyList t2 = new PyList(Arrays.asList(rectangleX2, rectangleY2));
		PyTuple t3 = PyTuple.fromIterable(new PyList(Arrays.asList(RegionShape.RECTANGLE, t1, t2)));
		PyList t4 = new PyList(Arrays.asList(grid.getxAxisPoints(), grid.getyAxisPoints()));

		Object[] arr = {scannable, anotherScannable,
				t3, Scanpath.LINE_POINTS, t4, detectorRunnableDevice};
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Only numeric parameters can be enclosed in brackets");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailWithoutRunnableDeviceMatchingDetector() throws Exception {
		when(runnableDeviceService.getRunnableDevice(DETECTOR_NAME)).thenReturn(null);
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
				Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), detector, EXPOSURE};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableDetectorElementProcessor(detector),
											new NumberElementProcessor(EXPOSURE))));
		exception.expect(ScanningException.class);
		exception.expectMessage(startsWith("Could not get detector"));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailWithTooManyDetectorParameters() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
													Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), detector, EXPOSURE, 1.7};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableDetectorElementProcessor(detector),
											new NumberElementProcessor(EXPOSURE),
											new NumberElementProcessor(1.7))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("too many elements in Detector clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailWithWrongTypeOfDetectorParameters() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
				Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), detector, Scanpath.GRID_POINTS};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableDetectorElementProcessor(detector),
									  new ScanpathElementProcessor(Scanpath.GRID_POINTS))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("2nd element of unexpected type in Detector clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailWithMonitorParameters() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
													Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), monitor, EXPOSURE};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableMonitorElementProcessor(monitor),
											new NumberElementProcessor(EXPOSURE))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("too many elements in Monitor clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailWithTooFewScanDataConsumerParametersInAClause() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
													Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), detector, EXPOSURE,
													ScanDataConsumer.TEMPLATE};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableDetectorElementProcessor(detector),
											new NumberElementProcessor(EXPOSURE)),
							  Arrays.asList(new ScanDataConsumerElementProcessor(ScanDataConsumer.TEMPLATE))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Incorrect number of parameters for ScanDataConsumer, must be 1");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailWithTooManyScanDataConsumerParametersInAClause() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
													Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), detector, EXPOSURE,
													ScanDataConsumer.TEMPLATE, "tempa", "tempb"};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableDetectorElementProcessor(detector),
											new NumberElementProcessor(EXPOSURE)),
							  Arrays.asList(new ScanDataConsumerElementProcessor(ScanDataConsumer.TEMPLATE),
								  			new TokenStringElementProcessor("tempa"),
								  			new TokenStringElementProcessor("tempb"))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Incorrect number of parameters for ScanDataConsumer, must be 1");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailWithWrongTypeOfScanDataConsumerParameters() throws Exception {
		Object[] arr = {scannable, anotherScannable, RegionShape.CIRCLE, circle.getPointX(), circle.getPointY(), circle.getRadius(),
													Scanpath.GRID_POINTS, grid.getxAxisPoints(), grid.getyAxisPoints(), detector, EXPOSURE,
													ScanDataConsumer.PROCESSOR, Scanpath.GRID_POINTS};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new ScannableElementProcessor(anotherScannable),
											new RegionShapeElementProcessor(RegionShape.CIRCLE),
											new NumberElementProcessor(circle.getPointX()),
											new NumberElementProcessor(circle.getPointY()),
											new NumberElementProcessor(circle.getRadius()),
											new ScanpathElementProcessor(Scanpath.GRID_POINTS),
											new NumberElementProcessor(grid.getxAxisPoints()),
											new NumberElementProcessor(grid.getyAxisPoints())),
							  Arrays.asList(new ScannableDetectorElementProcessor(detector),
											new NumberElementProcessor(EXPOSURE)),
							  Arrays.asList(new ScanDataConsumerElementProcessor(ScanDataConsumer.PROCESSOR),
									  		new ScanpathElementProcessor(Scanpath.GRID_POINTS))));
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("2nd element of unexpected type in Scan Consumer clause");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}


	@Test
	public void willAllowStaticScanWithDetectorAndNoScannable() throws Exception{
		final int size = 5;

		Object[] arr = {Scanpath.STATIC, size, detectorRunnableDevice, EXPOSURE};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScanpathElementProcessor(Scanpath.STATIC),
						new NumberElementProcessor(size)),
				Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice),
						new NumberElementProcessor(EXPOSURE))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(new CompoundModel(new StaticModel(size)))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));

	}

	@Test
	public void willAllowStaticScanWithDefaultSize() throws Exception{

		// > 1 exposure time to ensure number not stolen by previous clause
		Object[] arr = {Scanpath.STATIC, detectorRunnableDevice, 2.7};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScanpathElementProcessor(Scanpath.STATIC)),
				Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice),
						new NumberElementProcessor(1.7))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(new CompoundModel(new StaticModel()))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void willAllowStaticScanWithDefaultSizeAndNoDetectorParams() throws Exception{

		Object[] arr = {Scanpath.STATIC, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScanpathElementProcessor(Scanpath.STATIC)),
				Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(new CompoundModel(new StaticModel()))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void willAllowStaticScanWithOnlyDetector() throws Exception{

		Object[] arr = {detector};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScanpathElementProcessor(Scanpath.STATIC)),
						Arrays.asList(new ScannableDetectorElementProcessor(detector))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(new CompoundModel(new StaticModel()))));
	}

	@Test
	public void willAllowStaticScanWithOnlyDetectorAndParams() throws Exception{

		Object[] arr = {Scanpath.STATIC, detectorRunnableDevice, 2.5};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScanpathElementProcessor(Scanpath.STATIC)),
				Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice),
						new NumberElementProcessor(2.5))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(new CompoundModel(new StaticModel()))));
		assertThat(bean.getScanRequest().getDetectors().values(), contains(detectorRunnableDevice.getModel()));
	}

	@Test
	public void willNotAllowStaticScanWithoutDetectors() throws Exception{

		// No Size, so default size=1
		Object[] arr = {Scanpath.STATIC};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScanpathElementProcessor(Scanpath.STATIC))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		ScanBean bean = beanCaptor.getValue();
		assertThat(bean.getScanRequest().getCompoundModel(), is(equalTo(new CompoundModel(new StaticModel()))));

	}

	@Test
	public void willFailOnStaticWithScannableFirst() throws Exception {
		Object[] arr = {scannable, Scanpath.STATIC, 3};
		when (resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
						new ScanpathElementProcessor(Scanpath.STATIC),
						new NumberElementProcessor(3))));
		exception.expect(UnsupportedOperationException.class);
		exception.expectMessage("Out of sequence call");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	public void willFailOnStaticWithScannableAfter() throws Exception {
		// scannable is not a detector or monitor, should not be allowed
		Object[] arr = {Scanpath.STATIC, 3, scannable};
		when (resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScanpathElementProcessor(Scanpath.STATIC),
						new NumberElementProcessor(3),
						new ScannableElementProcessor(scannable))));
		exception.expect(UnsupportedOperationException.class);
		exception.expectMessage("Out of sequence call");
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(eventSubscriber, never()).addListener(any(IScanListener.class));
		verify(submitter, never()).blockingSubmit(any(ScanBean.class));
	}

	@Test
	// Behaviour combined with {@link StepTest#testTooLargeStep} shows expected behaviour
	public void trimIfStepTooLongForRegionOccursOnGenerator() throws Exception {
		final AxialStepModel expectedModel = new AxialStepModel(scannable.getName(), -1, 1, 5);
		expectedModel.setContinuous(false);
		Object[] arr = {scannable, RegionShape.AXIAL, -1, 1, Scanpath.AXIS_STEP, 5, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(-1),
											new NumberElementProcessor(1),
											new ScanpathElementProcessor(Scanpath.AXIS_STEP),
											new NumberElementProcessor(5)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		CompoundModel model = beanCaptor.getValue().getScanRequest().getCompoundModel();
		assertThat(model.getModels().get(0), is(equalTo(expectedModel)));
	}

	@Test
	// Behaviour combined with {@link StepTest#testImperfectSequence} shows expected behaviour
	public void trimIfStepAwkwardFitForRegionOccursOnGenerator() throws Exception {
		final AxialStepModel expectedModel = new AxialStepModel(scannable.getName(), -1, 1, 0.3);
		expectedModel.setContinuous(false);
		Object[] arr = {scannable, RegionShape.AXIAL, -1, 1, Scanpath.AXIS_STEP, 0.3, detectorRunnableDevice};
		when(resolver.resolveScanClauses()).thenReturn(
				Arrays.asList(Arrays.asList(new ScannableElementProcessor(scannable),
											new RegionShapeElementProcessor(RegionShape.AXIAL),
											new NumberElementProcessor(-1),
											new NumberElementProcessor(1),
											new ScanpathElementProcessor(Scanpath.AXIS_STEP),
											new NumberElementProcessor(0.3)),
							  Arrays.asList(new IRunnableDeviceDetectorElementProcessor(detectorRunnableDevice))));
		builder.buildAndSubmitBlockingScanRequest(arr);
		verify(submitter).blockingSubmit(beanCaptor.capture());
		verify(eventSubscriber).addListener(any(IScanListener.class));
		CompoundModel model = beanCaptor.getValue().getScanRequest().getCompoundModel();
		assertThat(model.getModels().get(0), is(equalTo(expectedModel)));
	}
}
