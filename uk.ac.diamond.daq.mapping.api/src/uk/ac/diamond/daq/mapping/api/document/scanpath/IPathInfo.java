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

package uk.ac.diamond.daq.mapping.api.document.scanpath;

import java.util.UUID;

/**
 * An object representing the result of a path calculation.
 * Sub-types will add more methods for the type of path they describe.
 */
public interface IPathInfo {

	/**
	 * A unique id of this path info.
	 * @return id
	 */
	UUID getEventId();

	/**
	 * The id of the source for this path calculation, i.e. a view.
	 * @return source id
	 */
	String getSourceId();

	/**
	 *
	 * @return the total point count
	 */
	int getTotalPointCount();

}
