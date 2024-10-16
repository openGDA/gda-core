/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.scan.event;

import java.util.EventListener;
import java.util.function.Consumer;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * A positioner moves the motors, taking into account level
 * and blocks until done.
 *
 * It is posible to get an event for the move, both when each level
 * is complete and at the end of the move, using this listener. This
 * can be useful for designs that to and action which should notify other
 * objects about moves.
 *
 * @author Matthew Gerring
 *
 */
public interface IPositionListener extends EventListener {


	/**
	 * Called just before the position reaches a given value
	 * @param event
	 * @return <code>false</code> to abort the point but not the overall scan.
	 * @throws ScanningException if an exception occurred responding to this event.
	 *   <em>Note:</em> throwing an exception will stop the scan. If this behaviour is
	 *   not desirable the exception should be caught and logged instead
	 */
	default boolean positionWillPerform(PositionEvent event) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
		return true; // true indicates scan should continue as normal
	}

	/**
	 * Called after a given move level has been reached.
	 * @param event
	 * @throws ScanningException if an exception occurred responding to this event.
	 *   <em>Note:</em> throwing an exception will stop the scan. If this behaviour is
	 *   not desirable the exception should be caught and logged instead
	 */
	default void levelPerformed(PositionEvent event) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	/**
	 * Called when the position of an {@link IScannable} changes.
	 * @param event
	 * @throws ScanningException if an exception occurred responding to this event.
	 *   <em>Note:</em> throwing an exception will stop the scan. If this behaviour is
	 *   not desirable the exception should be caught and logged instead
	 */
	default void positionChanged(PositionEvent event) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	/**
	 * Called after a given position in a scan has been performed.
	 * @param event
	 * @throws ScanningException if an exception occurred responding to this event.
	 *   <em>Note:</em> throwing an exception will stop the scan. If this behaviour is
	 *   not desirable the exception should be caught and logged instead
	 */
	default void positionPerformed(PositionEvent event) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	/**
	 * Called after a given position in a scan has been moved to but before
	 * detectors are exposed.
	 * TODO: is there a better name for this method
	 * @param event
	 * @throws ScanningException
	 */
	default void positionMovePerformed(PositionEvent event) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	public static IPositionListener postionPerformedListener(Consumer<PositionEvent> consumer) {
		return new IPositionListener() {
			@Override
			public void positionPerformed(PositionEvent e) {
				consumer.accept(e);
			}
		};
	}

	public static IPositionListener positionMovePerformed(Consumer<PositionEvent> consumer) {
		return new IPositionListener() {
			@Override
			public void positionMovePerformed(PositionEvent e) {
				consumer.accept(e);
			}
		};
	}


}
