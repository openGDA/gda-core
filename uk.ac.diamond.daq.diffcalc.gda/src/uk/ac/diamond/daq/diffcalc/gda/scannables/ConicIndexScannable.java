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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.diffcalc.ApiException;
import uk.ac.diamond.daq.diffcalc.gda.TypeConversion;
import uk.ac.diamond.daq.diffcalc.model.HklModel;

public class ConicIndexScannable extends ParametrisedHklScannable {

	private String fixedIndex;

	@Override
	public void configure() throws FactoryException {
		String[] inputNames = { fixedIndex, "a", "b", "c", "d" };
		String[] formats = Collections.nCopies(inputNames.length, OUTPUT_FORMAT).toArray(String[]::new);

		setNumberCachedParams(4);
		setInputNames(inputNames);
		setOutputFormat(formats);
		setConfigured(true);
	}

	@Override
	public List<List<Double>> parametersToHkl(List<Double> paramList)
			throws DeviceException, ApiException {

		Double indexValue = paramList.get(0);
		Double a = paramList.get(1);
		Double b = paramList.get(2);
		Double c = paramList.get(3);
		Double d = paramList.get(4);

		HklModel hkl = TypeConversion.millerIndicesToHklModel(diffcalcContext.getHklPosition());
		return diffcalcContext.solveForIndex(hkl, fixedIndex, indexValue, a, b, c, d);
	}

	@Override
	public List<Double> hklToParameters(List<Double> millerIndices) {
		List<Double> cachedParams = getCachedParams();

		Map<String, Double> hkl = new HashMap<>();
		hkl.put("h", millerIndices.get(0));
		hkl.put("k", millerIndices.get(1));
		hkl.put("l", millerIndices.get(2));

		return Arrays.asList(hkl.get(fixedIndex), cachedParams.get(0), cachedParams.get(1), cachedParams.get(2), cachedParams.get(3));
	}

	public String getFixedIndex() {
		return fixedIndex;
	}

	public void setFixedIndex(String fixedIndex) {
		if (fixedIndex.equals("h") || fixedIndex.equals("k") || fixedIndex.equals("l")) {
			this.fixedIndex = fixedIndex;
			return;
		}
		throw new IllegalArgumentException("The value of fixedIndex can only be h, k, or l.");
	}

}
