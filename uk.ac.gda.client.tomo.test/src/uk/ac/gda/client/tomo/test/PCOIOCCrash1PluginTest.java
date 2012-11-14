package uk.ac.gda.client.tomo.test;

import org.eclipse.swt.widgets.ToolItem;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ColumnLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TextLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class PCOIOCCrash1PluginTest extends UITestCaseSWT {

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
	public void testPCOIOCCrashPlugin() throws Exception {
		IUIContext ui = getUI();

		// PluginTestHelpers.delay(10000);
		ui.click(new MenuItemLocator("Window/Open Perspective/Other..."));
		ui.wait(new ShellShowingCondition("Open Perspective"));
		ui.click(new TableItemLocator("Tomo"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Open Perspective"));

		PluginTestHelpers.delay(5000);
		ui.click(new SWTWidgetLocator(ToolItem.class, "Reset Detector"));

		PluginTestHelpers.delay(5000);
		ui.click(new SWTWidgetLocator(ToolItem.class, "Reset Detector"));

		for (int i = 0; i < 1000; i++) {
			PluginTestHelpers.delay(5000);
			IWidgetLocator[] textBoxes = ui.findAll(new TextLocator());
			ui.click(2, textBoxes[0], WT.SHIFT);
			ui.keyClick(WT.DEL);
			PluginTestHelpers.delay(1000);

			ui.enterText("0.5");
			ui.keyClick(WT.CR);
			IWidgetLocator[] streamButtons = ui.findAll(new ButtonLocator("Stream"));
			IWidgetLocator[] singleButtons = ui.findAll(new ButtonLocator("Single"));

			ui.click(streamButtons[0]);
			PluginTestHelpers.delay(5000);

			ui.click(singleButtons[0]);
			PluginTestHelpers.delay(5000);

			ui.click(streamButtons[0]);
			PluginTestHelpers.delay(5000);

			//
			ui.click(new MenuItemLocator("Window/Show View/Tomography Configuration"));
			//

			PluginTestHelpers.delay(3000);
			ui.click(new ButtonLocator("Delete All Configurations", new ViewLocator(
					"uk.ac.gda.client.tomo.configuration.view")));
			PluginTestHelpers.delay(2000);

			ui.click(new MenuItemLocator("Window/Show View/Tomography Alignment"));

			// Part which fails the area detector
			// takeFlatAndDark(ui, streamButtons);

			//
			ui.click(streamButtons[0]);
			PluginTestHelpers.delay(5000);

			ui.click(new ButtonLocator("1", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));
			PluginTestHelpers.delay(2000);
			ui.click(new ButtonLocator("2", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));
			PluginTestHelpers.delay(2000);
			
			ui.click(new ButtonLocator("4", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));
			PluginTestHelpers.delay(2000);
			ui.click(new ButtonLocator("0.5", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));
			PluginTestHelpers.delay(2000);
			//
			ui.click(2, textBoxes[2], WT.SHIFT);
			ui.keyClick(WT.DEL);
			PluginTestHelpers.delay(5000);
			//
			ui.enterText("new tests:" +i);
			ui.keyClick(WT.CR);
			ui.click(new ButtonLocator("Save Alignment", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));
			ui.click(new MenuItemLocator("Window/Show View/Tomography Configuration"));
			ui.click(new TableItemLocator("::", new ViewLocator("uk.ac.gda.client.tomo.configuration.view")));

			ui.click(1, new ColumnLocator(1, new TableItemLocator("::", new ViewLocator(
					"uk.ac.gda.client.tomo.configuration.view"))));

			PluginTestHelpers.delay(2000);

			ui.click(new ButtonLocator("Start Tomo Runs", new ViewLocator("uk.ac.gda.client.tomo.configuration.view")));
			PluginTestHelpers.delay(120000);
			ui.click(new ButtonLocator("Stop Tomo Runs", new ViewLocator("uk.ac.gda.client.tomo.configuration.view")));
			ui.click(new MenuItemLocator("Window/Show View/Tomography Alignment"));

		}
		ui.wait(new ShellDisposedCondition("Beamline i12 - Tel. +44 1235 778375 - GDA - 8.26.0"));
	}

	private void takeFlatAndDark(IUIContext ui, IWidgetLocator[] streamButtons) throws WidgetSearchException {
		ui.click(new ButtonLocator("Take Flat && Dark", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));

		PluginTestHelpers.delay(10000);
		ui.wait(new IsWorkbenchWindowBusy(ui));

		ui.click(streamButtons[0]);

		ui.click(new ButtonLocator("Show Flat", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));

		PluginTestHelpers.delay(2000);
		ui.click(new ButtonLocator("Show Dark", new ViewLocator("uk.ac.gda.client.tomo.alignment.view")));
		PluginTestHelpers.delay(2000);
	}

}