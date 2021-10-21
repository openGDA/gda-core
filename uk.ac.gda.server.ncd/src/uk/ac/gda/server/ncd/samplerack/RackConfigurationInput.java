/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import gda.device.Scannable;
import gda.factory.Finder;

public record RackConfigurationInput(double xCalPos, double yCalPos, double xOffset, double yOffset, double xSpace, double ySpace, String xAxis, String yAxis)
		implements Serializable {

	public RackConfiguration intoRackConfiguration() {

		Scannable xColumnPositioner = Finder.findOptionalOfType(xAxis, Scannable.class)
				.orElseThrow(() -> new IllegalArgumentException("x Axis is not a valid Scannable motor."));

		Scannable yRowPositioner = Finder.findOptionalOfType(yAxis, Scannable.class)
				.orElseThrow(() -> new IllegalArgumentException("y Axis is not a valid Scannable motor."));

		return new RackConfiguration(xCalPos, yCalPos, xOffset, yOffset, xSpace, ySpace, xColumnPositioner, yRowPositioner);
	}
}