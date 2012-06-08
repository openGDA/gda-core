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

import gda.data.metadata.icat.IcatProvider;

/**
 * {@link MetadataEntry} that executes an ICAT query.
 */
public class IcatMetadataEntry extends MetadataEntry {

	protected String query;
	
	/**
	 * Creates an ICAT metadata entry.
	 */
	public IcatMetadataEntry() {
		// do nothing
	}
	
	/**
	 * Creates an ICAT metadata entry with the specified name and query.
	 * 
	 * @param name the metadata entry name
	 * @param query the value
	 */
	public IcatMetadataEntry(String name, String query) {
		setName(name);
		setQuery(query);
	}
	
	/**
	 * Sets the query that will be executed.
	 * 
	 * @param query the query
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	
	@Override
	public String readActualValue() throws Exception {
		return IcatProvider.getInstance().getCurrentInformation(query);
	}

}
