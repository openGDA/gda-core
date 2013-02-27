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

package uk.ac.gda.exafs.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.gda.exafs.ExafsActivator;

/**
 * Class used to initialize default preference values.
 */
public class ExafsPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ExafsActivator.getDefault().getPreferenceStore();
		store.setDefault(ExafsPreferenceConstants.A_ELEMENT_LINK, true);
		store.setDefault(ExafsPreferenceConstants.B_ELEMENT_LINK, true);
		store.setDefault(ExafsPreferenceConstants.C_ELEMENT_LINK, true);
		store.setDefault(ExafsPreferenceConstants.C_MIRRORS_B_LINK, true);
		store.setDefault(ExafsPreferenceConstants.INITIAL_ENERGY_ELEMENT_LINK, true);
		store.setDefault(ExafsPreferenceConstants.FINAL_ENERGY_ELEMENT_LINK, true);
		store.setDefault(ExafsPreferenceConstants.EXAFS_GRAPH_EDITABLE, true);
		store.setDefault(ExafsPreferenceConstants.NEVER_DISPLAY_GAS_PROPERTIES, false);
		store.setDefault(ExafsPreferenceConstants.DISPLAY_GAS_FILL_PERIOD, false);
		store.setDefault(ExafsPreferenceConstants.SHOW_METADATA_EDITOR, false);
		store.setDefault(ExafsPreferenceConstants.HIDE_LnI0ItScanPlotView, false);
		store.setDefault(ExafsPreferenceConstants.XAS_MAX_ENERGY, 35000.0);
		store.setDefault(ExafsPreferenceConstants.XAS_MIN_ENERGY, 2000.0);
		store.setDefault(ExafsPreferenceConstants.DETECTORS_SOFT_XRAY, false);
		store.setDefault(ExafsPreferenceConstants.DETECTORS_FLUO_ONLY, false);
		store.setDefault(ExafsPreferenceConstants.VORTEX_STATIC_ONLY, false);
		store.setDefault(ExafsPreferenceConstants.SHOW_MYTHEN, false);
		store.setDefault(ExafsPreferenceConstants.HIDE_WORKING_ENERGY, false);
		store.setDefault(ExafsPreferenceConstants.EXAFS_FINAL_ANGSTROM, false);
		store.setDefault(ExafsPreferenceConstants.XES_MODE_ENABLED, false);
	}

}
