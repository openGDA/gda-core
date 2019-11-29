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

package uk.ac.diamond.daq.mapping.ui.experiment.saver;

import java.util.Optional;
import java.util.function.Consumer;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.file.SavedScanMetaData;

public abstract class ScanSaver {
	private Consumer<Optional<IMappingExperimentBean>> postLoad;

	public ScanSaver(Consumer<Optional<IMappingExperimentBean>> postLoad) {
		this.postLoad = postLoad;
	}

	public abstract SavedScanMetaData[] listScans ();

	protected abstract Optional<IMappingExperimentBean> loadBean (SavedScanMetaData savedScanMetaData);

	public void load (SavedScanMetaData savedScanMetaData) {
		postLoad.accept(loadBean(savedScanMetaData));
	}

	public abstract void save ();

	public abstract void delete (SavedScanMetaData savedScanMetaData);
}
