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
 * Implementers of <code>NamedCompositeFactory</code> create a {@link CompositeFactory} which can be identified on the GUI.
 *
 * <p>
 * Classes implementing this interface can be used as factories for dynamic GUI, that is to create Composite on demand from a button text provided by {@link #getName()}
 * with a tooltip provided by {@link #getTooltip()}
 * </p>
 *
 * @author Maurizio Nagni
 */
public interface NamedCompositeFactory extends CompositeFactory {
	/**
	 * The human friendly name to identify this class in the GUI
	 * @return the factory name
	 */
	ClientMessages getName();

	/**
	 * The tooltip providing a brief info about this class
	 * @return the factory tooltip
	 */
	ClientMessages getTooltip();
}