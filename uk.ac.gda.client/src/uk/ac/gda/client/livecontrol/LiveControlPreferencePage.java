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

package uk.ac.gda.client.livecontrol;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import gda.rcp.GDAClientActivator;

/**
 *define preference for displaying more than one live {@link ControlSet}s in GDA client GUI. The default is false.
 *
 */
public class LiveControlPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "uk.ac.gda.client.liveControlPreferences";
	public static final String GDA_SHOW_ALL_CONTROLSETS_IN_SINGLE_VIEW = "gda.show.all.controlsets.in.single.view";


	public LiveControlPreferencePage() {
		super(GRID);
		setPreferenceStore(GDAClientActivator.getDefault().getPreferenceStore());
		setDescription("Preferences for showing live control sets in Live Controls View.");
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor showAllControlSetsInSingleView = new BooleanFieldEditor(GDA_SHOW_ALL_CONTROLSETS_IN_SINGLE_VIEW, "Show all control sets in the same live control view", getFieldEditorParent());
		addField(showAllControlSetsInSingleView);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
