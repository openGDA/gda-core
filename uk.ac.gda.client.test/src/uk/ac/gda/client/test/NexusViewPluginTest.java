/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package uk.ac.gda.client.test;

import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.net.URL;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rcp.results.navigator.DataNavigator;
import uk.ac.gda.ClientManager;
import uk.ac.gda.common.rcp.util.EclipseUtils;


/**
 * NOTE For this UI test to work properly we need to ensure that LD_LIBRARY_PATH is set.
 */
public class NexusViewPluginTest implements IScanDataPointProvider {
	
	
	@Before
	public void setup() {
		ClientManager.setTestingMode(true);
		
		// Set to stop the JythonTerminalView complaining 
		InterfaceProvider.setScanDataPointProviderForTesting(this);
	}
	
	public void selecteNexusFile() throws Exception {

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		// Open scripts perspective
		final IPerspectiveDescriptor scripts = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId("uk.ac.gda.pydev.extension.ui.JythonPerspective");
		window.getActivePage().setPerspective(scripts);
		EclipseUtils.delay(500);
		
		final URL dataDir   = ExafsScanDataPlotPluginTest.class.getResource("data");
		final String dir = (new File(EclipseUtils.getAbsoluteUrl(dataDir).getFile())).getAbsolutePath();
		DataNavigator.setDefaultDataFolder(dir);

		// Open data perspective
		final IPerspectiveDescriptor data = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId("uk.ac.diamond.scisoft.analysis.rcp.results");
		window.getActivePage().setPerspective(data);
		
		// We select the 101 plot
		final DataNavigator part = (DataNavigator)window.getActivePage().findView(DataNavigator.ID);
		part.setSelected("/FeKedge_1_98.nxs");
		part.setSelected("/FeKedge_1_101.nxs");
		part.setSelected("/FeKedge_1_104.nxs");
		EclipseUtils.delay(500);
	}

	@Test
	public void testPerspectiveChange() throws Exception {
		
		// Loop to try and break the loading
		for (int i = 0; i < 100; i++) {
			selecteNexusFile();
		}
		
		// We get to here is passed! Failior kills the VM.
	}

	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
		// Intentional do nothing
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
		//  Intentional do nothing
	}

	@Override
	public void update(Object dataSource, Object data) {
		//  Intentional do nothing
	}

	@Override
	public IScanDataPoint getLastScanDataPoint() {
		return null;
	}
	
}
