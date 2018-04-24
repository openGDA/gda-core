/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.scripting;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import gda.rcp.GDAClientActivator;

public class JythonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "uk.ac.gda.pydev.preferences.jythonPreferences";

	public JythonPreferencePage() {
		super(GRID);
		setPreferenceStore(GDAClientActivator.getDefault().getPreferenceStore());
		setDescription("Preferences for the Script projects imported into the workspace.");
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor showConfig = new BooleanFieldEditor(PreferenceConstants.SHOW_CONFIG_SCRIPTS,
				"Show the script projects from this GDA Configuration", getFieldEditorParent());
		addField(showConfig);

		BooleanFieldEditor showGDA = new BooleanFieldEditor(PreferenceConstants.SHOW_GDA_SCRIPTS,
				"Show all the script projects from core GDA", getFieldEditorParent());
		addField(showGDA);

		BooleanFieldEditor chkGDASyntax = new BooleanFieldEditor(PreferenceConstants.CHECK_SCRIPT_SYNTAX,
				"Syntax check scripts during file editing", getFieldEditorParent());
		addField(chkGDASyntax);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
