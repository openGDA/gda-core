package uk.ac.diamond.daq.guigenerator.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

import uk.ac.diamond.daq.guigenerator.test.TestBean.ExampleEnum;

public class GuiGeneratorTest extends GuiGeneratorTestBase {

	private TestBean testBean;

	@Before
	public void setUp() throws Exception {
		testBean = new TestBean();
		testBean.setStringField("String field value");
		testBean.setUiReadOnlyStringField("UiReadOnly string field value");
		testBean.setIntField(5);
		testBean.setType(ExampleEnum.SECOND_VALUE);

		metawidget = guiGenerator.generateGui(testBean, shell);
	}

	@After
	public void tearDown() throws Exception {
		testBean = null;
	}

	@Test
	public void testHiddenStringFieldIsStub() throws Exception {
		// This field should be hidden by XML metadata
		Control control = getNamedControl("hiddenStringField");
		assertThat(control.getClass().getName(), is(equalTo("org.metawidget.swt.Stub")));
	}

	@Test
	public void testStringFieldIsText() throws Exception {
		Control control = getNamedControl("stringField");
		assertThat(control, is(instanceOf(Text.class)));
	}

	@Test
	public void testStringFieldInitialValue() throws Exception {
		Control control = getNamedControl("stringField");
		assertThat(((Text) control).getText(), is(equalTo(testBean.getStringField())));
	}

	@Test
	public void testStringFieldTooltip() throws Exception {
		Control control = getNamedControl("stringField");
		assertThat(control.getToolTipText(), is(equalTo(TestBean.STRING_FIELD_TOOLTIP)));
	}

	@Test
	public void testStringFieldWithGetterOnlyIsLabel() throws Exception {
		Control control = getNamedControl("stringFieldWithGetterOnly");
		assertThat(control, is(instanceOf(Label.class)));
	}

	@Test
	public void testStringFieldWithGetterOnlyValue() throws Exception {
		Control control = getNamedControl("stringFieldWithGetterOnly");
		assertThat(((Label) control).getText(), is(equalTo(testBean.getStringFieldWithGetterOnly())));
	}

	@Test
	public void testUiReadOnlyStringFieldIsLabel() throws Exception {
		Control control = getNamedControl("uiReadOnlyStringField");
		assertThat(((Label) control).getText(), is(equalTo(testBean.getUiReadOnlyStringField())));
	}

	@Test
	public void testUiReadOnlyStringFieldValue() throws Exception {
		Control control = getNamedControl("uiReadOnlyStringField");
		assertThat(((Label) control).getText(), is(equalTo(testBean.getUiReadOnlyStringField())));
	}

	@Test
	public void testStringFieldToTextControlDataBinding() throws Exception {
		String newValue = "New value";
		testBean.setStringField(newValue);
		Control control = getNamedControl("stringField");
		assertThat(((Text) control).getText(), is(equalTo(newValue)));
	}

	@Test
	public void testTextControlToStringFieldDataBinding() throws Exception {
		Text control = (Text) getNamedControl("stringField");
		// Change the value in the GUI box
		String newValue = "New string value from text box";
		control.setText(newValue);
		//Check the bean is updated
		assertEquals("stringField not updated", newValue, testBean.getStringField());
	}

	@Test
	public void testIntFieldIsSpinner() throws Exception {
		Control control = getNamedControl("intField");
		assertThat(control, is(instanceOf(Spinner.class)));
	}

	@Test
	public void testIntFieldInitialValue() throws Exception {
		Control control = getNamedControl("intField");
		assertThat(((Spinner) control).getSelection(), is(equalTo(testBean.getIntField())));
	}

	@Test
	public void testIntFieldMinimumValue() throws Exception {
		Control control = getNamedControl("intField");
		assertThat(((Spinner) control).getMinimum(), is(equalTo(Integer.valueOf(TestBean.INT_FIELD_MIN_VALUE_STRING))));
	}

	@Test
	public void testIntFieldMaximumValue() throws Exception {
		Control control = getNamedControl("intField");
		assertThat(((Spinner) control).getMaximum(), is(equalTo(Integer.valueOf(TestBean.INT_FIELD_MAX_VALUE_STRING))));
	}

	@Test
	public void testIntFieldUnits() throws Exception {
		Control control = getNamedControl("intField_label");
		assertThat(((Label) control).getText(), is(equalTo("Int Field [eV]:")));
	}

	@Test
	public void testDoubleFieldInitalValue() throws Exception {
		Control control = getNamedControl("doubleField");
		assertThat(((Text) control).getText(), is(equalTo(String.valueOf(testBean.getDoubleField()))));
	}

	@Test
	public void testDoubleFieldToTextControlDataBinding() throws Exception {
		// The default conversion from String to double is a bit complicated and uses some data binding internals, so
		// it's easier to convert everything to strings and compare those.
		// We use the ICU default number format, which is the same as is used by the data binding conversion.
		double newValue = -1.452e5;
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		String expectedText = numberFormat.format(newValue);
		testBean.setDoubleField(newValue);
		Control control = getNamedControl("doubleField");
		assertEquals(expectedText, ((Text) control).getText());
	}

	@Test
	public void testTextControlToDoubleFieldDataBinding() throws Exception {
		Text control = (Text) getNamedControl("doubleField");
		// Change the value in the GUI box
		double newValue = 655.4;
		control.setText(Double.toString(newValue));
		//Check the bean is updated
		assertEquals("doubleField not updated", newValue, testBean.getDoubleField(), Double.MIN_VALUE);
	}

	@Test
	public void testDoubleFieldUnits() throws Exception {
		Control control = getNamedControl("doubleField_label");
		assertThat(((Label) control).getText(), is(equalTo("Double Field [Hz]:")));
	}

	@Test
	public void testDoubleFieldTextIsBlackInsideLimit() throws Exception {
		Text control = (Text) getNamedControl("doubleField");
		// Change the value in the GUI box outside the upper limit
		control.setText("642.3");
		//Check the text is red
		assertThat(control.getForeground(), is(equalTo(Display.getDefault().getSystemColor(SWT.COLOR_BLACK))));
	}

	@Test
	public void testDoubleFieldTextGoesRedOutsideUpperLimit() throws Exception {
		Text control = (Text) getNamedControl("doubleField");
		// Change the value in the GUI box outside the upper limit
		control.setText("745");
		//Check the text is red
		assertThat(control.getForeground(), is(equalTo(Display.getDefault().getSystemColor(SWT.COLOR_RED))));
	}

	@Test
	public void testDoubleFieldTextGoesRedOutsideLowerLimit() throws Exception {
		Text control = (Text) getNamedControl("doubleField");
		// Change the value in the GUI box outside the upper limit
		control.setText("222.22");
		//Check the text is red
		assertThat(control.getForeground(), is(equalTo(Display.getDefault().getSystemColor(SWT.COLOR_RED))));
	}

	@Test
	public void testEnumFieldIsCombo() throws Exception {
		Control control = getNamedControl("type");
		assertThat(control, is(instanceOf(Combo.class)));
	}

	@Test
	public void testEnumFieldInitalValue() throws Exception {
		Control control = getNamedControl("type");
		assertThat(((Combo) control).getText(), is(equalTo(testBean.getType().toString())));
	}

	@Test
	public void testEnumFieldToComboDataBinding() throws Exception {
		Control control = getNamedControl("type");
		assertThat(((Combo) control).getText(), is(equalTo(ExampleEnum.SECOND_VALUE.toString())));
		testBean.setType(ExampleEnum.FIRST_VALUE);
		assertThat(((Combo) control).getText(), is(equalTo(ExampleEnum.FIRST_VALUE.toString())));
		testBean.setType(null);
		assertThat(((Combo) control).getText(), is(equalTo("")));
	}

	@Test
	public void testComboToEnumFieldDataBinding() throws Exception {
		Combo combo = (Combo) getNamedControl("type");
		assertEquals(ExampleEnum.SECOND_VALUE, testBean.getType());
		combo.setText(ExampleEnum.FIRST_VALUE.toString());
		assertEquals(ExampleEnum.FIRST_VALUE, testBean.getType());
		combo.setText("");
		assertNull(testBean.getType());
	}
}
