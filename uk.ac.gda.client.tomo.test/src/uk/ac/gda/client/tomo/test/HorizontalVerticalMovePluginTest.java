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

public class HorizontalVerticalMovePluginTest extends UITestCaseSWT {

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
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
	public void testHorizontalVertical() throws Exception {
		IUIContext ui = getUI();

		PluginTestHelpers.openTomoPerspective(ui);
		PluginTestHelpers.delay(2000);
		ui.click(new SWTWidgetLocator(ToolItem.class, "Reset Detector"));
//		new DebugHelper().printWidgets();
		IWidgetLocator[] textBoxes = ui.findAll(new TextLocator());
		IWidgetLocator[] streamButtons = ui
				.findAll(new ButtonLocator("Stream"));

		ui.click(2, textBoxes[0], WT.SHIFT);
		ui.keyClick(WT.DEL);

		ui.enterText("0.5");
		ui.keyClick(WT.CR);
		if (!PluginTestHelpers.isStreaming()) {
			ui.click(streamButtons[0]);
		}

		PluginTestHelpers.delay(5000);

		ui.click(1, new ButtonLocator("Horizontal", new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(1000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 569,
				573));

		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 100,
				100));

		PluginTestHelpers.delay(20000);
		ui.click(1, new ButtonLocator("Vertical", new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(15000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 100,
				573));
		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 300,
				125));

		PluginTestHelpers.delay(4000);

		ui.click(1, new ButtonLocator("Horizontal", new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(1000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 100,
				100));

		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 569,
				573));
		
		PluginTestHelpers.delay(20000);
		ui.click(1, new ButtonLocator("Vertical", new ViewLocator(
				"uk.ac.gda.client.tomo.alignment.view")), WT.CTRL);

		PluginTestHelpers.delay(15000);
		ui.mouseMove(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 300,
				125));
		ui.dragTo(new XYLocator(new SWTWidgetLocator(Canvas.class,
				new ViewLocator("uk.ac.gda.client.tomo.alignment.view")), 100,
				573));
	}

}