/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp;

import gda.util.TestUtils;

import junit.framework.Assert;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rcp.views.NexusTreeView;
import uk.ac.gda.common.rcp.util.EclipseUtils;

public class NexusTreeViewPluginTest {
	
	private NexusTreeView view;
	
	static String TestFileFolder;
	@BeforeClass
	static public void setUpClass() {
		TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
		if( TestFileFolder == null){
			Assert.fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
	}
	
	@Before
	public void setUp() throws Exception {

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		view = (NexusTreeView)window.getActivePage().showView(NexusTreeView.ID);
		
		window.getActivePage().activate(view);
		window.getActivePage().setPartState(window.getActivePage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
		waitForJobs();
	}

	/**
	 * Wait until all background tasks are complete.
	 */
	public void waitForJobs() {
		while (Job.getJobManager().currentJob() != null)
			EclipseUtils.delay(1000);
	}	

	@Test
	public void testNexusTreeView() {
		view.loadTree(TestFileFolder+"FeKedge_1_103.nxs");//"327.nxs");
		waitForJobs();
		EclipseUtils.delay(10000);			
	}

	@Test
	public void testNexusTreeViewWithFileThatBreaksIt() {
		
		view.loadTree(TestFileFolder+"/NexusUITest/ID22-ODA-MapSpectra.h5");
		waitForJobs();
		
		// We open the first node, this causes a UI exception on linux 64 and possibly other platforms.
		view.expandAll();
		
		waitForJobs();
		EclipseUtils.delay(10000);			
	}


	@Test
	public void testNexusTreeViewWithAnotherFileThatBreaksIt() {
		
		view.loadTree(TestFileFolder+"/NexusUITest/DCT_201006-good.h5");
		waitForJobs();
		
		// We open the first node, this causes a UI exception on linux 64 and possibly other platforms.
		view.expandAll();
		
		waitForJobs();
		EclipseUtils.delay(10000);			
	}

}

