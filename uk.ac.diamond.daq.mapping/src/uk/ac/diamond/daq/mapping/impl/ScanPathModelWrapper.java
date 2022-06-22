/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import org.eclipse.scanning.api.points.models.IScanPathModel;

/**
 * A wrapper for an {@link IScanPathModel} for use in the mapping UI
 */
public class ScanPathModelWrapper<T extends IScanPathModel> extends ScanModelWrapper<T> {

	public ScanPathModelWrapper() {
		// no-arg constructor for json deserialization
	}

	public ScanPathModelWrapper(String name, T model, boolean includeInScan) {
		super(name, model, includeInScan);
	}

}
