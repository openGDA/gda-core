/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
package uk.ac.diamond.tomography.reconstruction.views;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.viewers.ISelection;

public class ProjectionSliceSelection implements ISelection {

	private int sliceNumber = 0;

	private Dataset dataSetPlotted;

	public ProjectionSliceSelection(int sliceNumber) {
		this.sliceNumber = sliceNumber;
	}

	public void setDataSetPlotted(Dataset dataSetPlotted) {
		this.dataSetPlotted = dataSetPlotted;
	}

	public Dataset getDataSetPlotted() {
		return dataSetPlotted;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	public int getSliceNumber() {
		return sliceNumber;
	}

}