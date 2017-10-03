/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.handlers;

import org.eclipse.january.dataset.IDataset;

public class SnapshotData {

	private String title;
	private IDataset dataset;
	private IDataset xAxis;
	private IDataset yAxis;

	public SnapshotData(String title, IDataset dataset) {
		this.title = title;
		this.dataset = dataset;
	}

	public IDataset getDataset() {
		return dataset;
	}

	public void setDataset(IDataset dataset) {
		this.dataset = dataset;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

}
