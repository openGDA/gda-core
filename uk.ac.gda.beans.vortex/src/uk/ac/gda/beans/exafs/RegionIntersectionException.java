/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs;

/**
 * Exception for overlapping regions
 */
public class RegionIntersectionException extends RegionException {

	private final Region start, end;
	/**
	 * @param start
	 * @param end
	 */
	public RegionIntersectionException(final Region start, final Region end) {
		super("Regions between "+start.getEnergy()+" and "+end.getEnergy()+" do not converge.");
		this.start = start;
		this.end   = end;
	}
	/**
	 * @return region start
	 */
	public Region getStart() {
		return start;
	}
	/**
	 * @return end of region
	 */
	public Region getEnd() {
		return end;
	}

}

	