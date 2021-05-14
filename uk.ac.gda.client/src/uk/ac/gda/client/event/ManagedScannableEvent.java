/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.event;

import org.springframework.context.ApplicationEvent;

import uk.ac.gda.client.properties.stage.ManagedScannable;
import uk.ac.gda.client.properties.stage.ScannableProperties;

/**
 * Event published when a {@link ManagedScannable} moves its controlled  scannable
 *
 * @author Maurizio Nagni
 *
 */
public class ManagedScannableEvent<T> extends ApplicationEvent {

	/**
	 * The properties which defines the scannable
	 */
	private final ScannableProperties scannablePropertiesDocument;
	/**
	 * The new scannable position
	 */
	private final T position;

	public ManagedScannableEvent(Object source, ScannableProperties scannablePropertiesDocument, T position) {
		super(source);
		this.scannablePropertiesDocument = scannablePropertiesDocument;
		this.position = position;
	}

	/**
	 * The properties which defines the scannable
	 *
	 * @return the scannable properties
	 */
	public ScannableProperties getScannablePropertiesDocument() {
		return scannablePropertiesDocument;
	}

	/**
	 * The new scannable position
	 *
	 * @return the position which caused this event
	 */
	public T getPosition() {
		return position;
	}
}