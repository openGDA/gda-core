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

package uk.ac.gda.pydev.ui.preferences;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.gda.pydev.extension.Activator;

public class JythonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public static final String ID = "uk.ac.gda.pydev.preferences.jythonPreferences";

	/**
	 * 
	 */
	public JythonPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for the Scripts perspective. For instance it automatically closes XML file editors for the GDA confguration.");
	}
	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor closeRiches = new BooleanFieldEditor(PreferenceConstants.CLOSE_RICH_BEAN_EDITORS, "Close editors when entering Scripts perspective", getFieldEditorParent());
    	addField(closeRiches);
    	
		BooleanFieldEditor showConfig = new BooleanFieldEditor(PreferenceConstants.SHOW_CONFIG_SCRIPTS, "Show all the scripts in the GDA Configuration", getFieldEditorParent());
    	addField(showConfig);
    	
		BooleanFieldEditor showGDA = new BooleanFieldEditor(PreferenceConstants.SHOW_GDA_SCRIPTS, "Show all the scripts in the GDA", getFieldEditorParent());
    	addField(showGDA);
    	
		BooleanFieldEditor showXML = new BooleanFieldEditor(PreferenceConstants.SHOW_XML_CONFIG, "Show all the XML in the GDA Configuration", getFieldEditorParent());
    	addField(showXML);

		BooleanFieldEditor chkGDASyntax = new BooleanFieldEditor(PreferenceConstants.CHECK_SCRIPT_SYNTAX, "Syntax check scripts during file editing", getFieldEditorParent());
    	addField(chkGDASyntax);
    	
    	
 	}

	@Override
	public void init(IWorkbench workbench) {
		

	}

}
