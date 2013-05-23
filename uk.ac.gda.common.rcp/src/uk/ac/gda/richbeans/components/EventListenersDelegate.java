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

package uk.ac.gda.richbeans.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import uk.ac.gda.richbeans.event.BoundsEvent;
import uk.ac.gda.richbeans.event.BoundsListener;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 * Component to manage lists of value and bounds listeners.
 */
public class EventListenersDelegate {

	protected Collection<ValueListener> valueListeners;
	protected Map<String, ValueListener> valueListenersMap;
	protected Collection<BoundsListener> boundsListeners;

	public EventListenersDelegate() {
	}

	/**
	 * Add a listener to be notified of the user entering new values into the widget.
	 * 
	 * @param l
	 */
	public void addValueListener(final ValueListener l) {
		if (l.getValueListenerName() != null) {
			if (valueListenersMap == null)
				valueListenersMap = new HashMap<String, ValueListener>(3);
			valueListenersMap.put(l.getValueListenerName(), l);
			return;
		}
		if (valueListeners == null)
			valueListeners = new HashSet<ValueListener>(3);
		valueListeners.add(l);
	}

	/**
	 * Remove a certain listener - can also use the clearListeners(...) method.
	 * 
	 * @param l
	 */
	public void removeValueListener(final ValueListener l) {
		if (l.getValueListenerName() != null) {
			if (valueListenersMap == null)
				return;
			valueListenersMap.remove(l.getValueListenerName());
			return;
		}
		if (valueListeners == null)
			return;
		valueListeners.remove(l);
	}

	/**
	 * Internal use only
	 * 
	 * @param evt
	 */
	public void notifyValueListeners(final ValueEvent evt) {

		if (valueListenersMap != null) {
			for (ValueListener l : valueListenersMap.values()) {
				l.valueChangePerformed(evt);
			}
		}
		if (valueListeners != null) {
			for (ValueListener l : valueListeners) {
				l.valueChangePerformed(evt);
			}
		}
	}

	/**
	 * Internal use only
	 * 
	 * @param evt
	 */
	public void notifyBoundsProviderListeners(final ValueEvent evt) {

		if (valueListenersMap != null) {
			for (ValueListener l : valueListenersMap.values()) {
				if (l instanceof BoundsUpdater) {
					l.valueChangePerformed(evt);
				}
			}
		}
		if (valueListeners != null) {
			for (ValueListener l : valueListeners) {
				if (l instanceof BoundsUpdater) {
					l.valueChangePerformed(evt);
				}
			}
		}
	}

	/**
	 * Add a listener to be notified of the user entering new values into the widget.
	 * 
	 * @param l
	 */
	public void addBoundsListener(final BoundsListener l) {
		if (boundsListeners == null)
			boundsListeners = new HashSet<BoundsListener>(3);
		boundsListeners.add(l);
	}

	/**
	 * Remove a certain listener - can also use the clearListeners(...) method.
	 * 
	 * @param l
	 */
	public void removeBoundsListener(final BoundsListener l) {
		if (boundsListeners == null)
			return;
		boundsListeners.remove(l);
	}

	/**
	 * Internal use only
	 * 
	 * @param evt
	 */
	public void notifyBoundsListeners(final BoundsEvent evt) {

		if (boundsListeners == null)
			return;

		if (evt.getMode() == BoundsEvent.Mode.GREATER) {
			for (BoundsListener l : boundsListeners)
				l.valueGreater(evt);
		} else if (evt.getMode() == BoundsEvent.Mode.LESS) {
			for (BoundsListener l : boundsListeners)
				l.valueLess(evt);
		} else if (evt.getMode() == BoundsEvent.Mode.LEGAL) {
			for (BoundsListener l : boundsListeners)
				l.valueLegal(evt);
		}
	}

	public void dispose() {
		if (valueListeners != null)
			valueListeners.clear();
		if (valueListenersMap != null)
			valueListenersMap.clear();
		if (boundsListeners != null)
			boundsListeners.clear();
	}

	public boolean checkCanNotify() {

		if (valueListeners == null && valueListenersMap == null)
			return false;

		if (valueListeners != null && valueListenersMap != null) {
			if (valueListeners.isEmpty() && valueListenersMap.isEmpty())
				return false;
		}
		if (valueListeners == null && valueListenersMap != null) {
			if (valueListenersMap.isEmpty())
				return false;
		}
		if (valueListenersMap == null && valueListeners != null) {
			if (valueListeners.isEmpty())
				return false;
		}

		return true;
	}

}
