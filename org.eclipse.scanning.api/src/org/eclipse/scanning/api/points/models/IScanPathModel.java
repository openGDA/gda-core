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

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.INameable;

/**
 * A model which defines the movement parameters for one or more dimensions of a scan.
 *
 * @author Colin Palmer
 *
 */
public interface IScanPathModel extends INameable {

    /**
     * The names of the axes which will be scanned by this model.
     * @return
     */
	default List<String> getScannableNames() {
		return Arrays.asList(getName());
	}

	/**
	 * Property change support
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Property change support
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);

	// Pre-region/excluder/mutator size/shape/rank- for use of MultiModels e.g. Zip
	public int size();

	public default int[] shape() {
		return new int[] {size()};
	}

	public default int rank() {
		return shape().length;
	}

	/**
	 * If <code>true</code> the scan is continuous if possible, i.e. the motors continue to move while the detectors
	 * are exposed, if <code>false</code> the motors stop at each point for the detectors are exposed.
	 * For a scan to be continous, generally it must done by malcolm device, i.e. for a GDA point of view the
	 * scan contains exactly one runnable device and that must be a malcolm device. Additionally continuous scanning
	 * is only possible for certain path models, e.g. {@link TwoAxisGridPointsModel}, where even then the scan will only
	 * be continuous in the fast axis, and step in the slow axis, where the x-axis is the fast axis by default.
	 * @return <code>true</code> if the scan should be continuous, <code>false</code> otherwise.
	 */
	public boolean isContinuous();
	public void setContinuous(boolean continuous);

	/**
     * This setting only makes sense when there is another model outside of this model within
     * a CompoundModel: e.g. a StepModel in Energy outside of a Spiral in x, y will run the
     * points of the Spiral backwards every alternate step of Energy, preventing having to return
     * to starting positions of that scan.
     * In the special case of Grid shaped scans both axes of the scan alternate, so it runs:
     * bottom left -> bottom right -> top right -> top left, top left -> top right...
     * @return <code>true</code> if the scan is a alternating, <code>false</code> otherwise
 	 */
	public boolean isAlternating();
	public void setAlternating(boolean continuous);

}