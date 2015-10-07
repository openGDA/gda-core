/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.nexus;

import gda.data.nexus.scan.NexusDevice;
import gda.device.DeviceBase;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.nexus.NXobject;

/**
 * Abstract wrapper class for devices that can create NeXus base classes.
 */
public abstract class AbstractNexusDevice<D extends NXobject> implements NexusDevice<D> {

	protected final DeviceBase device;

	protected ILazyWriteableDataset defaultWritableDataset = null;

	public AbstractNexusDevice(final DeviceBase device) {
		this.device = device;
	}

	@Override
	public ILazyWriteableDataset getDefaultWriteableDataset() {
		return defaultWritableDataset;
	}

	@Override
	public String getName() {
		return device.getName();
	}

}
