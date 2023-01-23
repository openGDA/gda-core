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

package gda.exafs.mucal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;

@Ignore
public class PressureCalculationTest {

	/**
	 * Generate tables of absorption coefficients for XrayLib and Mucal so they can be easily compared.
	 * This is intended to be run manually if needed rather than as a Jenkins unit test.
	 * Mucal binary used here is built for RHEL7 and *not* backwards compatible with RHEL6 (due to dependency on libquadmath)
	 * @throws Exception
	 */
	@Test
	public void testAbsorptionCoeff() throws Exception {
		// Set the gda.config path so the mucal binary can be found
		String pathToSrcDir = Paths.get(System.getProperty("user.dir"), "src/gda/exafs/mucal").toString();
		LocalProperties.set(LocalProperties.GDA_CONFIG, pathToSrcDir);

		String pathOutputDir = Paths.get(System.getProperty("user.dir"), "test-scratch/gda/exafs/mucal").toString();
		for (int atomicNumber = 2; atomicNumber < 50; atomicNumber += 2) {
			writeTable(atomicNumber, pathOutputDir + "/mu-values-z" + atomicNumber + ".dat");
		}
	}

	private void writeTable(int atomicNumber, String outputFile) throws Exception {
		StringBuilder builder = createTable(atomicNumber);
		File parentDir = Paths.get(outputFile).getParent().toFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			throw new FileNotFoundException("Output directory " + parentDir.toString() + " does not exist and could not be created");
		}

		try(BufferedWriter bufWriter = new BufferedWriter(new FileWriter(outputFile))) {
			bufWriter.write(builder.toString());
		}
	}

	private StringBuilder createTable(int atomicNumber) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("# Absorption coefficient values from Mucal and Xraylib (in cm2/g)"+
				"\n#Atomic number = "+atomicNumber+
				"\n#Energy [keV]\tMucal\tXrayLib\n");

		for(double energyKev = 0.5; energyKev <= 20; energyKev+= 0.1) {
			builder.append(String.format("%.3e\t%.5g\t%.5g\n", energyKev, PressureCalculation.getAbsorptionCoeffMucal(atomicNumber, energyKev),
					PressureCalculation.getAbsorptionCoeffXrayLib(atomicNumber, energyKev)));
		}
		return builder;
	}

}
