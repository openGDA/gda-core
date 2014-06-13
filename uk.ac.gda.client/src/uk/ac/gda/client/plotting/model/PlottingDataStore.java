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

package uk.ac.gda.client.plotting.model;

import uk.ac.gda.common.rcp.PreferenceDataStore;

public enum PlottingDataStore {
	INSTANCE;
	private static final String PLOTTING_DATA_STORE_PERFERENCES = "uk.ac.gda.plotting.data";
	private final PreferenceDataStore preferenceDataStore = new PreferenceDataStore(PLOTTING_DATA_STORE_PERFERENCES);

	public PreferenceDataStore getPreferenceDataStore() {
		return preferenceDataStore;
	}
}
