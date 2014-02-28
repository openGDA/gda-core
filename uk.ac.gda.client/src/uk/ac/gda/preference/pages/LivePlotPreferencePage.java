/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.preference.pages;

import gda.rcp.GDAClientActivator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.gda.preferences.PreferenceConstants;

public class LivePlotPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private IntegerFieldEditor threshold;
	
	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
		setPreferenceStore(preferenceStore);
		setDescription("When a new scan starts, if the number of scans exceed the threshold, the oldest visible scan is deselected.");
	}

	@Override
	protected void createFieldEditors() {
		threshold = new IntegerFieldEditor(PreferenceConstants.HIDE_SCAN_THRESHOLD, "Hide Scan Threshold", getFieldEditorParent());
		threshold.setValidRange(1, 10000);
		threshold.setEmptyStringAllowed(true);
		addField(threshold);
	}

}
