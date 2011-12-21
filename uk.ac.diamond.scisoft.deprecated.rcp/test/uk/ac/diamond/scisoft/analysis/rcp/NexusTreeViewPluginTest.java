/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

