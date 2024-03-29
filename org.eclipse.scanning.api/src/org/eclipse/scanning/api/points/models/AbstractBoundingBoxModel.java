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
 * A model for a scan within a rectangular box in two-dimensional space.
 * <p>
 * This abstract class defines the box size and the names of the two axes.
 *
 * @author Colin Palmer
 *
 */
public abstract class AbstractBoundingBoxModel extends AbstractMapModel implements IBoundingBoxModel {

	/**
	 * The bounding box is automatically calculated from the scan regions shown in the main plot
	 */
	private BoundingBox boundingBox;

	protected AbstractBoundingBoxModel() {
		super();
	}

	protected AbstractBoundingBoxModel(String fastName, String slowName, BoundingBox box) {
		super(fastName, slowName);
		this.boundingBox  = box;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void setBoundingBox(BoundingBox newValue) {
		this.boundingBox = newValue;
	}

	@Override
	public void setxAxisName(String newValue) {
		if (boundingBox!=null) boundingBox.setxAxisName(newValue);
		super.setxAxisName(newValue);
	}

	@Override
	public void setyAxisName(String newValue) {
		if (boundingBox!=null) boundingBox.setyAxisName(newValue);
		super.setyAxisName(newValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((boundingBox == null) ? 0 : boundingBox.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		AbstractBoundingBoxModel other = (AbstractBoundingBoxModel) obj;
		if (boundingBox == null) {
			return other.boundingBox == null;
		}
		return boundingBox.equals(other.boundingBox);
	}

	@Override
	public String toString() {
		return "boundingBox=" + boundingBox + ", " + super.toString();
	}

}
