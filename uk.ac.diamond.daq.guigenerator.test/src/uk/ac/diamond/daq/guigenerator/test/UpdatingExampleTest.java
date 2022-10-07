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
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

import uk.ac.diamond.daq.guigenerator.example.UpdatingBean;
import uk.ac.diamond.daq.guigenerator.example.UpdatingExample;

public class UpdatingExampleTest extends GuiGeneratorTestBase {

	private UpdatingBean updatingBean;

	@Before
	public void setUp() throws Exception {
		updatingBean = new UpdatingExample().createTestObject();
		metawidget = guiGenerator.generateGui(updatingBean, shell);
	}

	@After
	public void tearDown() throws Exception {
		updatingBean = null;
	}

	@Test
	public void testInitialUpdateValueAndTooltip() throws Exception {
		Control control = getNamedControl("update");
		assertThat(control, is(instanceOf(Button.class)));
		assertThat(((Button) control).getSelection(), is(equalTo(updatingBean.isUpdate())));
		assertThat(control.getToolTipText(), is(equalTo(UpdatingBean.UPDATE_BUTTON_TOOLTIP)));
	}

	@Test
	public void testInitialX() throws Exception {
		Control control = getNamedControl("x");
		assertThat(control, is(instanceOf(Label.class)));
		String expected = NumberFormat.getNumberInstance().format(updatingBean.getX());
		assertThat(((Label) control).getText(), is(equalTo(expected)));
	}

	@Test
	public void testInitialY() throws Exception {
		Control control = getNamedControl("y");
		assertThat(control, is(instanceOf(Label.class)));
		String expected = NumberFormat.getNumberInstance().format(updatingBean.getY());
		assertThat(((Label) control).getText(), is(equalTo(expected)));
	}

	@Test
	public void testValuesChangeWhileUpdateIsSelected() throws Exception {
		// Use the ICU default number format, which is the same as is used by the data binding conversion
		NumberFormat numberFormat = NumberFormat.getNumberInstance();

		// Record initial values. The default conversion from String to double is a bit complicated and uses some
		// data binding internals, so it's easier to convert everything to strings and compare those.
		String initialX = numberFormat.format(updatingBean.getX());
		String initialY = numberFormat.format(updatingBean.getY());

		Label xLabel = (Label) getNamedControl("x");
		Label yLabel = (Label) getNamedControl("y");

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
