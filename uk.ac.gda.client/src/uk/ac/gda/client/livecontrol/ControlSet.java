/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import java.util.List;

import gda.factory.FindableBase;

public class ControlSet extends FindableBase {

	private List<LiveControl> controls;

	private String viewName = "";

	private int numberOfColumns = 1;
	private boolean pack = false;

	public List<LiveControl> getControls() {
		return controls;
	}

	public void setControls(List<LiveControl> controls) {
		this.controls = controls;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

	public boolean isPack() {
		return pack;
	}

	public void setPack(boolean pack) {
		this.pack = pack;
	}

	public void dispose() {
		for (LiveControl control : controls) {
			control.dispose();
		}
	}
}
