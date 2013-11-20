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

package gda.epics;

import java.util.HashMap;
import java.util.Map;

public class CachedLazyPVFactory {
	protected Map<String, PV<Double>> pvDoubles;
	protected Map<String, ReadOnlyPV<Double>> pvReadOnlyDoubles;
	protected Map<String, ReadOnlyPV<Double[]>> pvReadOnlyDoubleArrays;
	protected Map<String, ReadOnlyPV<Integer>> pvReadOnlyIntegers;
	protected Map<String, PV<Integer>> pvIntegers;

	private String deviceprefix;

	public PV<Double> getPVDouble(String suffix) {
		if (pvDoubles == null)
			pvDoubles = new HashMap<String, PV<Double>>();
		PV<Double> pv = pvDoubles.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newDoublePV(deviceprefix + suffix);
			pvDoubles.put(suffix, pv);
		}
		return pv;
	}
	
	public ReadOnlyPV<Double> getReadOnlyPVDouble(String suffix) {
		if (pvReadOnlyDoubles == null)
			pvReadOnlyDoubles = new HashMap<String, ReadOnlyPV<Double>>();
		ReadOnlyPV<Double> pv = pvReadOnlyDoubles.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newReadOnlyDoublePV(deviceprefix + suffix);
			pvReadOnlyDoubles.put(suffix, pv);
		}
		return pv;
	}

	public ReadOnlyPV<Double[]> getReadOnlyPVDoubleArray(String suffix) {
		if (pvReadOnlyDoubleArrays == null)
			pvReadOnlyDoubleArrays = new HashMap<String, ReadOnlyPV<Double[]>>();
		ReadOnlyPV<Double[]> pv = pvReadOnlyDoubleArrays.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newReadOnlyDoubleArrayPV(deviceprefix + suffix);
			pvReadOnlyDoubleArrays.put(suffix, pv);
		}
		return pv;
	}

	public PV<Integer> getPVInteger(String suffix) {
		if (pvIntegers == null)
			pvIntegers = new HashMap<String, PV<Integer>>();
		PV<Integer> pv = pvIntegers.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newIntegerPV(deviceprefix + suffix);
			pvIntegers.put(suffix, pv);
		}
		return pv;
	}

	public ReadOnlyPV<Integer> getReadOnlyPVInteger(String suffix) {
		if (pvReadOnlyIntegers == null)
			pvReadOnlyIntegers = new HashMap<String, ReadOnlyPV<Integer>>();
		ReadOnlyPV<Integer> pv = pvReadOnlyIntegers.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newReadOnlyIntegerPV(deviceprefix + suffix);
			pvReadOnlyIntegers.put(suffix, pv);
		}
		return pv;
	}

	public CachedLazyPVFactory(String zebraPrefix) {
		super();
		this.deviceprefix = zebraPrefix;
	}

}
