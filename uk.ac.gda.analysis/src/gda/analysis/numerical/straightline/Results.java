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

package gda.analysis.numerical.straightline;

/*
 * Class to hold results from StraightLineFit class
 */
public class Results {
	final double[] offsets;
	final double[] slopes;
	final short[] fitok; 
	final long [] dims;
	public Results(double[] offsets, double[] slopes, long [] dims, short[] fitok) {
		super();
		this.offsets = offsets;
		this.slopes = slopes;
		this.dims = dims;
		this.fitok = fitok;
	}
	public double[] getOffsets() {
		return offsets;
	}
	public double[] getSlopes() {
		return slopes;
	}
	public long[] getDims() {
		return dims;
	}
	public short[] getFitok() {
		return fitok;
	}
	
}
