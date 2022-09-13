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

package uk.ac.diamond.daq.diffcalc.gda;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.diffcalc.model.HklModel;
import uk.ac.diamond.daq.diffcalc.model.XyzModel;

/**
 * Class which converts between the types of objects used in this package, and those used
 * by the diffcalc-api auto-generated java client.
 */
public class TypeConversion {
	public static List<List<Double>> bigDecimalArrayToDoubleArray(List<List<BigDecimal>> bigDecimalArray) {
		return bigDecimalArray.stream().map(eachList -> eachList.stream().map(BigDecimal::doubleValue).toList()).toList();
	}

	public static List<List<BigDecimal>> doubleArrayToBigDecimalArray(List<List<Double>> doubleArray) {
		return doubleArray.stream().map(eachList -> eachList.stream().map(BigDecimal::valueOf).toList()).toList();
	}

	public static List<Double> bigDecimalListToDoubleList(List<BigDecimal> bigDecimalList) {
		return bigDecimalList.stream().map(BigDecimal::doubleValue).toList();
	}

	public static List<BigDecimal> doubleListToBigDecimalList(List<Double> doubleList) {
		return doubleList.stream().map(BigDecimal::valueOf).toList();
	}

	public static HklModel millerIndicesToHklModel(List<Double> millerIndices) {
		if (millerIndices.size() != 3) {
			throw new IllegalArgumentException("3 miller indices must be provided");
		}

		HklModel hkl = new HklModel();

		List<BigDecimal> convertedMillerIndices = millerIndices.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

		hkl.setH(convertedMillerIndices.get(0));
		hkl.setK(convertedMillerIndices.get(1));
		hkl.setL(convertedMillerIndices.get(2));
		return hkl;
	}

	public static List<Double> hklModelToMillerIndices(HklModel hkl ) {
		return Arrays.asList(hkl.getH().doubleValue(), hkl.getK().doubleValue(), hkl.getL().doubleValue());
	}

	public static XyzModel coordsToXyzModel(List<Double> coords) {
		if (coords.size() != 3) {
			throw new IllegalArgumentException("3 coordinates must be provided");
		}

		XyzModel xyz = new XyzModel();

		List<BigDecimal> convertedCoords = coords.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

		xyz.setX(convertedCoords.get(0));
		xyz.setY(convertedCoords.get(1));
		xyz.setZ(convertedCoords.get(2));

		return xyz;
	}

	public static List<Double> xyzModelToCoords(XyzModel xyz) {
		return Arrays.asList(xyz.getX().doubleValue(), xyz.getY().doubleValue(), xyz.getZ().doubleValue());
	}
}
