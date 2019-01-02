/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.timing.data;

import java.util.List;

public class TFGCoreConfiguration {
	private List<Double> thresholds;
	private List<Double> debounce;
	private int inversion;
	private int drive;
	private int startMethod;
	private boolean extInhibit;
	private int cycles;

	public List<Double> getThresholds() {
		return thresholds;
	}

	public void setThresholds(List<Double> thresholds) {
		this.thresholds = thresholds;
	}

	public List<Double> getDebounce() {
		return debounce;
	}

	public void setDebounce(List<Double> debounce) {
		this.debounce = debounce;
	}

	public int getInversion() {
		return inversion;
	}

	public void setInversion(int inversion) {
		this.inversion = inversion;
	}

	public int getDrive() {
		return drive;
	}

	public void setDrive(int drive) {
		this.drive = drive;
	}

	public int getStartMethod() {
		return startMethod;
	}

	public void setStartMethod(int startMethod) {
		this.startMethod = startMethod;
	}

	public boolean isExtInhibit() {
		return extInhibit;
	}

	public void setExtInhibit(boolean extInhibit) {
		this.extInhibit = extInhibit;
	}

	public int getCycles() {
		return cycles;
	}

	public void setCycles(int cycles) {
		this.cycles = cycles;
	}
}
