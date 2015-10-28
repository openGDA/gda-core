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

package gda.epics;

import gda.observable.Observable;
import gda.observable.Observer;

import java.io.IOException;

public class PVValueCache<T> implements Observer<T> {
	PV<T> pv;
	private T cache;

	public PVValueCache(PV<T> pv) throws Exception {
		super();
		this.pv = pv;
		pv.addObserver(this);
	}
	
	public void putWait(T arg) throws IOException{
		if( !arg.equals(cache)){
			if( cache != null && arg instanceof Double){
				if (Math.abs((Double)arg-(Double)cache) < 1e-10)
					return;
			}
			pv.putWait(arg);
			cache=arg;
		}
	}

	public T get() throws IOException {
		if(  cache == null)
		{
			cache = pv.get();
		}
		return cache;
	}

	/**
	 * Where a PV pair of PVs are used to set and read back a value, and you
	 * read back soon after setting, you may end up with the old cached value
	 * instead on the new value. To prevent this, cached readback values need to
	 * be reset whenever the PV setting them is set.
	 */
	public void resetCache() {
		cache = null;
	}

	@Override
	public void update(Observable<T> source, T arg) {
		cache=arg;
		
	}
}
