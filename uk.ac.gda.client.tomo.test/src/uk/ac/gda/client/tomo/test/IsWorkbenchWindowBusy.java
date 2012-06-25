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

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.widgets.ProgressBar;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.condition.shell.TestAndWaitForIdleCondition;
import com.windowtester.runtime.swt.internal.widgets.ISWTWidgetReference;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;

public class IsWorkbenchWindowBusy extends TestAndWaitForIdleCondition {

	private final IUIContext ui2;

	public IsWorkbenchWindowBusy(IUIContext ui) {
		ui2 = ui;
	}

	@Override
	public boolean test() {

		IWidgetLocator[] findAll = ui2.findAll(new SWTWidgetLocator(ProgressBar.class, new SWTWidgetLocator(
				ProgressIndicator.class)));
		boolean isVis = false;
		for (IWidgetLocator iWidgetLocator : findAll) {
			boolean visible = ((ISWTWidgetReference<ProgressBar>) iWidgetLocator).isVisible();
			if (visible) {
				return false;
			}
		}

		return true;
	}
}