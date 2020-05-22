/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertGroupNodesEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.device.SimpleNexusDevice;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.nexus.NexusFileFactory;
import gda.scan.ScanDataPoint;

/**
 * Test class for {@link NexusDataWriter}.
 * Note: at of writing (2019-08-22) this only tests that templates are applied.
 * If we ever get around to testing the rest of NexusDataWriter's behaviour, those tests should be added to this class.
 */
public class NexusDataWriterTest {

	private static final String TEMPLATE_FILE_PATH = "testfiles/gda/scan/datawriter/NexusDataWriterTest/simple-template.yaml";

	private String testScratchDirectoryName;

	private NexusDataWriter nexusDataWriter;

	private String nexusFilePath;

	@Before
	public void setUp() throws Exception {
		testScratchDirectoryName = TestHelpers.setUpTest(NexusDataWriterTest.class, "", true);
		LocalProperties.set(NexusDataWriter.GDA_NEXUS_CREATE_SRS, "false");

		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setNexusTemplateService(new NexusTemplateServiceImpl());
		serviceHolder.setNexusDeviceService(new NexusDeviceService());

		nexusDataWriter = new NexusDataWriter();
		nexusDataWriter.configureScanNumber(1);
		nexusDataWriter.setNexusFileNameTemplate("scan-%d.nxs");
		assertThat(nexusDataWriter.getCurrentScanIdentifier(), is(1));
		assertThat(nexusDataWriter.getNexusFileName(), is(equalTo("scan-1.nxs")));
		assertThat(nexusDataWriter.getCurrentFileName(), endsWith(testScratchDirectoryName.toString() + "Data/scan-1.nxs"));
		nexusFilePath = nexusDataWriter.getCurrentFileName();
	}

	@After
	public void tearDown() {
		new File(nexusDataWriter.getCurrentFileName()).delete();
		new File(testScratchDirectoryName).delete();
	}

	@Test
	public void testNexusDataWriter() throws Exception {
		// Arrange: create a nexus device and register it with the nexus device service
		final NXuser user = NexusNodeFactory.createNXuser();
		user.setNameScalar("John Smith");
		user.setRoleScalar("Beamline Scientist");
		user.setAddressScalar("Diamond Light Source, Didcot, Oxfordshire, OX11 0DE");
		user.setEmailScalar("john.smith@diamond.ac.uk");
		user.setFacility_user_idScalar("wgp76868");
		final NXcollection collection = NexusNodeFactory.createNXcollection(); // to check that child groups are added
		collection.setField("foo", "bar");
		user.addGroupNode("collection", collection);

		final NexusObjectProvider<NXuser> userProvider = new NexusObjectWrapper<>("user", user);
		final SimpleNexusDevice<NXuser> userNexusDevice = new SimpleNexusDevice<NXuser>(userProvider);

		ServiceHolder.getNexusDeviceService().register(userNexusDevice);
		NexusDataWriter.setMetadatascannables(new HashSet<>(Arrays.asList(userNexusDevice.getName())));

		// Set the location of the template file
		final String templateFileAbsolutePath = Paths.get(TEMPLATE_FILE_PATH).toAbsolutePath().toString();
		NexusDataWriter.setNexusTemplateFiles(Arrays.asList(templateFileAbsolutePath));

		// Act: write a point. Writing the first point causes the nexus file to be created.
		final ScanDataPoint firstPoint = new ScanDataPoint();
		firstPoint.setScanDimensions(new int[] { 5 });
		nexusDataWriter.addData(firstPoint);
		nexusDataWriter.completeCollection();
		nexusDataWriter.releaseFile();

		// check that the file has been created as expected
		assertThat(new File(nexusFilePath).exists(), is(true));

		// We should add tests for the rest of NexusDataWriters behaviour
		final String entryPath = "/entry1/";
		try (NexusFile nexusFile = NexusFileFactory.openFileToRead(nexusFilePath)) {
			assertThat(nexusFile.getGroup(entryPath, false), is(notNullValue()));

			// test that the scan entry group created by the template has been added (note: we don't test the content here)
			assertThat(nexusFile.getGroup("/scan", false), is(notNullValue()));
			final String userGroupPath = entryPath + userNexusDevice.getName() + "/";
			assertThat(nexusFile.getGroup(userGroupPath, false), is(notNullValue()));
			final String userCollectionPath = userGroupPath + "collection";
			assertThat(nexusFile.getGroup(userCollectionPath, false), is(notNullValue()));

			final GroupNode userGroup = nexusFile.getGroup(userGroupPath, false);
			assertThat(user, is(not(sameInstance(userGroup))));
			assertGroupNodesEqual(userGroupPath, user, userGroup);
		}
	}

}
