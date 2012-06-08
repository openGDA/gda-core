/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import java.awt.Component;
import java.awt.Container;

/**
 * A class which provides the means to enable or disable a Component and all its descendents (normally setting enabled
 * on a Component has no effect on Components within it).
 */
public class RecursiveEnabler {
	/**
	 * Sets the enabled state of a Component and (recursively) all its descendents.
	 * 
	 * @param component
	 *            the Component to be changed
	 * @param enabled
	 *            the new value
	 */
	public static void setEnabled(Component component, boolean enabled) {
		// Set the Component's own state.
		component.setEnabled(enabled);

		// If the Component is a Container call this method again for
		// each child.
		if (component instanceof Container) {
			Component[] children = ((Container) component).getComponents();
			for (int i = 0; i < children.length; i++)
				setEnabled(children[i], enabled);
		}
	}

}