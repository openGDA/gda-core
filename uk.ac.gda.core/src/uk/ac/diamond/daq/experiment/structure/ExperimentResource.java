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

package uk.ac.diamond.daq.experiment.structure;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ExperimentResource {

	private final String name;
	private final URL location;

	private final List<URL> acquisitions = new ArrayList<>();

	ExperimentResource(String name, URL url) {
		this.name = name;
		location = url;
	}

	public String getExperimentName() {
		return name;
	}

	public URL getExperimentURL() {
		return location;
	}

	public void addAcquisition(URL acquisitionUrl) {
		acquisitions.add(acquisitionUrl);
	}

	public List<URL> getAcquisitions() {
		return acquisitions;
	}

}
