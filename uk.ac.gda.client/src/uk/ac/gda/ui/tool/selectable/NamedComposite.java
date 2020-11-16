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

package uk.ac.gda.ui.tool.selectable;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Adds to a composite factory a name and tooltip properties so it may be used as element in a collection.
 *
 * @author Maurizio Nagni
 */
public interface NamedComposite extends CompositeFactory {
	/**
	 * The name to display to the user when this factory is selectable
	 * @return the factory name
	 */
	ClientMessages getName();

	/**
	 * The tooltip to display to the user when this factory is selectable
	 * @return the factory tooltip
	 */
	ClientMessages getTooltip();
}
