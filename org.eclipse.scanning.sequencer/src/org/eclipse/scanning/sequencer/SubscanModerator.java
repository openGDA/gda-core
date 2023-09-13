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

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 *
 * This class takes a position iterator and it is a compound generator,
 * attempts to remove the inner scans which subscan devices such as Malcolm will take care of
 * from the compound generator and return the outer scans.
 *
 * @author Matthew Gerring
 */
public class SubscanModerator {

	private final ScanModel scanModel;
	private final IPointGenerator<?> pointGen;

	private IPointGenerator<?> outerPointGenerator;
	private IPointGenerator<?> innerPointGenerator;

	private List<IScanPointGeneratorModel> outerModels;
	private List<IScanPointGeneratorModel> innerModels;

	private final int innerScanSize;
	private final int outerScanSize;
	private final int totalScanSize;


	public SubscanModerator(ScanModel scanModel) throws ScanningException {
		this.scanModel = scanModel;
		this.pointGen = scanModel.getPointGenerator();
		try {
			moderate(scanModel);

			innerScanSize = innerPointGenerator == null ? 0 : innerPointGenerator.size();
			outerScanSize = outerPointGenerator.size();
			totalScanSize = pointGen.size();
		} catch (MalcolmDeviceException | GeneratorException e) {
			throw new ScanningException("Unable to moderate scan for malcolm devices!", e);
		}
	}

	private void moderate(ScanModel scanModel) throws GeneratorException, ScanningException {

		// get the scan path model as a compound model
		final IScanPointGeneratorModel scanPathModel = scanModel.getScanPathModel();
		Objects.requireNonNull(scanPathModel, "The scanPathModel of the ScanModel must be set");
		final CompoundModel compoundModel = scanPathModel instanceof CompoundModel cModel ? cModel : new CompoundModel(scanPathModel);
		final List<IScanPointGeneratorModel> models = compoundModel.getModels();

		final Optional<IMalcolmDevice> malcolmDevice = findMalcolmDevice(scanModel);
		if (malcolmDevice.isPresent()) {
			moderateMalcolmScan(compoundModel, models, malcolmDevice.get().getAvailableAxes());
		} else {
			this.outerPointGenerator = pointGen; // We will reassign it to the outer scan if there is one, otherwise it is the full scan.
			this.outerModels = models;
		}
	}

	private void moderateMalcolmScan(final CompoundModel compoundModel, final List<IScanPointGeneratorModel> models,
			List<String> innerScanAxes) throws ScanningException, GeneratorException {
		// We need a compound model to moderate this stuff
		if (models.isEmpty()) throw new ScanningException("No models are provided in the compound model!");

		this.outerModels = new ArrayList<>();
		this.innerModels = new ArrayList<>();

		final int numModels = models.size();
		// search the list of models from the end for the first with an outer (non malcolm-controlled) axis
		final OptionalInt optLastOuter = IntStream.range(0, numModels)
				.map(i -> numModels - i - 1)
				.filter(i -> !innerScanAxes.containsAll(models.get(i).getScannableNames()))
				.findFirst();
		final int firstInner = optLastOuter.orElse(-1) + 1; // index of first inner model

		outerModels = unmodifiableList(new ArrayList<>(models.subList(0, firstInner)));
		innerModels = unmodifiableList(new ArrayList<>(models.subList(firstInner, numModels)));

		final IScanPathModel innerModel = createInnerModel(compoundModel, innerScanAxes);
		this.innerPointGenerator = ServiceProvider.getService(IPointGeneratorService.class).createGenerator(innerModel);

		final IScanPathModel outerModel = createOuterModel(compoundModel, innerScanAxes);
		this.outerPointGenerator = ServiceProvider.getService(IPointGeneratorService.class).createGenerator(outerModel);
	}

	private IScanPathModel createInnerModel(final CompoundModel compoundModel, final List<String> innerScanAxes) {
		final IScanPathModel innerModel;
		if (innerModels.isEmpty()) {
			// if the inner scan is empty, we need a single empty point for each point of the outer scan
			innerModel = new StaticModel(1);
		} else {
			// otherwise we create a new compound generator with the inner models and the same
			// mutators, duration, etc. as the overall scan
			// But only the regions that are relevant for these scans
			CompoundModel innerC = new CompoundModel(compoundModel);
			innerC.setModels(innerModels);
			if (compoundModel.getRegions() != null) {
				innerC.setRegions(compoundModel.getRegions().stream().filter(x -> innerScanAxes.containsAll(x.getScannables())).toList());
			}
			innerModel = innerC;
		}
		return innerModel;
	}

	private IScanPathModel createOuterModel(final CompoundModel compoundModel, final List<String> innerScanAxes) {
		if (outerModels.isEmpty()) {
			// if the outer scan is empty, we need a single empty point so that we perform the inner scan once
			return new StaticModel(1);
		}

		final CompoundModel outerCompoundModel = new CompoundModel(compoundModel);
		outerCompoundModel.setModels(outerModels);
		if (compoundModel.getRegions() != null) {
			outerCompoundModel.setRegions(compoundModel.getRegions().stream().filter(x -> !innerScanAxes.containsAll(x.getScannables())).toList());
		}
		return outerCompoundModel;
	}

	private Optional<IMalcolmDevice> findMalcolmDevice(ScanModel scanModel) {
		return scanModel.getDetectors().stream()
				.filter(IMalcolmDevice.class::isInstance)
				.map(IMalcolmDevice.class::cast)
				.findFirst();
	}

	public ScanModel getScanModel() {
		return scanModel;
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
		return innerScanSize;
	}

	public int getOuterScanSize() {
		return outerScanSize;
	}

	public int getTotalScanSize() {
		return totalScanSize;
	}

}
