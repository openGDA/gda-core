/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

/**
 * Extends {@link IMappingSection} for sections that can be shown or hidden dynamically
 */
public interface HideableMappingSection extends IMappingSection {

	/**
	 * Whether the controls in this section are currently visible
	 *
	 * @return true is the controls are visible, false if they are not
	 */
	boolean isVisible();

	/**
	 * Set the visibility of the controls in this section
	 *
	 * @param visible
	 *            true of the controls are to be made visible, false if they are to be hidden
	 */
	void setVisible(boolean visible);

}
