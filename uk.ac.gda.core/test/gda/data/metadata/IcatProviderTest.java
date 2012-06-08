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

import org.junit.Test;

/**
 * Tests if the icat provider responds to the gda.icat.usersCanUseDefVisit properly
 */
public class IcatProviderTest {

	@Test
	public void testGetMyValidVisits() {
		try {
			
			LocalProperties.set(Icat.URL_PROP,IcatProviderTest.class.getResource("testicat.xml").getFile());
			LocalProperties.set(Icat.SHIFT_TOL_PROP,"1440");
			LocalProperties.set(Icat.ICAT_TYPE_PROP,gda.data.metadata.icat.XMLIcat.class.getName());
			
			LocalProperties.set("gda.defVisit", "0-0");
			VisitEntry[] visit = IcatProvider.getInstance().getMyValidVisits("some user");
			assertTrue(visit.length == 0);
			
			// switch on and off property which means users with no entry in the icat can use the default instead
			LocalProperties.set("gda.icat.usersCanUseDefVisit", "true");
			visit = IcatProvider.getInstance().getMyValidVisits("some user");
			assertTrue(visit.length == 1);
			
			LocalProperties.set("gda.icat.usersCanUseDefVisit", "false");
			visit = IcatProvider.getInstance().getMyValidVisits("some user");
			assertTrue(visit.length == 0);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testConfigurationErrors() {
		LocalProperties.set(Icat.URL_PROP,IcatProviderTest.class.getResource("").getFile());
		LocalProperties.set(Icat.SHIFT_TOL_PROP,"1440");
		LocalProperties.set(Icat.ICAT_TYPE_PROP,gda.data.metadata.icat.XMLIcat.class.getName());
		
		boolean sawError = false;
		try {
			IcatProvider.getInstance().getMyValidVisits("some user");
		} catch (Exception e) {
			// at this point the GDA client should find the default visit and log what has happened
			sawError = true;
		}
		
		if (!sawError){
			fail("Error not thrown when Icat misconfigured");
		}		
	}

}
