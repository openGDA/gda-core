package uk.ac.gda.common.rcp.test.inspector;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.metawidget.inspector.InspectionResultConstants.NAME;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.test.helpers.swt.SWTTestBase;

/**
 * Test to confirm that the copied annotations in org.eclipse.scanning.api.annotation are interpreted correctly.
 * <p>
 * This needs to be run as a plugin test with uk.ac.diamond.daq.guigenerator and org.eclipse.scanning.api bundles
 * included.
 */
public class GDAMetawidgetInspectorPluginTest extends SWTTestBase {

	// Can't actually access the Stub class without messing with the dependencies, so just check it by name
	private static final String HIDDEN_FIELD_CLASS_NAME = "org.metawidget.swt.Stub";

	private static IGuiGeneratorService guiGenerator;

	public static void setGuiGenerator(IGuiGeneratorService guiGeneratorService) {
		guiGenerator = guiGeneratorService;
	}

	private TwoAxisGridPointsModel gridModel;
	private Composite metawidget;

	@Before
	public void setUp() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-4.0);
		box.setyAxisStart(2.5);
		box.setxAxisLength(6.0);
		box.setyAxisLength(5.1);

		gridModel = new TwoAxisGridPointsModel();
		gridModel.setyAxisPoints(5);
		gridModel.setxAxisPoints(6);
		gridModel.setBoundingBox(box);

		metawidget = guiGenerator.generateGui(gridModel, shell);
	}

	@After
	public void tearDown() throws Exception {
		gridModel = null;
		metawidget = null;
	}

	@Test
	public void testUniqueKeyFieldIsHidden() throws Exception {
		Control control = getControl("uniqueKey");
		assertThat(control.getClass().getName(), is(equalTo(HIDDEN_FIELD_CLASS_NAME)));
	}

	@Test
	public void testBoundingBoxFieldIsHidden() throws Exception {
		Control control = getControl("boundingBox");
		assertThat(control.getClass().getName(), is(equalTo(HIDDEN_FIELD_CLASS_NAME)));
	}

	@Test
	public void testXNameFieldIsHidden() throws Exception {
		Control control = getControl("xName");
		assertThat(control.getClass().getName(), is(equalTo(HIDDEN_FIELD_CLASS_NAME)));
	}

	@Test
	public void testFastAxisPointsFieldIsSpinner() throws Exception {
		Control control = getControl("fastAxisPoints");
		assertThat(control, is(instanceOf(Spinner.class)));
	}

	@Test
	public void testFastAxisPointsFieldInitialValue() throws Exception {
		Control control = getControl("fastAxisPoints");
		assertThat(((Spinner) control).getSelection(), is(equalTo(gridModel.getxAxisPoints())));
	}

	@Test
	public void testFastAxisPointsFieldMinimumValue() throws Exception {
		Control control = getControl("fastAxisPoints");
		assertThat(((Spinner) control).getMinimum(), is(equalTo(1)));
	}

	private Control getControl(String name) {
		return getControl(metawidget, name);
	}

	private static Control getControl(Composite container, String name) {
		for (Control child : container.getChildren()) {
			// TODO investigate this - is it necessary to check for name == null, and does this code work for nested Metawidgets?
			if (child.getData(NAME) == null && child instanceof Composite) {
				Control control = getControl((Composite) child, name);
				if (control != null) {
					return control;
				}
			}
			if (name.equals(child.getData(NAME))) {
				return child;
			}
		}
		return null; // not found
	}
}
