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
import uk.ac.gda.exafs.ui.describers.OutputDescriber;
import uk.ac.gda.util.PackageUtils;

/**
 * Run as junit plugin test.
 */
public class OutputParametersUIEditorPluginTest {
	
	/**
	 * Force into testing mode.
	 */
	static {
		ClientManager.setTestingMode(true);
	}
	
	private OutputParametersEditor   editor;
	private OutputParametersUIEditor uiEd;
	/**
	 * @throws Throwable
	 */
	@Before
	public void setUp() throws Throwable {
		ClientManager.setTestingMode(true);
		
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		final File xml = new File(PackageUtils.getTestPath(OutputParametersUIEditorPluginTest.class)+"/Output_Parameters.xml");	
		final FileStoreEditorInput fileInput = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(xml));
	
		this.editor = (OutputParametersEditor)window.getActivePage().openEditor(fileInput, OutputDescriber.ID);
		this.uiEd = (OutputParametersUIEditor)editor.getRichBeanEditor();
	}
	
	/**
	 * Bounds working
	 * @throws Throwable
	 */ 
	@Test
	public final void testAFewValues() throws Throwable {
		if (!uiEd.getAsciiFileName().getValue().equals("FeKedge")) {
	    	throw new Exception("The asciiFileName was "+uiEd.getAsciiFileName().getValue()+" and not FeKedge");
	    }
	    if (!uiEd.getAsciiDirectory().getValue().equals("ascii")) {
	    	throw new Exception("The asciiDirectory was "+uiEd.getAsciiDirectory().getValue()+" and not ascii");
	    }
	    if (!uiEd.getNexusDirectory().getValue().equals("nexus")) {
	    	throw new Exception("The nexusDirectory was "+uiEd.getNexusDirectory().getValue()+" and not nexus");
	    }
	}
}
