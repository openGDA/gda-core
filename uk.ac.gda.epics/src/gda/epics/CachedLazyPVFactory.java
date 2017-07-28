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


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CachedLazyPVFactory {

	protected Map<String, PV<Double>> pvDoubles;
	protected Map<String, PV<Double[]>> pvDoublesArray;
	protected Map<String, ReadOnlyPV<Double>> pvReadOnlyDoubles;
	protected Map<String, ReadOnlyPV<Double[]>> pvReadOnlyDoubleArrays;
	protected Map<String, ReadOnlyPV<Integer>> pvReadOnlyIntegers;
	protected Map<String, PV<Integer>> pvIntegers;
	protected Map<String, PVValueCache<Integer>> pvValueCacheIntegers;
	protected Map<String, PVValueCache<Double>> pvValueCacheDoubles;
	protected Map<String, PV<String>> pvStrings;
	protected Map<String, PV<Class<?>>> pvEnums;

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

	public PV<Double[]> getPVDoubleArray(String suffix) {
		if (pvDoublesArray == null)
			pvDoublesArray = new HashMap<String, PV<Double[]>>();
		PV<Double[]> pv = pvDoublesArray.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newDoubleArrayPV(deviceprefix + suffix);
			pvDoublesArray.put(suffix, pv);
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

	public void set(String pv, double value) throws IOException{
		getPVDouble(pv).putWait(value);
	}

	public void set(String pv, int value) throws IOException{
		getPVInteger(pv).putWait(value);
	}

	public double getDouble(String pv) throws IOException{
		return getPVDouble(pv).get();
	}

	public int getInteger(String pv) throws IOException{
		return getPVInteger(pv).get();
	}

	public CachedLazyPVFactory(String devicePrefix) {
		super();
		this.deviceprefix = devicePrefix;
	}


	@Override
	public String toString() {
		return "CachedLazyPVFactory [deviceprefix=" + deviceprefix + "]";
	}

	public PVValueCache<Integer> getIntegerPVValueCache(String suffix) throws Exception{
		if (pvValueCacheIntegers == null)
			pvValueCacheIntegers = new HashMap<String, PVValueCache<Integer>>();

		PVValueCache<Integer> pv = pvValueCacheIntegers.get(suffix);
		if (pv == null) {
			pv = new PVValueCache<Integer>(getPVInteger(suffix));
			pvValueCacheIntegers.put(suffix, pv);
		}
		return pv;

	}

	public PVValueCache<Double> getDoublePVValueCache(String suffix) throws Exception{
		if (pvValueCacheDoubles == null)
			pvValueCacheDoubles = new HashMap<String, PVValueCache<Double>>();

		PVValueCache<Double> pv = pvValueCacheDoubles.get(suffix);
		if (pv == null) {
			pv = new PVValueCache<Double>(getPVDouble((suffix)));
			pvValueCacheDoubles.put(suffix, pv);
		}
		return pv;
	}

	public PV<String> getPVString(String suffix) {
		if (pvStrings == null)
			pvStrings = new HashMap<String, PV<String>>();
		PV<String> pv = pvStrings.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newStringPV(deviceprefix + suffix);
			pvStrings.put(suffix, pv);
		}
		return pv;
	}

	public PV<String> getPVStringAsBytes(String suffix) {
		if (pvStrings == null)
			pvStrings = new HashMap<String, PV<String>>();
		PV<String> pv = pvStrings.get(suffix);
		if (pv == null) {
			pv = LazyPVFactory.newStringFromWaveformPV(deviceprefix + suffix);
			pvStrings.put(suffix, pv);
		}
		return pv;
	}
}
