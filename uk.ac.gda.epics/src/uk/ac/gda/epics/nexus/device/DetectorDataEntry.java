/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.nexus.device;

import org.eclipse.january.dataset.Dataset;

public class DetectorDataEntry<T extends Dataset> {
	private T value;
	private String name;
	private String units;
	private Boolean isDetectorEntry = false;
	private Boolean enabled = true;

	public DetectorDataEntry(T value, String name, String units) {
		this(value, name, units, false, true);
	}

	public DetectorDataEntry(T value, String name, String units, Boolean isDetectorEntry) {
		this(value, name, units, isDetectorEntry, true);
	}

	public DetectorDataEntry(T value, String name, String units, Boolean isDetectorEntry, Boolean enabled) {
		this.value = value;
		this.name = name;
		this.units = units;
		this.isDetectorEntry = isDetectorEntry;
		this.enabled = enabled;
	}

	public T getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getUnits() {
		return units;
	}

	public Boolean getIsDetectorEntry() {
		return isDetectorEntry;
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public void setIsDetectorEntry(Boolean isDetectorEntry) {
		this.isDetectorEntry = isDetectorEntry;
	}
}
