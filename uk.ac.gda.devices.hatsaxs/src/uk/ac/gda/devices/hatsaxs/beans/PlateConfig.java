/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hatsaxs.beans;

import java.io.Serializable;
import java.util.List;

import gda.factory.FindableBase;

public class PlateConfig extends FindableBase implements Serializable {
	private static final long serialVersionUID = 6948031255054246852L;
	private List<Plate> plates;
	private String[] availableCapillaries;

	public List<Plate> getPlates() {
		return plates;
	}
	public void setPlates(List<Plate> plates) {
		this.plates = plates;
	}

	public String[] getAvailablePlates() {
		String[] names = new String[plates.size()];
		int i = 0;
		for (Plate plate : plates) {
			names[i++] = plate.getName();
		}
		return names;
	}
	public Plate getPlate(int i) {
		return plates.get(i-1);
	}

	public String[] getAvailableCapillaries() {
		return availableCapillaries;
	}

	public void setAvailableCapillaries(String[] availableCapillaries) {
		this.availableCapillaries = availableCapillaries;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((plates == null) ? 0 : plates.hashCode());
		return result;
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
		PlateConfig other = (PlateConfig) obj;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		if (plates == null) {
			if (other.plates != null) {
				return false;
			}
		} else if (!plates.equals(other.plates)) {
			return false;
		}
		return true;
	}

}
