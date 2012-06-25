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

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TextLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;
import com.windowtester.runtime.swt.util.DebugHelper;

/**
 *
 */
public class CameraMotionPluginTest extends UITestCaseSWT {

	private static final Logger logger = LoggerFactory.getLogger(CameraMotionPluginTest.class);

	private ArrayList<String> tiffFileNames = new ArrayList<String>();

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
		PluginTestHelpers.openPerspective(ui);
		// new DebugHelper().printWidgets();
		PluginTestHelpers.delay(500);
		ui.click(new SWTWidgetLocator(ToolItem.class, "Reset Detector"));

		PluginTestHelpers.delay(3000);
	}

	public void testCameraMotionControls() throws Exception {
		final IUIContext ui = getUI();
		new DebugHelper().printWidgets();
		IWidgetLocator[] singleButtons = ui.findAll(new ButtonLocator("Single"));
		IWidgetLocator sampleSingle = singleButtons[0];
		IWidgetLocator[] textBoxes = ui.findAll(new TextLocator());
		ui.click(sampleSingle);
		ui.wait(new IsWorkbenchWindowBusy(ui));
		addTiffFileNameToList();

		ui.wait(new IsWorkbenchWindowBusy(ui));
		for (int i = 0; i < 5; i++) {
			for (int count = 20; count < 190; count = count + 20) {
				PluginTestHelpers.delay(2000);
				ui.click(textBoxes[5]);

				ui.keyClick(WT.CTRL, 'a');
				ui.keyClick(WT.DEL);
				ui.enterText(new String(count + ".00"));
				ui.keyClick(WT.CR);

				ui.wait(new IsWorkbenchWindowBusy(ui));
				PluginTestHelpers.delay(1000);

				ui.click(sampleSingle);

				ui.wait(new IsWorkbenchWindowBusy(ui));
				PluginTestHelpers.delay(2000);
				addTiffFileNameToList();
			}
		}

		for (String fileName : tiffFileNames) {
			logger.debug("FName  {}", fileName);
		}

		PluginTestHelpers.delay(2000);

	}

	private void addTiffFileNameToList() {
		final String[] str = new String[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.getActivePart();

				if (activePart instanceof TomoAlignmentView) {
					TomoAlignmentView tav = (TomoAlignmentView) activePart;
					try {
						str[0] = tav.getTomoAlignmentViewController().getDemandRawTiffFullFileName();
					} catch (Exception e) {
						logger.error("TODO put description of error here", e);
					}

				}
			}
		});
		if (str[0] != null) {
			tiffFileNames.add(str[0]);
		}
	}

}
