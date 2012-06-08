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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A {@link MetadataEntry} that returns the date in a specified format.
 */
public class DateMetadataEntry extends MetadataEntry {

	protected String dateFormat;
	
	/**
	 * Creates a date metadata entry.
	 */
	public DateMetadataEntry() {
		// do nothing
	}
	
	/**
	 * Creates a date metadata entry with the specified date format.
	 * 
	 * @param name the metadata entry name
	 * @param dateFormat the date format string
	 */
	public DateMetadataEntry(String name, String dateFormat) {
		setName(name);
		setDateFormat(dateFormat);
	}
	
	/**
	 * Sets the date format.
	 * 
	 * @param dateFormat the date format
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	@Override
	public String readActualValue() throws Exception {
		Date date = new Date();
		Format formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(date);
	}

}
