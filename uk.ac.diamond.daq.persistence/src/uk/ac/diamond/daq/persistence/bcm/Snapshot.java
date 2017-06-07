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

package uk.ac.diamond.daq.persistence.bcm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 */
@Entity
class Snapshot {
	private Date id;
	private Date parentId;
	private String modeName;
	private String tag;
	private Map<String, PositionEntry> entries = new HashMap<String, PositionEntry>();
	private long usecount;

	Snapshot() {
	}

	Snapshot(String modeName, Date id, Date parentId) {
		this.modeName = modeName;
		this.id = id;
		this.parentId = parentId;
	}

	/**
	 * @return Returns the id.
	 */
	@Id
	@Temporal(value = TemporalType.TIMESTAMP)
	Date getId() {
		return new BcmDate(id);
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	void setId(Date id) {
		this.id = id;
	}

	long getUsecount() {
		return usecount;
	}

	void setUsecount(long usecount) {
		this.usecount = usecount;
	}

	@PostLoad
	void incUsecount() {
		usecount++;
	}

	/**
	 * @return Returns the mode name.
	 */
	String getModeName() {
		return modeName;
	}

	/**
	 * @param modeName
	 *            The mode name to set.
	 */
	void setModeName(String modeName) {
		this.modeName = modeName;
	}

	/**
	 * @return Returns the tag.
	 */
	String getTag() {
		return tag;
	}

	/**
	 * @param tag
	 *            The tag to set.
	 */
	void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * This is the method to get the scannable nominal positions used within the BCM
	 *
	 * @return Returns the entries.
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@MapKey(name = "name")
	Map<String, PositionEntry> getEntries() {
		return entries;
	}

	void setEntries(Map<String, PositionEntry> entries) {
		this.entries = entries;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	Date getParentId() {
		return parentId;
	}

	void setParentId(Date parentId) {
		this.parentId = parentId;
	}

	void addPositionEntry(String scannableName, Double nompos, Double offset) {
		entries.put(scannableName, new PositionEntry(scannableName, nompos, offset));
	}

	void removePositionEntry(String scannableName) {
		entries.remove(scannableName);
	}

	@Override
	public String toString() {
		// TODO print pretty
		return new BcmDate(id).toString() + "/" + tag;
	}
}
