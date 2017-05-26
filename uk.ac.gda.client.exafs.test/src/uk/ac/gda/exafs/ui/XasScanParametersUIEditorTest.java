/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withLabel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.core.PyObject;

import gda.configuration.properties.LocalProperties;
import gda.exafs.scan.ExafsTimeEstimator;
import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;


@RunWith(SWTBotJunit4ClassRunner.class)
public class XasScanParametersUIEditorTest extends ShellTest {

	private XasScanParametersUIEditor ui;
	private XasScanParametersEditor beanEd;
	private XasScanParameters bean;
	private List<NumberBox> numberBoxes;
	private String[] testElements = {"Ac", "Fe", "Au", "Mo", "Hf", "Pd", "La", "Ag", "Cs"};
	private Color black, grey, red;

	@BeforeClass
	public static void setUp() {

		LocalProperties.set("gda.beamline.name", "");
		ExafsActivator.getStore().setValue(ExafsPreferenceConstants.A_ELEMENT_LINK, true);
		ExafsActivator.getStore().setValue(ExafsPreferenceConstants.B_ELEMENT_LINK, true);
		ExafsActivator.getStore().setValue(ExafsPreferenceConstants.C_ELEMENT_LINK, true);
		ExafsActivator.getStore().setValue(ExafsPreferenceConstants.INITIAL_ENERGY_ELEMENT_LINK, true);
		ExafsActivator.getStore().setValue(ExafsPreferenceConstants.FINAL_ENERGY_ELEMENT_LINK, true);
	}

	@Before
	public void getComponents() throws Exception {
		bean = (XasScanParameters) ui.fetchEditingBean();
		numberBoxes = Arrays.asList(
				ui.getEdgeEnergy(),
				ui.getInitialEnergy(),
				ui.getFinalEnergy(),
				ui.getGaf1(),
				ui.getGaf2(),
				ui.getGaf3(),
				ui.getA(),
				ui.getB(),
				ui.getC(),
				ui.getPreEdgeStep(),
				ui.getPreEdgeTime(),
				ui.getEdgeStep(),
				ui.getEdgeTime(),
				ui.getExafsStep(),
				ui.getExafsTime(),
				ui.getExafsFromTime(),
				ui.getExafsToTime(),
				ui.getKWeighting()
				);

		black = synchExec(()->Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		red = synchExec(()->Display.getDefault().getSystemColor(SWT.COLOR_RED));
		grey = synchExec(()->Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	@Override
	protected Shell createShell(Display display) throws Exception {


		Shell parent = new Shell(display);
		parent.setText("Test XAS Scan Parameters UI");
		parent.setLayout(new FillLayout());

		beanEd = new XasScanParametersEditor();

		String dir = "src/uk/ac/gda/exafs/beans/TestFiles/";
		String XMLFile = "XAS_Parameters.xml";



		ui = (XasScanParametersUIEditor) beanEd.getRichBeanEditorPart(null, XasScanParameters.createFromXML(dir+XMLFile));

		ui.createPartControl(parent);
		ui.linkUI(true);


		parent.pack();
		parent.open();

		return parent;
	}


	@Test
	public void testInitialValues() throws Exception {

		// Test ComboBoxes

		assertEquals(bean.getElement().toString(),bot.comboBoxWithLabel("Element").getText());
		assertEquals(bean.getEdge().toString(),bot.comboBoxWithLabel("Edge").getText());

		// Test ScaleBoxes

		assertEquals(bean.getEdgeEnergy(),synchExec( ()-> ui.getEdgeEnergy().getNumericValue()));

		assertEquals(bean.getInitialEnergy(),synchExec( ()-> ui.getInitialEnergy().getNumericValue()),0);
		assertEquals(bean.getFinalEnergy(), synchExec( ()-> ui.getFinalEnergy().getNumericValue()),0);

		assertEquals(bean.getGaf1(),synchExec( ()-> ui.getGaf1().getNumericValue()),0);
		assertEquals(bean.getGaf2(),synchExec( ()-> ui.getGaf2().getNumericValue()),0);
		assertEquals(bean.getGaf3(),synchExec( ()-> ui.getGaf3().getNumericValue()),0);

		assertEquals(bean.getPreEdgeStep(), synchExec( ()-> ui.getPreEdgeStep().getNumericValue()),0);
		assertEquals(bean.getPreEdgeTime(), synchExec( ()-> ui.getPreEdgeTime().getNumericValue()),0);
		assertEquals(bean.getEdgeStep(), synchExec( ()-> ui.getEdgeStep().getNumericValue()),0);
		assertEquals(bean.getEdgeTime(), synchExec( ()-> ui.getEdgeTime().getNumericValue()),0);
		assertEquals(bean.getExafsStep(), synchExec( ()-> ui.getExafsStep().getNumericValue()),0);
		assertEquals(bean.getExafsTime(), synchExec( ()-> ui.getExafsTime().getNumericValue()),0);
	}


	@Test
	public void testInvalidInputs() {
		bot.comboBoxWithLabel("Element").setSelection("Au"); // no need to do this check with every element/edge combination
		numberBoxes.forEach(this::checkBounds);
	}


	@Test
	public void testElementEdgeLoop() throws Exception {

		SWTBotCombo elements = bot.comboBoxWithLabel("Element");
		SWTBotCombo edges = bot.comboBoxWithLabel("Edge");

		for (String element : testElements) { // tested for all elements.items() successfully; takes too long.
			elements.setSelection(element);

			for (String edge : edges.items()) {

				edges.setSelection(edge);

				checkBeanUILink(element,edge);

				checkToolTip(ui.getA());
				checkToolTip(ui.getB(), element, edge);
				checkToolTip(ui.getC());

			}
		}
	}

	@Test
	public void edgeRegionGafEditability(){

		// When Edge Region set to Gaf1/Gaf2, Gaf1..3 editable,  A..C not editable

		bot.comboBoxWithLabel("Edge Region").setSelection("Gaf1/Gaf2");

		assertTrue(ui.getGaf1().isEditable()) ;
		assertTrue(ui.getGaf2().isEditable()) ;
		assertTrue(ui.getGaf3().isEditable()) ;

		assertTrue( ! ui.getA().isEditable() );
		assertTrue( ! ui.getB().isEditable() );
		assertTrue( ! ui.getC().isEditable() );

	}

	@Test
	public void edgeRegionABEditability() {

		// Opposite case of edgeRegionGaf()

		bot.comboBoxWithLabel("Edge Region").setSelection("A/B");

		assertTrue( ! ui.getGaf1().isEditable()) ;
		assertTrue( ! ui.getGaf2().isEditable()) ;
		assertTrue( ! ui.getGaf3().isEditable()) ;

		assertTrue( ui.getA().isEditable() );
		assertTrue( ui.getB().isEditable() );
		assertTrue( ui.getC().isEditable() );

	}

	@Test
	public void constantTimeTypeVisibility() {

		// Check correct labels and scaleboxes appear when "Constant Time" selected

		bot.comboBoxWithLabel("Exafs Time Type").setSelection("Constant Time");

		assertNotNull(bot.label("Exafs Time Step"));
		assertNotNull(ui.getExafsStep());
	}


	@Test
	public void variableTimeTypeVisibility() throws Exception {

		// Check correct labels and scaleboxes appear when "Variable Time" selected

		bot.comboBoxWithLabel("Exafs Time Type").setSelection("Variable Time");

		assertNotNull(ui.getExafsFromTime());
		assertNotNull(bot.label("Exafs From Time"));
		assertNotNull(ui.getExafsToTime());
		assertNotNull(bot.label("Exafs To Time"));
		assertNotNull(ui.getKWeighting());
		assertNotNull(bot.label("K Weighting"));
	}


	private void checkBounds(NumberBox numberbox) {

		try {
			double hi = synchExec( () -> numberbox.getMaximum());
			double lo = synchExec( () -> numberbox.getMinimum());

			if (synchExec(()->numberbox.isEnabled()) && synchExec(()->numberbox.isEditable())) {

				synchExec( () -> numberbox.setNumericValue(hi));
				if (numberbox.isEditable()) checkColour(black,numberbox);
				else checkColour(black, numberbox);

				synchExec( () -> numberbox.setNumericValue(hi+0.01));
				checkColour(red,numberbox);

				synchExec( () -> numberbox.setNumericValue(lo-0.05));
				checkColour(red,numberbox);
			}
		} catch (Exception e) {
		}
	}


	private void checkColour(Color expected, NumberBox numberbox) throws Exception {

		Color foreground = synchExec( () -> numberbox.getForeground() );
		assertEquals(numberbox.getFieldName(),expected, foreground);

	}

	private void checkExists(String label) throws WidgetNotFoundException{

		assertNotNull(bot.widget(withLabel(label)));
		assertNotNull(bot.label(label));
	}

	private void checkBeanUILink(String element, String edge) throws Exception {

		assertEquals(bean.getEdgeEnergy(),synchExec( ()-> ui.getEdgeEnergy().getNumericValue()),0);
		assertEquals(bean.getInitialEnergy(), synchExec( ()-> ui.getInitialEnergy().getNumericValue()),0);
		assertEquals(bean.getFinalEnergy(), synchExec( ()-> ui.getFinalEnergy().getNumericValue()),0);
		assertEquals(bean.getA(), synchExec( ()-> ui.getA().getNumericValue()),0);
		assertEquals(bean.getB(), synchExec( ()-> ui.getB().getNumericValue()),0);
		assertEquals(bean.getC(), synchExec( ()-> ui.getC().getNumericValue()),0);

		assertNotNull(bot.label(formatCoreHoleEnergy( Element.getElement(element).getCoreHole(edge) )+ " eV"));

		checkPoints();
	}


	private void checkPoints() {

		List<PyObject[]> points;

		try {
			points = ui.getScanPoints( ui.fetchEditingBean() );
			long time = ExafsTimeEstimator.getTime(points);
			Date date = new Date(time);
			DateFormat format = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.UK);
			format.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
			String estimatedTime = format.format(date);

			assertNotNull(bot.label(points.size() + " points"));
			assertNotNull(bot.label(estimatedTime));
		} catch (Exception e) {
			// Change to valid parameters and test again?
		}
	}
	private void checkToolTip(NumberBox numberbox) throws Exception {
		checkToolTip(numberbox, null, null);
	}

	private void checkToolTip(NumberBox numberbox, String element, String edge) throws Exception {

		String tooltip = synchExec( ()-> numberbox.getTooltipOveride());
		double value = synchExec( ()-> numberbox.getNumericValue());

		double hi = synchExec( () -> numberbox.getMaximum());
		double lo = synchExec( () -> numberbox.getMinimum());


		if (tooltip==null) {
			assertTrue("Value outside bounds", lo <= value && value <= hi);
			if (numberbox.isEditable()) checkColour(black,numberbox);
			else checkColour(grey, numberbox);
		} else if (tooltip.equals("The value '" + value + "' is greater than the upper limit.")) {
			assertTrue("Incorrect tooltip, value not greater than maximum",value > hi);
			checkColour(red, numberbox);
		} else if (tooltip.equals("The value '" + value + "' is less than the lower limit.")) {
			assertTrue("Failing with "+element+edge+"Incorrect tooltip, value not less than minimum", value < lo);
			checkColour(red, numberbox);
		}
	}


	private static String formatCoreHoleEnergy(double d)	{
	    if (d == (long) d)	return String.format("%d",(long)d);
	    else				return String.format("%s",d);
	}

}
