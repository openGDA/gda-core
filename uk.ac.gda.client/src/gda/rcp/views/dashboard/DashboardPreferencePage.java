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

package gda.rcp.views.dashboard;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import gda.rcp.GDAClientActivator;

public class DashboardPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "uk.ac.gda.client.dashboardPreferences";

	public DashboardPreferencePage() {
		super(GRID);
		setPreferenceStore(GDAClientActivator.getDefault().getPreferenceStore());
		setDescription("Preferences for the polling dashboard view such as the update frequency.");
	}

	@Override
	protected void createFieldEditors() {
		final IntegerFieldEditor formatFieldEditor = new IntegerFieldEditor(DashboardView.FREQUENCY_LABEL, "Update frequency in seconds", getFieldEditorParent());
		formatFieldEditor.setValidRange(1, 300);
		addField(formatFieldEditor);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}