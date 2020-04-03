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
package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 *
 * This class takes a position iterator and it is a compound generator,
 * attempts to remove the inner scans which subscan devices such as Malcolm will take care of
 * from the compound generator and return the outer scans.
 *
 * @author Matthew Gerring
 */
public class SubscanModerator {

	private final IPointGenerator<?> pointGen;

	private IPointGenerator<?> outerPointGenerator;
	private IPointGenerator<?> innerPointGenerator;

	private List<IScanPointGeneratorModel> outerModels;
	private List<IScanPointGeneratorModel> innerModels;

	public SubscanModerator(ScanModel scanModel) throws ScanningException {
		this.pointGen = scanModel.getPointGenerator();
		try {
			moderate(scanModel);
		} catch (MalcolmDeviceException | GeneratorException e) {
			throw new ScanningException("Unable to moderate scan for malcolm devices!", e);
		}
	}

	private void moderate(ScanModel scanModel) throws GeneratorException, ScanningException {
		outerPointGenerator = pointGen; // We will reassign it to the outer scan if there is one, otherwise it is the full scan.

		// get the scan path model as a compound model
		final IScanPointGeneratorModel scanPathModel = scanModel.getScanPathModel();
		Objects.requireNonNull(scanPathModel, "The scanPathModel of the ScanModel must be set");
		final CompoundModel compoundModel = scanPathModel instanceof CompoundModel ? (CompoundModel) scanPathModel :
				new CompoundModel(scanPathModel);

		final Optional<IMalcolmDevice> malcolmDevice = findMalcolmDevice(scanModel);
		if (!malcolmDevice.isPresent()) {
			this.outerModels = compoundModel.getModels();
			return;
		}

		// We need a compound model to moderate this stuff
		List<IScanPointGeneratorModel> models = compoundModel.getModels();
		if (models.isEmpty()) throw new ScanningException("No models are provided in the compound model!");

		this.outerModels = new ArrayList<>();
		this.innerModels = new ArrayList<>();

		final IPointGeneratorService pointGenService = ServiceHolder.getGeneratorService();
		final List<String> innerScanAxes = malcolmDevice.get().getAvailableAxes();
		boolean reachedOuterScan = false;
		for (int i = models.size() - 1; i > -1; i--) {
			IScanPointGeneratorModel model = models.get(i);
			if (!reachedOuterScan) {
				List<String> names = model.getScannableNames();
				if (innerScanAxes.containsAll(names)) {// These will be deal with by malcolm
					innerModels.add(0, model);
					continue; // The device will deal with it.
				}
			}
			reachedOuterScan = true; // As soon as we reach one outer scan all above are outer.
			outerModels.add(0, model);
		}

		if (innerModels.isEmpty()) {
			// if the inner scan is empty, we need a single empty point for each point of the outer scan
			this.innerPointGenerator = pointGenService.createGenerator(new StaticModel(1));
		} else {
			// otherwise we create a new compound generator with the inner models and the same
			// mutators, duration, etc. as the overall scan
			// But only the regions that are relevant for these scans
			CompoundModel innerC = new CompoundModel(compoundModel);
			innerC.setModels(innerModels);
			if (compoundModel.getRegions() != null) {
				innerC.setRegions(compoundModel.getRegions().stream().filter(x -> innerScanAxes.containsAll(x.getScannables())).collect(Collectors.toList()));
			}
			this.innerPointGenerator = pointGenService.createCompoundGenerator(innerC);
		}

		if (outerModels.isEmpty()) {
			// if the outer scan is empty, we need a single empty point so that we perform the inner scan once
			this.outerPointGenerator = pointGenService.createGenerator(new StaticModel(1));
			return;
		}
		CompoundModel outerC = new CompoundModel(compoundModel);
		outerC.setModels(outerModels);
		if (compoundModel.getRegions() != null) {
			outerC.setRegions(compoundModel.getRegions().stream().filter(x -> !innerScanAxes.containsAll(x.getScannables())).collect(Collectors.toList()));
		}
		this.outerPointGenerator = pointGenService.createCompoundGenerator(outerC);
	}

	private Optional<IMalcolmDevice> findMalcolmDevice(ScanModel scanModel) {
		return scanModel.getDetectors().stream()
				.filter(IMalcolmDevice.class::isInstance)
				.map(IMalcolmDevice.class::cast)
				.findFirst();
	}

	/**
	 * Returns the point generator for the outer points of the scan.
	 * The outer iterable will not be <code>null</code> normally. Even if
	 * all of the scan is deal with by malcolm the outer scan will still
	 * be a static generator of one point. If there are no subscan devices,
	 * then the outer scan is the full scan.
	 * @return an iterator over the outer scan
	 */
	public IPointGenerator<?> getOuterPointGenerator() {
		return outerPointGenerator;
	}

	/**
	 * Returns the point generator for the over the inner points of the scan. For any scan
	 * that contains a malcolm device the inner point generator will not be <code>null</code>,
	 * even if all of the scan is dealt with outside the malcolm device.
	 *
	 * Note that this method is only used in <i>dummy</i> mode, for example by
	 * a <code>DummyMalcolmDevice</code>. The real malcolm device is passed
	 * the point generator for the whole scan. The malcolm device will itself
	 * determine what part of the scan it is responsible for (i.e. in python).
	 *
	 * @return an iterator over the inner scan
	 */
	public IPointGenerator<?> getInnerPointGenerator() {
		return innerPointGenerator;
	}

	public List<IScanPointGeneratorModel> getOuterModels() {
		return outerModels;
	}

	public List<IScanPointGeneratorModel> getInnerModels() {
		return innerModels;
	}

	public int getInnerScanSize() {
		if (innerPointGenerator == null) return 0; // the inner point generator can be null
		return innerPointGenerator.size();
	}

	public int getOuterScanSize() {
		return outerPointGenerator.size(); // the outer point generator can't be null
	}

	public int getTotalScanSize() {
		return pointGen.size();
	}

}
