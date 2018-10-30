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

package gda.scan;

import java.util.List;

import gda.factory.Configurable;
import gda.factory.FindableBase;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;

/**
 * Base class for IScanDataPoint caches. Initialises at the start of each scan and then
 * adds each point in turn.
 */
public abstract class DataPointCache extends FindableBase implements IScanDataPointObserver, Configurable {
	private boolean configured;

	/**
	 * Extract a list of values for the given field name from the cached data.
	 *
	 * @param scannableName The field name for which to extract data
	 * @return A list of positions for the given field
	 */
	public abstract List<Double> getPositionsFor(String scannableName);

	/**
	 * Add a {@link IScanDataPoint} to this cache. This will be called whenever a data point is
	 * produced by the {@link IScanDataPointProvider} this is observing.
	 * @param sdp The new {@link IScanDataPoint} to cache
	 */
	protected abstract void addDataPoint(IScanDataPoint sdp);

	/**
	 * Run any initialisation required at the start of a scan. This will be called with the
	 * first point (where the currentPointNumber == 0) of each scan.
	 * @param sdp The first {@link IScanDataPoint} of the scan
	 */
	@SuppressWarnings("unused") // this is for any initialisation implementations need
	protected void initialise(IScanDataPoint sdp) {}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof IScanDataPoint) {
			IScanDataPoint sdp = (IScanDataPoint) arg;
			if (sdp.getCurrentPointNumber() == 0) {
				initialise(sdp);
			}
			addDataPoint((IScanDataPoint)arg);
		}
	}

	@Override
	public void configure() {
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
		configured = true;
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}
}
