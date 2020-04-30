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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import gda.data.metadata.Metadata;
import gda.device.DeviceException;
import gda.factory.FactoryException;

public class IcatXMLCreatorTest {

	private IcatXMLCreatorForTest xmlCreator;
	private static final String EXPECTED_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<icat version=\"1.0 RC6\" xsi:noNamespaceSchemaLocation=\"icatXSD.xsd\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
			+ "<study> <investigation> \n";
	private static final String EXPECTED_FOOTER = "</investigation>\n</study>\n</icat>\n";

	@Before
	public void setUp() throws Exception {
		xmlCreator = new IcatXMLCreatorForTest();
	}

	@Test(expected = FactoryException.class)
	public void testConfigureFailsIfNoDirectorySet() throws Exception {
		xmlCreator.configure();
	}

	@Test
	public void testOutputFilePath() throws Exception {
		xmlCreator.setDirectory("/scratch/temp/cm19664-1");
		xmlCreator.setFilePrefix("ixx");

		xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		// Output file path should be constructed from the directory, filePrefix
		// and the creation time (as a long - to test for string of digits).
		final String outputFilePath = xmlCreator.getOutputFilePath().toString();
		assertTrue(outputFilePath.matches("/scratch/temp/cm19664-1/ixx-[0-9]+.xml"));
	}

	@Test
	public void testFilePrefixReadFromMetadataIfNotSet() throws Exception {
		xmlCreator.setDirectory("/scratch/temp/cm19664-1");
		xmlCreator.setMetadata(createMetadata("iyy", "Test scan", "cm19664-1"));
		xmlCreator.configure();

		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		final String outputFilePath = xmlCreator.getOutputFilePath().toString();
		assertTrue(outputFilePath.matches("/scratch/temp/cm19664-1/iyy-[0-9]+.xml"));
	}

	@Test(expected = FactoryException.class)
	public void testFilePrefixNotSetAndMetadataFails() throws Exception {
		final Metadata metadata = createMetadata("ixx", "Test scan", "cm19664-1");
		when(metadata.getMetadataValue(eq("instrument"), nullable(String.class), nullable(String.class)))
				.thenThrow(new DeviceException("Metadata error"));

		xmlCreator.setDirectory("/scratch/temp/cm19664-1");
		xmlCreator.setMetadata(metadata);
		xmlCreator.configure();
	}

	@Test
	public void testRegisterSingleFile() throws Exception {
		// A file in the visit directory is given a dataset name "topdir"
		final StringBuilder expected = new StringBuilder(EXPECTED_HEADER);
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
		expected.append("      <location>/scratch/temp/cm19664-1/ixx-766.nxs</location>\n");
		expected.append("      <description>unknown</description>\n");
		expected.append("      <datafile_version>1.0</datafile_version>\n");
		expected.append("      <datafile_create_time>created</datafile_create_time>\n");
		expected.append("      <datafile_modify_time>modified</datafile_modify_time>\n");
		expected.append("   </datafile>\n");
		expected.append(" </dataset>\n");
		expected.append(EXPECTED_FOOTER);

		xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		assertEquals(expected.toString(), normaliseResultString());
	}

	@Test
	public void testRegisterFilesInSubdirectories() throws Exception {
		// Files in subdirectories: dataset name is path relative to visit directory
		final StringBuilder expected = new StringBuilder(EXPECTED_HEADER);
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
		expected.append("      <location>/scratch/temp/cm19664-1/ixx-766.nxs</location>\n");
		expected.append("      <description>unknown</description>\n");
		expected.append("      <datafile_version>1.0</datafile_version>\n");
		expected.append("      <datafile_create_time>created</datafile_create_time>\n");
		expected.append("      <datafile_modify_time>modified</datafile_modify_time>\n");
		expected.append("   </datafile>\n");
		expected.append(" </dataset>\n");
		expected.append(" <dataset>\n");
		expected.append("   <name>files/panda</name>\n");
		expected.append("   <dataset_type>EXPERIMENT_RAW</dataset_type>\n");
		expected.append("   <description>unknown</description>\n");
		expected.append("   <datafile>\n");
		expected.append("      <name>ixx-766-panda.hdf</name>\n");
		expected.append("      <location>/scratch/temp/cm19664-1/files/panda/ixx-766-panda.hdf</location>\n");
		expected.append("      <description>unknown</description>\n");
		expected.append("      <datafile_version>1.0</datafile_version>\n");
		expected.append("      <datafile_create_time>created</datafile_create_time>\n");
		expected.append("      <datafile_modify_time>modified</datafile_modify_time>\n");
		expected.append("   </datafile>\n");
		expected.append(" </dataset>\n");
		expected.append(" <dataset>\n");
		expected.append("   <name>files</name>\n");
		expected.append("   <dataset_type>EXPERIMENT_RAW</dataset_type>\n");
		expected.append("   <description>unknown</description>\n");
		expected.append("   <datafile>\n");
		expected.append("      <name>ixx-766.hdf</name>\n");
		expected.append("      <location>/scratch/temp/cm19664-1/files/ixx-766.hdf</location>\n");
		expected.append("      <description>unknown</description>\n");
		expected.append("      <datafile_version>1.0</datafile_version>\n");
		expected.append("      <datafile_create_time>created</datafile_create_time>\n");
		expected.append("      <datafile_modify_time>modified</datafile_modify_time>\n");
		expected.append("   </datafile>\n");
		expected.append(" </dataset>\n");
		expected.append(EXPECTED_FOOTER);

		final String[] files = new String[] {
				"/scratch/temp/cm19664-1/ixx-766.nxs",
				"/scratch/temp/cm19664-1/files/panda/ixx-766-panda.hdf",
				"/scratch/temp/cm19664-1/files/ixx-766.hdf" };

		xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", files);

		assertEquals(expected.toString(), normaliseResultString());
	}

	@Test
	public void testRegisterFileNotUnderVisit() throws Exception {
		// A file not in the visit directory hierarchy: dataset name is full path
		// (This should never happen.)
		final StringBuilder expected = new StringBuilder(EXPECTED_HEADER);
		expected.append(" <inv_number>CM19664</inv_number>\n");
		expected.append(" <visit_id>CM19664-1</visit_id>\n");
		expected.append(" <instrument>ixx</instrument>\n");
		expected.append(" <title>Test scan</title>\n");
		expected.append(" <inv_type>experiment</inv_type>\n");
		expected.append(" <dataset>\n");
		expected.append("   <name>/scratch/temp/elsewhere</name>\n");
		expected.append("   <dataset_type>EXPERIMENT_RAW</dataset_type>\n");
		expected.append("   <description>unknown</description>\n");
		expected.append("   <datafile>\n");
		expected.append("      <name>ixx-766.nxs</name>\n");
		expected.append("      <location>/scratch/temp/elsewhere/ixx-766.nxs</location>\n");
		expected.append("      <description>unknown</description>\n");
		expected.append("      <datafile_version>1.0</datafile_version>\n");
		expected.append("      <datafile_create_time>created</datafile_create_time>\n");
		expected.append("      <datafile_modify_time>modified</datafile_modify_time>\n");
		expected.append("   </datafile>\n");
		expected.append(" </dataset>\n");
		expected.append(EXPECTED_FOOTER);

		xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/elsewhere/ixx-766.nxs" });

		assertEquals(expected.toString(), normaliseResultString());
	}

	@Test
	public void testRegisterFileNoMetadata() throws Exception {
		// If metadata is missing, title & instrument default to "unknown",
		// visit defaults to "0-0"
		final StringBuilder expected = new StringBuilder(EXPECTED_HEADER);
		expected.append(" <inv_number>0</inv_number>\n");
		expected.append(" <visit_id>0-0</visit_id>\n");
		expected.append(" <instrument>unknown</instrument>\n");
		expected.append(" <title>unknown</title>\n");
		expected.append(" <inv_type>experiment</inv_type>\n");
		expected.append(" <dataset>\n");
		expected.append("   <name>/scratch/temp/cm19664-1</name>\n");
		expected.append("   <dataset_type>EXPERIMENT_RAW</dataset_type>\n");
		expected.append("   <description>unknown</description>\n");
		expected.append("   <datafile>\n");
		expected.append("      <name>ixx-766.nxs</name>\n");
		expected.append("      <location>/scratch/temp/cm19664-1/ixx-766.nxs</location>\n");
		expected.append("      <description>unknown</description>\n");
		expected.append("      <datafile_version>1.0</datafile_version>\n");
		expected.append("      <datafile_create_time>created</datafile_create_time>\n");
		expected.append("      <datafile_modify_time>modified</datafile_modify_time>\n");
		expected.append("   </datafile>\n");
		expected.append(" </dataset>\n");
		expected.append(EXPECTED_FOOTER);

		xmlCreator.setMetadata(createMetadata(null, null, null));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		assertEquals(expected.toString(), normaliseResultString());
	}

	@Test
	public void testSanitiseMetadata() throws Exception {
		// Certain non-alphanumerics will be removed from instrument & title fields
		final StringBuilder expected = new StringBuilder(EXPECTED_HEADER);
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
		expected.append("      <location>/scratch/temp/cm19664-1/ixx-766.nxs</location>\n");
		expected.append("      <description>unknown</description>\n");
		expected.append("      <datafile_version>1.0</datafile_version>\n");
		expected.append("      <datafile_create_time>created</datafile_create_time>\n");
		expected.append("      <datafile_modify_time>modified</datafile_modify_time>\n");
		expected.append("   </datafile>\n");
		expected.append(" </dataset>\n");
		expected.append(EXPECTED_FOOTER);

		// Special characters in instrument & title will be removed
		xmlCreator.setMetadata(createMetadata("i&x<x", "<Te\'st> &s\\c/a\"n", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		assertEquals(expected.toString(), normaliseResultString());
	}

	/**
	 * Get the result string, replacing file times with a defined string
	 */
	private String normaliseResultString() {
		return xmlCreator.getStringWriter().toString()
				.replaceAll("<datafile_create_time>.*?</datafile_create_time>", "<datafile_create_time>created</datafile_create_time>")
				.replaceAll("<datafile_modify_time>.*?</datafile_modify_time>", "<datafile_modify_time>modified</datafile_modify_time>");
	}

	private Metadata createMetadata(String instrument, String title, String visitId) throws Exception {
		final Metadata metadata = mock(Metadata.class);
		when(metadata.getMetadataValue(eq("instrument"), nullable(String.class), nullable(String.class))).thenReturn(instrument);
		when(metadata.getMetadataValue("title")).thenReturn(title);
		when(metadata.getMetadataValue("visit")).thenReturn(visitId);
		return metadata;
	}

	//-----------------------------------------------------------------------------------

	/**
	 * Subclass IcatXMLCreator so it writes to a String we can inspect.
	 */
	private class IcatXMLCreatorForTest extends IcatXMLCreator {

		private Path outputFilePath;

		@Override
		protected void createFile(Path outputFilePath) throws IOException {
			this.outputFilePath = outputFilePath;
			fileWriter = new StringWriter();
		}

		@Override
		protected void closeFile() {
			// We don't want the string writer to be disposed
		}

		public StringWriter getStringWriter() {
			return (StringWriter) fileWriter;
		}

		public Path getOutputFilePath() {
			return outputFilePath;
		}
	}
}
