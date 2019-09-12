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

package gda.hrpd.sample.scans;

import static java.lang.Double.NaN;
import static java.lang.String.format;

import java.util.Collection;
import java.util.Map;

import gda.device.Scannable;

public class PsdScan extends ScanSetup {
	private final double delta;
	private final double step;
	private final double stop;

	public PsdScan(double collection, double delta, boolean spin, double spos, Map<Scannable, Object> initialPositions, Collection<String> backgroundScannables) {
		this(collection, delta, NaN, NaN, spin, spos, initialPositions, backgroundScannables);
	}

	public PsdScan(double collection, double start, double stop, double step, boolean spin, double spos, Map<Scannable, Object> initialPositions, Collection<String> backgroundScannables) {
		super(collection, spin, spos, initialPositions, backgroundScannables);
		this.delta = start;
		this.step = step;
		this.stop = stop;
	}

	public double getDelta() {
		return delta;
	}

	public double getStep() {
		return step;
	}

	public double getStop() {
		return stop;
	}

	public boolean isStatic() {
		return Double.isNaN(step) || Double.isNaN(stop);
	}

	@Override
	public Map<String, Object> asMap() {
		Map<String, Object> map = super.asMap();
		map.put("type", "psd");
		map.put("delta", isStatic() ? delta : format("%.3f, %.3f, %.3f", delta, stop, step));
		return map;
	}

	@Override
	public String toString() {
		if (isStatic()) {
			return "PSD Scan[psd " + getCollectionTime() + "]";
		} else {
			return "PSD Scan[scan delta " + delta + " " + stop + " " + step + " smythen " + getCollectionTime() + "]";
		}
	}
}
