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
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 *
 * This class takes a position iterator and it is a compound generator,
 * attempts to remove the inner scans which subscan devices such as Malcolm will take care of
 * from the compound generator and return the outer scans.
 *
 * @author Matthew Gerring
 */
public class SubscanModerator {

	private final IPointGeneratorService pointGenService;
	private final IPointGenerator<?> pointGen;

	private IPointGenerator<?> outerPointGenerator;
	private IPointGenerator<?> innerPointGenerator;

	private CompoundModel compoundModel;
	private List<Object> outerModels;
	private List<Object> innerModels;

	public SubscanModerator(IPointGenerator<?> generator, List<IRunnableDevice<?>> detectors,
			IPointGeneratorService gservice) throws ScanningException {
		this(generator, null, detectors, gservice);
	}

	public SubscanModerator(IPointGenerator<?> generator, CompoundModel cmodel, List<IRunnableDevice<?>> detectors,
			IPointGeneratorService gservice) throws ScanningException {
		this.pointGenService = gservice;
		this.pointGen = generator;
		this.compoundModel   = cmodel!=null ? cmodel : getModel(generator);
		try {
			moderate(generator, detectors);
		} catch (MalcolmDeviceException | GeneratorException e) {
			throw new ScanningException("Unable to moderate scan for malcolm devices!", e);
		}
	}

	private CompoundModel getModel(IPointGenerator<?> pointGenerator) {
		Object model = pointGenerator.getModel();
		if (model instanceof CompoundModel) {
			return (CompoundModel) model;
		}
		return new CompoundModel(model);
	}

	private boolean isMalcolmScan(List<IRunnableDevice<?>> detectors) {
		return detectors != null && !detectors.isEmpty() &&
				detectors.stream().map(IRunnableDevice::getRole).anyMatch(role -> role == DeviceRole.MALCOLM);
	}

	private void moderate(IPointGenerator<?> generator, List<IRunnableDevice<?>> detectors) throws GeneratorException, ScanningException {
		outerPointGenerator = generator; // We will reassign it to the outer scan if there is one, otherwise it is the full scan.

		if (!isMalcolmScan(detectors)) {
			this.outerModels = compoundModel.getModels();
			return;
		}

		// We need a compound model to moderate this stuff
		List<Object> orig   = compoundModel.getModels();
		if (orig.isEmpty()) throw new ScanningException("No models are provided in the compound model!");

		this.outerModels = new ArrayList<>();
		this.innerModels = new ArrayList<>();

		final List<String> axes = getAxes(detectors);
		boolean reachedOuterScan = false;
		for (int i = orig.size()-1; i > -1; i--) {
			Object model = orig.get(i);
			if (!reachedOuterScan) {
				IPointGenerator<?> pointGen = pointGenService.createGenerator(model);
				List<String> names = pointGen.getNames();
				if (axes.containsAll(names)) {// These will be deal with by malcolm
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
			// mutators, regions, duration, etc. as the overall scan
			this.innerPointGenerator = pointGenService.createCompoundGenerator(CompoundModel.copyAndSetModels(compoundModel, innerModels));
		}

		if (outerModels.isEmpty()) {
			// if the outer scan is empty, we need a single empty point so that we perform the inner scan once
			this.outerPointGenerator = pointGenService.createGenerator(new StaticModel(1));
			return;
		}

		this.outerPointGenerator = pointGenService.createCompoundGenerator(CompoundModel.copyAndSetModels(compoundModel, outerModels));
	}

	private List<String> getAxes(List<IRunnableDevice<?>> detectors) throws ScanningException {
		try {
			return detectors.stream()
					.filter(IMalcolmDevice.class::isInstance)
					.map(IMalcolmDevice.class::cast)
					.findFirst()
					.map(this::getAxesToMove)
					.orElse(Collections.emptyList());
		} catch (RuntimeException e) {
			if (e.getCause() instanceof ScanningException) throw (ScanningException) e.getCause();
			throw e;
		}
	}

	private List<String> getAxesToMove(IMalcolmDevice<?> malc) {
		try {
			return malc.getAvailableAxes();
		} catch (ScanningException e) {
			throw new RuntimeException(e);
		}
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

	public List<Object> getOuterModels() {
		return outerModels;
	}

	public List<Object> getInnerModels() {
		return innerModels;
	}

	public int getInnerScanSize() throws GeneratorException {
		if (innerPointGenerator == null) return 0; // the inner point generator can be null
		return innerPointGenerator.size();
	}

	public int getOuterScanSize() throws GeneratorException {
		return outerPointGenerator.size(); // the outer point generator can't be null
	}

	public int getTotalScanSize() throws GeneratorException {
		return pointGen.size();
	}

}
