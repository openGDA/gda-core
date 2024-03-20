/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.device.scannable;

import org.eclipse.scanning.api.device.models.BufferSizeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.observable.IObserver;

public class BufferSizeMonitorProvider extends ConfigurableBase implements BufferSizeProvider  {
	private static Logger logger = LoggerFactory.getLogger(BufferSizeMonitorProvider.class);

	private int bufferSize;
	/**
	 * This will get a detector's chunk size. The value must be an integer.
	 */
	private Scannable bufferMonitor;

	public BufferSizeMonitorProvider(Scannable bufferMonitor) {
		this.bufferMonitor = bufferMonitor;
	}

	@Override
	public void configure() throws FactoryException {
		IObserver handler = (source, arg) -> handleState();
		bufferMonitor.addIObserver(handler);
		setConfigured(true);
		handleState();
	}

	public void handleState() {
		try {
			var position = bufferMonitor.getPosition();
			if (position instanceof Integer)
				bufferSize = (int) position;
			else {
				bufferSize = 0;
				logger.error("Returned buffer size value is not an integer");
			}
		} catch(DeviceException e) {
			bufferSize = 0;
			logger.error("Could not get buffer size value", e);
		}
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

}
