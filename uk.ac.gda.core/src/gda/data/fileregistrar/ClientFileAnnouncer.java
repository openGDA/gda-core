/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.fileregistrar;

import gda.device.Device;
import gda.device.DeviceBase;
import gda.factory.FactoryException;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(Device.class)
public class ClientFileAnnouncer extends DeviceBase {
	@Override
	public void configure() throws FactoryException {
		// no configuration required
	}

	/**
	 * Notify observers (usually the project explorer) that files have been created
	 *
	 * @param files
	 *            Array of file names
	 */
	public void notifyFilesAvailable(String[] files) {
		notifyIObservers(this, files);
	}
}