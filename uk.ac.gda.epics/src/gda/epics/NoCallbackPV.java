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

import gov.aps.jca.event.PutListener;

import java.io.IOException;
import java.io.InterruptedIOException;

public interface NoCallbackPV<T> extends ReadOnlyPV<T> {

	/**
	 * Put a value over CA and return immediately.
	 * 
	 * @param value
	 *            the value to put across CA
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */

	public abstract void putNoWait(T value) throws IOException;

	/**
	 * Put a value over CA and return immediately specifying a {@link PutListener} which will be called when the put is
	 * complete.
	 * 
	 * @param value
	 *            the value to put across CA
	 * @throws IOException
	 *             if an Epics CA exception of some sort has occurred
	 * @throws InterruptedIOException
	 *             if an Epics CA operation has been interrupted
	 */
	public abstract void putNoWait(T value, PutListener pl) throws IOException;

}