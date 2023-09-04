/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import org.eclipse.january.dataset.Dataset;

/**
 * interface that returns a Dataset from an input Dataset. The classic example is to return a dataset that
 * is a region of interest in the first
 */
public interface DatasetCreator {
	Dataset createDataSet(Dataset ds);

	/**
	 * Allows disabling and enabling the processing on the fly.  If disabled, {@link DatasetCreator#createDataSet(Dataset)} will just return the supplied dataset.
	 */
	public boolean isEnabled();
}
