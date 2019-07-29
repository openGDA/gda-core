/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.data.metadata;

import gda.device.Device;
import gda.device.DeviceBase;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(Device.class)
public class MetadataBlaster extends DeviceBase implements IObserver {

	private MetadataEntry sme;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (sme != null)
			sme.addIObserver(this);
		setConfigured(true);
	}

	public void setStoredMetadataEntry(MetadataEntry sme) {
		this.sme = sme;
	}

	public MetadataEntry getStoredMetadataEntry() {
		return sme;
	}

	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(this, arg);
	}
}