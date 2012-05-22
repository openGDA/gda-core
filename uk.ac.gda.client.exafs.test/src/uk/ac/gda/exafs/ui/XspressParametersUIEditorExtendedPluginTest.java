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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.xspress.XspressParametersEditor;
import uk.ac.gda.exafs.ui.detector.xspress.XspressParametersUIEditor;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.util.PackageUtils;

/**
 * Run as junit plugin test.
 */
public class XspressParametersUIEditorExtendedPluginTest {
	
	private XspressParametersEditor boundsEditor;
	/**
	 * @throws Throwable
	 */
	@Before
	public void setUp() throws Throwable {
		
		ClientManager.setTestingMode(true);
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		File xml = new File(PackageUtils.getTestPath(XspressParametersUIEditorExtendedPluginTest.class)+"/Xspress_BoundsTest.xml");
		FileStoreEditorInput fileInput = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(xml));
		this.boundsEditor = (XspressParametersEditor)window.getActivePage().openEditor(fileInput, XspressParametersEditor.ID);

	}

	/**
	 * @throws Throwable 
	 */
	@Test
	public final void testInputIdentified() throws Throwable {
		
		final XspressParametersUIEditor uiEd = (XspressParametersUIEditor)boundsEditor.getRichBeanEditor();
		uiEd.setEnabled(true);
		
		uiEd._testSetSelectedElement(0);
		uiEd._testSetSelectedElement(1);
		
		// If defect occurs, Region Start will be out of bounds, incorrectly.
		final DetectorElementComposite comp = (DetectorElementComposite)uiEd.getDetectorList().getEditorUI();
		final Object              roiEditor = comp.getRegionList().getEditorUI();
		final ScaleBox          roiStart = (ScaleBox)BeanUI.getFieldWiget("roiStart", roiEditor);
		
		if (!roiStart.isValidBounds()) throw new Exception("'roiStart' has incorrectly identified invalid bounds!");
		if (boundsEditor.isDirty())       throw new Exception("Editor is dirty even though only different nodes have been selected.");
	}
	

	/**
	 * @throws Throwable 
	 */
	@Test
	public final void testCanDelete() throws Throwable {
		
		final XspressParametersUIEditor uiEd = (XspressParametersUIEditor)boundsEditor.getRichBeanEditor();
		uiEd.setEnabled(true);
		
		uiEd._testSetSelectedElement(0);
		uiEd._testSetSelectedElement(1);
		uiEd._testAddRegionOfInterest("foo");
		uiEd._testDeleteRegionOfInterest();
		uiEd._testSetSelectedElement(0);
		uiEd._testSetSelectedElement(1);
		
		if (uiEd._testGetNumberOfRegions()!=1) throw new Exception("Region of interest did not delete properly.");
	}


	/**
	 * @throws Throwable 
	 */
	@Test
	public final void testCanMove() throws Throwable {
		
		final XspressParametersUIEditor uiEd = (XspressParametersUIEditor)boundsEditor.getRichBeanEditor();
		uiEd.setEnabled(true);
		
		uiEd._testSetSelectedElement(0);
		uiEd._testSetSelectedElement(1);
		uiEd._testAddRegionOfInterest("bar");
		uiEd._testMoveRegionOfInterest(1);
		uiEd._testSetSelectedElement(0);
		uiEd._testSetSelectedElement(1);
		
		if (!uiEd._testGetRegionName(1).equals("bar")) throw new Exception("Move did not move and keep position when changing elements in parent.");
	}

}
