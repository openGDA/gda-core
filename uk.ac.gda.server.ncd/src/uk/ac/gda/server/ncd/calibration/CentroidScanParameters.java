/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.calibration;

import java.util.function.DoubleFunction;

import gda.scan.ScanPositionProvider;

/** Configuration that holds the parameters used to build a centroid scan position provider */
public class CentroidScanParameters implements DoubleFunction<ScanPositionProvider> {
	private final double before;
	private final double after;
	private final double step;

	public CentroidScanParameters(double before, double after, double step) {
		this.before = before;
		this.after = after;
		this.step = step;

	}
	@Override
	public ScanPositionProvider apply(double centre) {
		return new ScanPoints(centre-before, centre+after, step);
	}

	public static ScanPositionProvider symmetric(double centre, double range, double step) {
		return new CentroidScanParameters(range, range, step).apply(centre);
	}
}

/** {@link ScanPositionProvider} that uses start, stop and step to calculate points */
class ScanPoints implements ScanPositionProvider {
	private double start;
	private double stop;
	private double step;

	ScanPoints(double start, double stop, double step) {
		this.start = start;
		this.stop = stop;
		this.step = step;
	}

	@Override
	public Object get(int index) {
		return start + index * step;
	}

	@Override
	public int size() {
		var range = Math.abs(stop - start);
		var steps = (int)(range/step);
		var remaining = range - (steps * step);
		if (remaining/step > 0.999) {
			steps += 1;
		}
		return steps + 1;
	}

	@Override
	public String toString() {
		return String.format("ScanPoints(%f, %f, %f)", start, stop, step);
	}
}
