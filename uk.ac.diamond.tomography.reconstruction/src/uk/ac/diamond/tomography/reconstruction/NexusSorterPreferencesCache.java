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

package uk.ac.diamond.tomography.reconstruction;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

public enum NexusSorterPreferencesCache implements INexusSorterPreferencesCache {

	// Singleton instance
	CACHE;

	private String nexusSortPath;

	private NexusSorterPreferencesCache() {
		IEclipsePreferences prefs = null;
		prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

		if (prefs != null) {
			prefs.addPreferenceChangeListener(new PreferenceChangeListener());
		}
	}

	@Override
	public String getNexusSortPath() {
		return nexusSortPath;
	}

	/**
	 * Preference change listener. Listens for a change on the nexus path sort string
	 */
	private class PreferenceChangeListener implements IEclipsePreferences.IPreferenceChangeListener {
		@Override
		public void preferenceChange(final IEclipsePreferences.PreferenceChangeEvent event) {
			if (event.getKey().equals(Activator.PREF_NEXUS_SORT_PATH)) {
				// cast to String because getNewValue always returns String or null
				nexusSortPath = (String) event.getNewValue();
			}
		}
	}

}
