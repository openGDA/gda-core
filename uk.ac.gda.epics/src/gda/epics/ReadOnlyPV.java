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

import gda.observable.Observable;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.event.MonitorListener;

import java.io.IOException;
import java.io.InterruptedIOException;

// TODO: Explain IOException

/**
 * A read only Epics ProcessVariable. The {@link LazyPVFactory} class provides convenient factory methods to create lazy PVs
 * which don't connect until the first request across CA is to be made.
 * 
 * @param <T>
 */
public interface ReadOnlyPV<T> extends Observable<T> {

	public String getPvName();

	/**
	 * Get a value over CA.
	 * 
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public T get() throws IOException;

	/**
	 * Get the most recent value monitored across CA. If no value has yet been received, then get a value across CA. The
	 * PV must have been configured to monitor with {@link #setValueMonitoring(boolean)};
	 * 
	 * @throws IllegalStateException
	 *             if the PV is not configured to monitor with {@link #setValueMonitoring(boolean)};
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public T getLast() throws IOException;

	/**
	 * Waits for a value that meets the given {@link Predicate} (if-statement-like) condition and then returns it. The
	 * PV must have been configured to monitor with {@link #setValueMonitoring(boolean)};
	 * 
	 * @param predicate
	 *            The predicate used to test each value
	 * @param timeoutS
	 *            The time in seconds to wait for a value that passes the predicate. A value of 0 or less results in an
	 *            indefinite timeout.
	 * @return the first value monitored that passed the predicate test
	 * @throws IllegalStateException
	 *             if the PV is not configured to monitor with {@link #setValueMonitoring(boolean)};
	 * @throws java.util.concurrent.TimeoutException
	 *             if a value that passes the predicate does not appear within the provided timeoutS
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */

	public T waitForValue(Predicate<T> predicate, double timeoutS) throws IllegalStateException,
			java.util.concurrent.TimeoutException, IOException;

	/**
	 * Configure this PV to start or stop monitoring. When monitoring, calls to {@link #get()} will returned the most
	 * recent value monitored across CA.
	 * 
	 * @param shouldMonitor
	 *            true to start monitoring values and caching them internally
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public void setValueMonitoring(boolean shouldMonitor) throws IOException;

	/**
	 * Check if this PV is monitoring. See {@link #setValueMonitoring(boolean)}
	 * 
	 * @return true if monitoring
	 */
	public boolean isValueMonitoring();

	/**
	 * Add A MonitorListener to the channel associated with the PV. Will create the channel if required.
	 * 
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public void addMonitorListener(MonitorListener listener) throws IOException;

	public void removeMonitorListener(MonitorListener listener);

	/**
	 * Extacts a value from a {@link DBR}. Useful for writing MonitorListeners.
	 * 
	 * @param dbr
	 * @return the value
	 */
	public T extractValueFromDbr(DBR dbr);
	
}
