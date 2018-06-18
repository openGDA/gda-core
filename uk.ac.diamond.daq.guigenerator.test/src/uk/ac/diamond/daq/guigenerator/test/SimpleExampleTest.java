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
import static org.junit.Assert.assertThat;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.guigenerator.example.SimpleBean;
import uk.ac.diamond.daq.guigenerator.example.SimpleExample;

public class SimpleExampleTest extends GuiGeneratorTestBase {

	private SimpleBean simpleBean;

	@Before
	public void setUp() throws Exception {
		simpleBean = new SimpleExample().createTestObject();
		metawidget = guiGenerator.generateGui(simpleBean, shell);
	}

	@After
	public void tearDown() throws Exception {
		simpleBean = null;
	}

	@Test
	public void testInitialName() throws Exception {
		Control control = getNamedControl("name");
		assertThat(control, is(instanceOf(Label.class)));
		assertThat(((Label) control).getText(), is(equalTo(simpleBean.getName())));
	}

	@Test
	public void testInitialDescription() throws Exception {
		Control control = getNamedControl("description");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(((Text) control).getText().replace("\r", ""), is(equalTo(simpleBean.getDescription())));
	}

	@Test
	public void testInitialType() throws Exception {
		Control control = getNamedControl("type");
		assertThat(control, is(instanceOf(Combo.class)));
		assertThat(((Combo) control).getText(), is(equalTo(simpleBean.getType().toString())));
	}

	@Test
	public void testInitialCount() throws Exception {
		Control control = getNamedControl("count");
		assertThat(control, is(instanceOf(Spinner.class)));
		Spinner spinner = (Spinner) control;
		assertThat(spinner.getSelection(), is(equalTo(simpleBean.getCount())));
		assertThat(spinner.getMinimum(), is(equalTo(0)));
	}

	@Test
	public void testInitialX() throws Exception {
		Control control = getNamedControl("x");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(Double.parseDouble(((Text) control).getText()), is(equalTo(simpleBean.getX())));
	}

	@Test
	public void testInitialY() throws Exception {
		Control control = getNamedControl("y");
		assertThat(control, is(instanceOf(Text.class)));
		assertThat(Double.parseDouble(((Text) control).getText()), is(equalTo(simpleBean.getY())));
	}
}
