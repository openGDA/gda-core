/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.example.malcolm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;

/**
 * A model describing a detector that would be controlled by malcolm in a real scan.
 *
 * Note that this is <em>NOT</em> the model for a malcolm device - {@link DummyMalcolmModel}
 * fulfils that role. Instead one or more of these models in a child of a
   {@link DummyMalcolmModel} tells the
 * {@link DummyMalcolmDevice} what nexus files and datasets to write, so that it can be
 * set up to produce a nexus file similar to what a real malcolm device would.
 *
 * The first {@link DummyMalcolmDatasetModel} in the {@link List} returned by
 * {@link #getDatasets()} will be the primary one, the others will be secondary.
 *
 * @author Matthew Dickie
 */
public class DummyMalcolmDetectorModel extends MalcolmDetectorModel {

	private List<DummyMalcolmDatasetModel> datasets = null;

	public DummyMalcolmDetectorModel() {
		// no-arg constructor
	}

	public DummyMalcolmDetectorModel(String name) {
		setName(name);
	}

	public List<DummyMalcolmDatasetModel> getDatasets() {
		if (datasets == null) return Collections.emptyList();
		return datasets;
	}

	/**
	 * A collection of {@link DummyMalcolmDatasetModel}s each describing a dataset that
	 * should be written for this device.
	 * @param datasets
	 */
	public void setDatasets(List<DummyMalcolmDatasetModel> datasets) {
		this.datasets = datasets;
	}

	public void addDataset(DummyMalcolmDatasetModel dataset) {
		if (this.datasets == null) this.datasets = new ArrayList<>();
		datasets.add(dataset);
	}

	@Override
	public String toString() {
		return "DummyMalcolmControlledDetectorModel [name=" + getName() + ", datasets=" + datasets + "]";
	}

}
