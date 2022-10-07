package uk.ac.diamond.daq.guigenerator.test;

import static org.metawidget.inspector.InspectionResultConstants.NAME;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.metawidget.inspector.annotation.MetawidgetAnnotationInspector;

import uk.ac.diamond.daq.guigenerator.GuiGeneratorService;
import uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector;

/**
 * Helper class for unit tests of the GUI generator, which initialises the generator and provides a utility method for
 * getting controls associated with named fields from Metawidgets.
 */
public class GuiGeneratorTestBase extends SWTTestBase {

	protected IGuiGeneratorService guiGenerator;
	protected Composite metawidget;

	@BeforeClass
	public static void initialiseGuiGeneratorService() {
		GuiGeneratorService guiGeneratorService = new GuiGeneratorService();
		guiGeneratorService.addDomInspector(new RichbeansAnnotationsInspector());
		guiGeneratorService.addDomInspector(new MetawidgetAnnotationInspector());
	}

	@Before
	public void initialiseGuiGenerator() throws Exception {
		guiGenerator = new GuiGeneratorService();
	}

	@After
	public void destroyGuiGenerator() throws Exception {
		metawidget = null;
		guiGenerator = null;
	}

	protected Control getNamedControl(String name) {
		return getNamedControl(metawidget, name);
	}

	/**
	 * Get a named control from a composite. (A control is regarded as named if getData("name") returns a string.)
	 * <p>
	 * Nested Metawidgets are named like other controls, so to find a particular nested field correctly, this method
	 * can be called repeatedly to find each Metawidget in the tree in turn until the required one is found.
	 * <p>
	 * <em>Unlike</em> the getControl() method in SwtMetawidget, this method will not recurse into any Composite
	 * children of the given Composite, to avoid issues around searching for non-unique field names in nested objects.
	 *
	 * @param container a composite to search
	 * @param name the name to search for
	 * @return the named control, or <code>null</code> if not found
	 */
	protected static Control getNamedControl(Composite container, String name) {
		for (Control child : container.getChildren()) {
			if (name.equals(child.getData(NAME))) {
				return child;
			}
		}
		return null; // not found
	}

	protected void flushUIEventQueue() {
		while (display.readAndDispatch()) {} // loop until all UI update events have been processed
	}
}
