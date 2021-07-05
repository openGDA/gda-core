/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.data.scan.nexus.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScannableNexusDeviceConfigurationRegistry {

	private Map<String, ScannableNexusDeviceConfiguration> scannableNexusConfigurations = new HashMap<>();

	public void addScannableNexusDeviceConfiguration(ScannableNexusDeviceConfiguration config) {
		Objects.requireNonNull(config.getScannableName(), "The scannable name must be set");
		scannableNexusConfigurations.put(config.getScannableName(), config);
	}

	public ScannableNexusDeviceConfiguration getScannableNexusDeviceConfiguration(String scannableName) {
		return scannableNexusConfigurations.get(scannableName);
	}

}
