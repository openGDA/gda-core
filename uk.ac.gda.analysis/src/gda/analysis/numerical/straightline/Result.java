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
public class Result {
	private double slope;
	private double offset;

	/**
	 * @param slope
	 * @param offset
	 */
	public Result(double slope, double offset) {
		super();
		this.slope = slope;
		this.offset = offset;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Result)) {
			return false;
		}
		Result newResult = (Result) obj;
		if (newResult.getSlope() == getSlope()) {
			if (newResult.getOffset() == getOffset()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @return Returns the slope.
	 */
	public double getSlope() {
		return slope;
	}

	/**
	 * @return Returns the offset.
	 */
	public double getOffset() {
		return offset;
	}
}