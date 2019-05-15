/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.eclipse.richbeans.test.ui.ShellTest;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.scanning.device.ui.device.MonitorViewer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore("DAQ-2088 These tests time out and fail")
@RunWith(SWTBotJunit4ClassRunner.class)
public class MonitorViewerTest extends ShellTest {

	@BeforeClass
	public static void createServices() throws Exception {
		UITestServicesSetup.createTestServices(true);
	}

	@AfterClass
	public static void disposeServices() throws Exception {
		UITestServicesSetup.disposeTestServices();
	}

	private MonitorViewer viewer;

	@Override
	protected Shell createShell(Display display) throws Exception {

		this.viewer = new MonitorViewer();

		Shell shell = new Shell(display);
		shell.setText("Monitors");
		shell.setLayout(new GridLayout(1, false));
		viewer.createControl(shell);

		shell.pack();
		shell.setSize(500, 500);
		shell.open();

		return shell;
	}

	@Test
	public void checkShell() throws Exception {
		assertNotNull(bot.shell("Monitors"));
	}

	@Test
	public void checkSetEnabled() throws Exception {
		assertEquals(0, viewer.getEnabledMonitors().size());

		bot.table(0).click(0, 0); // enabled the first monitor
		Thread.sleep(100);
		assertEquals(1, viewer.getEnabledMonitors().size());
		assertEquals(bot.table(0).cell(0, 1), viewer.getEnabledMonitors().keySet().iterator().next());

		bot.table(0).click(0, 0); // disable the first monitor
		Thread.sleep(100);
		assertEquals(0, viewer.getEnabledMonitors().size());
	}

	@Test
	public void checkMonitorRole() throws Exception {
		int startIndex = 0; // find the index of 'monitor0'
		while (!bot.table(0).cell(startIndex, 1).equals("monitor0")) startIndex++;
		for (int i = 0; i < 10; i++) {
			int index = startIndex + i;
			if (i % 3 != 2)
				bot.table(0).click(index, 0);
			if (i % 3 == 1) {
				bot.table(0).click(index, 2);
				SWTBotCCombo combo = bot.ccomboBox(0);
				combo.setSelection(MonitorScanRole.PER_SCAN.getLabel());
			}
			Thread.sleep(100);
		}

		final Map<String, MonitorScanRole> monitorMap = viewer.getEnabledMonitors();
		assertEquals(7, monitorMap.size());
		for (int i = 0; i < 10; i++) {
			final String name = "monitor" + i;
			if (i % 3 == 2) {
				assertFalse(monitorMap.keySet().contains(name));
			} else {
				assertTrue(monitorMap.keySet().contains(name));
				MonitorScanRole expectedScanRole = i % 3 == 1 ? MonitorScanRole.PER_SCAN : MonitorScanRole.PER_POINT;
				assertEquals(expectedScanRole, monitorMap.get(name));
			}
		}
	}

	@Test
	public void checkSetFilter() throws Exception {
		assertEquals(67, bot.table(0).rowCount());

		for (int i = 0; i < 6; i++) {
			bot.table(0).click(i, 0); // enabled the first monitor
		}
		Thread.sleep(100);

		assertEquals(6, viewer.getEnabledMonitors().size());
		synchExec(() -> viewer.setShowEnabledOnly(true));
		assertEquals(6, bot.table(0).rowCount());
		synchExec(() -> viewer.setShowEnabledOnly(false));
		assertEquals(67, bot.table(0).rowCount());
	}
}
