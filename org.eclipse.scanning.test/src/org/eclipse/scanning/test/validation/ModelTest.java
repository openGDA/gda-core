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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.detector.PosDetectorModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.jupiter.api.Test;

public class ModelTest extends AbstractValidationTest {

	//TODO: Should all models be constructed valid?
	// Models that come complete when they are created with a no-arg constructor
	private static final Set<Class<?>> COMPLETE_MODELS = Set.of(StaticModel.class, CompoundModel.class,
			TwoAxisPointSingleModel.class, AxialStepModel.class);

	@Test
	public void emptyScanModels() throws Exception {
		final PointGeneratorService pservice = (PointGeneratorService) ValidatorService.getPointGeneratorService();
		for (Class<? extends IScanPathModel> modelType : pservice.getGenerators().keySet()) {
			final IScanPathModel emptyModel = modelType.getDeclaredConstructor().newInstance();
			final boolean completeModel = COMPLETE_MODELS.contains(modelType);
			try {
				validator.validate(emptyModel);
				// Validation successful
			    if (!completeModel) {
			    	fail("The model " + emptyModel + " validated but should not have done so");
			    }
			} catch (Exception ne) {
				// Validation failed
				if (completeModel) {
			    	fail("The model " + emptyModel + " did not validate but should have done so");
				}
			}
		}
	}

	@Test
	public void detectorModelsFromSpring() throws Exception {

		IRunnableDeviceService rservice = ValidatorService.getRunnableDeviceService();
		Collection<DeviceInformation<?>> infos =  rservice.getDeviceInformation();

		assertNotEquals("There must be some info! There must!", 0, infos.size());
		for (DeviceInformation<?> info : infos) {

			Object sprung = info.getModel();
			try {
				validator.validate(sprung);
			} catch (ModelValidationException ne) {
				if (sprung instanceof DummyMalcolmModel && ne.getFieldNames()[0].equals("fileDir")) continue;
				throw ne;
			}
		}
	}

	@Test
	public void exceptionWhenValidatingWithUnregisteredName() {
		assertThrows(ValidationException.class, () -> {
			MandelbrotModel model = new MandelbrotModel();
			model.setName("not_mandelbrot");
			validator.validate(model);
		});
	}

	@Test
	public void exceptionWhenValidatingWithUnregisteredModel() {
		assertThrows(ValidationException.class, () ->
			validator.validate(new PosDetectorModel()));
	}
}
