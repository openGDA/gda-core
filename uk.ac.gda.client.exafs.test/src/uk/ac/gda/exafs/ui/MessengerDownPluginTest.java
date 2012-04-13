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


import gda.TestHelpers;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.rcp.GDAClientActivator;
import gda.rcp.util.UIScanDataPointEventService;
import gda.scan.IScanDataPoint;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.actions.AlignmentModeHandler;
import uk.ac.gda.exafs.ui.actions.DataCollectionModeHandler;
import uk.ac.gda.preferences.PreferenceConstants;

/**
 * Run as junit plugin test.
 */
public class MessengerDownPluginTest implements IScanDataPointProvider {
	
	
	/**
	 * @throws Throwable
	 */
	@Before
	public void setUp() throws Throwable {
		ClientManager.setTestingMode(true);
		GDAClientActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS,1000);	
		
		TestHelpers.setUpTest(MessengerDownPluginTest.class, "setUp", false);
		InterfaceProvider.setScanDataPointProviderForTesting(this);
		UIScanDataPointEventService.getInstance().reconnect();
		
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IViewPart view = window.getActivePage().showView("org.eclipse.ui.views.ProblemView");
		window.getActivePage().activate(view);
		
		ActionFactory.IWorkbenchAction  maximizeAction = ActionFactory.MAXIMIZE.create(window);
		maximizeAction.run(); // Will maximize the active part

	}
	
	/**
	 * Try and kill messenger and get the following:
	 * 
	 * An IOException occurred at scim_bridge_client_imcontext_set_cursor_location ()
	 * The messenger is now down
	 */ 
	@Test
	public final void testConfuseScimBridge() {
		
		DataCollectionModeHandler.doDataCollectionMode();
		
		EclipseUtils.delay(5000);
		
		AlignmentModeHandler.doAlignemtMode();
		
		EclipseUtils.delay(5000);
		
		DataCollectionModeHandler.doDataCollectionMode();
		
		EclipseUtils.delay(5000);
		
		AlignmentModeHandler.doAlignemtMode();
	}

	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
	}

	@Override
	public void update(Object dataSource, Object data) {
	}

	@Override
	public IScanDataPoint getLastScanDataPoint() {
		return null;
	}
}
