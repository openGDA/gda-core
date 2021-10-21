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

package uk.ac.gda.server.ncd.samplerack;

import java.io.Serializable;
import java.util.UUID;

import gda.device.DeviceException;

public interface SampleRack extends Serializable {

	UUID getID();

	String getDescription();

	String getName();

	boolean validLocation(String element);

	void runSamples(SampleConfiguration samples) throws DeviceException;

	void configureRack(RackConfigurationInput rackConfigInput);

	RackConfigurationInput getRackConfigurationInput();

}