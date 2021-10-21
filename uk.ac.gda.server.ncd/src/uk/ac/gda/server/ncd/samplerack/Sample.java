/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.samplerack;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

public class Sample implements Serializable {
	private boolean active = true;
	private String cell;
	private String name;
	private int frames;
	private double tpf;

	public Sample(String cell, String name, int frames, double tpf) {
		requireNonNull(cell, "Location can't be null");
		requireNonNull(name, "Name can't be null");
		if (frames <= 0 || tpf <= 0) {
			throw new IllegalArgumentException("Frame count and time per frame must be > 0");
		}
		this.cell = cell;
		this.name = name;
		this.frames = frames;
		this.tpf = tpf;
	}

	@Override
	public String toString() {
		return "Sample [active=" + active + ", cell=" + cell + ", name=" + name + ", frames=" + frames + ", tpf=" + tpf + "]";
	}

	public String getCell() {
		return cell;
	}
	public void setCell(String cell) {
		this.cell = cell;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getFrames() {
		return frames;
	}
	public void setFrames(int frames) {
		this.frames = frames;
	}
	public double getTpf() {
		return tpf;
	}
	public void setTpf(double tpf) {
		this.tpf = tpf;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public Sample copy() {
		return new Sample(cell, name, frames, tpf);
	}
}
