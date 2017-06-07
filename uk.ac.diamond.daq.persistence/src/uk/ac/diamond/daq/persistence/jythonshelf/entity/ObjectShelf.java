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

package uk.ac.diamond.daq.persistence.jythonshelf.entity;

import static javax.persistence.CascadeType.ALL;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Java persistence API entity used by LocalJythonShelf.
 */
@Entity
public class ObjectShelf {
	private String name;
	private Collection<ObjectShelfEntry> entries;

	/**
	 * @return the shelf name
	 */
	@Id
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the shelves entries.
	 */
	@OneToMany(cascade = ALL, mappedBy = "shelf")
	public Collection<ObjectShelfEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 *            The configurations to set.
	 */
	public void setEntries(Collection<ObjectShelfEntry> entries) {
		this.entries = entries;
	}

}
