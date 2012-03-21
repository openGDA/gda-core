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
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.exafs.ui.detector.xspress.XspressParametersEditor;
import uk.ac.gda.exafs.ui.detector.xspress.XspressParametersUIEditor;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.util.PackageUtils;

/**
 * Run as junit plugin test.
 */
public class XspressParametersUIEditorPluginTest {
	
	private XspressParametersEditor editor;
	/**
	 * @throws Throwable
	 */
	@Before
	public void setUp() throws Throwable {
		
		ClientManager.setTestingMode(true);
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		File xml = new File(PackageUtils.getTestPath(XspressParametersUIEditorPluginTest.class)+"/Xspress_Parameters.xml");
		FileStoreEditorInput fileInput = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(xml));
		this.editor = (XspressParametersEditor)window.getActivePage().openEditor(fileInput, XspressParametersEditor.ID);

	}
	
	/**
	 * This method tests a BeanListEditor nested in a GridListEditor.
	 * @throws Throwable
	 */ 
	@Test
	public final void testNested() throws Throwable {
		
		final XspressParametersUIEditor uiEd = (XspressParametersUIEditor)editor.getRichBeanEditor();
		uiEd.setEnabled(true);
		
		uiEd._testSetSelectedElement(0);
		uiEd._testAddRegionOfInterest("bill");
		
		if (uiEd._testGetNumberOfRegions()!=1) throw new Exception("UI does not have correct value for regions of interest.");
		
		final XspressParameters params = new XspressParameters();
		BeanUI.uiToBean(uiEd, params);
		
		// Check 64 elements
		if (params.getDetectorList().size()!=64) throw new Exception("There should be 64 elements in the bean from this editor.");
		
		// Check ROI was added
		final DetectorElement ele = params.getDetectorList().get(0);
		if (ele.getRegionList().size()!=1) throw new Exception("There should be one ROI in the first element.");
		
	}
	
	/**
	 * This method tests a BeanListEditor nested in a GridListEditor.
	 * @throws Throwable
	 */ 
	@Test
	public final void testRoiName() throws Throwable {
	
		final XspressParametersUIEditor uiEd = (XspressParametersUIEditor)editor.getRichBeanEditor();
		uiEd.setEnabled(true);
		
		uiEd._testSetSelectedElement(0);
		uiEd._testAddRegionOfInterest("fred");
		
		final XspressParameters params = new XspressParameters();
		BeanUI.uiToBean(uiEd, params);
				
		// Check ROI was added
		final DetectorElement ele = params.getDetectorList().get(0);
		final List<XspressROI> ro = ele.getRegionList();
		
        for (XspressROI xspressROI : ro) {
			if (xspressROI.getRoiName().equals("fred")) {
				return;
			}
		}
        
        throw new Exception("Cannot find named ROI 'fred'.");
	}
}
