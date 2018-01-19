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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import gda.data.metadata.Metadata;

public class IcatXMLCreatorTest {

	private IcatXMLCreatorForTest xmlCreator;

	@Before
	public void setUp() {
		xmlCreator = new IcatXMLCreatorForTest();
	}

	@Test
	public void testRegisterSingleFile() throws Exception {
		// A file in the visit directory is given a dataset name "topdir"
		final String expected = "<inv_number>CM19664</inv_number>"
				+ "<visit_id>CM19664-1</visit_id>"
				+ "<instrument>ixx</instrument>"
				+ "<title>Test scan</title>"
				+ "<inv_type>experiment</inv_type>"
				+ "<dataset>"
				+ "<name>topdir</name>"
				+ "<dataset_type>EXPERIMENT_RAW</dataset_type>"
				+ "<description>unknown</description>"
				+ "<datafile>"
				+ "<name>ixx-766.nxs</name>"
				+ "<location>/scratch/temp/cm19664-1/ixx-766.nxs</location>"
				+ "<description>unknown</description>"
				+ "<datafile_version>1.0</datafile_version>"
				+ "<datafile_create_time>created</datafile_create_time>"
				+ "<datafile_modify_time>modified</datafile_modify_time>"
				+ "</datafile>"
				+ "</dataset>";

		xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		assertEquals(expected, normaliseResultString());
	}

	@Test
	public void testRegisterFilesInSubdirectories() throws Exception {
		// Files in subdirectories: dataset name is path relative to visit directory
		final String expected = "<inv_number>CM19664</inv_number>"
				+ "<visit_id>CM19664-1</visit_id>"
				+ "<instrument>ixx</instrument>"
				+ "<title>Test scan</title>"
				+ "<inv_type>experiment</inv_type>"
				+ "<dataset>"
				+ "<name>topdir</name>"
				+ "<dataset_type>EXPERIMENT_RAW</dataset_type>"
				+ "<description>unknown</description>"
				+ "<datafile><name>ixx-766.nxs</name>"
				+ "<location>/scratch/temp/cm19664-1/ixx-766.nxs</location>"
				+ "<description>unknown</description>"
				+ "<datafile_version>1.0</datafile_version>"
				+ "<datafile_create_time>created</datafile_create_time>"
				+ "<datafile_modify_time>modified</datafile_modify_time>"
				+ "</datafile>"
				+ "</dataset>"
				+ "<dataset>"
				+ "<name>files/panda</name>"
				+ "<dataset_type>EXPERIMENT_RAW</dataset_type>"
				+ "<description>unknown</description>"
				+ "<datafile>"
				+ "<name>ixx-766-panda.hdf</name>"
				+ "<location>/scratch/temp/cm19664-1/files/panda/ixx-766-panda.hdf</location>"
				+ "<description>unknown</description>"
				+ "<datafile_version>1.0</datafile_version>"
				+ "<datafile_create_time>created</datafile_create_time>"
				+ "<datafile_modify_time>modified</datafile_modify_time>"
				+ "</datafile>"
				+ "</dataset>"
				+ "<dataset>"
				+ "<name>files</name>"
				+ "<dataset_type>EXPERIMENT_RAW</dataset_type>"
				+ "<description>unknown</description>"
				+ "<datafile>"
				+ "<name>ixx-766.hdf</name>"
				+ "<location>/scratch/temp/cm19664-1/files/ixx-766.hdf</location>"
				+ "<description>unknown</description>"
				+ "<datafile_version>1.0</datafile_version>"
				+ "<datafile_create_time>created</datafile_create_time>"
				+ "<datafile_modify_time>modified</datafile_modify_time>"
				+ "</datafile>"
				+ "</dataset>";

		final String[] files = new String[] {
				"/scratch/temp/cm19664-1/ixx-766.nxs",
				"/scratch/temp/cm19664-1/files/panda/ixx-766-panda.hdf",
				"/scratch/temp/cm19664-1/files/ixx-766.hdf" };

		xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", files);

		assertEquals(expected, normaliseResultString());
	}

	@Test
	public void testRegisterFileNotUnderVisit() throws Exception {
		// A file not in the visit directory hierarchy: dataset name is full path
		// (This should never happen.)
		final String expected = "<inv_number>CM19664</inv_number>"
				+ "<visit_id>CM19664-1</visit_id>"
				+ "<instrument>ixx</instrument>"
				+ "<title>Test scan</title>"
				+ "<inv_type>experiment</inv_type>"
				+ "<dataset>"
				+ "<name>/scratch/temp/elsewhere</name>"
				+ "<dataset_type>EXPERIMENT_RAW</dataset_type>"
				+ "<description>unknown</description>"
				+ "<datafile>"
				+ "<name>ixx-766.nxs</name>"
				+ "<location>/scratch/temp/elsewhere/ixx-766.nxs</location>"
				+ "<description>unknown</description>"
				+ "<datafile_version>1.0</datafile_version>"
				+ "<datafile_create_time>created</datafile_create_time>"
				+ "<datafile_modify_time>modified</datafile_modify_time>"
				+ "</datafile>"
				+ "</dataset>";

		xmlCreator.setMetadata(createMetadata("ixx", "Test scan", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/elsewhere/ixx-766.nxs" });

		assertEquals(expected, normaliseResultString());
	}

	@Test
	public void testRegisterFileNoMetadata() throws Exception {
		// If metadata is missing, title & instrument default to "unknown",
		// visit defaults to "0-0"
		final String expected = "<inv_number>0</inv_number>"
				+ "<visit_id>0-0</visit_id>"
				+ "<instrument>unknown</instrument>"
				+ "<title>unknown</title>"
				+ "<inv_type>experiment</inv_type>"
				+ "<dataset>"
				+ "<name>/scratch/temp/cm19664-1</name>"
				+ "<dataset_type>EXPERIMENT_RAW</dataset_type>"
				+ "<description>unknown</description>"
				+ "<datafile>"
				+ "<name>ixx-766.nxs</name>"
				+ "<location>/scratch/temp/cm19664-1/ixx-766.nxs</location>"
				+ "<description>unknown</description>"
				+ "<datafile_version>1.0</datafile_version>"
				+ "<datafile_create_time>created</datafile_create_time>"
				+ "<datafile_modify_time>modified</datafile_modify_time>"
				+ "</datafile>"
				+ "</dataset>";

		xmlCreator.setMetadata(createMetadata(null, null, null));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		assertEquals(expected, normaliseResultString());
	}

	@Test
	public void testSanitiseMetadata() throws Exception {
		// Certain non-alphanumerics will be removed from instrument & title fields
		final String expected = "<inv_number>CM19664</inv_number>"
				+ "<visit_id>CM19664-1</visit_id>"
				+ "<instrument>ixx</instrument>"
				+ "<title>Test scan</title>"
				+ "<inv_type>experiment</inv_type>"
				+ "<dataset>"
				+ "<name>topdir</name>"
				+ "<dataset_type>EXPERIMENT_RAW</dataset_type>"
				+ "<description>unknown</description>"
				+ "<datafile>"
				+ "<name>ixx-766.nxs</name>"
				+ "<location>/scratch/temp/cm19664-1/ixx-766.nxs</location>"
				+ "<description>unknown</description>"
				+ "<datafile_version>1.0</datafile_version>"
				+ "<datafile_create_time>created</datafile_create_time>"
				+ "<datafile_modify_time>modified</datafile_modify_time>"
				+ "</datafile>"
				+ "</dataset>";

		// Special characters in instrument & title will be removed
		xmlCreator.setMetadata(createMetadata("i&x<x", "<Te\'st> &s\\c/a\"n", "cm19664-1"));
		xmlCreator.registerFiles("scan-766", new String[] { "/scratch/temp/cm19664-1/ixx-766.nxs" });

		assertEquals(expected, normaliseResultString());
	}

	/**
	 * Get the result string, removing carriage returns & trailing spaces and replacing file times with a defined string
	 */
	private String normaliseResultString() {
		return xmlCreator.getStringWriter().toString()
				.replaceAll("[\\n]", "")
				.replaceAll(">[\\s]*", ">")
				.replaceAll("[\\s]*<", "<")
				.replaceAll("<datafile_create_time>.*?</datafile_create_time>", "<datafile_create_time>created</datafile_create_time>")
				.replaceAll("<datafile_modify_time>.*?</datafile_modify_time>", "<datafile_modify_time>modified</datafile_modify_time>");
	}

	private Metadata createMetadata(String instrument, String title, String visitId) throws Exception {
		final Metadata metadata = mock(Metadata.class);
		when(metadata.getMetadataValue(eq("instrument"), anyString(), anyString())).thenReturn(instrument);
		when(metadata.getMetadataValue("title")).thenReturn(title);
		when(metadata.getMetadataValue("visit")).thenReturn(visitId);
		return metadata;
	}

	//-----------------------------------------------------------------------------------

	/**
	 * Subclass IcatXMLCreator so it writes to a String we can inspect.
	 */
	private class IcatXMLCreatorForTest extends IcatXMLCreator {

		@Override
		protected void createFile() throws IOException {
			fileWriter = new StringWriter();
		}

		@Override
		protected void closeFile() {
			// We don't want the file writer to be disposed
		}

		public StringWriter getStringWriter() {
			return (StringWriter) fileWriter;
		}
	}
}
