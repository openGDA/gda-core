/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import uk.ac.gda.util.CorrectionUtils;
import uk.ac.gda.util.number.DoubleUtils;

/**
 * This class uses vortex data corrected on I18 (from scripts) to test the 
 * Java implementation of the dead time correction.
 */
public class CorrectionTest {

	@Test
	public void testCorrectionFactorCalculation() throws Exception {
		
		final File data =  new File("testfiles/gda/util/CorrectionTest/43490.dat");
		final File corr =  new File("testfiles/gda/util/CorrectionTest/43490_corr.dat");
		
		final BufferedReader dat = new BufferedReader(new FileReader(data));
		final BufferedReader cor = new BufferedReader(new FileReader(corr));
		
		double[] fit = new double[4];
		fit[0] = 1.1029752060937018e-007;
		fit[1] = 1.1407794527246737e-007;
		fit[2] = 1.1465765791909203e-007;
		fit[3] = 1.0675602460939456e-007;

		try {
			String line = null;
			while((line = dat.readLine())!=null) {
				
				// Line split is:
				// Angle, energy, time , i0 ,It , Idrain, window1, window2, window3, window4, windowSum, ICR1,ICR2,ICR3,ICR4,OCR1,OCR2,OCR3,OCR4
				final String[] d = line.split(" ");
				if (d.length!=19) throw new Exception("Expected 19 columns in the data!");
				
				final double[]k = new double[4];
				for (int i = 0; i < k.length; i++) {
					final double ffr = Double.parseDouble(d[11+i]);
					final double sfr = Double.parseDouble(d[15+i]);
					k[i] = CorrectionUtils.getK(fit[i], ffr, sfr);
				}
				
				final String correctedLine = cor.readLine();
				// correctedLine split is:
				// Angle, energy, time , i0 ,It , Idrain, window1, window2, window3, window4, windowSum
				final String[] c = correctedLine.split(" ");
				for (int i = 0; i < k.length; i++) {
                    final double windowOrig = Double.parseDouble(d[6+i]);
                    if (DoubleUtils.equalsWithinTolerance(windowOrig*k[i], Double.parseDouble(c[6+i]), 0.0001)) {
                    	throw new Exception("The correction factor '"+k[i]+"' when applied to '"+windowOrig+"' gives '"+windowOrig*k[i]+"' not '"+Double.parseDouble(c[6+i])+"'");
                    }
				}
			
			}
			
		} finally {
			dat.close();
			cor.close();
		}
	}
}
