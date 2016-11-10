/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

import uk.ac.diamond.daq.guigenerator.example.DoubleWrapperBean;
import uk.ac.diamond.daq.guigenerator.example.DoublyNestedExample;
import uk.ac.diamond.daq.guigenerator.example.UpdatingBean;

public class DoublyNestedExampleTest extends GuiGeneratorTestBase {

	private DoubleWrapperBean doubleWrapperBean;

	@Before
	public void setUp() throws Exception {
		doubleWrapperBean = new DoublyNestedExample().createTestObject();
		metawidget = (Composite) guiGenerator.generateGui(doubleWrapperBean, shell);
	}

	@After
	public void tearDown() throws Exception {
		doubleWrapperBean = null;
	}

	@Test
	public void testInitialDoubleWrapperBeanName() throws Exception {
		Control control = getNamedControl("name");
		assertThat(control, is(instanceOf(Label.class)));
		assertThat(((Label) control).getText(), is(equalTo(doubleWrapperBean.getName())));
	}

	@Test
	public void testInitialDoubleWrapperBeanDescription() throws Exception {
		Control control = getNamedControl("description");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(((Text) control).getText().replace("\r", ""), is(equalTo(doubleWrapperBean.getDescription())));
	}

	@Test
	public void testInitialWrapperBeanName() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Control control = getNamedControl(wrapperBeanComposite, "name");
		assertThat(control, is(instanceOf(Label.class)));
		assertThat(((Label) control).getText().replace("\r", ""), is(equalTo(doubleWrapperBean.getWrapperBean().getName())));
	}

	@Test
	public void testInitialWrapperBeanDescription() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Control control = getNamedControl(wrapperBeanComposite, "description");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(((Text) control).getText().replace("\r", ""), is(equalTo(doubleWrapperBean.getWrapperBean().getDescription())));
	}

	@Test
	public void testInitialSimpleBeanName() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite simpleBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "simpleBean");
		Control control = getNamedControl(simpleBeanComposite, "name");
		assertThat(control, is(instanceOf(Label.class)));
		assertThat(((Label) control).getText(), is(equalTo(doubleWrapperBean.getWrapperBean().getSimpleBean().getName())));
	}

	@Test
	public void testInitialSimpleBeanDescription() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite simpleBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "simpleBean");
		Control control = getNamedControl(simpleBeanComposite, "description");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(((Text) control).getText().replace("\r", ""), is(equalTo(doubleWrapperBean.getWrapperBean().getSimpleBean().getDescription())));
	}

	@Test
	public void testInitialType() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite simpleBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "simpleBean");
		Control control = getNamedControl(simpleBeanComposite, "type");
		assertThat(control, is(instanceOf(Combo.class)));
		assertThat(((Combo) control).getText(), is(equalTo(doubleWrapperBean.getWrapperBean().getSimpleBean().getType().toString())));
	}

	@Test
	public void testInitialCount() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite simpleBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "simpleBean");
		Control control = getNamedControl(simpleBeanComposite, "count");
		assertThat(control, is(instanceOf(Spinner.class)));
		Spinner spinner = (Spinner) control;
		assertThat(spinner.getSelection(), is(equalTo(doubleWrapperBean.getWrapperBean().getSimpleBean().getCount())));
		assertThat(spinner.getMinimum(), is(equalTo(0)));
	}

	@Test
	public void testInitialSimpleBeanX() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite simpleBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "simpleBean");
		Control control = getNamedControl(simpleBeanComposite, "x");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(Double.parseDouble(((Text) control).getText()), is(equalTo(doubleWrapperBean.getWrapperBean().getSimpleBean().getX())));
	}

	@Test
	public void testInitialSimpleBeanY() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite simpleBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "simpleBean");
		Control control = getNamedControl(simpleBeanComposite, "y");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(Double.parseDouble(((Text) control).getText()), is(equalTo(doubleWrapperBean.getWrapperBean().getSimpleBean().getY())));
	}

	@Test
	public void testInitialUpdateValueAndTooltip() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite updatingBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "updatingBean");
		Control control = getNamedControl(updatingBeanComposite, "update");
		assertThat(control, is(instanceOf(Button.class)));
		assertThat(((Button) control).getSelection(), is(equalTo(doubleWrapperBean.getWrapperBean().getUpdatingBean().isUpdate())));
		assertThat(control.getToolTipText(), is(equalTo(UpdatingBean.UPDATE_BUTTON_TOOLTIP)));
	}

	@Test
	public void testInitialUpdatingBeanX() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite updatingBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "updatingBean");
		Control control = getNamedControl(updatingBeanComposite, "x");
		assertThat(control, is(instanceOf(Label.class)));
		String expected = NumberFormat.getNumberInstance().format(doubleWrapperBean.getWrapperBean().getUpdatingBean().getX());
		assertThat(((Label) control).getText(), is(equalTo(expected)));
	}

	@Test
	public void testInitialUpdatingBeanY() throws Exception {
		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite updatingBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "updatingBean");
		Control control = getNamedControl(updatingBeanComposite, "y");
		assertThat(control, is(instanceOf(Label.class)));
		String expected = NumberFormat.getNumberInstance().format(doubleWrapperBean.getWrapperBean().getUpdatingBean().getY());
		assertThat(((Label) control).getText(), is(equalTo(expected)));
	}

	@Test
	public void testValuesChangeWhileUpdateIsSelected() throws Exception {
		// Use the ICU default number format, which is the same as is used by the data binding conversion
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		UpdatingBean updatingBean = doubleWrapperBean.getWrapperBean().getUpdatingBean();

		// Record initial values. The default conversion from String to double is a bit complicated and uses some
		// data binding internals, so it's easier to convert everything to strings and compare those.
		String initialX = numberFormat.format(updatingBean.getX());
		String initialY = numberFormat.format(updatingBean.getY());

		Composite wrapperBeanComposite = (Composite) getNamedControl("wrapperBean");
		Composite updatingBeanComposite = (Composite) getNamedControl(wrapperBeanComposite, "updatingBean");
		Label xLabel = (Label) getNamedControl(updatingBeanComposite, "x");
		Label yLabel = (Label) getNamedControl(updatingBeanComposite, "y");

		// Confirm that GUI values are correct initially
		assertEquals("Initial X value is not set correctly in GUI", initialX, xLabel.getText());
		assertEquals("Initial Y value is not set correctly in GUI", initialY, yLabel.getText());

		// Turn on update and wait enough time for at least one update to happen
		// (Tried to do this by getting the "update" check box and selecting it, but for some reason the selection
		// event did not reach the bean, even though this works well in an interactive example.)
		updatingBean.setUpdate(true);
		Thread.sleep(1500);
		updatingBean.setUpdate(false);

		// Ensure the UI labels are updated
		flushUIEventQueue();

		String updatedX = xLabel.getText();
		String updatedY = yLabel.getText();

		// Check that the values in the GUI have now been changed by the updating thread
		assertNotEquals("X value should have changed but hasn't", initialX, updatedX);
		assertNotEquals("Y value should have changed but hasn't", initialY, updatedY);

		// Wait to ensure another update would have happened if updating were still active
		Thread.sleep(1500);
		flushUIEventQueue();

		// Check that the values in the GUI have stopped changing
		assertEquals("X value has changed", updatedX, xLabel.getText());
		assertEquals("Y value has changed", updatedY, yLabel.getText());
	}
}
