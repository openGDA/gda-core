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
package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.points.AbstractScanPointGenerator;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractGeneratorTest {

	protected static final IPointGeneratorService pointGeneratorService = new PointGeneratorService();

	@BeforeAll
	public static void beforeClass() {
		ServiceProvider.setService(IPointGeneratorService.class, new PointGeneratorService());
		ServiceProvider.setService(IValidatorService.class, new ValidatorService());
	}

	@AfterAll
	public static void afterClass() {
		ServiceProvider.reset();
	}

	protected static void validateModel(IScanPointGeneratorModel model) {
		ServiceProvider.getService(IValidatorService.class).validate(model);
	}

	/**
	 * Checks whether a model, with an optional IROI, matches its predicted size, when constructed  as a model and when wrapped in a CompoundModel.
	 * If the region is non-null, the model should be in axes (x, y)
	 * @param model
	 * @param roi
	 * @param size
	 * @throws GeneratorException
	 * @throws Exception
	 */
	protected void checkWrtCompound(IScanPointGeneratorModel model, IROI roi, int size) throws Exception {

		// Get the point list
		IPointGenerator<? extends IScanPointGeneratorModel> generator = roi!=null ? pointGeneratorService.createGenerator(model, roi) : pointGeneratorService.createGenerator(model);
	    List<IPosition> pointList = generator.createPoints();

		assertEquals(size, pointList.size());
		assertEquals(size, generator.size());

		CompoundModel cmodel = new CompoundModel(model);
		if (roi!=null) cmodel.setRegions(Arrays.asList(new ScanRegion(roi, Arrays.asList("x", "y"))));

		IPointGenerator<CompoundModel> cgenerator = pointGeneratorService.createCompoundGenerator(cmodel);
	    List<IPosition> cpointList = cgenerator.createPoints();
		assertEquals(size, cpointList.size());
		assertEquals(size, cgenerator.size());
	}

	/**
	 * Checks whether a generator's bounds (half step either side of first, last step) are as predicted.
	 * Bounds are the limits of motion for continuous motion, and also necessarily must be within limits of each other for ConsecutiveMultiModels.
	 */
	protected void checkBounds(IPointGenerator<?> gen, String axis, double lowerBound, double upperBound) {
		assertEquals("Lower bound not as expected", lowerBound, ((AbstractScanPointGenerator<?>) gen).initialBounds().get(axis));
		assertEquals("Upper bound not as expected", upperBound, ((AbstractScanPointGenerator<?>) gen).finalBounds().get(axis));
	}

}
