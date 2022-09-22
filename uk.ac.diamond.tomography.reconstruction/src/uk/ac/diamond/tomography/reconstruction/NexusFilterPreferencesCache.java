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
import org.eclipse.ui.WorkbenchException;

import uk.ac.diamond.tomography.reconstruction.views.NexusFilterDescriptor;

public enum NexusFilterPreferencesCache implements INexusFilterPreferencesCache {

	// Singleton instance
	CACHE;

	private NexusFilterDescriptor nexusFilterDescriptor = null;

	private NexusFilterPreferencesCache() {
		IEclipsePreferences prefs = null;
		prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

		if (prefs != null) {
			prefs.addPreferenceChangeListener(new PreferenceChangeListener());

			String pref = prefs.get(Activator.PREF_NEXUS_FILTER_DESCRIPTOR, "");
			try {
				nexusFilterDescriptor = new NexusFilterDescriptor(pref);
			} catch (WorkbenchException | NullPointerException | IllegalArgumentException e) {
				nexusFilterDescriptor = null;
			}
		}
	}

	/**
	 * Preference change listener. Listens for a change on the nexus path sort string
	 */
	private class PreferenceChangeListener implements IEclipsePreferences.IPreferenceChangeListener {
		@Override
		public void preferenceChange(final IEclipsePreferences.PreferenceChangeEvent event) {
			if (event.getKey().equals(Activator.PREF_NEXUS_FILTER_DESCRIPTOR)) {
				// cast to String because getNewValue always returns String or null
				try {
					nexusFilterDescriptor = new NexusFilterDescriptor((String) event.getNewValue());
				} catch (WorkbenchException | NullPointerException | IllegalArgumentException e) {
					nexusFilterDescriptor = null;
				}
			}
		}
	}

	@Override
	public INexusFilterDescriptor getNexusFilterDescriptor() {
		return nexusFilterDescriptor;
	}

}
