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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.ITimeoutable;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.points.IPosition;

/**
 * A Malcolm Model for a {@link DummyMalcolmDevice}. This model describes which nexus files
 * and datasets the dummy malcolm device should create. A {@link DummyMalcolmDetectorModel}
 * should be added for each device (i.e. detector, scannable) that is being simulated by the
 * real malcolm device.
 *
 * @author Matthew Dickie
 */
public class DummyMalcolmModel extends MalcolmModel implements ITimeoutable {

	/**
	 * The positioner names are the names of the datasets written by the dummy malcolm device.
	 * This will normally be the same as {@link #getAxesToMove()}.
	 * At each position in the scan the value written to that dataset will be the value for that
	 * name in the {@link IPosition} for that point of the scan if it contains a value for that
	 * name, otherwise a random value will be written.
	 *
	 */
	private List<String> positionerNames;

	/**
	 * The monitor datasets to write. These will be written to with random at each
	 * position in the scan.
	 */
	private List<String> monitorNames;

	public DummyMalcolmModel() {
		// the default model has a single detector with a single dataset
		// this can be overridden by calling setDummyDetectorModels()
		setName("malcolm");

		// some default detector models
		List<IMalcolmDetectorModel> detModels = new ArrayList<>();
		detModels.add(createMalcolmDetector("det1", 0.0, 1));
		detModels.add(createMalcolmDetector("det2", 0.0, 2));
		detModels.add(createMalcolmDetector("det3", 0.0, 3));

		List<String> axes = Arrays.asList("stage_x", "stage_y");
		setAxesToMove(axes); // determines the _set (i.e. demand) values to be written
		setPositionerNames(axes); // determines the value (a.k.a rbv) values to be written
		setDetectorModels(detModels);
	}

	private DummyMalcolmDetectorModel createMalcolmDetector(String name, double exposureTime, int framesPerStep) {
		DummyMalcolmDetectorModel detectorModel = new DummyMalcolmDetectorModel(name);
		detectorModel.setExposureTime(exposureTime);
		detectorModel.setFramesPerStep(framesPerStep);

		// datasets
		List<DummyMalcolmDatasetModel> datasets = new ArrayList<>();
		datasets.add(new DummyMalcolmDatasetModel(name, 2, Double.class));
		detectorModel.setDatasets(datasets);

		return detectorModel;
	}


	public List<String> getPositionerNames() {
		if (positionerNames == null) return Collections.emptyList();
		return positionerNames;
	}

	public void setPositionerNames(List<String> positionerNames) {
		this.positionerNames = positionerNames;
	}

	public List<String> getMonitorNames() {
		if (monitorNames == null) return Collections.emptyList();
		return monitorNames;
	}

	public void setMonitorNames(List<String> monitorNames) {
		this.monitorNames = monitorNames;
	}

	/**
	 * Sets the axes to move, as returned by {@link IMalcolmDevice#getAvailableAxes()}. Also
	 * sets the {@link #positionerNames} which determine what datasets are written by the
	 * {@link DummyMalcolmDevice}.
	 * @see org.eclipse.scanning.api.device.models.MalcolmModel#setAxesToMove(java.util.List)
	 */
	@Override
	public void setAxesToMove(List<String> axesToMove) {
		// overridden to make it easy to change the positioner names at the same time.
		super.setAxesToMove(axesToMove);
		setPositionerNames(axesToMove);
	}

}
