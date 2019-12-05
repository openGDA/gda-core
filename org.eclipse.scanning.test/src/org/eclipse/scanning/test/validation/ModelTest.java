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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.Test;

public class ModelTest extends AbstractValidationTest {

	//TODO: Should all models be constructed valid?
	private static Collection<Class<?>> COMPLETE_MODELS; // Models that come complete when they are created with a no-arg constructor
	static {
		COMPLETE_MODELS = new ArrayList<>();
		COMPLETE_MODELS.add(TwoAxisLissajousModel.class);
		COMPLETE_MODELS.add(StaticModel.class);
		COMPLETE_MODELS.add(TwoAxisLinePointsModel.class);
		COMPLETE_MODELS.add(TwoAxisLineStepModel.class);
		COMPLETE_MODELS.add(TwoAxisPointSingleModel.class);
	}

	@Test
	public void emptyScanModels() throws Exception {
		PointGeneratorService pservice = (PointGeneratorService) ValidatorService.getPointGeneratorService();
		for (Class<? extends IScanPathModel> modelType : pservice.getGenerators().keySet()) {
			IScanPathModel empty = modelType.newInstance();
			try {
			    validator.validate(empty);
			} catch (Exception ne) {
				continue;
			}
			if (!COMPLETE_MODELS.contains(empty.getClass())) {
			    fail("The model "+empty+" validated!");
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

}
