/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda.scannables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.scannablegroup.ScannableGroup;
import uk.ac.diamond.daq.diffcalc.gda.NoSolutionsFoundException;

public abstract class ParametrisedHklScannable extends HklScannable {

	private static final Logger logger = LoggerFactory.getLogger(ParametrisedHklScannable.class);

	public static final String OUTPUT_FORMAT = "%7.5f";

	private boolean cache = false;
	private int numberCachedParams = 0;
	private List<Integer> cachedBeamlinePositionIndices = new ArrayList<>();
	private Double hklTolerance = 0.0001;
	private List<Double> cachedParams = null;

	@Override
	public Object rawGetPosition() throws DeviceException {
		List<Double> hkl = diffcalcContext.getHklPosition();
		return hklToParameters(hkl).toArray(Double[]::new);
	}

	@Override
	public void rawAsynchronousMoveTo(Object params) throws DeviceException {
		ScannableGroup diffractometer = diffcalcContext.getDiffractometer();

		List<List<Double>> hkl;
		List<Double> positionFromHkl;

		try {
			hkl = parametersToHkl(parseParams(params));
		} catch (Exception e1) {
			logger.error("Something went wrong: " + e1.getMessage());
			return;
		}

		try {
			positionFromHkl = diffcalcContext.closestBeamlinePositionFromHklList(hkl);
		}  catch (NoSolutionsFoundException e) {
			logger.warn("No solutions found!");
			return;
		}

		if (cache) {cachePosition(positionFromHkl);}
		diffractometer.asynchronousMoveTo(positionFromHkl);

	}

	// Word of warning; python stores lists of varying types. Java does not.
	// If a pos or scan command in jython contains a position with mixed types, subclass methods will fail
	// when retrieving cachedParams because Java thinks its a List<Double> but actually its a List<Double, Integer>.
	@SuppressWarnings("unchecked")
	private List<Double> parseParams(Object params) {
		List<Double> paramList = new ArrayList<>();
		if (params instanceof Double[]) {
			var p = (Double[]) params;
			for (Double item: p) {paramList.add(item);}

		} else if (params instanceof List) {
			paramList = (List<Double>) params;

		} else {
			throw new IllegalArgumentException("Parameters: " + params + " given in an unsupported format");
		}

		try {
			paramList.stream().forEach(Double::doubleValue);

		} catch (ClassCastException e) {
			throw new IllegalArgumentException("all parameters must be doubles. Not integers!");
		}

		if (paramList.size() != getInputNames().length) {
			throw new IllegalArgumentException("Please supply the following parameters: " + getInputNames());
		}

		cachedParams = paramList.subList(paramList.size() - numberCachedParams, paramList.size());
		return paramList;
	}

	private void cachePosition(List<Double> positionFromHkl) throws DeviceException {

		ScannableGroup diffractometer = diffcalcContext.getDiffractometer();

		@SuppressWarnings("unchecked")
		List<Double> currentBeamlinePosition = (List<Double>) diffractometer.getPosition();
		Map<Integer, Double> cachedBeamlinePosition = new HashMap<>();

		for (int index: cachedBeamlinePositionIndices) {
			cachedBeamlinePosition.put(index, currentBeamlinePosition.get(index));
		}

		List<Double> positionFromHklWithCached = new ArrayList<>();

		IntStream.range(0, positionFromHkl.size()).forEach(index -> {
			Double value = cachedBeamlinePosition.get(index);
			if (Objects.nonNull(value)) {
				positionFromHklWithCached.add(value);
			}
			else {
				positionFromHklWithCached.add(positionFromHkl.get(index));
			}
		});

		List<Double> calcHkl = diffcalcContext.getHklPosition(positionFromHkl);
		List<Double> calcHklWithCached = diffcalcContext.getHklPosition(positionFromHklWithCached);

		for (int i=0; i< calcHkl.size(); i++) {
			Double difference = calcHkl.get(i) - calcHklWithCached.get(i);
			if (difference > hklTolerance) {
				logger.warn("Warning: Final hkl position " + calcHklWithCached + " exceeds tolerance " + hklTolerance + " compared to calculated hkl " + calcHkl);
			}
		}
	}

	protected abstract List<List<Double>> parametersToHkl(List<Double> paramList) throws DeviceException;

	protected abstract List<Double> hklToParameters(List<Double> hkl);

	public int getNumberCachedParams() {
		return numberCachedParams;
	}

	public void setNumberCachedParams(int numberCachedParams) {
		this.numberCachedParams = numberCachedParams;
	}

	public List<Integer> getCachedBeamlinePositionIndices() {
		return cachedBeamlinePositionIndices;
	}

	public void setCachedBeamlinePositionIndices(List<Integer> cachedBeamlinePositionIndices) {
		this.cachedBeamlinePositionIndices = cachedBeamlinePositionIndices;
	}

	public Double getHklTolerance() {
		return hklTolerance;
	}

	public void setHklTolerance(Double tolerance) {
		this.hklTolerance = tolerance;
	}

	public List<Double> getCachedParams() {
		return cachedParams;
	}

	public void setCachedParams(List<Double> cachedParams) {
		this.cachedParams = cachedParams;
	}
}
