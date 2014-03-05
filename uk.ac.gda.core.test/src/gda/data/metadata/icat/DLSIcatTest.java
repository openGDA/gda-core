/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.data.metadata.icat;

import gda.data.metadata.icat.DLSIcat.Shift;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class DLSIcatTest extends TestCase {
	
	public void testCreateShortQuery() {
		final String accessName = "INV_NUMBER:investigation:id";
		final String expectedQuery = "select distinct INV_NUMBER from investigation where id='INV001' OR id='INV002'";
		
		List<Shift> shifts = Arrays.asList(
			new Shift("INV001", new Date(), new Date()),
			new Shift("INV002", new Date(), new Date())
		);
		
		String actualQuery = DLSIcat.createQuery(accessName, shifts);
		assertEquals(expectedQuery, actualQuery);
	}
	
	public void testCreateLongQuery() {
		final String accessName = "first_name, middle_name, last_name:facility_user where facility_user_id in (select facility_user_id from investigator:investigation_id";
		final String expectedQuery = "select distinct first_name, middle_name, last_name from facility_user where facility_user_id in (select facility_user_id from investigator where investigation_id='INV001' OR investigation_id='INV002')";
		
		List<Shift> shifts = Arrays.asList(
			new Shift("INV001", new Date(), new Date()),
			new Shift("INV002", new Date(), new Date())
		);
		
		String actualQuery = DLSIcat.createQuery(accessName, shifts);
		assertEquals(expectedQuery, actualQuery);
	}

}
