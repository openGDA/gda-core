/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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
package uk.ac.gda.client.tomo.test;

import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class TomoStreamButtonPluginTest extends UITestCaseSWT {

	private static final Logger logger = LoggerFactory
			.getLogger(TomoStreamButtonPluginTest.class);

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
		ui.ensureThat(new WorkbenchLocator().isMaximized());
	}

	/**
	 * Main test method.
	 */
	public void testStreamSingle() throws Exception {

		IUIContext ui = getUI();
		PluginTestHelpers.openTomoPerspective(ui);

		//new DebugHelper().printWidgets();
		ui.click(new SWTWidgetLocator(ToolItem.class, "Reset Detector"));
		IWidgetLocator[] streamButtons = ui
				.findAll(new ButtonLocator("Stream"));
		IWidgetLocator[] singleButtons = ui
				.findAll(new ButtonLocator("Single"));

		IWidgetLocator sampleStream = streamButtons[0];
		IWidgetLocator flatSingle = singleButtons[1];
		IWidgetLocator flatStream = streamButtons[1];
		IWidgetLocator sampleSingle = singleButtons[0];

		for (int i = 0; i < 20; i++) {
			ui.click(sampleSingle);
			logger.info("Sample Single Clicked");
			PluginTestHelpers.delay(3000);
			
			ui.click(sampleStream);
			logger.info("Sample Stream Clicked");
			PluginTestHelpers.delay(3000);
			
			ui.click(flatStream);
			logger.info("Flat stream clicked");
			PluginTestHelpers.delay(3000);
			
			ui.click(flatSingle);
			logger.info("Sample Stream Clicked");
			PluginTestHelpers.delay(3000);
			
			ui.click(flatStream);
			logger.info("Flat stream clicked");
			PluginTestHelpers.delay(3000);
		}
		PluginTestHelpers.delay(10000);
	}
}