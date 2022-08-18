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
package org.eclipse.scanning.test.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.richbeans.test.utilities.ui.ShellTest;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.api.ui.auto.IInterfaceService;
import org.eclipse.scanning.api.ui.auto.IModelViewer;
import org.eclipse.scanning.device.ui.model.InterfaceService;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class KnownModelsTest extends ShellTest{

	private static IInterfaceService interfaceService; // We really get this from OSGi services!

	@BeforeAll
	public static void createServices() throws Exception {
		interfaceService = new InterfaceService(); // Just for testing! This comes from OSGi really.
		UITestServicesSetup.createTestServices(false);
	}

	@AfterAll
	public static void disposeServices() throws Exception {
		interfaceService = null;
		UITestServicesSetup.disposeTestServices();
	}

	private IModelViewer<Object> viewer;

	@Override
	protected Shell createShell(Display display) throws Exception {

		this.viewer = interfaceService.createModelViewer();

		Shell shell = new Shell(display);
		shell.setText("Point Model");
		shell.setLayout(new GridLayout(1, false));
        viewer.createPartControl(shell);

		shell.pack();
		shell.setSize(500, 500);
		shell.open();

		return shell;
	}

	@Test
	public void checkShell() {
		assertNotNull(bot.shell("Point Model"));
	}

	@Test
	public void testVariousPointsModels() {
		assertNotNull(bot.shell("Point Model"));

		List<ModelTest> models = createTestPointsModels();
		testModels(models);
	}

	private void testModels(List<ModelTest> models) {
		for (ModelTest tcase : models) {
			bot.shell("Point Model").display.syncExec(()->{
				try {
					viewer.setModel(tcase.getModel());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			String className = tcase.getModel().getClass().getSimpleName();
			// All fields are now editable, as annotations have been removed
			assertEquals("Checking editable fields of "+className, tcase.getFieldCount(), bot.table(0).rowCount());
			System.out.println(className+" Passed");
		}
	}

	private List<ModelTest> createTestPointsModels() {
		List<ModelTest> models = new ArrayList<>();
		models.add(new ModelTest(new AxialStepModel("x", 0, 10, 1), 8));
		models.add(new ModelTest(new AxialPointsModel("x", 7.5, 3), 8));
		models.add(new ModelTest(new AxialCollatedStepModel(0, 10, 1, "x1", "y1"), 9));
		models.add(new ModelTest(new AxialArrayModel(0,1,2,3,4,5,6,7,8,9), 5));
		models.add(new ModelTest(new TwoAxisGridPointsModel("x", "y"), 14));
		models.add(new ModelTest(new TwoAxisGridStepModel("x", "y"), 14));
		models.add(new ModelTest(new TwoAxisSpiralModel("x", "y", 2, null), 11));
		models.add(new ModelTest(new TwoAxisLissajousModel(), 12));
		return models;
	}

	@Test
	public void testVariousDetectorModels() {
		assertNotNull(bot.shell("Point Model"));

		List<ModelTest> models = createTestDetectorModels();
		testModels(models);

	}

	private List<ModelTest> createTestDetectorModels() {
		final List<ModelTest> models = new ArrayList<>();
		models.add(new ModelTest(new MandelbrotModel("x", "y"), 17));
		models.add(new ModelTest(new DarkImageModel(), 6));
		models.add(new ModelTest(new ConstantVelocityModel(), 9));
		return models;
	}

	private class ModelTest {
		private final Object model;
		private final int fieldCount;

		public ModelTest(Object model, int fieldCount) {
			this.model = model;
			this.fieldCount = fieldCount;
		}
		public Object getModel() {
			return model;
		}
		public int getFieldCount() {
			return fieldCount;
		}
	}

}
