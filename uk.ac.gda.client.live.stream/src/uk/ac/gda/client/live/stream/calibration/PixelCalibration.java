/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.calibration;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
/**
 * Create axes datasets for a data stream displaying camera pixel numbers on axes.
 */
public class PixelCalibration implements CalibratedAxesProvider {
	private IntegerDataset yAxis;
	private IntegerDataset xAxis;

	@Override
	public void connect() {
		// no-op
	}

	@Override
	public void disconnect() {
		// no-op
	}

	@Override
	public IDataset getXAxisDataset() {
		return xAxis;
	}

	@Override
	public IDataset getYAxisDataset() {
		return yAxis;
	}

	@Override
	public void resizeStream(int[] newShape) {
		xAxis = DatasetFactory.createLinearSpace(IntegerDataset.class, 0, newShape[1], newShape[1]);
		yAxis = DatasetFactory.createLinearSpace(IntegerDataset.class, 0, newShape[0], newShape[0]);
	}

}
