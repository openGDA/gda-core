/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.pes.api;

import java.io.Serializable;

import org.eclipse.january.dataset.IDataset;

public class LiveDataPlotUpdate implements Serializable {
	private IDataset xAxis = null;
	private IDataset yAxis = null;
	private IDataset Data = null;

	private Boolean updateSameFrame = false;

	private AcquisitionMode acquisitionMode;

	public void resetLiveDataUpdate() {
		setxAxis(null);
		setyAxis(null);
		setData(null);
		setUpdateSameFrame(false);
		setAcquisitionMode(null);
	}

	public void setAcquisitionMode(AcquisitionMode acquisitionMode) {
		this.acquisitionMode = acquisitionMode;
	}

	public AcquisitionMode getAcquisitionMode() {
		return acquisitionMode;
	}

	public IDataset getxAxis() {
		return xAxis;
	}

	public void setxAxis(IDataset xAxis) {
		this.xAxis = xAxis;
	}

	public IDataset getyAxis() {
		return yAxis;
	}

	public void setyAxis(IDataset yAxis) {
		this.yAxis = yAxis;
	}

	public IDataset getData() {
		return Data;
	}

	public void setData(IDataset data) {
		Data = data;
	}

	public Boolean isUpdateSameFrame() {
		return updateSameFrame;
	}

	public void setUpdateSameFrame(Boolean updateSameFrame) {
		this.updateSameFrame = updateSameFrame;
	}
}
