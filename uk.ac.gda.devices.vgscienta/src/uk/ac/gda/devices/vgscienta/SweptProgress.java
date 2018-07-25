/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import java.io.Serializable;

public class SweptProgress implements Serializable {
	private final int currentPoint;
	private final int max;
	private final int percent;
	private final int currentIter;

	public SweptProgress(int currentPoint, int max, int percent, int currentIter) {
		this.currentPoint = currentPoint;
		this.max = (max >= currentPoint) ? max : currentPoint;
		this.percent = percent;
		this.currentIter = currentIter;
	}

	public int getCurrentPoint() {
		return currentPoint;
	}

	public int getMax() {
		return max;
	}

	public int getPercent() {
		return percent;
	}

	public int getCurrentIter() {
		return currentIter;
	}

	@Override
	public String toString() {
		return String.format("SweptProgress with sweep #%d of %d = %d%% complete. Current Iteration number is %d.",
				currentPoint, max, percent, currentIter);
	}
}
