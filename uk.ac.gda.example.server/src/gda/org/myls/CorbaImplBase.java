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

package gda.org.myls;

import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.NameFilter;
import gda.observable.IObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class CorbaImplBase implements IObserver{
	private static final Logger logger = LoggerFactory.getLogger("testing.devices.CorbaImplBase");
	protected EventDispatcher dispatcher;
	protected String id;
	String name;

	/**
	 * @param corbarisable
	 */
	public CorbaImplBase(ICorbarisable corbarisable){
		name = corbarisable.getName();
		id = NameFilter.MakeEventChannelName(name);
		dispatcher = EventService.getInstance().getEventDispatcher();
		corbarisable.addIObserver(this); //FIXME: potential race condition
	}

	@Override
	public void update(java.lang.Object o, java.lang.Object arg) {
		dispatcher.publish(name,arg);
	}

}
