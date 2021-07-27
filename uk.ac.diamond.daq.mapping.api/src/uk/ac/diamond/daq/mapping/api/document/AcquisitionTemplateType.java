/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document;

/**
 * Defines some keys to identifies different types of {@link AcquisitionTemplate}
 *
 * @author Maurizio Nagni
 */
public enum AcquisitionTemplateType {
	/**
	 * Identifies a point in a 2D space
	 */
	TWO_DIMENSION_POINT,
	/**
	 * Identifies a line in a 2D space
	 */
	TWO_DIMENSION_LINE,
	/**
	 * Identifies a rectangle in a 2D space
	 */
	TWO_DIMENSION_GRID,
	/**
	 * Identifies a line in a 1D space
	 */
	ONE_DIMENSION_LINE,
	/**
	 * Identifies a static point
	 */
	STATIC_POINT
}
