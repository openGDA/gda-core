/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.analysis.deprecated.rcp.Activator;

public class DeprecatedPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(DeprecatedPreferenceConstants.IGNORE_DATASET_FILTERS, false);
		store.setDefault(DeprecatedPreferenceConstants.SHOW_XY_COLUMN, false);
		store.setDefault(DeprecatedPreferenceConstants.SHOW_DATA_SIZE, false);
		store.setDefault(DeprecatedPreferenceConstants.SHOW_DIMS, false);
		store.setDefault(DeprecatedPreferenceConstants.SHOW_SHAPE, false);
		store.setDefault(DeprecatedPreferenceConstants.DATA_FORMAT, "#0.00");
		store.setDefault(DeprecatedPreferenceConstants.PLAY_SPEED, 1500);
	}
}
