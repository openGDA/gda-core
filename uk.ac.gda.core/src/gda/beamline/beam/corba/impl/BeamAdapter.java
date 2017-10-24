/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.beamline.beam.corba.impl;

import gda.beamline.BeamInfo;
import gda.beamline.beam.corba.CorbaBeamInfo;
import gda.beamline.beam.corba.CorbaBeamInfoHelper;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.LoggingConstants;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the adapter pattern for the BeamInfo interface
 */
public class BeamAdapter implements BeamInfo, EventSubscriber {
	private static final Logger logger = LoggerFactory.getLogger(BeamAdapter.class);
	CorbaBeamInfo paramsObj;

	NetService netService;

	String name;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Create client side interface to the CORBA package.
	 *
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public BeamAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		paramsObj = CorbaBeamInfoHelper.narrow(obj);
		this.netService = netService;
		this.name = name;

		// subscribe to events coming over CORBA from the impl
		EventService eventService = EventService.getInstance();
		if (eventService != null)
			eventService.subscribe(this, new NameFilter(name, observableComponent));
	}

	// this is for accepting events over the CORBA event mechanism
	@Override
	public void inform(Object obj) {
		if (obj == null)
			logger.debug(LoggingConstants.FINEST, "BeamAdapter: Received event for NULL");
		notifyIObservers(this, obj);
	}

	@Override
	public void setWavelength(double value) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setWavelength(value);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public double getWavelength() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				double result = paramsObj.getWavelength();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return -1.0;
	}

	@Override
	public double getEnergy() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				double result = paramsObj.getEnergy();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return 0;
	}

	@Override
	public void setEnergy(double energy) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setEnergy(energy);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setEnergy() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setEnergyFromDCM();
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException("Could not set energy from DCM", e);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isCalibrated() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return paramsObj.isCalibrated();
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaBeamInfoHelper.narrow(netService.reconnect(name));
			}
		}
		return false;
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify observers of this class.
	 *
	 * @param theObserved
	 *            the observed class
	 * @param changeCode
	 *            the changed code
	 */
	public void notifyIObservers(java.lang.Object theObserved, java.lang.Object changeCode) {
		observableComponent.notifyIObservers(theObserved, changeCode);
	}

}
