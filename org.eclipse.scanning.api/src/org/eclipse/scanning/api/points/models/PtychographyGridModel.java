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

package org.eclipse.scanning.api.points.models;

import java.util.Random;

public class PtychographyGridModel extends AbstractOverlapModel {

	private int seed = new Random().nextInt();
	private double randomOffset = 0.05;

	public PtychographyGridModel() {
		setName("Ptychography Grid");
		setOverlap(0.5);
	}

	private boolean snake = true;
	public boolean isSnake() {
		return snake;
	}
	public void setSnake(boolean snake) {
		boolean oldValue = this.snake;
		this.snake = snake;
		this.pcs.firePropertyChange("snake", oldValue, snake);
	}

	/**
	 * @return maximum offset as percentage of step size
	 */
	public double getRandomOffset() {
		return randomOffset;
	}

	public void setRandomOffset(double offset) {
		double oldOffset = randomOffset;
		randomOffset = offset;
		pcs.firePropertyChange("randomOffset", oldOffset, randomOffset);
	}

	public int getSeed() {
		return seed;
	}
}
