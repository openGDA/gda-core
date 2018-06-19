/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.org.myls.powersupply.corba.impl;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

import gda.factory.FindableBase;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.org.myls.powersupply.IObservablePowerSupply;
import gda.org.myls.powersupply.corba.CorbaPowerSupply;
import gda.org.myls.powersupply.corba.CorbaPowerSupplyHelper;

/**
 *
 */
public class PowersupplyAdapter extends FindableBase implements IObservablePowerSupply, EventSubscriber{

	protected CorbaPowerSupply corbaPowerSupply;

	protected NetService netService;

	protected ObservableComponent observableComponent = new ObservableComponent();
	/**
	 * @param obj
	 * @param name
	 * @param netService
	 */
	public PowersupplyAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		corbaPowerSupply = CorbaPowerSupplyHelper.narrow(obj);
		this.netService = netService;
		setName(name);

		EventService eventService = EventService.getInstance();
		if (eventService != null) {
			eventService.subscribe(this, new NameFilter(name, this.observableComponent));
		}
	}

	@Override
	public boolean getOn() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try{
				return corbaPowerSupply.getOn();
			} catch (TRANSIENT ct) {
				corbaPowerSupply = CorbaPowerSupplyHelper.narrow(netService.reconnect(getName()));
			} catch (COMM_FAILURE cf) {
				corbaPowerSupply = CorbaPowerSupplyHelper.narrow(netService.reconnect(getName()));
			}
		}
		return false;
	}
	@Override
	public void setOn(boolean on) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try{
				corbaPowerSupply.setOn(on);
			} catch (TRANSIENT ct) {
				corbaPowerSupply = CorbaPowerSupplyHelper.narrow(netService.reconnect(getName()));
			} catch (COMM_FAILURE cf) {
				corbaPowerSupply = CorbaPowerSupplyHelper.narrow(netService.reconnect(getName()));
			}
		}
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

	@Override
	public void inform(Object obj) {
		observableComponent.notifyIObservers(this, obj);
	}

	@Override
	public String toString() {
		return "[PowerSupply:" + getName() + (getOn() ? "On" : "Off") + "]";
	}
}
