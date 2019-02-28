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

import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;


/**
 * An {@link IPositioner} is used to move a set of scannables each to a desired position.
 * An instance can be created from an {@link IRunnableDeviceService} by calling
 * {@link IRunnableDeviceService#createPositioner(org.eclipse.scanning.api.INameable)}.
 * A position can be set by calling {@link #setPosition(IPosition)}, e.g.
 * <pre>
 * positioner.setPosition(new MapPosition("x:1, y:2"));
 * </pre>
 * The method {@link #setPosition(IPosition)} moves the scannables by level to their respective
 * positions as specified in the {@link IPosition} argument and then returns.
 * This is used by the scanning framework to move to a scan point during a scan.
 * <p>
 * The {@link MapPosition} used in the example above is the most general implementation
 * of {@link IPosition}. Since it has a constuctor that takes a map, in jython a
 * dictionary can be used, e.g.:
 * <pre>
 * pos.setPosition(new MapPosition({'x':1, 'y':2})) }
 * </pre>
 * A instance of this class can be limited to just moving a specified set of scannables,
 * by calling {@link #setScannables(List)}. Doing this means that the positioner can cache
 * these scannables, otherwise it has to find the scannables using the {@link IScannableDeviceService}
 * on each call to {@link #setPosition(IPosition)}.
 * <p>Additionally per-point monitors can be added to the positioner by calling
 * {@link #setMonitorsPerPoint(List)} or a variant of this method. Note that monitors are also
 * implementations of {@link IScannable}. On each call to {@link #setPosition(IPosition)} these
 * will have their {@link IScannable#setPosition(Object, IPosition)} methods call with
 * <code>null</code> as the first argument and the position passed to the
 * {@link #setPosition(IPosition)} of this instance as the second.
 *
 * @author Matthew Gerring
 *
 */
public interface IPositioner extends IPositionListenable {

	/**
	 * This method moves to the position passed in and returns when the move
	 * is complete.
	 *
	 * It takes into account the levels of the scannables and moves them
	 * using a separate thread for each one. An executor service is used
	 * for each level and attempts to process all scannables on a given level
	 * before exiting.
	 *
	 * This method blocks until all the scannables have reached their desired location.
	 *
	 * @param position
	 * @return false if the position could not be reached. Normally an exception will be thrown if this is the case.
	 * @throws ScanningException if an error occurred moving to the position, including a timeout
	 * @throws InterruptedException if the move was aborted by a call to {@link #abort()} in another thread (not guaranteed)
	 *
	 */
	boolean setPosition(IPosition position) throws ScanningException, InterruptedException;

	/**
	 * This method will return null if the positioner has not been told to move to a
	 * position. If it has it will read the scannable values from the last position it
	 * was told to go to and return a position of those values. For instance if the
	 * IPositioner was last told to go to x:1 and y:2, it would return the current values
	 * of the scannables x and y by reading them again.
	 *
	 * @return a position containing the positions of the last scannables moved
	 * @throws ScanningException
	 */
	IPosition getPosition() throws ScanningException;

	/**
	 * Gets the per-point monitors for this positioner.
	 * <p>
	 * Monitors are a set of scannables which will have {@link IScannable#setPosition(Object, IPosition)}
	 * called with <code>null</code> as the first argument. They may
	 * block until happy or write an additional record during the scan.
	 * <p>
	 * For instance the beam current or ambient temperature could be monitors which are written
	 * to the NeXus file. Monitors are sorted into level with the scannables of the current position.
	 *
	 * @return monitors
	 * @throws ScanningException
	 */
	List<IScannable<?>> getMonitorsPerPoint()  throws ScanningException;

	/**
	 * Sets the per-point monitors for this positioner.
	 * <p>
	 * Monitors are a set of scannables which will have {@link IScannable#setPosition(Object, IPosition)}
	 * called with <code>null</code> as the first argument. They may
	 * block until happy or write an additional record during the scan.
	 * <p>
	 * For instance the beam current or ambient temperature could be monitors which are written
	 * to the NeXus file. Monitors are sorted into level with the scannables of the current position.
     *
	 * @param monitors
	 */
	void setMonitorsPerPoint(List<IScannable<?>> monitors);

	/**
	 * Sets the per-point monitors for this positioner.
	 * <p>
	 * Monitors are a set of scannables which will have {@link IScannable#setPosition(Object, IPosition)}
	 * called with <code>null</code> as the first argument. They may
	 * block until happy or write an additional record during the scan.
	 * <p>
	 * For instance the beam current or ambient temperature could be monitors which are written
	 * to the NeXus file. Monitors are sorted into level with the scannables of the current position.
     *
	 * @param monitors
	 */
	void setMonitorsPerPoint(IScannable<?>... monitors);

	/**
	 * Set the scannables that this positioner can move. If set, this limits the positioner to
	 * only move these scannables, but has the advantage of allowing it to cache the scannables.
	 * Set this if the scannables that this positioner will move are known in advance.
	 * If not set, the positioner can move any scannable, allowing calls to
	 * {@link #setPosition(IPosition)} to move scannables that were not moved in a previous
	 * call to that method, but at the disadvantage of the positioner not being able to cache
	 * the scannables.
	 *
	 * @param scannables scannables that this positioner can move
	 */
	void setScannables(List<IScannable<?>> scannables);

	/**
     * Aborts any move currently taking place. Any thread that is currently performing a positioner
     * move may be interrupted. This positioner cannot be used again after calling this method.
     * This method returns immediately.
	 */
	void abort();

	/**
	 * Closes the positioner, allowing it to dispose of any
	 * This frees up threads and the positioner may still be used (it will create
	 * a new thread pool if reused after it has been closed).
	 */
	void close();
}
