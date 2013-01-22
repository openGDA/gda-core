/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.rcp.views;



import org.eclipse.swt.graphics.Image;


public interface TabCompositeFactory extends CompositeFactory{

	/**
	 * Return a user-friendly name for this mode
	 * @return String name
	 */
	public abstract String getTooltip();
	

	/**
	 * Returns the image to be used when displaying the tab
	 * for this mode.
	 * <p>
	 * It is up to the mode to dispose of any image
	 * resources created.
	 * </p>
	 * @return a tab image, or null to use default image
	 */
	public abstract Image getImage();
	
	
	/**
	 * The label displayed on the tab 
	 */
	public abstract String getLabel();
	

}