/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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


import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.exafs.ui.describers.XasDescriber;
import uk.ac.gda.util.PackageUtils;

/**
 * Run as junit plugin test.
 */
public class XasParametersUIEditorPluginTest {
	
	/**
	 * Force into testing mode.
	 */
	static {
		ClientManager.setTestingMode(true);
	}
	
	private XasScanParametersEditor editor;
	private XasScanParametersUIEditor uiEd;

	
	@Before
	public void setUp() throws Throwable {
		ClientManager.setTestingMode(true);
		
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		final File xml = new File(PackageUtils.getTestPath(XasParametersUIEditorPluginTest.class)+"/XAS_Parameters.xml");
		
		final FileStoreEditorInput fileInput = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(xml));
		
		// Close the introduction page.
		this.editor = (XasScanParametersEditor)window.getActivePage().openEditor(fileInput, XasDescriber.ID);
		
		this.uiEd = (XasScanParametersUIEditor)editor.getRichBeanEditor();
	}
	
	@Test
	public final void testBounds() throws Throwable {
		
		final Double initialEnergy = (Double)uiEd.getInitialEnergy().getValue();
		if (initialEnergy!=6911.14d) throw new Exception("The initial energy is not as expected.");
		
		uiEd.getInitialEnergy().setValue(8000d);
		if (uiEd.getInitialEnergy().isValidBounds()) throw new Exception("The initial energy should be out of bounds but isn't.");
		if (uiEd.getB().isValidBounds()) throw new Exception("The B should be out of bounds but isn't.");
		
		uiEd._testSetGapChoice(0);
		if (uiEd.getB().isValidBounds()) throw new Exception("The B should be out of bounds but isn't.");
		
		final Color boxColor = uiEd.getB()._testGetForeGroundColor();
		final Color red      = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		if (boxColor.getRed() != red.getRed()) {
			throw new Exception("The B should have red foreground text but doesn't.");
		}
		
	}
	
	/**
	 * Simple test that element and values are linked.
	 * @throws Throwable
	 */
	@Test
	public final void testElements() throws Throwable {
        checkElement("Ag", 6.24d, 25316.4d,  26366.4d);
        checkElement("Cr", 1.01d, 5788.74d,  6838.74d);
        checkElement("Ge", 1.82d, 10903.13d, 11953.13d);
	}
	
	@Test
	public final void testStepUnits() throws Throwable {
		
		if (!uiEd.getExafsStep().getUnit().equals("eV")) throw new Exception("Funny unit for exafs step energy "+uiEd.getExafsStep().getUnit());
	    
		uiEd.getExafsStepType().setValue("k");
		
		if (!uiEd.getExafsStep().getUnit().equals("Å⁻¹")) throw new Exception("Funny unit for exafs step energy "+uiEd.getExafsStep().getUnit());
	}

	@Test
	public final void testConstantTimeVisibility() throws Throwable {
		
	    
		if (!uiEd.getExafsTime().isActivated())    throw new Exception("Exafs step time should be active.");
		if (uiEd.getExafsFromTime().isActivated()) throw new Exception("Exafs from time should not be active.");
		if (uiEd.getExafsToTime().isActivated())   throw new Exception("Exafs to time should not be active.");
		if(uiEd.getKWeighting().isActivated())		throw new Exception("K weighting should not be active.");
		uiEd._testSetTimeType(1);
		
		if (uiEd.getExafsTime().isActivated())      throw new Exception("Exafs step time should not be active.");
		if (!uiEd.getExafsFromTime().isActivated()) throw new Exception("Exafs from time should be active.");
		if (!uiEd.getExafsToTime().isActivated())   throw new Exception("Exafs to time should be active.");
		if(!uiEd.getKWeighting().isActivated())		throw new Exception("K weighting should be active.");
		
	}
	
	
	private void checkElement(String ename, double cHole, double initialE, double finalE) throws Exception {
		
        uiEd.getElement().setValue(ename);
        checkValue(cHole,    uiEd.getCoreHoleLabel().getValue(), "Core Hole");
        checkValue(initialE, uiEd.getInitialEnergy().getValue(),   "Initial Energy");
        checkValue(finalE,   uiEd.getFinalEnergy().getValue(),     "Final Energy");
		
	}

	private void checkValue(double check, Object value, String name) throws Exception {
		
		double dblValue;
		if (value instanceof String) {
			dblValue = Double.parseDouble((String)value);
		} else {
			dblValue = (Double)value;
		}
        if (check!=dblValue) throw new Exception("The '"+name+"' is incorrect. Found "+value+" should be "+check);
	}
}
