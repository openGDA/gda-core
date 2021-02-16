/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.rcp.views;

import static gda.rcp.preferences.GdaRootPreferencePage.SHOW_ALL_INPUT;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import gda.rcp.GDAClientActivator;

/**
 * Action for enabling/disabling display of jython input from other clients on this
 * clients terminal view.
 */
public class OtherClientInputToggleAction extends Action {

	public OtherClientInputToggleAction() {
		super("Show all input", IAction.AS_CHECK_BOX);
	}

	@Override
	public void run() {
		GDAClientActivator.getDefault().getPreferenceStore().setValue(SHOW_ALL_INPUT, !isChecked());
	}

	@Override
	public boolean isChecked() {
		return GDAClientActivator.getDefault().getPreferenceStore().getBoolean(SHOW_ALL_INPUT);
	}
	@Override
	public String getToolTipText() {
		return "Show input from other clients in this terminal";
	}
}
