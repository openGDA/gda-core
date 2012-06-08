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

package gda.rcp.preferences;

import gda.rcp.GDAClientActivator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * This class is to represent global GDA/Client preferences. It provides a root node for the other GDA preference pages
 * TODO: Add actual preferences here
 */
public class GdaRootPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String SHOW_MENU_TEXT = "SHOW_MENU_TEXT";
	private BooleanFieldEditor showMenuTextFieldEditor;

	/**
	 * 
	 */
	public GdaRootPreferencePage() {
		super(GRID);
		setPreferenceStore(GDAClientActivator.getDefault().getPreferenceStore());
		setDescription("GDA Preferences (see sub pages)");
	}

	@Override
	protected void createFieldEditors() {
		showMenuTextFieldEditor = new BooleanFieldEditor(SHOW_MENU_TEXT,
				"Show text along with icons for menus in the toolbar", BooleanFieldEditor.DEFAULT,
				getFieldEditorParent());
		addField(showMenuTextFieldEditor);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		boolean before = GDAClientActivator.getDefault().getPreferenceStore().getBoolean(SHOW_MENU_TEXT);
		boolean performOk = super.performOk();
		boolean after = GDAClientActivator.getDefault().getPreferenceStore().getBoolean(SHOW_MENU_TEXT);
		boolean shouldPopupRestart = false;
		if (after != before) {
			shouldPopupRestart = true;
		} else {
			shouldPopupRestart = false;
		}
		if (getContainer() instanceof PreferenceDialog) {
			PreferenceDialog dialog = (PreferenceDialog) getContainer();
			dialog.close();
		}
		if (shouldPopupRestart) {
			boolean openConfirm = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Restart GDA",
					"In order to apply the changes, GDA needs to restart");
			if (openConfirm) {
				PlatformUI.getWorkbench().restart();
			}
		}

		return performOk;
	}
}
