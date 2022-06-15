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

package uk.ac.gda.preferences;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.gda.client.liveplot.LivePlotView;

/**
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String DATA_PROJECT_NAME_AS_VISIT = "$visit$";
	private Collection<String> integerPrefs;
	private Collection<String> booleanPrefs;

	@Override
	public void initializeDefaultPreferences() {

		IPreferenceStore store = gda.rcp.GDAClientActivator.getDefault().getPreferenceStore();

		// Booleans
		booleanPrefs = new HashSet<String>(7);
		store.setDefault(PreferenceConstants.KEEP_BATON,             false);
		booleanPrefs.add(PreferenceConstants.KEEP_BATON);
		store.setDefault(PreferenceConstants.DASHBOARD_BOUNDS,       false);
		booleanPrefs.add(PreferenceConstants.DASHBOARD_BOUNDS);
		store.setDefault(PreferenceConstants.DASHBOARD_DESCRIPTION,  false);
		booleanPrefs.add(PreferenceConstants.DASHBOARD_DESCRIPTION);

		// Ints
		integerPrefs = new HashSet<String>(7);
		store.setDefault(PreferenceConstants.BATON_REQUEST_TIMEOUT,       2);
		integerPrefs.add(PreferenceConstants.BATON_REQUEST_TIMEOUT);
		store.setDefault(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS, 25);
		integerPrefs.add(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS);

		// Strings
		store.setDefault(PreferenceConstants.DASHBOARD_FORMAT, "#0.00");

		store.setDefault(PreferenceConstants.GDA_DATA_PROJECT_CREATE_ON_STARTUP, true);
		store.setDefault(PreferenceConstants.GDA_DATA_PROJECT_NAME, DATA_PROJECT_NAME_AS_VISIT);
		store.setDefault(PreferenceConstants.GDA_DATA_PROJECT_FILTER, ".*xml");
		store.setDefault(PreferenceConstants.GDA_DATA_PROJECT_FILTER_IS_EXCLUDE, true);

		store.setDefault(PreferenceConstants.GDA_USE_SCANDATAPOINT_SERVICE, true);
		store.setDefault(PreferenceConstants.GDA_OPEN_XYPLOT_ON_SCAN_START, true);
		store.setDefault(PreferenceConstants.GDA_OPEN_XYPLOT_ON_SCAN_START_ID, LivePlotView.ID);
		store.setDefault(PreferenceConstants.GDA_CLIENT_PLOT_AUTOHIDE_LAST_SCAN, true);
		store.setDefault(PreferenceConstants.GDA_CLIENT_PLOT_PERIOD_MS, 500);


		store.setDefault(PreferenceConstants.GDA_COMMAND_QUEUE_SHOW_TEXT, false);
		store.setDefault(PreferenceConstants.GDA_COMMAND_QUEUE_DISABLE_JYTHON_CONTROLS, false);
		store.setDefault(PreferenceConstants.GDA_SHOW_PANIC_STOP_ON_JYTHON_CONSOLE, false);

		store.setDefault(PreferenceConstants.HIDE_SCAN_THRESHOLD, 100);

		store.setDefault(PreferenceConstants.REMOVE_OLD_SCANS, false);
		store.setDefault(PreferenceConstants.REMOVE_SCAN_THRESHOLD, 500);

		readExtensionPoints(store);
	}

	private void readExtensionPoints(final IPreferenceStore store) {

		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.gda.client.preferences");

		for (IConfigurationElement e : config) {
			final String name     = e.getAttribute("name");
			if (isInt(name)) {
				store.setValue(name, Integer.parseInt(e.getAttribute("value")));
			} else if (isBoolean(name)) {
				store.setValue(name, Boolean.parseBoolean(e.getAttribute("value")));
			} else {
				store.setValue(name, e.getAttribute("value"));
			}
		}

	}

	private boolean isInt(String name) {
		return integerPrefs.contains(name);
	}

	private boolean isBoolean(String name) {
		return booleanPrefs.contains(name);
	}

}
