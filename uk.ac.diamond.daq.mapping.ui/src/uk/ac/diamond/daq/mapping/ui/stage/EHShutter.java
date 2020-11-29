/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.stage;

import uk.ac.diamond.daq.mapping.ui.properties.stages.ManagedScannable;
import uk.ac.diamond.daq.mapping.ui.properties.stages.ScannablesPropertiesHelper;


/**
 * A default object to retrieve a beam selector scannable through the {@link ScannablesPropertiesHelper#getManagedScannable(String, String, Class)}
 *
 * <p>
 * A beam selector is an object which can select a special type of beam (by energy, by size or other) either through a filter or a mechanical device.
 * </p>
 *
 * <p>
 * A widget can use its own implementation to use a scannable, however object like this should help to both have a more parametrise
 * client and reduce the multiplication of different groupID and scannableID.
 * </p>
 *
 * @author Maurizio Nagni
 */
public class EHShutter {

	public static final String GROUP_ID = "shutter";
	public static final String SCANNABLE_ID = "shutter";
	public static final Class<String> SCANNABLE_TYPE = String.class;

	private EHShutter() {}

	/**
	 * Returns the {@link ManagedScannable} associated with this class {@link #GROUP_ID}, {@link #SCANNABLE_ID} and {@link #SCANNABLE_TYPE}
	 * @return a managed scannable instance
	 */
	public static final ManagedScannable<String> getManagedScannable() {
		return ScannablesPropertiesHelper.getManagedScannable(GROUP_ID, SCANNABLE_ID, SCANNABLE_TYPE);
	}

}
