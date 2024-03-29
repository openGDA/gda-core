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

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.AbstractNameable;

/**
 *
 * A scan region encapsulates a geometric region of interest with
 * the names of the scan axes over which it is a region.
 * If the ScanRegion contains no ROI, it is treated as a SquashingExcluder,
 * collapsing all dimensions that contain at least one of its axis into
 * a single Dimension.
 *
 * @author Matthew Gerring
 *
 * @param <T>
 */
public class ScanRegion extends AbstractNameable {

	private Object       type;
	private IROI            roi;
	private List<String> scannables;

	public ScanRegion() {
		// We are a bean
	}
	public ScanRegion(String name) {
		setName(name);
	}
	public ScanRegion(String name, Object type, List<String> snames) {
		setName(name);
		this.type = type;
		this.scannables = snames;
	}

	public ScanRegion(String name, List<String> axes) {
		this(name, null, axes);
	}

	public ScanRegion(IROI roi, List<String> names) {
		this.roi = roi;
		this.scannables = names;
	}
	public ScanRegion(IROI roi, String... names) {
		this.roi = roi;
		this.scannables = Arrays.asList(names);
	}

	public IROI getRoi() {
		return roi;
	}
	public void setRoi(IROI roi) {
		this.roi = roi;
	}
	public List<String> getScannables() {
		return scannables;
	}
	public void setScannables(List<String> scannables) {
		this.scannables = scannables;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roi == null) ? 0 : roi.hashCode());
		result = prime * result + ((scannables == null) ? 0 : scannables.hashCode());
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
		ScanRegion other = (ScanRegion) obj;
		if (roi == null) {
			if (other.roi != null)
				return false;
		} else if (!roi.equals(other.roi))
			return false;
		if (scannables == null) {
			if (other.scannables != null)
				return false;
		} else if (!scannables.equals(other.scannables))
			return false;
		return true;
	}

	public Object getType() {
		return type;
	}
	public void setType(Object type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (getName() != null) {
			buf.append(getName());
			buf.append(" ");
		}
		if (roi!=null) {
			buf.append("Type: [");
			buf.append(roi.getClass().getSimpleName());
			buf.append(": ");
			buf.append(roi);
			buf.append("], ");
		}
		if (scannables!=null) {
			buf.append("Axes: ");
			buf.append(scannables);
		}
		return buf.toString();
	}
}
