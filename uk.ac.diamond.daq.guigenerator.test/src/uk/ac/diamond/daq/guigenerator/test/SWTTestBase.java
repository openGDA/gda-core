package uk.ac.diamond.daq.guigenerator.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Helper class for running SWT unit tests.
 * <p>
 * The test runner must be configured to have a virtual display avalable e.g. Xvfb
 *
 * @author Colin Palmer
 */
public class SWTTestBase {

	protected static Display display;
	protected Shell shell;

	@BeforeClass
	public static void initializeDisplay() throws Exception {
		display = Display.getDefault();
	}

	@Before
	public void initializeShell() throws Exception {
		destroyShell();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		shell.open();
	}

	@After
	public void destroyShell() throws Exception {
		if (shell != null) {
			shell.dispose();
		}
	}

	@AfterClass
	public static void destroyDisplay() throws Exception {
		if (display != null) {
			display.dispose();
		}
	}
}
