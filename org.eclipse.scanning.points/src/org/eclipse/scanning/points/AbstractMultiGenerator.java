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

package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AbstractMultiModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * A Generator for Models that are made of more than one model- as currently all implementations are SPG models which
 * are unable to deal with CompoundModels, the validation ensures that no grid models are passed.
 *
 * Maintains a list of IPointGenerator equivalent to the models its model contains, passing their PPointGenerators down.
 * While there is some redundancy here, the MultiModel contains all the model information in one place, and as generators
 * now instantiate their JythonGenerator on creation, it is better to cache them if they might be needed more than once.
 *
 * @param <T>
 */
public abstract class AbstractMultiGenerator<T extends AbstractMultiModel<?>> extends AbstractScanPointGenerator<T> {

	protected IPointGeneratorService service;
	protected List<IPointGenerator<?>> generators;
	// Used when validating a model that is not the model of this generator. Usually blank.
	protected List<IPointGenerator<?>> cachedGenerators;
	protected static final double DIFF_LIMIT = 1e-5;

	protected AbstractMultiGenerator(T model, IPointGeneratorService service) {
		// Need service to be set before validating, so cannot use super()
		this(service);
		this.model = model;
		validateModel();
		pointGenerator = createPythonPointGenerator();
	}

	protected AbstractMultiGenerator(IPointGeneratorService service) {
		this.service = service;
	}

	@Override
	public T validate(T model) {
		// Intensive validation so check super first
		super.validate(model);
		cachedGenerators = new ArrayList<>();
		if (model.getModels() == null || model.getModels().isEmpty())
			throw new ModelValidationException("MultiModel requires at least one internal model!", model, "models");
		for (IScanPointGeneratorModel models : model.getModels()) {
			if (models instanceof AbstractTwoAxisGridModel) {
				throw new ModelValidationException("MultiGenerators cannot operate on already Compounded models, like grids.",
						model, "models");
			}
			if (models.isAlternating()) {
				throw new ModelValidationException("MultiGenerators cannot contain Alternating models, set it on the multimodel instead",
						model, "models");
			}
			// Validates and adds generator to generators
			try {
				cachedGenerators.add(service.createGenerator(models));
			} catch (GeneratorException e) {
				throw new ModelValidationException(e);
			}
		}
		if (model == this.model) {
			generators = cachedGenerators;
		}
		return model;
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> generatorFactory = getFactory();
        final T model = getModel();
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();

        final PPointGenerator pointGen = generatorFactory.createObject(initGenerators(), alternating);

        return createWrappingCompoundGenerator(pointGen, continuous);
	}

	protected abstract JythonObjectFactory<PPointGenerator> getFactory();

	protected PPointGenerator[] initGenerators() {
		return generators.stream().map(AbstractScanPointGenerator.class::cast).map(AbstractScanPointGenerator::getPointGenerator)
				.toArray(PPointGenerator[]::new);
	}

	protected List<IPointGenerator<?>> getGenerators() {
		return generators;
	}

}
