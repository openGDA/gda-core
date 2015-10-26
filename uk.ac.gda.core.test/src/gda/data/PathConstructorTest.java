/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data;

import static org.junit.Assert.assertEquals;
import gda.data.metadata.Metadata;
import gda.data.metadata.MetadataEntry;
import gda.data.metadata.PropertyMetadataEntry;
import gda.data.metadata.icat.IcatProvider;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.ObjectServer;
import gda.util.TestUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A Class for performing unit tests on a class for constructing paths based on templates. Separate Java properties and
 * object server XML files are associated with and used by this test. Their location is constructed within the test. By
 * default the tests are assumed to be under the users home directory under ${user.home}/gda/src... unless the Java
 * property gda.tests is set AS A VM ARGUMENT when running the test. In the latter case the test's Java property and XML
 * files are assumed to be under ${gda.tests}/gda/src...
 */
@Ignore("2010/06/07 Test ignored since not passing GDA-3276")
public class PathConstructorTest {
	private static Metadata metadata;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d.H.m");
	private String date = "2007-01-19.12.00";
	private String validId = "shk78";
	private static String year = null;
	private String propertyName = "test.property";

	/**
	 * Test setup to be run once at the start.
	 * @throws FactoryException
	 */
	@BeforeClass()
	public static void setUpBeforeClass() throws Exception {
		/*
		 * The following line is required to ensure that the default LocalProperties are obtained from the test's Java
		 * properties file. The property gda.propertiesFile must be set BEFORE LocalProperties is used and thus it's
		 * static block is invoked.
		 */
		System.setProperty("gda.propertiesFile", TestUtils.getResourceAsFile(PathConstructorTest.class, "java.properties").getAbsolutePath());

		ObjectServer.createLocalImpl(TestUtils.getResourceAsFile(PathConstructorTest.class, "metadata_test_beans.xml").getAbsolutePath());
		metadata = (Metadata) Finder.getInstance().find("GDAMetadata");

		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyy");
		year = formatter.format(date);
	}

	/**
	 * Test the construction of paths from templates. This test concentrates on the use of default Java properties where
	 * the special cases for properties do not exist in GDA metadata.
	 *
	 * @throws DeviceException
	 */
	@Test()
	public void testPathConstructorDefaultProperties() throws DeviceException {
		testPathConstructorPropertyAccess();

		/*
		 * Add these metadata entries here rather than via the XML configuration so that this test can use the default
		 * property names that the path construction uses. Other tests will use the values in the metadata entries.
		 */
		MetadataEntry entry = new PropertyMetadataEntry("facility", "gda.facility");
		metadata.addMetadataEntry(entry);
		entry = new PropertyMetadataEntry("instrument", "gda.instrument");
		metadata.addMetadataEntry(entry);
	}

	/**
	 * Test the construction of paths from templates. This test concentrates on the substitution of GDA metadata items
	 * that are of type MetadataEntry.PROPERTY.
	 */
	@Test
	public void testPathConstructorPropertyAccess() {
		testPathConstructor("/$facility$/$instrument$/data/2007/sp0-0", "/dls/i02/data/2007/sp0-0");
	}

	private void testPathConstructor(String given, String expected) {
		System.setProperty(propertyName, given);
		assertEquals("Path contruction failed", expected, PathConstructor.createFromTemplate(given));
		assertEquals("Path contruction failed", expected, PathConstructor.createFromProperty(propertyName));
	}

	/**
	 * Test the construction of paths from templates. This test concentrates on the substitution of GDA metadata items
	 * that are of type MetadataEntry.DATE.
	 */
	@Test
	public void testPathConstructorDateAccess() {
		testPathConstructor("/dls/i18/data/$year$/sp0-0", "/dls/i18/data/" + year + "/sp0-0");
	}

	/**
	 * Test the construction of paths from templates. This test concentrates on the substitution of GDA metadata items
	 * that are of type MetadataEntry.ICAT.
	 * @throws Exception
	 */
	// These tests are not working. There does not appear to be entries for proposal or visit.
	// @Test
	public void testPathConstructorIkittenAccess() throws Exception {
		IcatProvider.getInstance().setOperatingDate(dateFormat.parse(date));
		metadata.setMetadataValue("federalid", validId);

		testPathConstructor("/dls/i18/data/2007/sp0-$visit$", "/dls/i18/data/2007/sp0-1");
		testPathConstructor("/dls/i18/data/2007/sp$proposal$-1", "/dls/i18/data/2007/sp666-1");
		testPathConstructor("/dls/i18/data/2007/sp$proposal$-$visit$", "/dls/i18/data/2007/sp666-1");

		IcatProvider.getInstance().setOperatingDate(null);
	}
}
