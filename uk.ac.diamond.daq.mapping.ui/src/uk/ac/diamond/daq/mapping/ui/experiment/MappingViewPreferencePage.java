/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static uk.ac.gda.preferences.PreferenceConstants.GDA_MAPPING_MAPPING_REGION_COLOUR;
import static uk.ac.gda.preferences.PreferenceConstants.GDA_MAPPING_SCAN_PATH_COLOUR;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.daq.mapping.ui.Activator;

public class MappingViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		final IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(prefStore);
		setDescription("Preferences for the Mapping Perspective");
	}

	@Override
	protected void createFieldEditors() {
		addField(new ColorFieldEditor(GDA_MAPPING_MAPPING_REGION_COLOUR, "Mapping region colour", getFieldEditorParent()));
		addField(new ColorFieldEditor(GDA_MAPPING_SCAN_PATH_COLOUR, "Scan path colour", getFieldEditorParent()));
	}

}
