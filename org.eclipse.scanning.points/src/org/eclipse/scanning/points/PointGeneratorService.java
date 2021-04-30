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

package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AbstractMultiModel;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ConcurrentMultiModel;
import org.eclipse.scanning.api.points.models.ConsecutiveMultiModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.points.models.JythonGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;

public class PointGeneratorService implements IPointGeneratorService {

	private static final Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator<?>>> modelToGenerator;
	private static final Map<String, GeneratorInfo> idToInfo;

	// Use a factory pattern to register the types.
	// This pattern can always be replaced by extension points
	// to allow point generators to be dynamically registered.
	static {
		System.out.println("Starting generator service");
		Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator<?>>> gens = new HashMap<>();
		// NOTE Repeated generators are currently not allowed. Will not break the service
		// (models class keys are different) but causes ambiguity in the GUI when it creates a
		// generator for a model.
		gens.put(AxialStepModel.class, AxialStepGenerator.class);
		gens.put(AxialPointsModel.class, AxialPointsGenerator.class);
		gens.put(AxialCollatedStepModel.class, AxialCollatedStepGenerator.class);
		gens.put(AxialMultiStepModel.class, AxialMultiStepGenerator.class);
		gens.put(AxialArrayModel.class, AxialArrayGenerator.class);
		gens.put(TwoAxisGridPointsModel.class, TwoAxisGridPointsGenerator.class);
		gens.put(TwoAxisLinePointsModel.class, TwoAxisLinePointsGenerator.class);
		gens.put(TwoAxisLineStepModel.class, TwoAxisLineStepGenerator.class);
		gens.put(TwoAxisGridStepModel.class, TwoAxisGridStepGenerator.class);
		gens.put(StaticModel.class, StaticGenerator.class);
		gens.put(TwoAxisGridPointsRandomOffsetModel.class, TwoAxisGridPointsRandomOffsetGenerator.class);
		gens.put(TwoAxisSpiralModel.class, TwoAxisSpiralGenerator.class);
		gens.put(TwoAxisLissajousModel.class, TwoAxisLissajousGenerator.class);
		gens.put(JythonGeneratorModel.class, JythonGenerator.class);
		gens.put(TwoAxisPtychographyModel.class, TwoAxisPtychographyGenerator.class);
		gens.put(TwoAxisPointSingleModel.class, TwoAxisPointSingleGenerator.class);
		gens.put(ConsecutiveMultiModel.class, ConsecutiveMultiGenerator.class);
		gens.put(InterpolatedMultiScanModel.class, ConsecutiveMultiGenerator.class);
		gens.put(ConcurrentMultiModel.class, ConcurrentMultiGenerator.class);
		gens.put(CompoundModel.class, CompoundGenerator.class);

		Map<String, GeneratorInfo> tinfo = new TreeMap<>();
		fillStaticGeneratorInfo(gens, tinfo);

		try { // Extensions must provide an id, it is a compulsory field.
			readExtensions(gens, tinfo);
		} catch (CoreException e) {
			e.printStackTrace(); // Static block, intentionally do not use logging.
		}

		modelToGenerator = Collections.unmodifiableMap(gens);
		idToInfo = Collections.unmodifiableMap(tinfo);
	}

	public Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator<?>>> getGenerators() {
		return modelToGenerator;
	}

	@Override
	public <T extends IScanPointGeneratorModel, R> void setBounds(T model, List<R> regions) {
		if (regions == null || regions.isEmpty()) return;
		IRectangularROI rect = ((IROI) regions.get(0)).getBounds();
		for (R roi : regions) {
			rect = rect.bounds((IROI) roi);
		}

		if (model instanceof IBoundingBoxModel) {
			IBoundingBoxModel bbm = (IBoundingBoxModel) model;
			if (bbm.getBoundingBox() != null) {
				IRectangularROI modelsROI = new RectangularROI(bbm.getBoundingBox().getxAxisStart(),
						bbm.getBoundingBox().getyAxisStart(), bbm.getBoundingBox().getxAxisLength(),
						bbm.getBoundingBox().getyAxisLength(), 0);

				rect = rect.bounds(modelsROI);
			}
			BoundingBox box = new BoundingBox();
			box.setxAxisStart(rect.getPoint()[0]);
			box.setyAxisStart(rect.getPoint()[1]);
			box.setxAxisLength(rect.getLength(0));
			box.setyAxisLength(rect.getLength(1));
			bbm.setBoundingBox(box);
		} else if (model instanceof IBoundingLineModel) {
			BoundingLine line = new BoundingLine();
			LinearROI lroi = (LinearROI) regions.get(0);
			line.setxStart(lroi.getPoint()[0]);
			line.setyStart(lroi.getPoint()[1]);
			line.setLength(lroi.getLength());
			line.setAngle(lroi.getAngle());
			((IBoundingLineModel) model).setBoundingLine(line);
		} else if (model instanceof TwoAxisPointSingleModel) {
			((TwoAxisPointSingleModel) model).setX(rect.getPointX());
			((TwoAxisPointSingleModel) model).setY(rect.getPointY());
		}
	}

	private static void fillStaticGeneratorInfo(
			Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator<?>>> gens,
			Map<String, GeneratorInfo> ids) {

		for (Map.Entry<Class<? extends IScanPathModel>, Class<? extends IPointGenerator<?>>> genEntry : gens
				.entrySet()) {
			try {
				final GeneratorInfo info = new GeneratorInfo();
				info.setModelClass(genEntry.getKey());
				info.setGeneratorClass(genEntry.getValue());
				ids.put(info.getGeneratorClass().getName(), info);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private static void readExtensions(Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator<?>>> gens,
			Map<String, GeneratorInfo> tids) throws CoreException {

		if (Platform.getExtensionRegistry() != null) {
			final IConfigurationElement[] eles = Platform.getExtensionRegistry()
					.getConfigurationElementsFor("org.eclipse.scanning.api.generator");
			for (IConfigurationElement e : eles) {
				final IPointGenerator<?> generator = (IPointGenerator<?>) e.createExecutableExtension("class");
				final IScanPathModel model = (IScanPathModel) e.createExecutableExtension("model");

				final Class<? extends IScanPathModel> modelClass = model.getClass();
				@SuppressWarnings("unchecked")
				final Class<? extends IPointGenerator<?>> generatorClass = (Class<? extends IPointGenerator<?>>) generator
						.getClass();
				gens.put(modelClass, generatorClass);

				final GeneratorInfo info = new GeneratorInfo();
				info.setModelClass(model.getClass());
				info.setGeneratorClass(generator.getClass());
				info.setLabel(e.getAttribute("label"));
				info.setDescription(e.getAttribute("description"));

				String id = e.getAttribute("id");
				tids.put(id, info);
			}
		}
	}

	@Override
	public IPointGenerator<CompoundModel> createCompoundGenerator(List<IPointGenerator<? extends IScanPointGeneratorModel>> generators) throws GeneratorException {
		return new CompoundGenerator(generators, this);
	}

	@Override
	public Collection<String> getRegisteredGenerators() {
		return idToInfo.keySet();
	}

	@Override
	public IPointGenerator<CompoundModel> createCompoundGenerator(CompoundModel cmodel) throws GeneratorException {
		return new CompoundGenerator(cmodel, this);
	}

	@Override
	public List<IROI> findRegions(IScanPointGeneratorModel model, Collection<ScanRegion> sregions) throws GeneratorException {
		if (sregions == null || sregions.isEmpty())
			return Collections.emptyList();

		final Collection<String> names = model.getScannableNames();
		final Predicate<ScanRegion> shouldAddRoi = scanRegion -> {
			final List<String> scannables = scanRegion.getScannables();
			return scannables == null || scannables.containsAll(names) || findNamesAsEntry(scannables, names);
		};

		return sregions.stream()
				.filter(shouldAddRoi)
				.map(ScanRegion::getRoi)
				.collect(Collectors.toList());
	}

	private boolean findNamesAsEntry(List<String> scannables, Collection<String> names) {
		return names.stream()
				.allMatch(name -> scannables.stream()
						.anyMatch(sName -> sName.matches("/entry/.+/" + name + "_value_set")));
	}

	@Override
	@Deprecated
	public <T> IPointGenerator<T> createGenerator(String id) throws GeneratorException {
		throw new GeneratorException("Must create a generator with a model, creating by id is no longer valid");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IPointGenerator<T> createGenerator(T model) throws GeneratorException {
		Class<IPointGenerator<T>> genClass = (Class<IPointGenerator<T>>) modelToGenerator.get(model.getClass());
		try {
			if (model instanceof AbstractMultiModel) {
				return genClass.getDeclaredConstructor(model.getClass(), IPointGeneratorService.class).newInstance(model, this);
			}
			return genClass.getDeclaredConstructor(model.getClass()).newInstance(model);
		} catch (Exception e) {
			throw new GeneratorException(e);
		}
	}

	@Override
	public <T extends IScanPointGeneratorModel> IPointGenerator<CompoundModel> createGenerator(T model, List<IROI> regions, List<IMutator> mutators, float duration) throws GeneratorException {
		CompoundModel cModel = new CompoundModel();
		cModel.addData(model, regions);
		cModel.addMutators(mutators);
		cModel.setDuration(duration);
		setBounds(model, new ArrayList<>(regions));
		return createCompoundGenerator(cModel);
	}
}
