/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.driver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public class SingleAxisLinearSeries extends DriverModelBase {

	private static final long serialVersionUID = -834737476722369747L;

	private List<DriverProfileSection> profile;
	private String axisName;

	public SingleAxisLinearSeries() {
		profile = Collections.emptyList();
	}

	public SingleAxisLinearSeries(String axisName) {
		this();
		setAxisName(axisName);
	}

	public List<DriverProfileSection> getProfile() {
		return profile;
	}

	public void setProfile(List<DriverProfileSection> series) {
		this.profile = series;
	}

	public String getAxisName() {
		return axisName;
	}

	public void setAxisName(String axisName) {
		this.axisName = axisName;
	}

	@Override
	public EditableWithListWidget createDefault() {
		DriverModel model = new SingleAxisLinearSeries(axisName);
		model.setName("New Profile");
		return model;
	}

	@Override
	public List<Dataset> getPlottableDatasets() {
		double[] x = new double[profile.size()+1];
		double[] y = new double[profile.size()+1];

		x[0] = 0;
		y[0] = profile.get(0).getStart();

		for (int i = 0; i < profile.size(); i++) {
			x[i+1] = profile.get(i).getDuration() + x[i];
			y[i+1] = profile.get(i).getStop();
		}

		final Dataset xDataset = DatasetFactory.createFromObject(x);
		xDataset.setName("Time");
		final Dataset yDataset = DatasetFactory.createFromObject(y);
		yDataset.setName(axisName);

		return Arrays.asList(xDataset, yDataset);
	}

}