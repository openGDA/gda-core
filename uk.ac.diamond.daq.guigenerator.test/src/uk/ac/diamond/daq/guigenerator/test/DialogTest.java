package uk.ac.diamond.daq.guigenerator.test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.metawidget.inspector.annotation.MetawidgetAnnotationInspector;

import uk.ac.diamond.daq.guigenerator.GuiGeneratorService;
import uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector;

/**
 * Tests for the {@link IGuiGeneratorService#openDialog(Object, Shell, String)} method.
 * <p>
 * Testing a modal dialog is a little tricky. This class works by setting up an SWT Display in a new thread which is
 * <em>not</em> the same as the thread used by JUnit to run the tests. This means the test thread is not the UI thread,
 * so all interactions with the UI in the tests should be done via {@link Display#syncExec(Runnable)}.
 * <p>
 * If other modal dialog tests are needed in future, it could be worth trying to extract some common functionality from
 * this into a base or utility class.
 */
@Ignore("This test runs in isolation but fails when run with the other tests in this plugin")
public class DialogTest {

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Static setup section.
	//
	// This code deals with setting up and destroying the display, running the event loop, and setting up the static
	// inspectors in the GUI generator service.
	//

	protected static Display display;

	@BeforeClass
	public static void initializeStatics() throws Exception {
		initializeDisplay();
		initialiseGuiGeneratorService();
	}

	private static void initializeDisplay() throws Exception {
		final CountDownLatch displayInitializedLatch = new CountDownLatch(1);
		new Thread(() -> {
			display = Display.getDefault();
			displayInitializedLatch.countDown();
			while (!display.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}).start();
		displayInitializedLatch.await();
	}

	private static void initialiseGuiGeneratorService() {
		GuiGeneratorService guiGeneratorService = new GuiGeneratorService();
		guiGeneratorService.addDomInspector(new RichbeansAnnotationsInspector());
		guiGeneratorService.addDomInspector(new MetawidgetAnnotationInspector());
	}

	@AfterClass
	public static void destroyDisplay() throws Exception {
		if (display != null) {
			display.syncExec(() -> display.dispose());
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Instance setup section.
	//
	// This code deals with creating a test bean, opening a dialog for it using the GUI generator, extracting the
	// Metawidget from the dialog so tests can interact with it, and closing the dialog after the test has finished.
	//

	protected IGuiGeneratorService guiGenerator;
	protected Composite metawidget;
	private CountDownLatch dialogClosedLatch;
	private Button okButton;

	private TestBean testBean;

	@Before
	public void setUp() throws Throwable {
		guiGenerator = new GuiGeneratorService();

		testBean = new TestBean();
		testBean.setStringField("String field value");
		testBean.setUiReadOnlyStringField("UiReadOnly string field value");
		testBean.setIntField(5);

		dialogClosedLatch = new CountDownLatch(1);

		final List<Throwable> errors = new ArrayList<Throwable>(1);

		// The details are important here! If the dialog is opened with syncExec(), the call will only return when the
		// user closes the dialog (which might be impossible in headless tests). So, we open the dialog with
		// asyncExec(), which somehow works and doesn't block the UI thread.
		display.asyncExec(() -> {
			try {
				guiGenerator.openDialog(testBean, null, "Test dialog");
				dialogClosedLatch.countDown();
			} catch (Throwable ne) {
				errors.add(ne);
			}
		});

		if (!errors.isEmpty()) throw errors.get(0);

		// Then, we get the metawidget from the dialog. We use syncExec() to ensure that the metawidget field is
		// properly initialised before the test method is called.
		display.syncExec(() -> {
			try {
				// Get the dialog shell
				// (We need to get all shells, confirm there is only one and then use it, because display.getActiveShell()
				// returns null when the tests are run via ant in headless mode.)
				assertThat(display.getShells().length, is(equalTo(1)));
				Shell dialog = display.getShells()[0];

				// There is one child Composite in the shell. The first of its children should be the generated metawidget.
				assertThat(dialog.getChildren().length, is(equalTo(1)));
				Control[] windowContents = ((Composite) dialog.getChildren()[0]).getChildren();
				metawidget = (Composite) windowContents[0];
				// Confirm that the composite is the Metawidget before we try and run any tests
				assertEquals("org.metawidget.swt.SwtMetawidget", metawidget.getClass().getName());

				// Get the OK button, confirm its identity and keep a reference to it.
				Composite buttonComposite = (Composite) windowContents[1];
				Control[] buttons = buttonComposite.getChildren();
				assertThat(buttons[0], is(instanceOf(Button.class)));
				okButton = (Button) buttons[0];
				assertThat(okButton.getText(), is(equalTo("OK")));
			} catch (Throwable ne) {
				errors.add(ne);
			}
		});

		if (!errors.isEmpty()) throw errors.get(0);

	}

	@After
	public void tearDown() throws Throwable {

		final List<Throwable> errors = new ArrayList<Throwable>(1);
		// Programmatically simulate pressing the OK button to close the dialog, by sending a selection event.
		display.syncExec(() -> {
			try {
				okButton.notifyListeners(SWT.Selection, new Event());
			} catch (Throwable ne) {
				errors.add(ne);
			}
		});

		if (!errors.isEmpty()) throw errors.get(0);


		// Wait for the dialog to close (if this fails, any subsequent tests will probably fail during setup)
		dialogClosedLatch.await(2, SECONDS);

		testBean = null;
		metawidget = null;
		guiGenerator = null;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Test section.
	//
	// This code copies a few of the tests from GuiGeneratorTest, wrapped in syncExec() calls to ensure JUnit waits for
	// the test code to finish before calling tearDown().
	//

	@Test
	public void testStringFieldIsText() throws Throwable {
		final List<Throwable> errors = new ArrayList<Throwable>(1);
		// Important to use syncExec() here
		display.syncExec(() -> {
			try {
				Control control = getNamedControl("stringField");
				assertThat(control, is(instanceOf(Text.class)));
			} catch (Throwable ne){
				errors.add(ne);
			}
		});
		if (!errors.isEmpty()) throw errors.get(0);
	}

	@Test
	public void testDoubleFieldInitalValue() throws Throwable {
		final List<Throwable> errors = new ArrayList<Throwable>(1);
		display.syncExec(() -> {
			try {
				Control control = getNamedControl("doubleField");
				assertThat(((Text) control).getText(), is(equalTo(String.valueOf(testBean.getDoubleField()))));
			} catch (Throwable ne){
				errors.add(ne);
			}
		});
		if (!errors.isEmpty()) throw errors.get(0);
	}

	@Test
	public void testDoubleFieldDataBinding() throws Throwable {
		final List<Throwable> errors = new ArrayList<Throwable>(1);
		display.syncExec(() -> {
			try {
				Text control = (Text) getNamedControl("doubleField");
				// Change the value in the GUI box
				control.setText("655.4");
				// Check the bean is updated
				assertEquals("doubleField not updated", 655.4, testBean.getDoubleField(), Double.MIN_VALUE);
			} catch (Throwable ne){
				errors.add(ne);
			}
		});
		if (!errors.isEmpty()) throw errors.get(0);
	}

	protected Control getNamedControl(String name) {
		return GuiGeneratorTestBase.getNamedControl(metawidget, name);
	}
}
