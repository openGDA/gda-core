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

import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import gda.configuration.properties.LocalProperties;
import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.XanesScanParameters;


@RunWith(SWTBotJunit4ClassRunner.class)
public class XanesScanParametersUIEditorTest extends ShellTest {

	private XanesScanParametersEditor beanEd;
	private XanesScanParametersUIEditor ui;
	private XanesScanParameters bean;

	@BeforeClass
	public static void setUp() {
		LocalProperties.set("gda.beamline.name", "");
	}

	@Before
	public void getBean() {
		bean = (XanesScanParameters) ui.fetchEditingBean();
	}

	private void setElementAndEdge(String element, String edge) {
		bot.comboBoxWithLabel("Element").setSelection(element);
		bot.comboBoxWithLabel("Edge").setSelection(edge);
	}

	@Test
	public void testDefaultRegions() throws Exception {
		SWTBotCombo elements = bot.comboBoxWithLabel("Element");
		SWTBotCombo edges = bot.comboBoxWithLabel("Edge");
		String[] elementArray = {"Fe", "Cl", "Au", "Mo", "Hf", "Pd", "La", "Ag", "Cs"};

		DecimalFormat numberFormat = new DecimalFormat();
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setGroupingUsed(false);

		for (final String element : elementArray) {

			elements.setSelection(element);

			for (final String edge : edges.items()) {

				edges.setSelection(edge);

					double edgeVal = Element.getElement(element).getEdgeEnergy(edge);
					double coreHole = Element.getElement(element).getCoreHole(edge);

					bot.button("             Get Defaults            ").click();
					for (int region = 0; region < bean.getRegions().size(); region ++) {
						if (region==0) {
							assertEquals("Failed with "+element+" "+edge,String.valueOf(edgeVal-100*coreHole),bot.table().cell(region, 1));
							assertEquals(String.valueOf(5 * coreHole),bot.table().cell(region, 2));
						}
						else if (region==1) {
							assertEquals(String.valueOf(edgeVal-20*coreHole),bot.table().cell(region, 1));
							assertEquals(String.valueOf(coreHole),bot.table().cell(region, 2));
						}
						else if (region==2) {
							assertEquals(String.valueOf(edgeVal-10*coreHole),bot.table().cell(region, 1));
							assertEquals(String.valueOf(coreHole/5),bot.table().cell(region, 2));
						}
						else if (region==3) {
							assertEquals(String.valueOf(edgeVal+10*coreHole),bot.table().cell(region, 1));
							assertEquals(String.valueOf(coreHole),bot.table().cell(region, 2));
						}
						else if (region ==4) {
							assertEquals(String.valueOf(edgeVal+20*coreHole),bot.table().cell(region, 1));
							assertEquals(String.valueOf(coreHole*2),bot.table().cell(region, 2));
						}
					}


					// Additionally, check that the final energy is loaded correctly.
					assertEquals("Failed with "+element+" "+edge,numberFormat.format(Element.getElement(element).getFinalEnergy(edge)), numberFormat.format(synchExec( ()-> ui.getFinalEnergy().getNumericValue())));


			}
		}
	}


	@Test
	public void checkShell() {
		assertNotNull(bot.shell("XANES Scan Parameters UI Test"));

		assertNotNull(bot.comboBoxWithLabel("Element"));
		assertNotNull(bot.comboBoxWithLabel("Edge"));

		assertNotNull(ui.getEdgeEnergy());
		assertNotNull(ui.getCoreHole());
		assertNotNull(ui.getFinalEnergy());

		assertNotNull(bot.button("             Get Defaults            "));
		assertNotNull(bot.button("           Add Region           "));
		assertNotNull(bot.button("           Remove Region           "));
	}

	@Test
	public void loadFromBean() throws Exception {

		assertEquals(bean.getElement(), synchExec(()->ui.getElement().getValue()));
		assertEquals(bean.getEdge(), synchExec(()->ui.getEdge().getValue()));
		assertEquals(bean.getFinalEnergy(), synchExec(()->ui.getFinalEnergy().getValue()));

		List<Region> regions = bean.getRegions();
		for (int region=0;region<regions.size();region++) {
			assertEquals(String.valueOf(regions.get(region).getEnergy()),bot.table().cell(region, 1));
			assertEquals(String.valueOf(regions.get(region).getStep()),bot.table().cell(region, 2));
			assertEquals(String.valueOf(regions.get(region).getTime()),bot.table().cell(region, 3));
		}

	}

	@Test
	public void updateElementAndEdgeEnergies() throws Exception {

		final String element = "Au";
		final String edge = "L2";
		setElementAndEdge(element, edge);

		assertEquals(Element.getElement(element).getEdgeEnergy(edge),synchExec( ()-> ui.getEdgeEnergy().getNumericValue()),0);
		assertNotNull(bot.label(formatCoreHoleEnergy( Element.getElement(element).getCoreHole(edge) )+ " eV"));

	}

	@Test
	public void testAddRemoveButtons() {
		int hi = 10;
		for (int i=0; i<hi;i++) {
			bot.button("           Add Region           ").click();
		}
		assertEquals(bean.getRegions().size(),hi+5);


		for (int i=hi; i>0;i--) {
			bot.table().click(i+4, 0);
			bot.button("           Remove Region           ").click();
		}
		assertEquals(5,bean.getRegions().size());

	}

	@Override
	protected Shell createShell(Display display) throws Exception {

		Shell parent = new Shell(display);
		parent.setText("XANES Scan Parameters UI Test");
		parent.setLayout(new FillLayout());

		beanEd = new XanesScanParametersEditor();

		String dir = "src/uk/ac/gda/exafs/beans/TestFiles/";
		String XMLFile = "XanesScanParameters.xml";

		ui = (XanesScanParametersUIEditor) beanEd.getRichBeanEditorPart(null, XanesScanParameters.createFromXML(dir+XMLFile));

		ui.createPartControl(parent);
		ui.linkUI(true);

		parent.pack();
		parent.setSize(430, 600);
		parent.open();

		return parent;
	}

	public static String formatCoreHoleEnergy(double d)	{
	    if (d == (long) d)	return String.format("%d",(long)d);
	    else				return String.format("%s",d);
	}

}