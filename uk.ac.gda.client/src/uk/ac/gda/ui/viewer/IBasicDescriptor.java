/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.viewer;

/**
 * Minimal descriptor that can be used for widgets 
 * such as buttons. 
 * <p>
 * For example IBasicDescriptor is used with EnumSourceViewer 
 */
public interface IBasicDescriptor {

	/**
	 * Return text to be displayed in labels
	 * @return user friendly string
	 */
	public String getLabelText();
	
	/**
	 * Return text to displayed in tool tips, or empty string
	 * if none
	 * 
	 * @return user friendly string
	 */
	public String getToolTipText();
}
