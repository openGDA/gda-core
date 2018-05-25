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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.metadata.MetadataEntry;
import gda.data.metadata.PropertyMetadataEntry;
import gda.data.metadata.icat.IcatProvider;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.util.HostId;
import gda.util.ObjectServer;
import gda.util.TestUtils;

/**
 * A Class for performing unit tests on a class for constructing paths based on templates. Separate Java properties and
 * object server XML files are associated with and used by this test. Their location is constructed within the test. By
 * default the tests are assumed to be under the users home directory under ${user.home}/gda/src... unless the Java
 * property gda.tests is set AS A VM ARGUMENT when running the test. In the latter case the test's Java property and XML
 * files are assumed to be under ${gda.tests}/gda/src...
 */
public class PathConstructorTest {
	private static Metadata metadata;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d.H.m");
	private String date = "2007-01-19.12.00";
	private String validId = "shk78";
	private static String year = null;
	private String propertyName = "test.property";

	@Before
	public void setUpBeforeEachTest() throws Exception {

		Finder.getInstance().removeAllFactories();
		GDAMetadataProvider.setInstanceForTesting(null);
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
		MetadataEntry entry = new PropertyMetadataEntry("facility", LocalProperties.GDA_FACILITY);
		metadata.addMetadataEntry(entry);
		entry = new PropertyMetadataEntry("instrument", LocalProperties.GDA_INSTRUMENT);
		metadata.addMetadataEntry(entry);
	}

	/**
	 * Test the construction of paths from templates. This test concentrates on the substitution of GDA metadata items
	 * that are of type MetadataEntry.PROPERTY.
	 */
	@Test
	public void testPathConstructorPropertyAccess() {
		System.setProperty(LocalProperties.GDA_FACILITY, "dls");
		System.setProperty(LocalProperties.GDA_INSTRUMENT, "i02");
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
	@Test
	public void testPathConstructorIkittenAccess() throws Exception {
		IcatProvider.getInstance().setOperatingDate(dateFormat.parse(date));
		metadata.setMetadataValue("federalid", validId);
		metadata.setMetadataValue("visit", "sp666-1");

		testPathConstructor("/dls/i18/data/2007/$visit$", "/dls/i18/data/2007/sp666-1");
		testPathConstructor("/dls/i18/data/2007/$proposal$-1", "/dls/i18/data/2007/sp666-1");

		IcatProvider.getInstance().setOperatingDate(null);
	}

	@Test
	public void testDefaultValues() {

		final int thisYear = LocalDate.now().getYear();
		final String hostId = HostId.getId();

		assertEquals("/0", PathConstructor.createFromTemplate("/$proposal$"));
		assertEquals("/0-0", PathConstructor.createFromTemplate("/$visit$"));
		assertEquals("/", PathConstructor.createFromTemplate("/$instrument$"));
		assertEquals("/", PathConstructor.createFromTemplate("/$facility$"));
		assertEquals("/" + thisYear, PathConstructor.createFromTemplate("/$year$"));
		assertEquals("/", PathConstructor.createFromTemplate("/$subdirectory$"));
		assertEquals("/" + hostId, PathConstructor.createFromTemplate("/$hostid$"));
	}

	@Test
	public void testDefaultProperties() {

		final ThreadLocalRandom random = ThreadLocalRandom.current();
		final String proposal = "cm" + random.nextInt(10000, 20000);
		final String visit = proposal + "-" + random.nextInt(1, 100);
		final String instrument = "p" + random.nextInt(10, 100);
		final String facility = "d" + random.nextInt(10, 100);

		System.setProperty(LocalProperties.GDA_DEF_VISIT, visit);
		System.setProperty(LocalProperties.GDA_INSTRUMENT, instrument);
		System.setProperty(LocalProperties.GDA_FACILITY, facility);

		final int thisYear = LocalDate.now().getYear();
		final String hostId = HostId.getId();

		assertEquals("/" + proposal, PathConstructor.createFromTemplate("/$proposal$"));
		assertEquals("/" + visit, PathConstructor.createFromTemplate("/$visit$"));
		assertEquals("/" + instrument, PathConstructor.createFromTemplate("/$instrument$"));
		assertEquals("/" + facility, PathConstructor.createFromTemplate("/$facility$"));
		assertEquals("/" + thisYear, PathConstructor.createFromTemplate("/$year$"));
		assertEquals("/", PathConstructor.createFromTemplate("/$subdirectory$"));
		assertEquals("/" + hostId, PathConstructor.createFromTemplate("/$hostid$"));
	}

	@Test
	public void testInvalidToken() {
		// Should not result in an exception, but use of invalid token should be logged
		assertEquals("/test/token", PathConstructor.createFromTemplate("/test/$invalid$/token"));
	}

	@After
	public void cleanUpAfterEachTest() {

		Finder.getInstance().removeAllFactories();
		GDAMetadataProvider.setInstanceForTesting(null);

		System.clearProperty(LocalProperties.GDA_DEF_VISIT);
		System.clearProperty(LocalProperties.GDA_FACILITY);
		System.clearProperty(LocalProperties.GDA_INSTRUMENT);
	}
}
