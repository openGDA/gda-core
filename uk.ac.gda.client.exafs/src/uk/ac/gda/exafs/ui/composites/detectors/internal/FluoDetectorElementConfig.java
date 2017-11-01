/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import java.util.Map;

import gda.factory.Findable;

/**
 * This class is used to hold configuration to control how the detector elements in Fluorescence detector view are displayed.
 * Instances of this class should be created in the client side spring configuration for each detector to be customised;
 * use by setting the detector name {@link #setDetectorName(String)}, and grid order or {@link #setElementMap(Map)}.
 * @since 6/10/2017
 */
public class FluoDetectorElementConfig implements Findable {

	private String name;
	private String detectorName;
	private int gridOrder;
	private Map<Integer, Integer> elementMap;

	public int getGridOrder() {
		return gridOrder;
	}

	/**
	 * Set the detector element grid order :
	 * <li>0 = left to right top-to-bottom <li>1 = right to left, top-to-bottom
	 * @param gridOrder
	 */
	public void setGridOrder(int gridOrder) {
		this.gridOrder = gridOrder;
	}

	public Map<Integer, Integer> getElementMap() {
		return elementMap;
	}

	/**
	 * Set a custom Map defining the detector element order:
	 * <li> key = index in grid <li>value = detector index. </li>
	 * (Grid index runs from top to bottom, left to right)
	 * @param elementMap
	 */
	public void setElementMap(Map<Integer, Integer> elementMap) {
		this.elementMap = elementMap;
	}

	public String getDetectorName() {
		return detectorName;
	}

	/**
	 * Name of detector this configuration is to be used with
	 * @param detectorName
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	/**
	 * Name of this Findable
	 * @param name
	 */
	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public String getName() {
		return name;
	}
}
