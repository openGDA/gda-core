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

package uk.ac.diamond.tomography.reconstruction.views;

import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.tomography.reconstruction.Activator;

public enum CENTRE_OF_ROTATION_MODE {

	COARSE_MODE("Coarse", Activator.PREF_COARSE_TOTAL_STEPS, Activator.PREF_COARSE_STEP_SIZE),

	FINE_MODE("Fine", Activator.PREF_FINE_TOTAL_STEPS, Activator.PREF_FINE_STEP_SIZE),

	VERY_FINE_MODE("Very Fine", Activator.PREF_VERY_FINE_TOTAL_STEPS, Activator.PREF_VERY_FINE_STEP_SIZE);

	private final String displayString;
	private final String stepSizePreference;
	private final String totalStepsPreference;

	private CENTRE_OF_ROTATION_MODE(String display, String totalStepsPreference, String stepSizePreference) {
		this.displayString = display;
		this.totalStepsPreference = totalStepsPreference;
		this.stepSizePreference = stepSizePreference;
	}

	public String getDisplayString() {
		return displayString;
	}

	public IPreferenceStore getStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public void setDefaults() {
		getStore().setValue(totalStepsPreference, getStore().getDefaultInt(totalStepsPreference));
		getStore().setValue(stepSizePreference, getStore().getDefaultDouble(stepSizePreference));
	}

	public void saveContent(int totSteps, double stepSize) {
		getStore().setValue(totalStepsPreference, totSteps);
		getStore().setValue(stepSizePreference, stepSize);
	}

	public double getStepSize() {
		return getStore().getDouble(stepSizePreference);
	}

	public int getTotalSteps() {
		return getStore().getInt(totalStepsPreference);
	}

}