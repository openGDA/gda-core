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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.scan.AxisConfiguration;
import org.eclipse.scanning.api.ui.auto.IInterfaceService;
import org.eclipse.scanning.api.ui.auto.IModelDialog;
import org.eclipse.scanning.api.ui.auto.IModelViewer;
import org.eclipse.scanning.device.ui.model.InterfaceService;
import org.eclipse.scanning.test.util.JUnit5ShellTest;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * @deprecated along with {@link AxisConfiguration} - remove together.
 */
@Deprecated(since="9.33", forRemoval=true)
public class AxisConfigurationTest extends JUnit5ShellTest {

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

	private AxisConfiguration    config;
	private IModelViewer<AxisConfiguration> viewer;

	@Override
	protected Shell createShell(Display display) throws Exception {

		this.config = new AxisConfiguration();
		config.setApplyModels(true);
		config.setApplyRegions(true);
		config.setXAxisName("stage_x");
		config.setXAxisStart(0);
		config.setXAxisEnd(100);
		config.setYAxisName("stage_y");
		config.setYAxisStart(-100);
		config.setYAxisEnd(-200);
		config.setMicroscopeImage("C:/tmp/fred.png");


		this.viewer = interfaceService.createModelViewer();

		Shell shell = new Shell(display);
		shell.setText("Scan Area");
		shell.setLayout(new GridLayout(1, false));
        viewer.createPartControl(shell);
		viewer.setModel(config);

		shell.pack();
		shell.setSize(500, 500);
		shell.open();

		return shell;
	}

	@Test
	public void checkShell() throws Exception {
		assertNotNull(bot.shell("Scan Area"));
	}

	@Test
	public void testDialog() throws Exception {
		Shell shell = bot.shell("Scan Area").widget;

		List<Exception> errors = new ArrayList<>();
		shell.getDisplay().syncExec(()->{
			try {
				// Intentionally loose generics here because the
				// ModelCellEditor cannot type check so we mimik
				// what it does to reproduce an old defect
				IModelDialog dialog = interfaceService.createModelDialog(shell);
				dialog.create();
				dialog.setModel(config);
			} catch (Exception ne) {
				errors.add(ne);
			}
		});
		if (errors.size()>0) throw errors.get(0);
	}

	@Disabled("DAQ-2088 Fails due to expecting units")
	@Test
	public void checkInitialValues() throws Exception {

		assertEquals(config.getMicroscopeImage(),                 bot.table(0).cell(0, 1));

		assertEquals(config.getXAxisName(),                    bot.table(0).cell(2, 1));
		assertEquals(String.valueOf(config.getXAxisStart())+" mm",   bot.table(0).cell(3, 1));
		assertEquals(String.valueOf(config.getXAxisEnd())+" mm",     bot.table(0).cell(4, 1));

		assertEquals(config.getYAxisName(),                    bot.table(0).cell(5, 1));
		assertEquals(String.valueOf(config.getYAxisStart())+" mm",   bot.table(0).cell(6, 1));
		assertEquals(String.valueOf(config.getYAxisEnd())+" mm",     bot.table(0).cell(7, 1));

	}

	@Disabled("DAQ-2088 This test consistently fails on Jenkins only")
	@Test
	public void checkFilePath() throws Exception {

		assertEquals(config.getMicroscopeImage(), bot.table(0).cell(0, 1));

		bot.table(0).click(0, 1); // Make the file editor

		SWTBotText text = bot.text(0);
		assertNotNull(text);
		assertEquals(config.getMicroscopeImage(), text.getText());

		text.setText("Invalid Path");

		Color red = new Color(bot.getDisplay(), 255, 0, 0, 255);
        assertEquals(red, text.foregroundColor());

        File file = File.createTempFile("a_testFile", ".txt");
        file.deleteOnExit();
		text.setText(file.getAbsolutePath());

		Color black = new Color(bot.getDisplay(), 0, 0, 0, 255);
        assertEquals(black, text.foregroundColor());


	}

	@Disabled("DAQ-2088 Fails due to expecting units")
	@Test
	public void checkFastStart() throws Exception {

		assertEquals(String.valueOf(config.getXAxisStart())+" mm", bot.table(0).cell(3, 1));

		bot.table(0).click(3, 1); // Make the file editor

		SWTBotText text = bot.text(0);
		assertNotNull(text);
		assertEquals(String.valueOf(config.getXAxisStart()), text.getText());

		Color red = new Color(bot.getDisplay(), 255, 0, 0, 255);
		Color black = new Color(bot.getDisplay(), 0, 0, 0, 255);
        assertEquals(black, text.foregroundColor());

        text.setText("-2000");
        assertEquals(red, text.foregroundColor());

        text.setText("1");
        assertEquals(black, text.foregroundColor());

        text.setText("1001");
        assertEquals(red, text.foregroundColor());

	}
}
