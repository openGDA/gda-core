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

package uk.ac.diamond.daq.pes.api;

import java.io.Serializable;

public class DeflectorRange implements Serializable {

	private double deflectorXMinimum;
	private double deflectorXMaximum;

	public DeflectorRange(double deflectorXMinimum, double deflectorXMaximum) {
		this.deflectorXMinimum = deflectorXMinimum;
		this.deflectorXMaximum = deflectorXMaximum;
	}

	public double getDeflectorXMinimum() {
		return deflectorXMinimum;
	}

	public double getDeflectorXMaximum() {
		return deflectorXMaximum;
	}

	public boolean hasDeflectorEnabled() {
		return deflectorXMinimum != 0 || deflectorXMaximum != 0;
	}
}
