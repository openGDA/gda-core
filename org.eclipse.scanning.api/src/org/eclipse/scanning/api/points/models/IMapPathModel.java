/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.points.models;

/**
 * A model for a scan in two-dimensional space.
 */
public interface IMapPathModel extends IScanPathModel {

	// Note: x and y must be in lower case in getter/setter names for JFace bindings to work correctly.
	/**
	 * The name of the scannable for the x-axis, i.e. the horizontal axis as plotted.
	 * @return x-axis name
	 */
	public String getxAxisName();
	public void setxAxisName(String newValue);

	/**
	 * The name of the scannable for the y-axis, i.e. the vertical axis as plotted.
	 * @return
	 */
	public String getyAxisName();
	public void setyAxisName(String newValue);

	/**
	 * The units for the x-axis
	 * @return
	 */
	public String getxAxisUnits();
	public void setxAxisUnits(String units);

	/**
	 * The units for the y-axis
	 * @return
	 */
	public String getyAxisUnits();
	public void setyAxisUnits(String units);

}
