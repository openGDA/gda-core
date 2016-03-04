package uk.ac.gda.test.helpers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Helper class for running SWT unit tests. To use it, extend this class, and then use the 'shell' field as the parent
 * Composite for your tests. This class will take care of setting up the display and the shell, and disposing of them
 * appropriately.
 * <p>
 * For use in the standard ant build (where a physical display is not usually available), the ant script and testing
 * classpath need to be set up correctly to initialize a display and ensure the correct version of SWT is available.
 * See the releng.ant file in uk.ac.gda.common.rcp.test for an example.
 * 
 * @author Colin Palmer
 */
public class SWTTestBase {

	protected static Display display;
	protected Shell shell;

	@BeforeClass
	public static void initializeDisplay() {
		display = Display.getDefault();
	}

	@Before
	public void initializeShell() {
		disposeShell();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		shell.open();
	}

	@After
	public void destroyShell() {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				disposeShell();
			}
		});
	}

	@AfterClass
	public static void destroyDisplay() {
		if (display != null) {
			display.dispose();
			display = null;
		}
	}

	protected void newShell() {
		disposeShell();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		shell.open();
	}

	private void disposeShell() {
		if (shell != null) {
			shell.dispose();
			shell = null;
		}
	}
}
