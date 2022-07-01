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

import static org.eclipse.scanning.api.constants.PathConstants.X_AXIS_NAME;
import static org.eclipse.scanning.api.constants.PathConstants.Y_AXIS_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractMapModel extends AbstractPointsModel implements IMapPathModel {

	/** The name of the scannable in the x-axis direction as plotted, for instance 'stage_x'. */
	private String xAxisName = "stage_x";

	/** The name of the scannable in the y-axis direction as plotted, for instance 'stage_y'. */
	private String yAxisName = "stage_y";

	private String xAxisUnits = "mm";

	private String yAxisUnits = "mm";

	protected AbstractMapModel() {
		super();
	}

	protected AbstractMapModel(String xName, String yName) {
		this.xAxisName = xName;
		this.yAxisName = yName;
	}

	@Override
	public String getxAxisName() {
		return xAxisName;
	}

	@Override
	public void setxAxisName(String newValue) {
		String oldValue = this.xAxisName;
		this.xAxisName = newValue;
		this.pcs.firePropertyChange(X_AXIS_NAME, oldValue, newValue);
	}

	@Override
	public String getyAxisName() {
		return yAxisName;
	}

	@Override
	public void setyAxisName(String newValue) {
		String oldValue = this.yAxisName;
		this.yAxisName = newValue;
		this.pcs.firePropertyChange(Y_AXIS_NAME, oldValue, newValue);
	}

	@Override
	public String getxAxisUnits() {
		return xAxisUnits;
	}

	@Override
	public void setxAxisUnits(String xAxisUnits) {
		this.xAxisUnits = xAxisUnits;
	}

	@Override
	public String getyAxisUnits() {
		return yAxisUnits;
	}

	@Override
	public void setyAxisUnits(String yAxisUnits) {
		this.yAxisUnits = yAxisUnits;
	}

	@Override
	public List<String> getUnits(){
		List<String> dimensions = new ArrayList<>();
		dimensions.add(xAxisUnits);
		dimensions.add(yAxisUnits);
		return dimensions;
	}

	@Override
	public void setUnits(List<String> units) {
		setxAxisUnits(units.get(0));
		setyAxisUnits(units.get(1));
	}

	@Override
	public List<String> getScannableNames() {
		return Arrays.asList(getxAxisName(), getyAxisName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((xAxisName == null) ? 0 : xAxisName.hashCode());
		result = prime * result + ((yAxisName == null) ? 0 : yAxisName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		AbstractMapModel other = (AbstractMapModel) obj;
		if (xAxisName == null) {
			if (other.xAxisName != null)
				return false;
		} else if (!xAxisName.equals(other.xAxisName))
			return false;
		if (xAxisUnits == null) {
			if (other.xAxisUnits != null)
				return false;
		} else if (!xAxisUnits.equals(other.xAxisUnits))
			return false;
		if (yAxisUnits == null) {
			if (other.yAxisUnits != null)
				return false;
		} else if (!yAxisUnits.equals(other.yAxisUnits))
			return false;
		if (yAxisName == null) {
			return (other.yAxisName == null);
		}
		return yAxisName.equals(other.yAxisName);
	}

	@Override
	public String toString() {
		return "xAxisName=" + xAxisName + ", yAxisName=" + yAxisName + ", xAxisUnits="
				+ xAxisUnits + ", yAxisUnits=" + yAxisUnits + ", " + super.toString();
	}

}
