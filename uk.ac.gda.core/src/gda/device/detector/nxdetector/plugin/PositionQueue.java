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

package gda.device.detector.nxdetector.plugin;

import gda.device.DeviceException;
import gda.device.scannable.PositionInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of PositionInputStream that returns values from a queue. Values are put into the queue
 * using the put method
 */

public class PositionQueue<T> implements PositionInputStream<T>{

	public PositionQueue() {
		super();
	}
	
	List<T> cache = new ArrayList<T> ();
	@Override
	public List<T> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<T> items = new ArrayList<T>();
		for( int i = 0; i< maxToRead && cache.size()>0; i++){
			items.add( cache.remove(0));
		}
		return items;
	}

	public void addToCache(T appender) throws NoSuchElementException{
		cache.add(appender);
	}
}
