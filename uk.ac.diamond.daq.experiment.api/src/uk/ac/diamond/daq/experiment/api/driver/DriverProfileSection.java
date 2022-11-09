/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public class DriverProfileSection implements EditableWithListWidget, Serializable {

	private static final long serialVersionUID = 689298404406092807L;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private double start;
	private double stop;
	private double duration;

	public DriverProfileSection() {}

	public DriverProfileSection(double start, double stop, double duration) {
		this.start = start;
		this.stop = stop;
		this.duration = duration;
	}

	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		double old = this.start;
		this.start = start;
		pcs.firePropertyChange("start", old, start);
		refresh();
	}
	public double getStop() {
		return stop;
	}
	public void setStop(double stop) {
		double old = this.stop;
		this.stop = stop;
		pcs.firePropertyChange("stop", old, stop);
		refresh();
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		double old = this.duration;
		this.duration = duration;
		pcs.firePropertyChange("duration", old, duration);
		refresh();
	}

	private void refresh() {
		pcs.firePropertyChange(EditableWithListWidget.REFRESH_PROPERTY, null, null);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public String getLabel() {
		String description;
		if (start < stop) {
			description = "Rise";
		} else if (start > stop) {
			description = "Fall";
		} else {
			description = "Hold";
		}
		return description + " (" + duration + " min)";
	}

	@Override
	public EditableWithListWidget createDefault() {
		DriverProfileSection defaultSection = new DriverProfileSection();
		defaultSection.setStart(0.0);
		defaultSection.setStop(1.0);
		defaultSection.setDuration(1.0);
		return defaultSection;
	}

	@Override
	public String toString() {
		return "DriverProfileSection [from " + start + " to " + stop + " in " + duration + " m]";
	}

}
