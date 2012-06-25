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

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;

public class TomographyPerspectivePluginTest extends UITestCaseSWT {

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		IUIContext ui = getUI();
		ui.ensureThat(new WorkbenchLocator().hasFocus());
		ui.ensureThat(ViewLocator.forName("Welcome").isClosed());
		ui.ensureThat(new WorkbenchLocator().isMaximized());
	}

	/**
	 * Main test method.
	 */
	public void testTomoAlignmentOperations3Plugin() throws Exception {
		IUIContext ui = getUI();

		PluginTestHelpers.openPerspective(ui);
	}

}