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

package gda.rcp.ncd.ui.preferences;

import gda.rcp.ncd.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TfgPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String USR0 = "USR0";
	public static final String USR1 = "USR1";
	public static final String USR2 = "USR2";
	public static final String USR3 = "USR3";
	public static final String USR4 = "USR4";
	public static final String USR5 = "USR5";
	public static final String USR6 = "USR6";
	public static final String USR7 = "USR7";

	public TfgPreferencePage() {
		super(GRID);

	}

	@Override
	public void createFieldEditors() {
		addField(new StringFieldEditor(USR0, "Output trigger name: lemo 0", getFieldEditorParent()));
		addField(new StringFieldEditor(USR1, "Output trigger name: lemo 1", getFieldEditorParent()));
		addField(new StringFieldEditor(USR2, "Output trigger name: lemo 2", getFieldEditorParent()));
		addField(new StringFieldEditor(USR3, "Output trigger name: lemo 3", getFieldEditorParent()));
		addField(new StringFieldEditor(USR4, "Output trigger name: lemo 4", getFieldEditorParent()));
		addField(new StringFieldEditor(USR5, "Output trigger name: lemo 5", getFieldEditorParent()));
		addField(new StringFieldEditor(USR6, "Output trigger name: lemo 6", getFieldEditorParent()));
		addField(new StringFieldEditor(USR7, "Output trigger name: lemo 7", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A preference page for the time frame generator");
	}
}
