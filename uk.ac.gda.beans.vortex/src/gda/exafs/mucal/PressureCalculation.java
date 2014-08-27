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

package gda.exafs.mucal;

import gda.configuration.properties.LocalProperties;
import gda.util.Element;
import gda.util.OSCommandRunner;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.util.OSUtils;

/**
 * A class which calculates pressure in the ion chamber.
 * 
 * In order to use this class, you must have bin/mucal.bin in your configuration.
 */
public class PressureCalculation {

	private final static Logger logger = LoggerFactory.getLogger(PressureCalculation.class);
	
	/**
	 * 
	 * @param bean 
	 * @return the pressure calculated, null if the software mucal is not available.
	 * @throws Exception 
	 */
	public static PressureBean getPressure(final IonChamberParameters bean) throws Exception {
		if(!bean.isUseGasProperties())
			throw new Exception("Gas parameters are not available to calculate pressure");
		if (!OSUtils.isLinuxOS()) 
			throw new Exception("PressureCalculation currently only runs on linux.");
		// If removing the isLinuxOS restriction, also remove it from DetectorParametersTest.java in plugin uk.ac.gda.client.exafs.test
		if (LocalProperties.getConfigDir()==null) 
			throw new Exception("The 'gda.config' variable must be set and your configuration must have the 'bin/mucal.bin' file - see the I20 config.");
		
		PressureBean ret = new PressureBean();
		
		// First run mucal to get the absorption.
		String mucal  = LocalProperties.getConfigDir()+"/bin/mucal.bin";
		if (!(new File(mucal)).exists()) 
			return null;
		
		String [] inputs = new String[]{getWorkingEnergyInKev(bean), getGasAtomicNumber(bean)};
		OSCommandRunner osRunner = new OSCommandRunner(Arrays.asList(new String[]{mucal}), true, inputs, null);

		// When error there is only one line.
		if (osRunner.getOutputLines() == null || !(osRunner.getOutputLines().size()>=13)) {
			logger.warn("The mucal code did not run on this operating system. Cannot validate pressure values from bean.");
			return ret;
		}
		String gasAbLine = osRunner.getOutputLines().get(12);
		
		double muA = Double.parseDouble(gasAbLine.split("    ")[1]);

		if (osRunner.exception!=null) throw osRunner.exception;
		logger.debug("The gas absorption is '"+muA+"'.");

		String symbol = bean.getGasType();
		double ABS    = 1-(bean.getPercentAbsorption())/100d;
		double t      = bean.getIonChamberLength();
		double pA = Double.NaN;
		double pB = Double.NaN;

		String[] errorMessageText = null; 
		if ("He".equals(symbol)) {
			pA = -1d*Math.log(ABS)/(muA*t);
			errorMessageText = checkPressure(pA, muA, "He");
			if (pA<0) 
				pA = 0;
			ret.setPressure(pA);
		} else {
			inputs    = new String[]{getWorkingEnergyInKev(bean), getHeliumAtomicNumber()};
			osRunner  = new OSCommandRunner(Arrays.asList(new String[]{mucal}), true, inputs, null);
			if (osRunner.exception!=null) 
				throw osRunner.exception;
			gasAbLine = osRunner.getOutputLines().get(12);		

			double muB = muA;
			muA = Double.parseDouble(gasAbLine.split("    ")[1]);
			double pT  = bean.getTotalPressure();
			double muT = -1*Math.log(ABS)/t;

			pB = (muT- (muA*pT)) / (muB - muA);
			pA = pT-pB;

			if (Math.abs(muB-muA)<Double.parseDouble("1e-7"))
				errorMessageText = new String[]{"Cannot calculate gas pressure. Suggest using a single gas system.",null};

			double ad = Math.exp(-(muA*pA)-(muB*pB));
			if (errorMessageText==null) 
				errorMessageText = checkPressure(pB, ad, bean.getGasType());
			if (errorMessageText==null) 
				errorMessageText = checkPressure(pA, ad, "He");

			if (pB<0) 
				pB = 0;
			ret.setPressure(pB);
		}

		if (errorMessageText!=null) {
			ret.setErrorMessage(errorMessageText[0]);
			ret.setErrorTooltip(errorMessageText[1]);
		} 
		
		return ret;
	}

	
	private static String[] checkPressure(double p, double ad, String gasSymbol) {
		if (gasSymbol.equals("N"))
			gasSymbol = "N\u2082";
		if (p>2) {
			return new String[]{"The required absorption cannot be obtained with the present gas choice.",
								"The pressure of '"+gasSymbol+"' is "+p+" which is too large, as Pressure is limited to 2 bar.",
								"Absorption will be: "+ad+"."};
		}
		if (p<0.001) {
			return new String[]{"'"+gasSymbol+"' would have a pressure of less than 1 mbar.",
								"Pressures of less than 1 mbar give a low accuracy, try a different mix."};
		}
		return null;
	}

	private static String getWorkingEnergyInKev(final IonChamberParameters bean) {
		final Double value = bean.getWorkingEnergy();
		return ""+(value/1000.0d);
	}
	
	private static String getGasAtomicNumber(final IonChamberParameters bean) {
		final String symbol = bean.getGasType();
		return Element.getElement(symbol).getAtomicNumber()+"";
	}
	
	private static String getHeliumAtomicNumber() {
		return Element.getElement("He").getAtomicNumber()+"";
	}

}
