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

package uk.ac.gda.views.baton;

import gda.rcp.GDAClientActivator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.gda.preferences.PreferenceConstants;

/**
 *
 */
public class BatonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public static final String ID = "uk.ac.gda.client.batonPreferences";
	
	private BooleanFieldEditor keepPrefEditor;
	private IntegerFieldEditor timeoutFieldEditor;

	/**
	 * 
	 */
	public BatonPreferencePage() {
		super(GRID);
		setPreferenceStore(GDAClientActivator.getDefault().getPreferenceStore());
		setDescription("Preferences for the baton manager such as how long to hold the baton for if another user requests it.");
	}
	
	@Override
	protected void createFieldEditors() {
		keepPrefEditor = new BooleanFieldEditor(PreferenceConstants.KEEP_BATON, "Always keep baton", getFieldEditorParent());
		addField(keepPrefEditor);
		keepPrefEditor.setPropertyChangeListener(this);
		
		timeoutFieldEditor = new IntegerFieldEditor(PreferenceConstants.BATON_REQUEST_TIMEOUT, "Request timeout (minutes)", getFieldEditorParent());
		addField(timeoutFieldEditor);
		timeoutFieldEditor.setPropertyChangeListener(this);
		
	}
	
	@Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (PreferenceConstants.KEEP_BATON.equals(event.getProperty())) {
        	timeoutFieldEditor.setEnabled((Boolean)event.getNewValue(), getFieldEditorParent());
        }
	}
	
	@Override
	protected void checkState() {
		super.checkState();
		if (timeoutFieldEditor.getIntValue()<1) {
			setErrorMessage("The timeout cannot be less than 1 mninute.");
			setValid(false);
			return;
		}
		if (timeoutFieldEditor.getIntValue()>15) {
			setErrorMessage("The timeout cannot be greater than 15 mninutes.");
			setValid(false);
			return;
		}
		
		setErrorMessage(null);
		setValid(true);
		
	}

	@Override
	public void init(IWorkbench workbench) {

	}

}
