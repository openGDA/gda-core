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

import java.util.Calendar;

import junit.framework.TestCase;

public class MetadataEntryTest extends TestCase {
	
	public void testDateMetadataEntry() {
		DateMetadataEntry dme = new DateMetadataEntry("test", "yyyy-M-d");
		
		Calendar timeNow = Calendar.getInstance();
		int year = timeNow.get(Calendar.YEAR);
		int month = timeNow.get(Calendar.MONTH) + 1;
		int day = timeNow.get(Calendar.DAY_OF_MONTH);
		String expectedDate = year + "-" + month + "-" + day;
		
		assertEquals(expectedDate, dme.getMetadataValue());
	}
	
	public void testPropertyMetadataEntry() {
		PropertyMetadataEntry pme = new PropertyMetadataEntry("test", "user.name");
		String expectedUsername = System.getProperty("user.name");
		assertEquals(expectedUsername, pme.getMetadataValue());
	}

	public void testStoredMetadataEntry() {
		StoredMetadataEntry sme = new StoredMetadataEntry("test", "testvalue");
		assertEquals("testvalue", sme.getMetadataValue());
	}

}
