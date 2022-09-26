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

package uk.ac.diamond.tomography.reconstruction;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class TomoReconPrefInitializer extends AbstractPreferenceInitializer {

	public TomoReconPrefInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_COARSE_TOTAL_STEPS, 10);
		Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_COARSE_STEP_SIZE, 10);
		Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_FINE_TOTAL_STEPS, 10);
		Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_FINE_STEP_SIZE, 2);
		Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_VERY_FINE_TOTAL_STEPS, 10);
		Activator.getDefault().getPreferenceStore().setDefault(Activator.PREF_VERY_FINE_STEP_SIZE, 0.2);
	}

}
