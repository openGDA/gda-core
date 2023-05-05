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

package uk.ac.diamond.daq.mapping.ui;

import static uk.ac.gda.preferences.PreferenceConstants.GDA_MAPPING_MAPPING_REGION_COLOUR;
import static uk.ac.gda.preferences.PreferenceConstants.GDA_MAPPING_SCAN_PATH_COLOUR;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

public class MappingPreferenceInitializer extends AbstractPreferenceInitializer {

	private static final RGB DEFAULT_MAPPING_REGION_COLOUR = new RGB(255, 196, 0); // orange
	private static final RGB DEFAULT_MAPPING_SCAN_PATH_COLOUR = new RGB(160, 32, 240); // purple

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

		PreferenceConverter.setDefault(preferenceStore, GDA_MAPPING_MAPPING_REGION_COLOUR, DEFAULT_MAPPING_REGION_COLOUR);
		PreferenceConverter.setDefault(preferenceStore, GDA_MAPPING_SCAN_PATH_COLOUR, DEFAULT_MAPPING_SCAN_PATH_COLOUR);
	}

}
