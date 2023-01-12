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

package uk.ac.diamond.daq.gasrig.api;

import java.io.Serializable;

public class GasRigSequenceUpdate implements Serializable {

	private String name;
	private String status;
	private double percentComplete;

	public GasRigSequenceUpdate(String name, String status, double percentComplete) {
		this.name = name;
		this.status = status;
		this.percentComplete = percentComplete;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public double getPercentComplete() {
		return percentComplete;
	}

	@Override
	public String toString() {
		return "GasRigSequenceUpdate [name=" + name + ", status=" + status + ", percentComplete=" + percentComplete
				+ "]";
	}
}
