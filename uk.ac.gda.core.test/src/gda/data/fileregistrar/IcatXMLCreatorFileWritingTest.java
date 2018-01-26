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

package gda.data.fileregistrar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.data.metadata.Metadata;

/**
 * Unlink IcatXMLCreatorTest, this uses an unmodified IcatXMLCreator and writes a file to a temporary directory
 */
public class IcatXMLCreatorFileWritingTest {

	final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private IcatXMLCreator xmlCreator;
	private String expectedHeader;
	private String expectedFooter;

	@Before
	public void setUp() throws Exception {
		xmlCreator = new IcatXMLCreator();

		final StringBuilder sbHeader = new StringBuilder();
		sbHeader.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sbHeader.append("<icat version=\"1.0 RC6\" xsi:noNamespaceSchemaLocation=\"icatXSD.xsd\" ");
		sbHeader.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
		sbHeader.append("<study> <investigation> \n");
		expectedHeader = sbHeader.toString();

		expectedFooter = "</investigation>\n</study>\n</icat>\n";
	}

	@Test
	public void testWritingDropFile() throws Exception {
		// Get a temporary directory and write an (empty) nexus file into the visit subdirectory
		final File outputDir = new File(TestHelpers.setUpTest(IcatXMLCreatorTest.class, "testWithFiles", true));
		final Path visitDir = Paths.get(outputDir.toString(), "cm19664-1");
		final Path nexusFilePath = Paths.get(visitDir.toString(), "ixx-766.nxs");
		final File nexusFile = nexusFilePath.toFile();

		try {
			// Create Nexus file (contents irrelevant)
			Files.createDirectory(visitDir);
			FileUtils.writeStringToFile(nexusFile, "Hello", StandardCharsets.UTF_8, false);

			// IcatXMLCreator writes creation date: get it here so we can check the XML later
			final Date creationDate = new Date(nexusFile.lastModified());
			final String creationDateString = dateFormatter.format(creationDate);

			// Create the drop file
			xmlCreator.setDirectory(outputDir.toString());
			xmlCreator.setFilePrefix("ixx");
			xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
			xmlCreator.registerFiles("scan-766", new String[] { nexusFilePath.toString() });

			// Check that it has created a drop file
			final File[] filesInOutputDir = outputDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					final String lowercaseName = name.toLowerCase();
					return lowercaseName.endsWith(".xml");
				}
			});
			assertEquals(1, filesInOutputDir.length);

			// Check file name
			final File outputFile = filesInOutputDir[0];
			assertTrue(outputFile.getName().matches("ixx-[0-9]+.xml"));

			// Check contents of drop file
			final StringBuilder expected = new StringBuilder(expectedHeader);
			expected.append(" <inv_number>CM19664</inv_number>\n");
			expected.append(" <visit_id>CM19664-1</visit_id>\n");
			expected.append(" <instrument>ixx</instrument>\n");
			expected.append(" <title>Test scan</title>\n");
			expected.append(" <inv_type>experiment</inv_type>\n");
			expected.append(" <dataset>\n");
			expected.append("   <name>topdir</name>\n");
			expected.append("   <dataset_type>EXPERIMENT_RAW</dataset_type>\n");
			expected.append("   <description>unknown</description>\n");
			expected.append("   <datafile>\n");
			expected.append("      <name>ixx-766.nxs</name>\n");
			expected.append("      <location>" + nexusFile.getAbsolutePath() + "</location>\n");
			expected.append("      <description>unknown</description>\n");
			expected.append("      <datafile_version>1.0</datafile_version>\n");
			expected.append("      <datafile_create_time>" + creationDateString + "</datafile_create_time>\n");
			expected.append("      <datafile_modify_time>" + creationDateString + "</datafile_modify_time>\n");
			expected.append("      <file_size>5</file_size>\n");
			expected.append("   </datafile>\n");
			expected.append(" </dataset>\n");
			expected.append(expectedFooter);

			final byte[] fileContents = Files.readAllBytes(outputFile.toPath());
			final String fileContentsString = new String(fileContents, StandardCharsets.UTF_8);
			assertEquals(expected.toString(), fileContentsString);
		} finally {
			if (outputDir.exists()) {
				FileUtils.deleteDirectory(outputDir);
			}
		}
	}

	private Metadata createMetadata(String instrument, String title, String visitId) throws Exception {
		final Metadata metadata = mock(Metadata.class);
		when(metadata.getMetadataValue(eq("instrument"), anyString(), anyString())).thenReturn(instrument);
		when(metadata.getMetadataValue("title")).thenReturn(title);
		when(metadata.getMetadataValue("visit")).thenReturn(visitId);
		return metadata;
	}

}
