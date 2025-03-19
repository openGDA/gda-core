/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.util;

import java.util.HashMap;
import java.util.Map;

import com.github.tschoonj.xraylib.Xraylib;

public class XrayLibHelper {

	private static Map<String, Integer> edgeIndices =  getDefaultEdgeIndices();

	private XrayLibHelper() {
	}

	private static Map<String, Integer> getDefaultEdgeIndices() {
		Map<String, Integer> shellIndices = new HashMap<>();
		shellIndices.put("K", Xraylib.K_SHELL);

		shellIndices.put("L1", Xraylib.L1_SHELL);
		shellIndices.put("L2", Xraylib.L2_SHELL);
		shellIndices.put("L3", Xraylib.L3_SHELL);

		shellIndices.put("M1", Xraylib.M1_SHELL);
		shellIndices.put("M2", Xraylib.M2_SHELL);
		shellIndices.put("M3", Xraylib.M3_SHELL);
		shellIndices.put("M4", Xraylib.M4_SHELL);
		shellIndices.put("M5", Xraylib.M5_SHELL);
		return shellIndices;
	}

	/**
	 * Look up energy for given element and edge name using Xraylib.
	 *
	 * @param elementName (Cu, Mn, Fe etc)
	 * @param edgeName (K, L1, L2 etc)
	 * @return energy of edge (eV)
	 */
	public static double getEdgeEnergy(String elementName, String edgeName) {
		int atomicNumber = Xraylib.SymbolToAtomicNumber(elementName);
		if (!edgeIndices.keySet().contains(edgeName.toUpperCase())) {
			throw new IllegalArgumentException("Could not find index for edge name '"+edgeName+"'");
		}
		return Xraylib.EdgeEnergy(atomicNumber, edgeIndices.get(edgeName))*1000.0;
	}
}
