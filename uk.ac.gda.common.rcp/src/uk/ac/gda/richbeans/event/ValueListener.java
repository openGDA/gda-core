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
public interface ValueListener extends EventListener {

	/**
	 * Implement in notification code.
	 * @param e
	 */
	public void valueChangePerformed(ValueEvent e);
	
	/**
	 * May be implemented to return null or a name.
	 * 
	 * If a name is returned it ensures that anonymous
	 * listeners do not build up in the listener array.
	 * 
	 * ValueAdapter can be used to help by automatically
	 * implementing this method. ValueAdapter() is essentially
	 * the same as assuming you are adding a unique instance once
	 * and ValueAdapter(String name) defines the map key and protects
	 * against duplicates in code that may be called more than once.
	 * 
	 * @return name
	 */
	public String getValueListenerName();
	
}

	