/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import java.beans.PropertyChangeListener;

import org.eclipse.dawnsci.analysis.api.roi.IROI;

/**
 * Interface for region shapes supported for mapping scans
 *
 * @author James Mudd
 */
public interface IMappingScanRegionShape {

	/**
	 * @return The name of the shape
	 */
	public String getName();

	/**
	 * @return The ROI representation of the mapping region appropriate for the region shape
	 */
	public IROI toROI();

	/**
	 * @return The region type which can be used by the plotting system to display the region.
	 */
	public String whichPlottingRegionType();

	/**
	 * Update the values of the shape from a ROI obtained from the plotting system.
	 *
	 * @param newROI
	 *            The ROI to update from
	 */
	public void updateFromROI(IROI newROI);

	/**
	 * Classes implementing this interface must have property change support, to allow dynamic GUIs
	 *
	 * @param listener
	 *            to add
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Classes implementing this interface must have property change support, to allow dynamic GUIs
	 *
	 * @param listener
	 *            to remove
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);

}
