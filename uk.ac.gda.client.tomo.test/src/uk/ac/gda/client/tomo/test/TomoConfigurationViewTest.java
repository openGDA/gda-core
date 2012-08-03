/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.test;

import gda.factory.FactoryException;
import gda.util.SpringObjectServer;

import java.io.File;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.configuration.view.factory.TomoConfigurationViewFactory;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

/**
 *
 */
public class TomoConfigurationViewTest extends UITestCaseSWT {
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigurationViewTest.class);

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
		ui.ensureThat(new WorkbenchLocator().isMaximized());
	}

	@Test
	public void testConfigView() throws PartInitException, FactoryException, WidgetSearchException {

		SpringObjectServer os = new SpringObjectServer(
				new File(
						"/scratch/i12Workspc_git/gda-dls-beamlines-i12.git/i12/clients/rcp/simulation/tomoConfiguration_test.xml"),
				true);
		os.configure();
		PluginTestHelpers.delay(2000);

		IUIContext ui = getUI();
		try {
			PluginTestHelpers.openTomoPerspective(ui);
		} catch (Exception e) {
			logger.error("Error:{}", e);
		}

		//PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TomoConfigurationViewFactory.ID);
		PluginTestHelpers.showConfigurationView(ui);
		PluginTestHelpers.delay(300000);
	}
}
