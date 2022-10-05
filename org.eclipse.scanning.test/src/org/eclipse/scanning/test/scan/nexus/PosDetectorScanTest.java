package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_DIAMOND_SCAN;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_UNIQUE_KEYS;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDataNodesEqual;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertNXentryMetadata;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.PosDetectorModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Flaky new-scanning test")
class PosDetectorScanTest extends NexusTest {

	private IWritableDetector<PosDetectorModel> detector;

	@BeforeEach
	void before() throws Exception {
		final PosDetectorModel model = new PosDetectorModel(3);
		detector = TestDetectorHelpers.createAndConfigurePosDetector(model);
		assertThat(detector, is(notNullValue()));
	}

	@AfterEach
	void after() {
		File parentDir = output.getParentFile();
		String fileName = output.getName().substring(0, output.getName().indexOf('.'));
		File outputDir = new File(parentDir, fileName);
		for (File file : outputDir.listFiles()) {
			file.delete();
		}
		outputDir.delete();
	}

	@Test
	void testPosScan() throws Exception {
		final int[] scanShape = new int[] { 8, 5 };
		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, scanShape);
		// add a the UniqueKeyChecker as a position listener to check the unique key is written at each point
		((IPositionListenable) scanner).addPositionListener(
				new UniqueKeyChecker(scanner.getModel().getFilePath()));

		assertScanNotFinished(getNexusRoot(scanner).getEntry());

		scanner.run(null);

		checkNexusFile(scanner, scanShape);
	}

	private static class UniqueKeyChecker implements IPositionListener {

		private final String filePath;

		UniqueKeyChecker(String filePath) {
			this.filePath = filePath;
		}

		@Override
		public void positionMovePerformed(PositionEvent event) throws ScanningException {
			checkUniqueKeyWritten(event.getPosition());
		}

		@Override
		public void positionPerformed(PositionEvent event) throws ScanningException {
			checkUniqueKeyWritten(event.getPosition());
		}

		private IDataset getUniqueKeysDataset() {
			try (NexusFile nf = fileFactory.newNexusFile(filePath)) {
				nf.openToRead();

				final TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
				final NXroot root = (NXroot) nexusTree.getGroupNode();
				final NXentry entry = root.getEntry();

				final NXcollection solsticeScanCollection = entry.getCollection(GROUP_NAME_DIAMOND_SCAN);
				final NXcollection keysCollection = (NXcollection) solsticeScanCollection.getGroupNode(GROUP_NAME_UNIQUE_KEYS);
				final DataNode dataNode = keysCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
				final ILazyDataset uniqueKeysDataset = dataNode.getDataset();

//				((IDynamicDataset) uniqueKeysDataset).refreshShape();
				return uniqueKeysDataset.getSlice(); // it's only a small dataset, so this is ok
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private void checkUniqueKeyWritten(IPosition position) {
			final IDataset uniqueKeysDataset = getUniqueKeysDataset();
			assertThat(uniqueKeysDataset, is(notNullValue()));

			int uniqueKey = uniqueKeysDataset.getInt(position.getIndex(0), position.getIndex(1));
			assertThat(uniqueKey, is(equalTo(position.getStepIndex() + 1)));
		}

	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int[] sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));

		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check that the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertDiamondScanGroup(entry, false, false, 8, 5);

		final String detectorName = detector.getName();
		final NXdetector nxDetector = instrument.getDetector(detectorName);
		assertThat(nxDetector, is(notNullValue()));
		final double expectedExposureTime = ((IDetectorModel) scanner.getModel().getDetectors().get(0).getModel()).getExposureTime();
		assertThat(nxDetector.getCount_timeScalar().doubleValue(), is(closeTo(expectedExposureTime, 1e-15)));

		assertThat(entry.getAllData().size(), is(1));
		final NXdata dataGroup = entry.getData(detectorName);
		assertThat(dataGroup, is(notNullValue()));

		assertSignal(dataGroup, NXdetector.NX_DATA);

		final DataNode dataNode = nxDetector.getDataNode(NXdetector.NX_DATA);
		assertThat(dataNode, is(notNullValue()));
		assertDataNodesEqual("", dataNode, dataGroup.getDataNode(NXdetector.NX_DATA));

		final IDataset dataset = dataNode.getDataset().getSlice();
		int[] shape = dataset.getShape();
		for (int i = 0; i < sizes.length; i++) {
			assertThat(shape[i], is(equalTo(sizes[i])));
		}

		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls
		final PositionIterator it = new PositionIterator(shape);
		while (it.hasNext()) {
			int[] next = it.getPos();
			assertThat(Double.isNaN(dataset.getDouble(next)), is(false));
		}

		// Check axes
		final IPosition pos = scanModel.getPointGenerator().iterator().next();
		final List<String> scannableNames = pos.getNames();
		assertThat(scannableNames.size(), is(sizes.length));

		// Append _value_demand to each position name, append "." twice for 2 image dimensions
		final List<String> expectedAxesNames = Stream.concat(scannableNames.stream().map(x -> x + "_value_set"),
				Collections.nCopies(2, ".").stream()).collect(Collectors.toList());
		assertAxes(dataGroup, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

		final int[] defaultDimensionMappings = IntStream.range(0, scannableNames.size()).toArray();

		int i = 0;
		for (String positionerName : scannableNames) {
			final NXpositioner positioner = entry.getInstrument().getPositioner(positionerName);

			// check value_demand data node
			final String demandFieldName = positionerName + "_" + NXpositioner.NX_VALUE + "_set";
			assertSame(positioner.getDataNode("value_set"), is(sameInstance(dataGroup.getDataNode(demandFieldName))));
			assertIndices(dataGroup, demandFieldName, i);
			assertTarget(dataGroup, demandFieldName, rootNode, "/entry/instrument/" + positionerName + "/value_set");

			// check value data node
			final String valueFieldName = positionerName + "_" + NXpositioner.NX_VALUE;
			assertThat(positioner.getDataNode(NXpositioner.NX_VALUE), is(equalTo(dataGroup.getDataNode(valueFieldName))));
			assertIndices(dataGroup, valueFieldName, defaultDimensionMappings);
			assertTarget(dataGroup, valueFieldName, rootNode,
					"/entry/instrument/" + positionerName + "/" + NXpositioner.NX_VALUE);
			i++;
		}
	}

}
