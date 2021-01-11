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

import org.eclipse.swt.widgets.Widget;

/**
 * Locks some class features. Depending on the implementation, a class implementing this interface allows a second class to acquire some rights on the first.
 *
 * @author Maurizio Nagni
 */
public interface Lockable {

	/**
	 * This constant may be used as key to identify a LockableSelectable instance via {@link Widget#getData(String)}
	 */
	public static final String LOCKABLE_SELECTABLE = "LockableSelectable";

	/**
	 * Disable the selectable implementing this interface
	 * @param lock {@code true} to lock, {@code false} to unlock
	 */
	void lock(boolean lock);

}
