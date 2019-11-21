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

import java.io.Serializable;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * A model defining a box in two dimensional space, which can be used to confine and give scale to a {@link
 * IBoundingBoxModel}.
 * <p>
 * The two axes of the box are abstracted as "x" and "y" as they represent the horizontal and vertical axes of a plot.
 * Often these will be the X and Y stage motors, but other axes could be used depending on the beamline configuration
 * or the required experiment. The axis names to be used are defined in AbstractBoundingBoxModel.
 *
 * Important difference between BoundingBox and IRectangularROI - rois are in data coordinates and bounding boxes are
 * in axis coordinates i.e. locations of the motors rather than the selection of the data.
 *
 * @author Colin Palmer
 * @author Matthew Gerring
 *
 * @Deprecated Replaced by ScanRegion which is provided with the CompoundModel
 */
public class BoundingBox  implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3775847793520017725L;

	@FieldDescriptor(editable=false, fieldPosition=0)
	private String xAxisName="stage_x";

	@FieldDescriptor(scannable="xAxisName", fieldPosition=1)
	private double xAxisStart;

	@FieldDescriptor(scannable="xAxisName", validif="xAxisLength!=0", fieldPosition=2)
	private double xAxisLength;

	@FieldDescriptor(editable=false, fieldPosition=3)
	private String yAxisName="stage_y";

	@FieldDescriptor(scannable="yAxisName", fieldPosition=4)
	private double yAxisStart;

	@FieldDescriptor(scannable="yAxisName", validif="yAxisLength!=0", fieldPosition=5)
	private double yAxisLength;

	@FieldDescriptor(visible=false, hint="Provides information about the visible region we are linked to.")
	private String regionName;


	public BoundingBox() {

	}

	public BoundingBox(String xName, String yName) {
		this.xAxisName = xName;
		this.yAxisName = yName;
	}

	public BoundingBox(double xAxisStart, double yAxisStart, double xAxisLength, double yAxisLength) {
		super();
		this.xAxisStart = xAxisStart;
		this.yAxisStart = yAxisStart;
		this.xAxisLength = xAxisLength;
		this.yAxisLength = yAxisLength;
	}

	/**
	 *
	 * @param spt [xStart, yStart]
	 * @param ept [xEnd, yEnd]
	 */
	public BoundingBox(double[] spt, double[] ept) {

		double[] len = new double[2];
		double lx = ept[0] - spt[0];
		double ly = ept[1] - spt[1];
		@SuppressWarnings("unused")
		double ang = 0d; // TODO should be used?
		if (lx > 0) {
			if (ly > 0) {
				len[0] = lx;
				len[1] = ly;
				ang = 0;
			} else {
				len[0] = lx;
				len[1] = -ly;
				ang = Math.PI * 1.5;
			}
		} else {
			if (ly > 0) {
				len[0] = -lx;
				len[1] = ly;
				ang = Math.PI * 0.5;
			} else {
				len[0] = -lx;
				len[1] = -ly;
				ang = Math.PI;
			}
		}

		xAxisStart  = spt[0];
		xAxisLength = len[0];
		yAxisStart  = spt[1];
		yAxisLength = len[1];
	}


	// Note: x and y must be in lower case in getter/setter names for JFace bindings to work correctly.
	public double getxAxisStart() {
		return xAxisStart;
	}
	public void setxAxisStart(double xAxisStart) {
		this.xAxisStart = xAxisStart;
	}
	public double getyAxisStart() {
		return yAxisStart;
	}
	public void setyAxisStart(double yAxisStart) {
		this.yAxisStart = yAxisStart;
	}
	public double getxAxisLength() {
		return xAxisLength;
	}
	public void setxAxisLength(double xAxisLength) {
		this.xAxisLength = xAxisLength;
	}
	public double getyAxisLength() {
		return yAxisLength;
	}
	public void setyAxisLength(double yAxisLength) {
		this.yAxisLength = yAxisLength;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(yAxisLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xAxisLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xAxisStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yAxisStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundingBox other = (BoundingBox) obj;
		if (Double.doubleToLongBits(yAxisLength) != Double
				.doubleToLongBits(other.yAxisLength))
			return false;
		if (Double.doubleToLongBits(xAxisLength) != Double
				.doubleToLongBits(other.xAxisLength))
			return false;
		if (Double.doubleToLongBits(xAxisStart) != Double
				.doubleToLongBits(other.xAxisStart))
			return false;
		if (Double.doubleToLongBits(yAxisStart) != Double
				.doubleToLongBits(other.yAxisStart))
			return false;
		return true;
	}

	public String getxAxisName() {
		return xAxisName;
	}

	public void setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}

	public String getyAxisName() {
		return yAxisName;
	}

	public void setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public double getyAxisEnd() {
		return getyAxisStart()+getyAxisLength();
	}

	public double getxAxisEnd() {
		return getxAxisStart()+getxAxisLength();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +" [xAxisName=" + xAxisName + ", xAxisStart=" + xAxisStart + ", xAxisLength=" + xAxisLength + ", "
				+ "yAxisName=" + yAxisName + ", yAxisStart=" + yAxisStart + ", yAxisLength=" + yAxisLength + ", "
						+ "regionName=" + regionName + "]";
	}

}
