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

package uk.ac.gda.richbeans.components;

import uk.ac.gda.richbeans.event.ValueListener;

/**
 * Interface used to override bounds settings.
 * Used when input devices are bound to each other in value.
 * 
 * @author fcp94556
 *
 */
public interface BoundsProvider {
	/**
	 * The bound value
	 * @return double
	 */
    public double getBoundValue();
    
    /**
     * The acceptor of the BoundsProvider can also listen
     * to value changes from the BoundsProvider and update
     * it's bounds as required.
     * @param l
     */
    public void addValueListener(final ValueListener l);
}

	