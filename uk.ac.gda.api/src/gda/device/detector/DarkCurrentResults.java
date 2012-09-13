/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector;

public class DarkCurrentResults {

	private Double timeInS = 1d;
	private Double[] counts;

	public DarkCurrentResults(Double timeInS, Double[] counts) {
		super();
		this.timeInS = timeInS;
		this.counts = counts;
	}

	public Double getTimeInS() {
		return timeInS;
	}

	public void setTimeInS(Double timeInS) {
		this.timeInS = timeInS;
	}

	/**
	 * @return raw counts collected, not corrected by collection time into a rate
	 */
	public Double[] getCounts() {
		return counts;
	}

	public void setCounts(Double[] counts) {
		this.counts = counts;
	}
}
