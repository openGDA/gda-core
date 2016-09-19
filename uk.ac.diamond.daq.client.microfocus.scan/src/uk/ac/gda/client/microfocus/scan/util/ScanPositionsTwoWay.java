/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.scan.util;

import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.scan.ScanBase;
import gda.scan.ScanPositionProvider;

/**
 * A ScanPositionProvider which flips every other line of data for use in two-way (back and forth) 2D map scans.
 */
public class ScanPositionsTwoWay implements ScanPositionProvider {

	private double start;
	private double stop;
	private double step;
	private double[] points;
	private boolean forward;

	public ScanPositionsTwoWay(Scannable firstScannable, double start, double stop, double step) throws Exception {
		this.start = start;
		this.stop = stop;
		this.step = (Double) ScanBase.sortArguments(start, stop, step);
		int numberSteps = ScannableUtils.getNumberSteps(firstScannable, this.start, this.stop, this.step);
		this.points = new double[numberSteps + 1];
		this.points[0] = start;
		double nextPoint = start;
		for (int i = 0; i <= numberSteps; i++) {
			this.points[i] = nextPoint;
			nextPoint += step;
		}

		// give some run-up to the first point. It seems that the trajectory scanning system seems
		// to expect the ScanPositionProvider to account for this, so this class should be doing this.
		if (start < stop) {
			this.points[0] -= step;
		} else {
			this.points[0] += step;
		}
		// this assumes that the step size is > the motor dead zone which seems reasonable

		this.forward = false;
	}

	@Override
	public Object get(int index) {
		int max_index = this.size() - 1;
		Object val = null;
		if (index > max_index)
			throw new IndexOutOfBoundsException("Position " + index + " is outside possible range : " + max_index);
		if (this.forward) {
			val = this.points[index];
			if (index == max_index)
				this.forward = false;
		} else {
			val = this.points[max_index - index];
			if (index == max_index)
				this.forward = true;
		}
		return val;
	}

	@Override
	public int size() {
		return points.length;
	}
}
