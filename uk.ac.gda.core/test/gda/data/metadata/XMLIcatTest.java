/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.metadata;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.icat.Icat;
import gda.data.metadata.icat.IcatProvider;
import gda.jython.authenticator.UserAuthentication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class XMLIcatTest {

	private GdaMetadata metadata;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		LocalProperties.set(Icat.URL_PROP,IcatProviderTest.class.getResource("testicat.xml").getFile());
		LocalProperties.set(Icat.SHIFT_TOL_PROP,"1440");
		LocalProperties.set(Icat.ICAT_TYPE_PROP,gda.data.metadata.icat.XMLIcat.class.getName());

		metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);

		MetadataEntry inst = new StoredMetadataEntry("instrument", "p45");
		inst.configure();
		metadata.addMetadataEntry(inst);
		MetadataEntry fedid = new StoredMetadataEntry("federalid", "abc123");
		fedid.configure();
		metadata.addMetadataEntry(fedid);

		System.setProperty("user.name", "abc123");
		UserAuthentication.setToUseOSAuthentication();

	}

	/**
	 * 
	 */
	@Test
	public void testGetValidInformationFromIcat() {
		String test = "";
		try {
			// set now to be 14:47 9/7/9 when there was a duplicate on I24.
			DateFormat formatter = new SimpleDateFormat("HH:mm dd-MM-yy");
			Date date = formatter.parse("11:00 16-02-10");
			IcatProvider.deleteInstance();
			IcatProvider.getInstance().setOperatingDate(date);
			test = IcatProvider.getInstance().getMyInformation("visitID", "def456", null);
			assertTrue(test.contains(","));
			IcatProvider.getInstance().setMyVisit("gda456-1");
			// need to manually add these next 3 lines to the unit test here as in the real system this would be
			// performed by the JythonServer as it would check that this user has the baton to allow this metadata value
			// to be changed.
			MetadataEntry inst = new StoredMetadataEntry("visit", "gda456-1");
			inst.configure();
			metadata.addMetadataEntry(inst);
			test = IcatProvider.getInstance().getCurrentInformation("visitID");
			assertTrue(test.equals("gda456-1"));
			test = IcatProvider.getInstance().getCurrentInformation("beamline");
			assertTrue(test.equals("p45"));
			test = IcatProvider.getInstance().getCurrentInformation("experimentalTitle");
			assertTrue(test.equals("test experiment 2"));
		} catch (Exception e) {
			fail("exception occurred: " + e.getMessage());
		}
		IcatProvider.deleteInstance();
	}

}
