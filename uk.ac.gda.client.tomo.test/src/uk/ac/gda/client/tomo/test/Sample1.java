package uk.ac.gda.client.tomo.test;

import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;

public class Sample1 extends UITestCaseSWT {

	/* @see junit.framework.TestCase#setUp()
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
	public void testSample1() throws Exception {
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("Window/Open Perspective/Other..."));
		ui.wait(new ShellShowingCondition("Open Perspective"));
		ui.click(2, new TableItemLocator("Tomography"));
		ui.wait(new ShellDisposedCondition("Open Perspective"));
		ui.click(new MenuItemLocator("Window/Show View/Tomography Alignment"));
	}

}