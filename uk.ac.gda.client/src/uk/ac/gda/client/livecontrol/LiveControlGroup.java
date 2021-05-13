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

package uk.ac.gda.client.livecontrol;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class LiveControlGroup extends LiveControlBase {

	private List<LiveControl> controls = Collections.emptyList();
	private int numColumns = 0;
	private boolean equalWidthColumns = true;

	@Override
	public void createControl(Composite parent) {
		if (numColumns > 0) {
			parent.setLayout(new GridLayout(numColumns, equalWidthColumns));
		}
		// Create the child widgets
		for (LiveControl control : controls) {
			control.createControl(parent);
		}
	}

	public void toggleIncrementControlDisplay() {
		controls.stream().filter(ScannablePositionerControl.class::isInstance)
				.map(ScannablePositionerControl.class::cast)
				.forEach(ScannablePositionerControl::toggleIncrementControlDisplay);
	}

	public void toggleShowStopButton() {
		controls.stream().filter(ScannablePositionerControl.class::isInstance)
				.map(ScannablePositionerControl.class::cast).forEach(ScannablePositionerControl::toggleShowStop);
	}

	public void setControls(List<LiveControl> controls) {
		this.controls = controls;
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public void setEqualWidthColumns(boolean equalWidthColumns) {
		this.equalWidthColumns = equalWidthColumns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((controls == null) ? 0 : controls.hashCode());
		result = prime * result + (equalWidthColumns ? 1231 : 1237);
		result = prime * result + numColumns;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiveControlGroup other = (LiveControlGroup) obj;
		if (controls == null) {
			if (other.controls != null)
				return false;
		} else if (!controls.equals(other.controls))
			return false;
		if (equalWidthColumns != other.equalWidthColumns)
			return false;
		if (numColumns != other.numColumns)
			return false;
		return true;
	}
}
