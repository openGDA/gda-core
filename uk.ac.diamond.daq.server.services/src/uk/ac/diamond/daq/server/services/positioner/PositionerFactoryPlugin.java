/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.positioner;

import gda.device.Scannable;
import uk.ac.diamond.daq.jms.positioner.Positioner;
import uk.ac.diamond.daq.jms.positioner.PositionerStatus;

public interface PositionerFactoryPlugin {
	boolean matches(Scannable scannable);

	Positioner createPositioner(Scannable scannable) throws PositionerFactoryException;

	String getPosition(Scannable scannable) throws PositionerFactoryException;

	String moveTo(Scannable scannable, String position) throws PositionerFactoryException;

	String stop(Scannable scannable) throws PositionerFactoryException;

	PositionerStatus convertStatus(Scannable scannable, Object event) throws PositionerFactoryException;

	PositionerStatus getStatus(Scannable scannable) throws PositionerFactoryException;
}
