/*-
d * Copyright © 2017 Diamond Light Source Ltd.
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
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.IonChambersBean;
import uk.ac.gda.exafs.ui.ionchambers.IonChamber;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;


@RunWith(SWTBotJunit4ClassRunner.class)
public class IonChamberUITest extends ShellTest {

	private final static String CONFIG_PATH = "src/uk/ac/gda/exafs/beans/TestFiles";


	@Override
	protected Shell createShell(Display display) throws Exception {

		Shell parent = new Shell(display);
		parent.setText("Ion Cambers UI Test");
		IonChambersBean bean = null;
		bean = beanFromXML();
		new IonChamber(parent, bean);

		parent.pack();
		parent.open();

		return parent;
	}

	@BeforeClass
	public static void setUpProperties() {

		LocalProperties.set(LocalProperties.GDA_CONFIG, CONFIG_PATH);
		setupBeansFactory();
	}

	@Test
	public void testPressureCalculation() throws Exception {

		bot.comboBoxWithLabel("Gas Type").setSelection("He");
		bot.textWithLabel("Absorption (%)").setText("65.5");

		// Will fail the test with WidgetNotFoundException if the calculation does not work
		bot.label("110.00 bar");
	}

	@Test
	public void testFromXML() {

		IonChambersBean bean = beanFromXML();
		assertEquals("Energy not correctly loaded from XML", String.valueOf(bean.getEnergy()), bot.textWithLabel("Energy (eV)", 0).getText());

		List<IonChamberParameters> ionChambers = bean.getIonChambers();
		ionChambers.forEach(this::checkIonChamberParametersFromXML);
	}

	private IonChambersBean beanFromXML() {
		try {
			return (IonChambersBean) XMLHelpers.createFromXML(IonChambersBean.mappingURL, IonChambersBean.class, IonChambersBean.schemaURL, CONFIG_PATH+"/templates/ionChambers.xml");
		} catch (Exception e) {
			fail(e+"Could not load bean from XML");
		}
		return null;
	}

	private void checkIonChamberParametersFromXML(IonChamberParameters icp) {

		final String failureSuffix = " not correctly loaded from XML";
		final String ionChamber = icp.getName();
		final String group = "Ion chambers - "+ionChamber;

		assertEquals("Absorption for "+ionChamber+failureSuffix, String.valueOf(icp.getPercentAbsorption()), bot.textWithLabelInGroup("Absorption (%)", group,0).getText());
		assertEquals("Checkbox for "+ionChamber+failureSuffix, icp.getFlush(), bot.checkBoxInGroup(group,0).isChecked());

		bot.labelInGroup("Advanced", group).click();

		assertEquals("Pressure for " + ionChamber+failureSuffix, String.valueOf(icp.getTotalPressure()), bot.textInGroup(group,1).getText());
		assertEquals("Chamber length for " + ionChamber+failureSuffix,String.valueOf(icp.getIonChamberLength()),bot.textInGroup(group,2).getText());
		assertEquals("Fill 1 period for "+ionChamber+failureSuffix, String.valueOf(icp.getGas_fill1_period_box()),bot.textInGroup(group,3).getText());
		assertEquals("Fill 2 period for "+ionChamber+failureSuffix, String.valueOf(icp.getGas_fill2_period_box()),bot.textInGroup(group,4).getText());

	}

	private static void setupBeansFactory() {
		try {
			BeansFactory.getClasses();
		} catch (NullPointerException npe) {
			Class<?>[] classes = new Class<?>[]{IonChambersBean.class};
			BeansFactory.setClasses(classes);
		}
	}
}