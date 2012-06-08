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

package gda.analysis.plotmanager;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.factory.Localizable;
import gda.observable.IObservable;
import gda.device.Device;

/**
 * IPlotManager Interface
 */
public interface IPlotManager extends Findable, IObservable, Configurable, Localizable, Device {

	/**
	 * @param panelName
	 * @param xAxis
	 * @param dataSets
	 */
	public void plot(String panelName, DoubleDataset xAxis, DoubleDataset... dataSets);

	/**
	 * @param panelName
	 * @param xAxis
	 * @param dataSets
	 */
	public void plotOver(String panelName, DoubleDataset xAxis, DoubleDataset... dataSets);

	/**
	 * @param panelName
	 * @param dataSets
	 */
	public void plotImage(String panelName, DoubleDataset... dataSets);
	
	/**
	 * @param panelName
	 * @param dataSets
	 */
	
	public void plotImages(String panelName, DoubleDataset... dataSets);
	
	/**
	 * @param panelName
	 * @param dataSets
	 */
	public void plot3D(String panelName, DoubleDataset...dataSets);
	
	/**
	 * @param panelName
	 * @param useWindow
	 * @param dataSets
	 */
	public void plot3D(String panelName, boolean useWindow, DoubleDataset...dataSets);
	
	/**
	 * @param panelName
	 * @param dataSets
	 */
	public void addPlot3D(String panelName, DoubleDataset...dataSets);

}
