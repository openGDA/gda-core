/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.epics.connection.EpicsController;
import gda.epics.util.EpicsGlobals;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * An Epics Process Variable. The {@link LazyPVFactory} class provides convenient factory methods to create PVs which don't
 * connect until the first request across CA is to be made.
 * 
 * @param <T>
 */
public interface PV<T> extends NoCallbackPV<T> {

	/**
	 * Maps PV keys to values.
	 */
	public interface PVValues {

		/**
		 * Returns the value to which the specified PV is mapped.
		 * 
		 * @param pv
		 *            the key whose associated value is to be returned
		 * @return the value to which the specified key is mapped
		 * @throws IllegalArgumentException
		 *             if this map contains no mapping for the key
		 */
		public <N> N get(ReadOnlyPV<N> pv) throws IllegalArgumentException;

	}

	/**
	 * Put a value over CA and wait for callback.
	 * <p>
	 * Currently, the {@link EpicsController} which backs PV's returned by {@link LazyPVFactory} will use
	 * {@link EpicsGlobals#getTimeout()} to determine the default timeout. Currently this will be 30s unless
	 * {@link LocalProperties} "gda.epics.request.timeout" was set at startup.
	 * 
	 * @param value
	 *            the value to put across CA
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public void putCallback(T value) throws IOException;

	/**
	 * Put a value over CA and wait for callback.
	 * 
	 * @param value
	 *            the value to put across CA
	 * @param timeoutS
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public void putCallback(T value, double timeoutS) throws IOException;

	/**
	 * Put value over CA asking for a later callback and <bold>return immediately</bold>. A later call to
	 * {@link #waitForCallback()} can be used to wait for the callback.
	 * <p>
	 * If second request to putCallback is made while a callback is pending this will result in an
	 * {@link IllegalStateException}
	 * <p>
	 * Currently, the {@link EpicsController} which backs PV's returned by {@link LazyPVFactory} will use
	 * {@link EpicsGlobals#getTimeout()} to determine the default timeout. Currently this will be 30s unless
	 * {@link LocalProperties} "gda.epics.request.timeout" was set at startup.
	 * 
	 * @param value
	 *            the value to put across CA
	 * @throws IllegalStateException
	 *             If a second request to putCallback is made when there is already a callback pending.
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public void startPutCallback(T value) throws IOException;

	/**
	 * Waits for a previously made {@link #startPutCallback} call to complete. Waits up to the default epics timeout
	 * determined using {@link EpicsGlobals#getTimeout()}. Currently this will be 30s unless {@link LocalProperties}
	 * "gda.epics.request.timeout" was set at startup.
	 * 
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public void waitForCallback() throws IOException;

	/**
	 * Return true if a callback is pending
	 * @return true if a callback is pending
	 */
	boolean isCallbackPending();
	
	/**
	 * Cancel a pending callback. This will cause waitForCallback() to return if blocked.
	 */
	void cancelPendingCallback();
	
	/**
	 * Waits for a previously made {@link #startPutCallback} call to complete.
	 * 
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */

	public void waitForCallback(double timeoutS) throws IOException;

	/**
	 * Put a value over CA and wait for callback, then return the values for PVs specified by toReturn at the time of
	 * the callback.
	 * 
	 * @param value
	 *            the value to put across CA
	 * @param toReturn
	 *            PVs whose values are to be returned
	 * @return A PVValues object with a value for each PV in toReturn.
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public PVValues putCallbackResult(T value, ReadOnlyPV<?>... toReturn) throws IOException;

	/**
	 * Put a value over CA and wait for callback with specified timeout, then return the values for PVs specified by
	 * toReturn at the time of the callback.
	 * 
	 * @param value
	 *            the value to put across CA
	 * @param toReturn
	 *            PVs whose values are to be returned
	 * @return A PVValues with a value for each PV in toReturn
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public PVValues putCallbackResult(T value, double timeoutS, ReadOnlyPV<?>... toReturn) throws IOException;

}