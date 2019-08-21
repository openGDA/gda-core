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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.dawnsci.nexus.NexusFile;
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
		new ServiceHolder().setNexusTemplateService(new NexusTemplateServiceImpl());

		nexusDataWriter = new NexusDataWriter();
		nexusDataWriter.configureScanNumber(1);
		nexusDataWriter.setNexusFileNameTemplate("scan-%d.nxs");
		assertThat(nexusDataWriter.getCurrentScanIdentifier(), is(1));
		assertThat(nexusDataWriter.getNexusFileName(), is(equalTo("scan-1.nxs")));
		assertThat(nexusDataWriter.getCurrentFileName(), endsWith(testScratchDirectoryName.toString() + "Data/scan-1.nxs"));
		nexusFilePath = nexusDataWriter.getCurrentFileName();

		final String templatefileAbsolutePath = Paths.get(TEMPLATE_FILE_PATH).toAbsolutePath().toString();
		NexusDataWriter.setNexusTemplateFiles(Arrays.asList(templatefileAbsolutePath));
	}

	@After
	public void tearDown() {
		new File(nexusDataWriter.getCurrentFileName()).delete();
		new File(testScratchDirectoryName).delete();
	}

	@Test
	public void testNexusDataWriter() throws Exception {
		ScanDataPoint firstPoint = new ScanDataPoint();
		firstPoint.setScanDimensions(new int[] { 5 });

		nexusDataWriter.addData(firstPoint);

		nexusDataWriter.completeCollection();
		nexusDataWriter.releaseFile();

		// check that the file has been created
		assertThat(new File(nexusFilePath).exists(), is(true));

		try (NexusFile nexusFile = NexusFileFactory.openFileToRead(nexusFilePath)) {
			assertThat(nexusFile.getGroup("/entry1", false), is(notNullValue()));
			assertThat(nexusFile.getGroup("/scan", false), is(notNullValue())); // created by the template, proving it has been applied
		}
	}

}
