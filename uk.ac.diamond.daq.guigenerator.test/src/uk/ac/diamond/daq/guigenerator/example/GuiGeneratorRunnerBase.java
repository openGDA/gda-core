package uk.ac.diamond.daq.guigenerator.example;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.metawidget.inspector.annotation.MetawidgetAnnotationInspector;

import uk.ac.diamond.daq.guigenerator.GuiGeneratorService;
import uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector;

/**
 * Base class for GUI generator examples. When run() is called, it will call the abstract createTestObject() method,
 * auto-generate a GUI for the object and display it in a shell. The GUI will include automatic data binding from UI to
 * object, and if the object has PropertyChangeSupport it will add automatic object to UI binding as well.
 *
 * @param <T> the type returned by createTestObject(). Included only for convenience to avoid casts.
 */
public abstract class GuiGeneratorRunnerBase<T> {

	private Display display;
	private Shell shell;
	private IGuiGeneratorService guiGenerator;

	/**
	 * Display a window containing an auto-generated GUI. This method will return when the user closes the window.
	 */
	protected final void run() {
		setup();

		Object testObject = createTestObject();
		guiGenerator.generateGui(testObject, shell);

		displayShell();
	}

	/**
	 * @return an object to bind to the GUI
	 */
	public abstract T createTestObject();

	protected String getWindowTitle() {
		return "GUI generator example";
	}

	private void setup() {
		initializeShell();
		initializeGuiGenerator();
	}

	private void initializeShell() {
		display = Display.getDefault();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(getWindowTitle());
		FillLayout layout = new FillLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		shell.setLayout(layout);
	}

	private void initializeGuiGenerator() {
		// Add annotation inspectors to the GUI generator service. Normally this would be done automatically by OSGi.
		GuiGeneratorService.addDomInspector(new RichbeansAnnotationsInspector());
		GuiGeneratorService.addDomInspector(new MetawidgetAnnotationInspector());
		guiGenerator = new GuiGeneratorService();
	}

	private void displayShell() {
		resizeAndOpenShell();
		runEventLoop();
		destroyShell();
	}

	private void resizeAndOpenShell() {
		shell.pack();
		int xSize = Math.max(shell.getSize().x, 300);
		int ySize = Math.max(shell.getSize().y, 300);
		shell.setSize(xSize, ySize);

		Rectangle displaySize = display.getPrimaryMonitor().getBounds();
		int xLocation = displaySize.x + (displaySize.width - xSize) / 2;
		int yLocation = displaySize.y + (displaySize.height - ySize) / 2;
		shell.setLocation(xLocation, yLocation);

		shell.open();
	}

	private void runEventLoop() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void destroyShell() {
		if (shell != null) {
			shell.dispose();
			shell = null;
		}
		if (display != null) {
			display.dispose();
			display = null;
		}
	}
}
