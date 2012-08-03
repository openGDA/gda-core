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

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TextLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

/**
 * Comprehensive plugin test which includes testing stream and single, horizontal and vertical move and finding tomo
 * rotation axis.
 */
public class ComprehensiveTomoOperationsPluginTest extends UITestCaseSWT {

	private static final Logger logger = LoggerFactory.getLogger(ComprehensiveTomoOperationsPluginTest.class);

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
		PluginTestHelpers.openTomoPerspective(ui);
		// new DebugHelper().printWidgets();
		ui.click(new SWTWidgetLocator(ToolItem.class, "Reset Detector"));

		PluginTestHelpers.delay(3000);
	}

	/**
	 * Main test method.
	 */
	public void testStreamSingle() throws Exception {

		IUIContext ui = getUI();

		IWidgetLocator[] streamButtons = ui.findAll(new ButtonLocator("Stream"));
		IWidgetLocator[] singleButtons = ui.findAll(new ButtonLocator("Single"));

		IWidgetLocator sampleStream = streamButtons[0];
		IWidgetLocator flatSingle = singleButtons[1];
		IWidgetLocator flatStream = streamButtons[1];
		IWidgetLocator sampleSingle = singleButtons[0];

		for (int i = 0; i < 10; i++) {
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

	/**
	 * Main test method.
	 */
	public void testFindTomoRotationAxisPlugin() throws Exception {
		IUIContext ui = getUI();

		PluginTestHelpers.delay(2000);

		IWidgetLocator[] textBoxes = ui.findAll(new TextLocator());
		IWidgetLocator[] streamButtons = ui.findAll(new ButtonLocator("Stream"));

		ui.click(2, textBoxes[0], WT.SHIFT);
		ui.keyClick(WT.DEL);

		ui.enterText("0.5");
		ui.keyClick(WT.CR);
		if (!PluginTestHelpers.isStreaming()) {
			ui.click(streamButtons[0]);
		}
		PluginTestHelpers.delay(10000);

		ui.click(1, new ButtonLocator("Find Tomo Axis", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")),
				WT.CTRL);

		PluginTestHelpers.delay(15000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 569, 573));

		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 100, 573));

		PluginTestHelpers.delay(4000);
		ui.click(1, new ButtonLocator("Find Tomo Axis", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")),
				WT.CTRL);

		PluginTestHelpers.delay(15000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 100, 573));
		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 600, 573));

		PluginTestHelpers.delay(4000);
	}

	/**
	 * Main test method.
	 */
	public void testHorizontalVertical() throws Exception {
		IUIContext ui = getUI();

		IWidgetLocator[] textBoxes = ui.findAll(new TextLocator());
		IWidgetLocator[] streamButtons = ui.findAll(new ButtonLocator("Stream"));

		ui.click(2, textBoxes[0], WT.SHIFT);
		ui.keyClick(WT.DEL);

		ui.enterText("0.5");
		ui.keyClick(WT.CR);
		if (!PluginTestHelpers.isStreaming()) {
			ui.click(streamButtons[0]);
		}

		PluginTestHelpers.delay(5000);

		ui.click(1, new ButtonLocator("Horizontal", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(1000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 569, 573));

		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 100, 100));

		PluginTestHelpers.delay(20000);
		ui.click(1, new ButtonLocator("Vertical", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(15000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 100, 573));
		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 300, 125));

		PluginTestHelpers.delay(4000);

		ui.click(1, new ButtonLocator("Horizontal", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(1000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 100, 100));

		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 569, 573));

		PluginTestHelpers.delay(20000);
		ui.click(1, new ButtonLocator("Vertical", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(15000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 300, 125));
		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class, new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), 100, 573));
	}

}
