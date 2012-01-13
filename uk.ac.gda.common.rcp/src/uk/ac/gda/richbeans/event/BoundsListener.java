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

package uk.ac.gda.richbeans.event;

import java.util.EventListener;

/**
 * Listener interface for being notified of value changes
 * @author fcp94556
 *
 */
public interface BoundsListener extends EventListener {

	/**
	 * Called when value is greater than bounds
	 * @param e
	 */
	public void valueGreater(BoundsEvent e);
	
	/**
	 * Called when value is less than bounds
	 * @param e
	 */
	public void valueLess(BoundsEvent e);
	
	/**
	 * Called when value is in bounds
	 * @param e
	 */
	public void valueLegal(BoundsEvent e);

}

	