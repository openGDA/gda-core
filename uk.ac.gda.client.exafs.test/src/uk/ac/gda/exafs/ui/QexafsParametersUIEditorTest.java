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

import java.text.DecimalFormat;
import java.util.Random;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
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

import gda.configuration.properties.LocalProperties;
import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.QEXAFSParameters;

@Ignore("These are slow tests. Run them manually after editing GUIs")
@RunWith(SWTBotJunit4ClassRunner.class)
public class QexafsParametersUIEditorTest extends ShellTest {

	private QEXAFSParametersEditor beanEd;
	private QEXAFSParametersUIEditor ui;
	private QEXAFSParameters bean;
	private final double tolerance = 1e-6;

	@BeforeClass
	public static void setUp() {
		LocalProperties.set("gda.beamline.name", "");
	}

	@Before
	public void getComponents() {
		bean = (QEXAFSParameters) ui.fetchEditingBean();
	}

	@Override
	protected Shell createShell(Display display) throws Exception {


		Shell parent = new Shell(display);
		parent.setText("QEXAFS UI Test");
		parent.setLayout(new FillLayout());

		beanEd = new QEXAFSParametersEditor();

		String dir = "src/uk/ac/gda/exafs/beans/TestFiles/";
		String XMLFile = "QEXAFS_Parameters.xml";

		ui = (QEXAFSParametersUIEditor) beanEd.getRichBeanEditorPart(null,QEXAFSParameters.createFromXML(dir+XMLFile));
		ui.createPartControl(parent);
		ui.linkUI(true);

		parent.pack();
		parent.open();

		return parent;
	}

	@Test
	public void testShell() {

		assertNotNull(bot.shell("QEXAFS UI Test"));

		assertNotNull(bot.comboBoxWithLabel("Element"));
		assertNotNull(bot.comboBoxWithLabel("Edge"));

		assertNotNull(ui.getEdgeEnergy());
		assertNotNull(ui.getInitialEnergy());
		assertNotNull(ui.getFinalEnergy());
		assertNotNull(ui.getSpeed());
		assertNotNull(ui.getStepSize());
		assertNotNull(ui.getTime());

		assertNotNull(bot.button("Load defaults"));

		assertNotNull(bot.checkBox("Scan mono both ways"));

	}

	@Test
	public void testBinding() throws Exception {

		assertEquals(bean.getInitialEnergy(), synchExec(()->ui.getInitialEnergy().getValue()));
		assertEquals(bean.getFinalEnergy(), synchExec(()->ui.getFinalEnergy().getValue()));
		assertEquals(bean.getSpeed(), synchExec(() -> ui.getSpeed().getValue()));
		assertEquals(bean.getStepSize(), synchExec(() -> ui.getStepSize().getValue()));
		assertEquals(bean.getTime(), synchExec(() -> ui.getTime().getValue()));
		assertEquals(bean.getElement(), synchExec(() -> ui.getElement().getValue()));
		assertEquals(bean.getEdge(), synchExec(() -> ui.getEdge().getValue()));

	}

	private String[] randomElementEdgeCombination() {
		SWTBotCombo elements = bot.comboBoxWithLabel("Element");
		SWTBotCombo edges = bot.comboBoxWithLabel("Edge");

		Random random = new Random();
		String element = elements.items()[random.nextInt(elements.itemCount())];
		elements.setSelection(element);

		String edge = edges.items()[random.nextInt(edges.itemCount())];
		edges.setSelection(edge);

		return new String[] {element, edge};
	}


	@Test
	public void testDefaultParams() throws Exception {


		final DecimalFormat numberFormat = new DecimalFormat();
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setGroupingUsed(false);

		String[] selection = randomElementEdgeCombination();
		String element = selection[0];
		String edge = selection[1];

		bot.button("Load defaults").click();

		Element elem = Element.getElement(element);

		assertEquals(elem.getEdgeEnergy(edge), synchExec( () -> ui.getEdgeEnergy().getNumericValue()), tolerance);
		assertEquals(elem.getInitialEnergy(edge),(double) synchExec( () -> ui.getInitialEnergy().getValue()), tolerance);
		assertEquals(numberFormat.format(elem.getFinalEnergy(edge)), numberFormat.format( synchExec( () -> ((NumberBox)ui.getFinalEnergy()).getNumericValue())));
		assertEquals(formatCoreHoleEnergy(elem.getCoreHole(edge)), synchExec( () -> ui.getCoreHole().getValue()));

	}


	@Test
	public void testScanTimeNotEditable() {
		assertTrue(!((NumberBox) ui.getTime()).isEditable());
	}


	@Test
	public void testDefaultsButton() throws Exception {

		final String element = "Au";
		final String edge = "L2";
		final Element elem = Element.getElement(element);


		bot.comboBoxWithLabel("Element").setSelection(element);
		bot.comboBoxWithLabel("Edge").setSelection(edge);
		bot.button("Load defaults").click();

		assertEquals(elem.getEdgeEnergy(edge), synchExec( ()->ui.getEdgeEnergy().getNumericValue()), tolerance);
		assertEquals(elem.getInitialEnergy(edge),synchExec( ()->ui.getInitialEnergy().getValue()));
		assertEquals(elem.getFinalEnergy(edge), synchExec( ()->ui.getFinalEnergy().getValue()));
		assertEquals(formatCoreHoleEnergy(elem.getCoreHole(edge)), ui.getCoreHole().getValue());

		// With "Speed" and "Step Size" from XML file (to keep dependencies down)
		assertNotNull(bot.label("809"));
		assertNotNull(bot.label("58.96773 ms"));


	}

	@Test
	public void testBothWaysCheckBox() throws Exception {
		bot.checkBox().click();
		assertTrue(bot.checkBox().isChecked());
		assertTrue( synchExec( ()-> ui.getBothWays().getValue() ));
		bot.checkBox().click();
		assertTrue(!bot.checkBox().isChecked());
		assertTrue(!synchExec( ()-> ui.getBothWays().getValue() ));
	}

	@Test
	public void testUI2Bean() throws Exception {

		double initialEnergy = 5000.0;
		double finalEnergy = 7000.0;
		synchExec( ()->((NumberBox) ui.getInitialEnergy()).setNumericValue(initialEnergy));
		assertEquals(initialEnergy, bean.getInitialEnergy(), tolerance);

		synchExec( ()->((NumberBox) ui.getFinalEnergy()).setNumericValue(finalEnergy));
		assertEquals(finalEnergy, bean.getFinalEnergy(), tolerance);
	}

	private static String formatCoreHoleEnergy(double d)	{
	    if (d == (long) d)	return String.format("%d",(long)d);
	    else				return String.format("%s",d);
	}
}
