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

package gda.org.myls.powersupply;

import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 *
 */
public abstract class CorbarisablePowerSupplyBase implements ICorbarisablePowerSupply {
	protected String name;
	protected boolean local=false;
	ObservableComponent obs = new ObservableComponent();


	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		obs.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obs.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		obs.deleteIObservers();
	}

	protected void notifyObservers(){
		obs.notifyIObservers(this, null);
	}
}
