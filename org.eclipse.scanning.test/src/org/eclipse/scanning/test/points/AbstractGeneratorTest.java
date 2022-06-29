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
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.points.AbstractScanPointGenerator;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.BeforeClass;

public abstract class AbstractGeneratorTest {

	protected static final IPointGeneratorService service = new PointGeneratorService();

	@BeforeClass
	public static void beforeClass() {
		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setPointGeneratorService(service);
		serviceHolder.setValidatorService(new ValidatorService());
	}

	protected static void validateModel(IScanPointGeneratorModel model) {
		ServiceHolder.getValidatorService().validate(model);
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
		IPointGenerator<? extends IScanPointGeneratorModel> generator = roi!=null ? service.createGenerator(model, roi) : service.createGenerator(model);
	    List<IPosition> pointList = generator.createPoints();

		assertEquals(size, pointList.size());
		assertEquals(size, generator.size());

		CompoundModel cmodel = new CompoundModel(model);
		if (roi!=null) cmodel.setRegions(Arrays.asList(new ScanRegion(roi, Arrays.asList("x", "y"))));

		IPointGenerator<CompoundModel> cgenerator = service.createCompoundGenerator(cmodel);
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
