/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.mscan.processor;

import gda.mscan.ClauseContext;
import gda.mscan.element.Roi;

/**
 * An interface to be implemented by all MScan Clause ElementProcessors. These are responsible for
 * triggering the filling in of the {@link ClauseContext} for the MScan clause being parsed based on the
 * type that was used to create them. Each implementing class must  override the process method to
 * cause the filling in of the relevant bits of the context associated with the type of element
 * being processed. For instance for {@link Roi} elements it should call the method(s) on the context that
 * set up the state associated with the specified {@link Roi}.
 */
public interface IClauseElementProcessor {

	/*
	 * Default implementation to indicate whether the processor is associated with a Scannable
	 */
	public default boolean hasScannable() {
		return false;
	}

	/*
	 * Default implementation to indicate whether the processor is associated with a ScannableGroup
	 */
	public default boolean hasScannableGroup() {
		return false;
	}

	/*
	 * Default implementation to indicate whether the processor is associated with a Detector
	 */
	public default boolean hasDetector() {
		return false;
	}

	/*
	 * Default implementation to indicate whether the processor is associated with a Monitor
	 */
	public default boolean hasMonitor() {
		return false;
	}

	/*
	 * Default implementation to indicate whether the processor is associated with a Roi
	 */
	public default boolean hasRoi() {
		return false;
	}

	/*
	 * Default implementation to indicate whether the processor is associated with a Numeric value
	 */
	public default boolean hasNumber() {
		return false;
	}

	/*
	 * Fires the required method(s) on the supplied context to fill in a Clause element's details
	 *
	 * @param context	The {@link ClauseContext} object being completed for the current MSCan clause
	 * @param index		The index of the clause element associated with the processor within the current clause
	 */
	public void process(final ClauseContext context, final int index);

	/**
	 * Implementations of this method should return the source object used to create the processor instance
	 *
	 * @return	the source object used to create the processor instance
	 */
	public Object getSource();
}
