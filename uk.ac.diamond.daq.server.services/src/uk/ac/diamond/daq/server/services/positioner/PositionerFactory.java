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

import java.util.List;

import gda.device.Scannable;
import uk.ac.diamond.daq.jms.positioner.Positioner;
import uk.ac.diamond.daq.jms.positioner.PositionerStatus;

public class PositionerFactory {
	private List<PositionerFactoryPlugin> plugins;

	public PositionerFactory(List<PositionerFactoryPlugin> plugins) {
		this.plugins = plugins;
	}

	private PositionerFactoryPlugin getMatchingPlugin(Scannable scannable) throws PositionerFactoryException {
		for (PositionerFactoryPlugin plugin : plugins) {
			if (plugin.matches(scannable)) {
				return plugin;
			}
		}
		String error = String.format("Scannable %s is a %s which is unsupported", scannable.getName(),
				scannable.getClass().getCanonicalName());
		throw new PositionerFactoryException(error);
	}

	public Positioner createPositioner(Scannable scannable) throws PositionerFactoryException {
		PositionerFactoryPlugin plugin = getMatchingPlugin(scannable);
		return plugin.createPositioner(scannable);
	}

	public String getPosition(Scannable scannable) throws PositionerFactoryException {
		PositionerFactoryPlugin plugin = getMatchingPlugin(scannable);
		return plugin.getPosition(scannable);
	}

	public String moveTo(Scannable scannable, String position) throws PositionerFactoryException {
		PositionerFactoryPlugin plugin = getMatchingPlugin(scannable);
		return plugin.moveTo(scannable, position);
	}

	public String stop(Scannable scannable) throws PositionerFactoryException {
		PositionerFactoryPlugin plugin = getMatchingPlugin(scannable);
		return plugin.stop(scannable);
	}

	public PositionerStatus convertStatus(Scannable scannable, Object event) throws PositionerFactoryException {
		PositionerFactoryPlugin plugin = getMatchingPlugin(scannable);
		return plugin.convertStatus(scannable, event);
	}

	public PositionerStatus getStatus(Scannable scannable) throws PositionerFactoryException {
		PositionerFactoryPlugin plugin = getMatchingPlugin(scannable);
		return plugin.getStatus(scannable);
	}
}
