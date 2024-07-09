/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_DIAMOND_SCAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.eclipse.scanning.device.NexusMetadataDevice;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.TestHelpers;
import gda.data.scan.nexus.NexusScanDataWriterTestSetup;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyScannable;
import gda.scan.ConcurrentScan;
import uk.ac.diamond.osgi.services.ServiceProvider;

class TransformationsScanTest {

	private static final int NUM_POINTS = 6;
	private static final String SCANNABLE_NAME = "s1";
	private static final String DETECTOR_NAME = "det";

	private static final String DELTA_SCANNABLE_NAME = "kdelta";
	private static final String GAMMA_SCANNABLE_NAME = "kgam";
	private static final String PHI_SCANNABLE_NAME = "kphi";
	private static final String KAPPA_SCANNABLE_NAME = "kap";
	private static final String THETA_SCANNABLE_NAME = "kth";
	private static final String MU_SCANNABLE_NAME = "kmu";

	private static final String TRANSFORMATIONS_DEVICE_NAME = "transformations";
	private static final String DELTA_LINK_NAME = "delta";
	private static final String DELTA_LINK_PATH = "/entry/instrument/kdelta/value";
	private static final String GAMMA_LINK_NAME = "gamma";
	private static final String GAMMA_LINK_PATH = "/entry/instrument/kgam/value";

	private static final String PHI_LINK_NAME = "phi";
	private static final String PHI_LINK_PATH = "/entry/instrument/kphi/value";
	private static final String KAPPA_LINK_NAME = "kappa";
	private static final String KAPPA_LINK_PATH = "/entry/instrument/kap/value";
	private static final String THETA_LINK_NAME = "theta";
	private static final String THETA_LINK_PATH = "/entry/instrument/kth/value";
	private static final String MU_LINK_NAME = "mu";
	private static final String MU_LINK_PATH = "/entry/instrument/kmu/value";

	private static final String SAMPLE_TRANSFORMATIONS_DEVICE_NAME = "sampleTransformations";
	private static final String SAMPLE_TRANSFORMATIONS_NODE_NAME = "transformations";

	private Scannable scannable;
	private Detector detector;

	private Scannable deltaScannable;
	private Scannable gammaScannable;

	private Scannable phiScannable;
	private Scannable kappaScannable;
	private Scannable thetaScannable;
	private Scannable muScannable;

	private NexusMetadataDevice<NXtransformations> instrumentTransformationsDevice;
	private NexusMetadataDevice<NXtransformations> sampleTransformationsDevice;

	@BeforeAll
	static void setUpServices() {
		NexusScanDataWriterTestSetup.setUp();
	}

	@AfterAll
	static void tearDownServices() {
		NexusScanDataWriterTestSetup.tearDown();
	}

	@BeforeEach
	void setUp() {
		scannable = new DummyScannable(SCANNABLE_NAME);

		detector = new DummyDetector(DETECTOR_NAME);
		detector.setExtraNames(new String[] { NXdetector.NX_DATA });

		deltaScannable = new DummyScannable(DELTA_SCANNABLE_NAME);
		gammaScannable = new DummyScannable(GAMMA_SCANNABLE_NAME);

		phiScannable = new DummyScannable(PHI_SCANNABLE_NAME);
		kappaScannable = new DummyScannable(KAPPA_SCANNABLE_NAME);
		thetaScannable = new DummyScannable(THETA_SCANNABLE_NAME);
		muScannable = new DummyScannable(MU_SCANNABLE_NAME);

		instrumentTransformationsDevice = new NexusMetadataDevice<>(NexusBaseClass.NX_TRANSFORMATIONS);
		instrumentTransformationsDevice.setName(TRANSFORMATIONS_DEVICE_NAME);
		instrumentTransformationsDevice.setCategory(NexusBaseClass.NX_INSTRUMENT);
		instrumentTransformationsDevice.addLinkedField(DELTA_LINK_NAME, DELTA_LINK_PATH);
		instrumentTransformationsDevice.addLinkedField(GAMMA_LINK_NAME, GAMMA_LINK_PATH);
		ServiceProvider.getService(INexusDeviceService.class).register(instrumentTransformationsDevice);

		sampleTransformationsDevice = new NexusMetadataDevice<>(NexusBaseClass.NX_TRANSFORMATIONS);
		sampleTransformationsDevice.setName(SAMPLE_TRANSFORMATIONS_DEVICE_NAME);
		sampleTransformationsDevice.setNodeName(SAMPLE_TRANSFORMATIONS_NODE_NAME);
		sampleTransformationsDevice.setCategory(NexusBaseClass.NX_SAMPLE);
		sampleTransformationsDevice.addLinkedField(PHI_LINK_NAME, PHI_LINK_PATH);
		sampleTransformationsDevice.addLinkedField(KAPPA_LINK_NAME, KAPPA_LINK_PATH);
		sampleTransformationsDevice.addLinkedField(THETA_LINK_NAME, THETA_LINK_PATH);
		sampleTransformationsDevice.addLinkedField(MU_LINK_NAME, MU_LINK_PATH);
		ServiceProvider.getService(INexusDeviceService.class).register(sampleTransformationsDevice);

		final CommonBeamlineDevicesConfiguration beamlineConfig = new CommonBeamlineDevicesConfiguration();
		beamlineConfig.addAdditionalDeviceName(TRANSFORMATIONS_DEVICE_NAME);
		beamlineConfig.addAdditionalDeviceName(SAMPLE_TRANSFORMATIONS_DEVICE_NAME);
		CommonBeamlineDevicesConfiguration.setInstance(beamlineConfig);
	}

	@Test
	void testTransformations() throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), "testTransformations", true, NexusScanDataWriter.class);

		final Object[] scanArguments = { scannable, 0, NUM_POINTS - 1, 1, detector,
				deltaScannable, gammaScannable, phiScannable, kappaScannable, thetaScannable, muScannable };
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);

		scan.run();

		final Path filePath =  Path.of(testDir, "Data", "1.nxs");
		assertThat(Files.exists(filePath), is(true));
		try (final NexusFile nexusFile = NexusTestUtils.openNexusFile(filePath.toString())) {
			checkNexusFile(nexusFile);
		}
	}

	private void checkNexusFile(NexusFile nexusFile) throws NexusException, DatasetException {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));

		assertThat(entry.getGroupNodeNames(), containsInAnyOrder("instrument", "sample",
				GROUP_NAME_DIAMOND_SCAN, DETECTOR_NAME));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(SCANNABLE_NAME, DETECTOR_NAME,
				DELTA_SCANNABLE_NAME, GAMMA_SCANNABLE_NAME,
				PHI_SCANNABLE_NAME, KAPPA_SCANNABLE_NAME, THETA_SCANNABLE_NAME, MU_SCANNABLE_NAME,
				TRANSFORMATIONS_DEVICE_NAME));

		final NXdetector detector = instrument.getDetector(DETECTOR_NAME);
		assertThat(detector, is(notNullValue()));
		assertThat(detector.getDataNode(NXdetector.NX_DATA), is(notNullValue()));

		for (String scannableName : List.of(SCANNABLE_NAME, DELTA_SCANNABLE_NAME, GAMMA_SCANNABLE_NAME,
				PHI_SCANNABLE_NAME, KAPPA_SCANNABLE_NAME, THETA_SCANNABLE_NAME, MU_SCANNABLE_NAME)) {
			final NXpositioner positioner = instrument.getPositioner(scannableName);
			assertThat(positioner, is(notNullValue()));
		}

		final NXtransformations transformations = (NXtransformations) instrument.getGroupNode(TRANSFORMATIONS_DEVICE_NAME);
		assertThat(transformations, is(notNullValue()));
		assertThat(transformations.getDataNodeNames(), containsInAnyOrder(DELTA_LINK_NAME, GAMMA_LINK_NAME));
		checkLink(DELTA_LINK_NAME, DELTA_SCANNABLE_NAME, instrument, transformations);
		checkLink(GAMMA_LINK_NAME, GAMMA_SCANNABLE_NAME, instrument, transformations);

		final NXsample sample = entry.getSample();
		assertThat(sample, is(notNullValue()));
		final NXtransformations sampleTransformations = sample.getTransformations();
		assertThat(sampleTransformations, is(notNullValue()));
		checkLink(PHI_LINK_NAME, PHI_SCANNABLE_NAME, instrument, sampleTransformations);
		checkLink(KAPPA_LINK_NAME, KAPPA_SCANNABLE_NAME, instrument, sampleTransformations);
		checkLink(THETA_LINK_NAME, THETA_SCANNABLE_NAME, instrument, sampleTransformations);
		checkLink(MU_LINK_NAME, MU_SCANNABLE_NAME, instrument, sampleTransformations);
	}

	private void checkLink(String linkName, String scannableName, NXinstrument instrument, NXtransformations transformations) {
		final NXpositioner positioner = instrument.getPositioner(scannableName);
		final DataNode targetDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		final DataNode linkDataNode = transformations.getDataNode(linkName);
		assertThat(linkDataNode.getDataset().getSize(), is(NUM_POINTS));
		assertThat(linkDataNode, is(sameInstance(targetDataNode)));
	}

}
