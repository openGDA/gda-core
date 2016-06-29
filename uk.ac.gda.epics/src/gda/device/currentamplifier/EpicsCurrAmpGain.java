/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.currentamplifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.enumpositioner.EpicsEnumConstants;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.epics.CAClient;
import gda.factory.Findable;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class EpicsCurrAmpGain extends ScannableBase implements Scannable, Findable, IObserver {

	private String pvName;
	private CAClient ca_client = new CAClient();
	private static final Logger logger = LoggerFactory.getLogger(EpicsCurrAmpGain.class);
//	private boolean busy;

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
//			busy=true;
			ca_client.caput(pvName, Double.parseDouble(position.toString()) - 3);
			notifyIObservers(this, "");
//			busy=false;
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawAsynchronousMoveTo", e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			return int2str(Integer.parseInt(ca_client.caget(pvName)));
		} catch (CAException e) {
			logger.error("CAException", e);
		} catch (TimeoutException e) {
			logger.error("TimeoutException", e);
		} catch (NumberFormatException e) {
			logger.error("NumberFormatException", e);
		} catch (InterruptedException e) {
			logger.error("InterruptedException", e);
		}
		return null;
	}

	public String int2str(int num){
		for (int i = 0; i < EpicsEnumConstants.CHANNEL_NAMES.length; i++) {
			if(num==i)
				try {
					String pv = String.format("%s.%s", pvName, EpicsEnumConstants.CHANNEL_NAMES[i]);
					return ca_client.caget(pv);
				} catch (CAException e) {
					logger.error("CAException", e);
				} catch (TimeoutException e) {
					logger.error("TimeoutException", e);
				} catch (InterruptedException e) {
					logger.error("InterruptedException", e);
				}
		}
		return null;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(this, "");
	}
}
