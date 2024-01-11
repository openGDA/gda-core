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
package org.eclipse.scanning.test.validation;

import static org.junit.Assert.assertThrows;

import java.util.Arrays;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.junit.jupiter.api.Test;

public class CompoundModelTest extends AbstractValidationTest {


	@Test
	public void testNoBoundingBox() throws ValidationException {
		final CompoundModel cmodel = new CompoundModel(
				Arrays.asList(new AxialStepModel("fred", 10, 20, 1), new TwoAxisGridPointsModel("stage_x", "stage_y")));
		assertThrows(ValidationException.class, () -> validatorService.validate(cmodel));
	}

	@Test
	public void testAxesColliding() {

		final CompoundModel cmodel = new CompoundModel(Arrays.asList(new AxialStepModel("stage_x", 10, 20, 1), new TwoAxisGridPointsModel("stage_x", "stage_y")));
		assertThrows(ValidationException.class, () -> validatorService.validate(cmodel));
	}

	@Test
	public void testBoundingBox() throws ValidationException {

		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		validatorService.validate(new CompoundModel(new AxialStepModel("fred", 10, 20, 1), gmodel));
	}

	@Test
	public void nullAxisTest() throws ValidationException {
		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel(null, "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		assertThrows(ModelValidationException.class, () -> validatorService
				.validate(new CompoundModel(Arrays.asList(new AxialStepModel("fred", 10, 20, 1), gmodel))));
	}
}
