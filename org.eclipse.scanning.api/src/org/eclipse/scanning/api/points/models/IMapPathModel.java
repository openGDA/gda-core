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

	public String getXAxisName();
	public void setXAxisName(String newValue);

	public String getYAxisName();
	public void setYAxisName(String newValue);

	public String getXAxisUnits();
	public void setXAxisUnits(String units);

	public String getYAxisUnits();
	public void setYAxisUnits(String units);

	public boolean isContinuous();
	public void setContinuous(boolean continuous);

}
