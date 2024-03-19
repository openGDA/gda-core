/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

import org.eclipse.january.dataset.IDataset;

public class MbsLiveDataUpdate {
	private IDataset xAxis = null;
	private IDataset yAxis = null;
	private IDataset zAxis = null;
	private IDataset Data = null;
	private Boolean accumulate = false;
	private String acquisitionMode;

	public MbsLiveDataUpdate() {
	}

//	public MbsLiveDataUpdate(IDataset xAxis, IDataset yAxis, IDataset zAxis, IDataset Data, boolean accumulate) {
//		this.xAxis = xAxis;
//		this.yAxis = yAxis;
//		this.zAxis = zAxis;
//		this.Data = Data;
//		this.setAccumulate(accumulate);
//	}

	public void resetMbsLiveDataUpdate() {
		setxAxis(null);
		setyAxis(null);
		setzAxis(null);
		setData(null);
		setAccumulate(false);
		setAcquisitionMode(null);
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

	public IDataset getzAxis() {
		return zAxis;
	}

	public void setzAxis(IDataset zAxis) {
		this.zAxis = zAxis;
	}

	public IDataset getData() {
		return Data;
	}

	public void setData(IDataset data) {
		Data = data;
	}

	public Boolean getAccumulate() {
		return accumulate;
	}

	public void setAccumulate(Boolean accumulate) {
		this.accumulate = accumulate;
	}

	public void setAcquisitionMode(String acquisitionMode) {
		this.acquisitionMode = acquisitionMode;

	}

	public String getAcquisitionMode() {
		return acquisitionMode;
	}

}
