/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import java.util.List;

import gda.factory.Findable;
import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.Region;

public interface DefaultXanesRegions extends Findable {

	/**
	 * The list should be mutable as it is simply a sensible starting point, given element and edge.
	 */
	List<Region> getDefaultRegions(Element element, String edge);

	/**
	 * Necessary because {@link Region} does not specify final energy
	 */
	double getFinalEnergy(Element element, String edge);

}
