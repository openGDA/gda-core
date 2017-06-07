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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * * Java persistence API entity used by LocalJythonShelf.
 */
@Entity
public class ObjectShelfEntry {
	private int entryId;
	private ObjectShelf shelf;
	private String keyName;
	private byte[] data; // Serialized objects go in here

	/**
	 * @return Returns the id.
	 */
	@Id
	@GeneratedValue
	public int getId() {
		return entryId;
	}

	/**
	 * Will be generated automatically, no need to use this method explicitely.
	 *
	 * @param id
	 *            The id to set.
	 */
	public void setId(int id) {
		this.entryId = id;
	}

	/**
	 * @return Returns the shelf.
	 */
	@ManyToOne()
	@JoinColumn(name = "shelfid")
	// That's BcmMode.name!
	public ObjectShelf getShelf() {
		return shelf;
	}

	/**
	 * @param shelf
	 *            The shelf this entry is to join.
	 */
	public void setShelf(ObjectShelf shelf) {
		this.shelf = shelf;
	}

	/**
	 * @return Returns the key name.
	 */
	public String getKeyName() {
		return keyName;
	}

	/**
	 * @param key
	 *            The key name to set.
	 */
	public void setKeyName(String key) {
		this.keyName = key;
	}

	/**
	 * @return The data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param toStore
	 *            a byte array to store. Most likely from a serialized stream.
	 */
	public void setData(byte[] toStore) {
		this.data = toStore;
	}

}
