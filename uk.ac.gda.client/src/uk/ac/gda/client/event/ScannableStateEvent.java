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

/**
 * Publishes info regarding a scannable
 *
 * @author Maurizio Nagni
 *
 */
public class ScannableStateEvent extends ApplicationEvent {

	private final String scannableName;
	private final double scannablePosition;

	/**
	 * @param source the object publishing this event
	 * @param scannableName the name of the scannable causing the event
	 * @param scannablePosition the scannable position, at the time the event occurs
	 */
	public ScannableStateEvent(Object source, String scannableName, double scannablePosition) {
		super(source);
		this.scannableName = scannableName;
		this.scannablePosition = scannablePosition;
	}

	public String getScannableName() {
		return scannableName;
	}

	public double getScannablePosition() {
		return scannablePosition;
	}
}