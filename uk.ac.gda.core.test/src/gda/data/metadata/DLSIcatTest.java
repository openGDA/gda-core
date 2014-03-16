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

import static org.junit.Assert.fail;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.icat.DLSIcat;
import gda.data.metadata.icat.Icat;
import gda.data.metadata.icat.IcatProvider;
import gda.jython.authenticator.UserAuthentication;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Test that the DLSIcat logic works
 */

public class DLSIcatTest {

	private GdaMetadata metadata;

	/**
	 * @throws Exception 
	 */
	@Before
	public void setUp() throws Exception {
		LocalProperties.set(Icat.URL_PROP,"jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(HOST=i24-control.diamond.ac.uk)(PROTOCOL=tcp)(PORT=1521))(CONNECT_DATA=(SID=xe)))");
		LocalProperties.set(Icat.SHIFT_TOL_PROP,"1440");
		LocalProperties.set(Icat.ICAT_TYPE_PROP,gda.data.metadata.icat.DLSIcat.class.getName());
		LocalProperties.set(DLSIcat.USER_PROP,"ikiti24");
		LocalProperties.set(DLSIcat.PASSWORD_PROP,"d24sb4k");

		metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);

		MetadataEntry fedid = new StoredMetadataEntry("federalid", "mzp47");
		fedid.configure();
		metadata.addMetadataEntry(fedid);

		MetadataEntry visitid = new StoredMetadataEntry("visit", "");
		visitid.configure();
		metadata.addMetadataEntry(visitid);
		
		System.setProperty("user.name", "mzp47");
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
			Calendar cal = Calendar.getInstance();
			cal.set(2009, 6, 9, 14, 47);
			Date date = new Date(cal.getTimeInMillis());
			IcatProvider.deleteInstance();
			((DLSIcat)IcatProvider.getInstance()).setInstrumentName("i24");
			IcatProvider.getInstance().setOperatingDate(date);
			test = IcatProvider.getInstance().getCurrentInformation("lower(visit_id)visit_id:investigation:id");
			
			Assert.assertEquals("mx1234-1", test);
			
			
		} catch (Exception e) {
			fail("exception occurred: " + e.getMessage());
		}
		IcatProvider.deleteInstance();

	}

}
