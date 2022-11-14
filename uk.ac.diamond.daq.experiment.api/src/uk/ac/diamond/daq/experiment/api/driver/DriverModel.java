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

import java.io.Serializable;
import java.util.List;

import org.eclipse.january.dataset.Dataset;

import gda.factory.Findable;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public interface DriverModel extends Findable, Serializable, EditableWithListWidget {

	/**
	 * Each dataset should be named appropriately, especially if the driver which
	 * runs this model treats this variable as a readout as well.
	 */
	List<Dataset> getPlottableDatasets();

}
