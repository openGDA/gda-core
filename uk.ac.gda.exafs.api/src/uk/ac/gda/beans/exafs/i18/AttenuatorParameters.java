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

package uk.ac.gda.beans.exafs.i18;

import java.io.Serializable;
import java.util.ArrayList;

public class AttenuatorParameters  implements Serializable{
	private static final long serialVersionUID = 8216501671758607731L;

	public AttenuatorParameters() {
		this.position = new ArrayList<String>();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AttenuatorParameters other = (AttenuatorParameters) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!position.equals(other.position)) {
			return false;
		}
		return true;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		+ ((name == null) ? 0 : name.hashCode());
		result = prime * result
		+ ((position == null) ? 0 : position.hashCode());
		return result;
	}
	private String name;
	private ArrayList<String> position;
	private String selectedPosition;


	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setPosition(ArrayList<String> position) {
		this.position = position;
	}
	public void addPosition(String position) {
		this.position.add(position);
	}
	public ArrayList<String> getPosition() {
		return position;
	}
	public void setSelectedPosition(String selectedPosition) {
		this.selectedPosition = selectedPosition;
	}
	public String getSelectedPosition() {
		return selectedPosition;
	}

	public void clear()
	{
		if(this.position != null)
		this.position.clear();
	}
}
