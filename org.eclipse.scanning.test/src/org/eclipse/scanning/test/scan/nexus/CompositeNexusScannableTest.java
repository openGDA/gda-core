package org.eclipse.scanning.test.scan.nexus;

import static java.util.stream.Collectors.toList;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_DIAMOND_SCAN;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDatasetValue;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXslit;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.device.composite.ChildFieldNode;
import org.eclipse.scanning.device.composite.ChildGroupNode;
import org.eclipse.scanning.device.composite.ChildNode;
import org.eclipse.scanning.device.composite.CompositeNexusScannable;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompositeNexusScannableTest extends NexusTest {

	private static final class BeamPerScanMonitor extends MockScannable implements INexusDevice<NXbeam> {

		BeamPerScanMonitor() {
			setName("beam");
		}

		@Override
		public NexusObjectProvider<NXbeam> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXbeam nxBeam = NexusNodeFactory.createNXbeam();

			nxBeam.setIncident_beam_divergenceScalar(0.123);
			nxBeam.setFinal_beam_divergenceScalar(0.456);

			return new NexusObjectWrapper<NXbeam>(getName(), nxBeam);
		}

	}

	private static final class TransformsScannable extends MockScannable implements INexusDevice<NXtransformations> {

		TransformsScannable() {
			setName("transforms");
		}

		@Override
		public NexusObjectProvider<NXtransformations> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXtransformations nxTransformations = NexusNodeFactory.createNXtransformations();

			// TODO: should this be a CompositeNexusScannable also?
			nxTransformations.setField("x_centre", 123.456);
			nxTransformations.setAttribute("x_centre", "transformation_type", "translation");
			nxTransformations.setAttribute("x_centre", "vector", new int[] { 1, 0, 0 });
			nxTransformations.setAttribute("x_centre", "depends_on", "y_centre");
			nxTransformations.setAttribute("x_centre", "units", "mm");
			nxTransformations.setAttribute("x_centre", "controller_record", "x centre controller name");

			nxTransformations.setField("y_centre", 789.012);
			nxTransformations.setAttribute("y_centre", "transformation_type", "translation");
			nxTransformations.setAttribute("y_centre", "vector", new int[] { 0, 1, 0 });
			nxTransformations.setAttribute("y_centre", "depends_on", ".");
			nxTransformations.setAttribute("y_centre", "offset", new int[] { 0, 0, -14500 });
			nxTransformations.setAttribute("y_centre", "units", "mm");
			nxTransformations.setAttribute("y_centre", "controller_record", "y centre controller name");

			return new NexusObjectWrapper<NXtransformations>(getName(), nxTransformations);
		}

	}

	private static final class SlitMotorScannable extends MockScannable implements INexusDevice<NXpositioner> {

		private final String nexusGroupName;
		private final String description;

		SlitMotorScannable(String name, double position, String nexusGroupName, String description) {
			super(name, position);
			this.nexusGroupName = nexusGroupName;
			this.description = description;
		}

		public String getNexusGroupName() {
			return nexusGroupName;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public NexusObjectProvider<NXpositioner> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXpositioner nxPositioner = NexusNodeFactory.createNXpositioner();
			nxPositioner.setNameScalar(getName());
			nxPositioner.setDescriptionScalar(description);
			nxPositioner.setController_recordScalar(getName() + "_controller"); // this would be the EPICS name

			// Write the value. Note: we would initialize a lazy dataset if the scannable could be scanned
			nxPositioner.setValueScalar(getPosition());

			return new NexusObjectWrapper<>(getName(), nxPositioner);
		}

	}

	private static IWritableDetector<MandelbrotModel> detector;
	private static BeamPerScanMonitor beam;

	private static final int[] SCAN_SIZE = { 2, 3 };

	@BeforeClass
	public static void beforeClass() throws Exception {
		final MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));

		beam = new BeamPerScanMonitor();
		((MockScannableConnector) connector).register(beam);
	}

	@Test
	public void testNXSlitCompositeNexusScannable() throws Exception {
		// Arrange
		final CompositeNexusScannable<NXslit> primarySlit = new CompositeNexusScannable<>();
		primarySlit.setName("primary_slit");
		primarySlit.setNexusClass(NexusBaseClass.NX_SLIT);
		primarySlit.setNexusCategory(NexusBaseClass.NX_INSTRUMENT);

		final List<ChildNode> childNodes = new ArrayList<>();

		// create the nodes for x_gap and y_gap
		final String gapXName = "s1gapX"; // TODO Move to @Before method?
		final IScannable<?> s1gapX = new MockNeXusScannable(gapXName, 1.234, 1);
		((MockScannableConnector) connector).register(s1gapX);
		final String gapYName = "s1gapY";
		final IScannable<?> s1gapY = new MockNeXusScannable(gapYName, 2.345, 1);
		((MockScannableConnector) connector).register(s1gapY);

		childNodes.add(new ChildFieldNode(gapXName, NXpositioner.NX_VALUE, "x_gap"));
		childNodes.add(new ChildFieldNode(gapYName, NXpositioner.NX_VALUE, "y_gap"));

		// create the transformations scannable
		// TODO: should we add a mechanism within ComplexCompositeNexusScannable
		// rather than
		// create a new scannable for this?
		final TransformsScannable transformsScannable = new TransformsScannable();
		transformsScannable.setName("transforms");
		((MockScannableConnector) connector).register(transformsScannable);

		final ChildGroupNode transformsNode = new ChildGroupNode();
		transformsNode.setScannableName(transformsScannable.getName());
		childNodes.add(transformsNode);

		// create the beam
		final BeamPerScanMonitor beamScannable = new BeamPerScanMonitor();
		((MockScannableConnector) connector).register(beamScannable);

		final ChildGroupNode beamNode = new ChildGroupNode();
		beamNode.setScannableName(beamScannable.getName());
		beamNode.setGroupName("beam");
		childNodes.add(beamNode);

		// create the motors composite scannable
		final CompositeNexusScannable<NXcollection> motorsCompositeScannable = new CompositeNexusScannable<>();
		motorsCompositeScannable.setName("primary_slit_motors");
		((MockScannableConnector) connector).register(motorsCompositeScannable);

		final List<SlitMotorScannable> slitMotorScannables = new ArrayList<>();
		slitMotorScannables.add(new SlitMotorScannable("s1dsX", 1.0, "downstream_x", "Downstream X position"));
		slitMotorScannables.add(new SlitMotorScannable("s1dsY", 2.0, "downstream_y", "Downstream Y position"));
		slitMotorScannables.add(new SlitMotorScannable("s1usX", 3.0, "upstream_x", "Upstream X position"));
		slitMotorScannables.add(new SlitMotorScannable("s1usY", 4.0, "upstream_y", "Upstream Y position"));
		slitMotorScannables.forEach(((MockScannableConnector) connector)::register);
		motorsCompositeScannable.setChildNodes(slitMotorScannables.stream().
				map(scannable -> new ChildGroupNode(scannable.getName(), scannable.getNexusGroupName())).
				collect(toList()));

		final ChildGroupNode motorsNode = new ChildGroupNode();
		motorsNode.setScannableName("primary_slit_motors");
		motorsNode.setGroupName("motors");
		childNodes.add(motorsNode);

		// add the groups to the child primary slit and register the primary
		// slit scannable
		primarySlit.setChildNodes(childNodes);
		((MockScannableConnector) connector).register(primarySlit);

		// Act: run the scan
		final NXroot root = createAndRunScan(primarySlit);
		final NXentry entry = root.getEntry();

		// Assert: check the nexus file
		assertThat(entry.getGroupNodeNames(), containsInAnyOrder(
				"instrument", "sample", GROUP_NAME_DIAMOND_SCAN,
				detector.getName(), detector.getName() + "_spectrum", detector.getName() + "_value"));
		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(
				detector.getName(), "xNex", "yNex", "primary_slit"));

		final NXslit nxSlit = (NXslit) instrument.getGroupNode("primary_slit");
		assertThat(nxSlit, is(notNullValue()));
		assertThat(nxSlit.getNexusBaseClass(), is(NexusBaseClass.NX_SLIT));
		assertThat(nxSlit.getGroupNodeNames(), containsInAnyOrder("beam", "motors", "transforms"));
		assertThat(nxSlit.getDataNodeNames(), containsInAnyOrder("x_gap", "y_gap"));

		// assertEquals(1.234, nxSlit.getX_gapScalar().doubleValue(), 1e-15);
		// TODO reinstate when DAQ-599 fixed
		assertDatasetValue(1.234, nxSlit.getDataset("x_gap"));
		// assertEquals(2.345, nxSlit.getY_gapScalar().doubleValue(), 1e-15);
		assertDatasetValue(2.345, nxSlit.getDataset("y_gap"));

		final NXtransformations transforms = (NXtransformations) nxSlit.getGroupNode("transforms");
		assertDatasetValue(123.456, transforms.getDataset("x_centre"));
		assertDatasetValue("translation", transforms.getAttr("x_centre", "transformation_type"));
		assertDatasetValue(new int[] { 1, 0, 0 }, transforms.getAttr("x_centre", "vector"));
		assertDatasetValue("y_centre", transforms.getAttr("x_centre", "depends_on"));
		assertDatasetValue("mm", transforms.getAttr("x_centre", "units"));
		assertDatasetValue("x centre controller name", transforms.getAttr("x_centre", "controller_record"));

		assertDatasetValue(789.012, transforms.getDataset("y_centre"));
		assertDatasetValue("translation", transforms.getAttr("y_centre", "transformation_type"));
		assertDatasetValue(new int[] { 0, 1, 0 }, transforms.getAttr("y_centre", "vector"));
		assertDatasetValue(".", transforms.getAttr("y_centre", "depends_on"));
		assertDatasetValue(new int[] { 0, 0, -14500 }, transforms.getAttr("y_centre", "offset"));
		assertDatasetValue("mm", transforms.getAttr("y_centre", "units"));
		assertDatasetValue("y centre controller name", transforms.getAttr("y_centre", "controller_record"));

		final NXbeam beam = (NXbeam) nxSlit.getGroupNode("beam");
		assertThat(beam, is(notNullValue()));

		assertThat(beam.getIncident_beam_divergenceScalar(), is(closeTo(0.123, 1e-15)));
		assertThat(beam.getFinal_beam_divergenceScalar(), is(closeTo(0.456, 1e-15)));

		final NXcollection motors = (NXcollection) nxSlit.getGroupNode("motors");
		assertThat(motors, is(notNullValue()));
		assertThat(motors.getDataNodeNames(), is(empty()));
		assertThat(motors.getGroupNodeNames(), containsInAnyOrder(
				slitMotorScannables.stream().map(SlitMotorScannable::getNexusGroupName).toArray()));
		for (SlitMotorScannable slitMotorScannable : slitMotorScannables) {
			NXpositioner positioner = (NXpositioner) motors.getGroupNode(slitMotorScannable.getNexusGroupName());
			assertThat(positioner, is(notNullValue()));

			assertThat(positioner.getNameScalar(), is(equalTo(slitMotorScannable.getName())));
			assertThat(positioner.getDescriptionScalar(), is(equalTo(slitMotorScannable.getDescription())));
			assertThat(positioner.getController_recordScalar(), is(equalTo(slitMotorScannable.getName() + "_controller")));
			assertThat(positioner.getValueScalar(), is(equalTo(positioner.getValueScalar())));
		}
	}

	@Test
	public void testCompositeNexusScannable() throws Exception {
		// Create a list of 3 ChildFieldNodes and 5 ChildGroupNodes
		final int numFieldNodes = 3;
		final int numGroupNodes = 5;
		final int numTotalNodes = numFieldNodes + numGroupNodes;
		final IntFunction<ChildNode> toNode = i -> i <= numFieldNodes
				? new ChildFieldNode("neXusScannable" + i, NXpositioner.NX_VALUE, "pos" + i)
				: new ChildGroupNode("neXusScannable" + i);
		final List<ChildNode> childNodes = IntStream.range(1, numTotalNodes + 1)
				.mapToObj(toNode).collect(toList());

		final CompositeNexusScannable<NXobject> composite = new CompositeNexusScannable<>();
		composite.setName("composite");
		composite.setNexusClass(NexusBaseClass.NX_COLLECTION);
		composite.setChildNodes(childNodes);
		((MockScannableConnector) connector).register(composite);

		final NXroot root = createAndRunScan(composite);
		final NXentry entry = root.getEntry();
		assertThat(entry.getGroupNodeNames(), containsInAnyOrder( // NXinstrument, NXdata groups, etc
				"instrument", "sample", "composite", GROUP_NAME_DIAMOND_SCAN,
				detector.getName(), detector.getName() + "_spectrum", detector.getName() + "_value"));
		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(detector.getName(), "xNex", "yNex"));

		final NXcollection compositeCollection = (NXcollection) entry.getGroupNode("composite");
		assertThat(compositeCollection, is(notNullValue()));
		assertThat(compositeCollection.getNexusBaseClass(), is(NexusBaseClass.NX_COLLECTION));
		assertThat(compositeCollection.getDataNodeNames(), containsInAnyOrder( // pos1-3
				IntStream.range(1, numFieldNodes+1).mapToObj(i -> "pos"+i).toArray()));
		assertThat(compositeCollection.getGroupNodeNames(), containsInAnyOrder( // NeXusScannable4-8
				IntStream.range(0, numGroupNodes).map(i -> i + numFieldNodes + 1)
					.mapToObj(i -> "neXusScannable" + i).toArray()));

		for (ChildNode childNode : childNodes) {
			final String scannableName = childNode.getScannableName();
			if (childNode instanceof ChildGroupNode) {
				// check scannables added as groups
				final NXpositioner positioner = (NXpositioner) compositeCollection.getGroupNode(scannableName);
				assertThat(positioner, is(notNullValue()));
				assertThat(positioner.getNameScalar(), is(equalTo(scannableName)));
				assertThat(positioner.getValue(), is(notNullValue()));
			} else if (childNode instanceof ChildFieldNode) {
				// check scannables added as fields
				final DataNode fieldNode = compositeCollection
						.getDataNode(((ChildFieldNode) childNode).getDestinationFieldName());
				assertThat(fieldNode, is(notNullValue()));
				assertThat(fieldNode.getDataset().getSlice().getShape(), is(equalTo(new int[0])));
			}
		}
	}

	private NXroot createAndRunScan(CompositeNexusScannable<?> compositeScannable) throws Exception {
		final IRunnableDevice<ScanModel> scanner = createGridScan(compositeScannable);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		return checkNexusFile(scanner, false, SCAN_SIZE);
	}

	private IRunnableDevice<ScanModel> createGridScan(IScannable<?> perScanMonitor) throws Exception {
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setxAxisName("xNex");
		gridModel.setxAxisPoints(SCAN_SIZE[1]);
		gridModel.setyAxisName("yNex");
		gridModel.setyAxisPoints(SCAN_SIZE[0]);
		gridModel.setBoundingBox(new BoundingBox(0, 0, 3, 3));
		gridModel.setAlternating(false);

		final IPointGenerator<? extends IScanPointGeneratorModel> pointGen = pointGenService.createGenerator(gridModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(gridModel);
		if (perScanMonitor != null) {
			perScanMonitor.setActivated(true);
		}
		scanModel.setDetector(detector);

		scanModel.setMonitorsPerScan(perScanMonitor);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}

}
