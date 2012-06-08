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

import gda.rcp.GDAClientActivator;

import java.text.DecimalFormat;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.gda.preferences.PreferenceConstants;

/**
 *
 */
public class DashboardPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public static final String ID = "uk.ac.gda.client.dashboardPreferences";
	
	private StringFieldEditor formatFieldEditor;

	/**
	 * 
	 */
	public DashboardPreferencePage() {
		super(GRID);
		setPreferenceStore(GDAClientActivator.getDefault().getPreferenceStore());
		setDescription("Preferences for the dashboard view such as the format for the values and bounds and visibility controls.");
	}
	
	@Override
	protected void createFieldEditors() {
		formatFieldEditor = new StringFieldEditor(PreferenceConstants.DASHBOARD_FORMAT, "Dashboard number format", getFieldEditorParent());
		addField(formatFieldEditor);
				
		final BooleanFieldEditor showBounds = new BooleanFieldEditor(PreferenceConstants.DASHBOARD_BOUNDS, "Show bounds", getFieldEditorParent());
		addField(showBounds);
		
		final BooleanFieldEditor showDes = new BooleanFieldEditor(PreferenceConstants.DASHBOARD_DESCRIPTION, "Show description", getFieldEditorParent());
		addField(showDes);
	}
	
	@Override
	protected void checkState() {
		super.checkState();
		
		try {
			DecimalFormat format = new DecimalFormat(formatFieldEditor.getStringValue());
			format.format(100.001);
		} catch (IllegalArgumentException ne) {
			setErrorMessage("The format '"+formatFieldEditor.getStringValue()+"' is not valid.");
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
