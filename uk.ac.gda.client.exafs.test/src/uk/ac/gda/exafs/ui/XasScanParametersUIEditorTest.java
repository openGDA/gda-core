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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.core.PyObject;

import gda.configuration.properties.LocalProperties;
import gda.exafs.scan.ExafsTimeEstimator;
import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

@Ignore("These are slow tests. Run them manually after editing GUIs")
@RunWith(SWTBotJunit4ClassRunner.class)
public class XasScanParametersUIEditorTest extends ShellTest {

	private XasScanParametersUIEditor ui;
	private XasScanParametersEditor beanEd;
	private XasScanParameters bean;

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
	public void getComponents() {
		bean = (XasScanParameters) ui.fetchEditingBean();
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
	public void binding() throws Exception {

		// Select a random element/edge combination (which will change the values in the GUI), and check the ui/bean binding

		SWTBotCombo elements = bot.comboBoxWithLabel("Element");
		SWTBotCombo edges = bot.comboBoxWithLabel("Edge");

		Random random = new Random();
		String element = elements.items()[random.nextInt(elements.itemCount())];
		elements.setSelection(element);

		String edge = edges.items()[random.nextInt(edges.itemCount())];
		edges.setSelection(edge);

		checkBeanUIBinding(element,edge);
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
	public void variableTimeTypeVisibility() {

		// Check correct labels and scaleboxes appear when "Variable Time" selected

		bot.comboBoxWithLabel("Exafs Time Type").setSelection("Variable Time");

		assertNotNull(ui.getExafsFromTime());
		assertNotNull(bot.label("Exafs From Time"));
		assertNotNull(ui.getExafsToTime());
		assertNotNull(bot.label("Exafs To Time"));
		assertNotNull(ui.getKWeighting());
		assertNotNull(bot.label("K Weighting"));
	}

	private void checkBeanUIBinding(String element, String edge) throws Exception {

		assertEquals(bean.getEdgeEnergy(),synchExec( ()-> ui.getEdgeEnergy().getNumericValue()),0);
		assertEquals(bean.getInitialEnergy(), synchExec( ()-> ui.getInitialEnergy().getNumericValue()),0);
		assertEquals(bean.getFinalEnergy(), synchExec( ()-> ui.getFinalEnergy().getNumericValue()),0);
		assertEquals(bean.getA(), synchExec( ()-> ui.getA().getNumericValue()),0);
		assertEquals(bean.getB(), synchExec( ()-> ui.getB().getNumericValue()),0);
		assertEquals(bean.getC(), synchExec( ()-> ui.getC().getNumericValue()),0);

		assertEquals(bean.getPreEdgeStep(), synchExec( ()-> ui.getPreEdgeStep().getNumericValue()),0);
		assertEquals(bean.getPreEdgeTime(), synchExec( ()-> ui.getPreEdgeTime().getNumericValue()),0);
		assertEquals(bean.getEdgeStep(), synchExec( ()-> ui.getEdgeStep().getNumericValue()),0);
		assertEquals(bean.getEdgeTime(), synchExec( ()-> ui.getEdgeTime().getNumericValue()),0);
		assertEquals(bean.getExafsStep(), synchExec( ()-> ui.getExafsStep().getNumericValue()),0);
		assertEquals(bean.getExafsTime(), synchExec( ()-> ui.getExafsTime().getNumericValue()),0);

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

	private static String formatCoreHoleEnergy(double d)	{
	    if (d == (long) d)	return String.format("%d",(long)d);
	    else				return String.format("%s",d);
	}

}
