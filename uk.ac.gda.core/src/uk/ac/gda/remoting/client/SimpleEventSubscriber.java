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

package uk.ac.gda.remoting.client;

import gda.factory.corba.util.EventSubscriber;
import gda.observable.ObservableComponent;

/**
 * An implementation of {@link EventSubscriber} that receives events from the CORBA event system, and broadcasts them
 * out to observers through an {@link ObservableComponent}.
 */
public class SimpleEventSubscriber implements EventSubscriber {

	private final Object source;
	
	private final ObservableComponent observableComponent;
	
	/**
	 * Creates an event subscriber that receives events, and notifies observers through the specified
	 * {@link ObservableComponent}, using the specified source object.
	 * 
	 * @param source the object that events should appear to have been sent from
	 * @param observableComponent the component through which to dispatch events to observers
	 */
	public SimpleEventSubscriber(Object source, ObservableComponent observableComponent) {
		this.source = source;
		this.observableComponent = observableComponent;
	}
	
	@Override
	public void inform(Object message) {
		observableComponent.notifyIObservers(source, message);
	}

}
