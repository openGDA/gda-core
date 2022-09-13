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
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.diffcalc.ApiException;
import uk.ac.diamond.daq.diffcalc.gda.Maths;
import uk.ac.diamond.daq.diffcalc.gda.ReflectionResult;
import uk.ac.diamond.daq.diffcalc.gda.TypeConversion;
import uk.ac.diamond.daq.diffcalc.model.HklModel;
import uk.ac.diamond.daq.diffcalc.model.SphericalCoordinates;

public class ConicHklScannable extends ParametrisedHklScannable {

	@Override
	public void configure() throws FactoryException {
		String[] inputNames = { "rlu", "az" };
		String[] formats = Collections.nCopies(inputNames.length, OUTPUT_FORMAT).toArray(String[]::new);

		setInputNames(inputNames);
		setOutputFormat(formats);
		setConfigured(true);
	}

	@Override
	protected List<List<Double>> parametersToHkl(List<Double> paramList)
			throws DeviceException, ApiException {
		Double sc = paramList.get(0);
		Double az = paramList.get(1);

		List<Double> hkl = diffcalcContext.getHklPosition();
		SimpleMatrix ub = Maths.listOfListsToSimpleMatrix(diffcalcContext.getUb());
		ReflectionResult reflection = diffcalcContext.getReflection(1);


		SimpleMatrix hklVector = Maths.listToColumnVector(hkl);

		List<Double> reflectionHkl = reflection.hkl();
		SimpleMatrix reflectionHklVector = Maths.listToColumnVector(reflectionHkl);


		SimpleMatrix nphiFromHkl = ub.mult(hklVector);
		SimpleMatrix reflectionNphi = ub.mult(reflectionHklVector);

		reflectionNphi.scale(nphiFromHkl.normF() / reflectionNphi.normF());

		SimpleMatrix nphi = Maths.listOfListsToSimpleMatrix(
				Maths.columnVectorFromCoordsList(
						diffcalcContext.getLabReferenceVector()
				)
		);

		SimpleMatrix inplane = Maths.crossProduct(reflectionNphi, nphi);
		inplane.scale((Math.sqrt(1 - Math.pow(sc, 2)) * nphiFromHkl.normF()) / inplane.normF());

		SimpleMatrix reflectionNhkl = ub.invert().mult(reflectionNphi);
		HklModel hklRef = TypeConversion.millerIndicesToHklModel(Maths.columnVectorToList(reflectionNhkl));

		List<Double> offset = diffcalcContext.calculateVectorFromHklAndOffset(hklRef, Math.acos(sc), az);

		return Arrays.asList(offset);
	}

	@Override
	protected List<Double> hklToParameters(List<Double> hkl) {
		ReflectionResult reflection = null;
		SphericalCoordinates offset = null;

		reflection = diffcalcContext.getReflection(1);
		List<Double> reflectionHkl = reflection.hkl();

		offset = diffcalcContext.calculateOffsetFromVectorAndHkl(
				TypeConversion.millerIndicesToHklModel(hkl),
				TypeConversion.millerIndicesToHklModel(reflectionHkl)
			);



		return Arrays.asList(Math.cos(offset.getPolarAngle().doubleValue()), offset.getAzimuthAngle().doubleValue());
	}

}
