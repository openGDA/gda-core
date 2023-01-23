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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;

import gda.configuration.properties.LocalProperties;
import gda.util.exafs.Element;
import gda.util.OSCommandRunner;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.util.OSUtils;

/**
 * A class which calculates pressure in the ion chamber.
 * The default behaviour is to use XRayLib to provide the absorption coefficients used in the calculation;
 * in order to use this class with Mucal, set useMucal = true and make sure you have bin/mucal.bin in your configuration path.
 */
public class PressureCalculation {

	private static final Logger logger = LoggerFactory.getLogger(PressureCalculation.class);
	private static double maxAllowedPressure = 2.0;
	private static double minAllowedPressure = 0.001;
	private static boolean useMucal = false;

	private PressureCalculation() {
	}

	/** Run mucal code and return absorption coefficient for element of given atomic number at specified photon energy
	 * Refactored from PressureCalculation.getPressure(...) function.
	 *
	 * @param atomicNumber
	 * @param workingEnergyKev [keV]
	 * @return Absorption coefficient [cm2/g]
	 * @throws Exception
	 * @since 23/6/2016
	 */
	public static double getAbsorptionCoeffMucal( int atomicNumber, double workingEnergyKev ) throws Exception {

		if (!OSUtils.isLinuxOS())
			throw new Exception("PressureCalculation currently only runs on linux.");
		// If removing the isLinuxOS restriction, also remove it from DetectorParametersTest.java in plugin uk.ac.gda.client.exafs.test
		if (LocalProperties.getConfigDir()==null)
			throw new Exception("The 'gda.config' variable must be set and your configuration must have the 'bin/mucal.bin' file - see the I20 config.");

		String mucalExecutablePath  = LocalProperties.getConfigDir()+"/bin/mucal.bin";

		if (!(new File(mucalExecutablePath)).exists()) {
			String missingMucalMessage = "Could not find mucal code in expected location : "+mucalExecutablePath;
			logger.warn(missingMucalMessage);
			throw new Exception(missingMucalMessage);
		}

		String [] inputs = new String[]{ String.valueOf(workingEnergyKev), String.valueOf(atomicNumber) };
		logger.debug("Running mucal :  atomic number = {} , working energy = {} keV", atomicNumber, workingEnergyKev);

		OSCommandRunner osRunner = new OSCommandRunner(Arrays.asList(new String[]{mucalExecutablePath}), true, inputs, null);

		// When error there is only one line.
		List<String> commandOutput = osRunner.getOutputLines();
		if (commandOutput == null || !(commandOutput.size()>=13)) {
			logger.warn("The mucal code did not run correctly.");
		}
		if (osRunner.exception!=null)
			throw osRunner.exception;

		String gasAbLine = commandOutput.get(12).trim();

		double muA = Double.parseDouble(gasAbLine.split("\\s+")[2]);
		logger.debug("mucal absorption coefficient = {} cm2/g", muA);

		return muA;
	}
	/**
	 * Run mucal code and return gas pressure for given set of IonChamberParameters
	 *
	 * @param bean
	 * @return the pressure calculated, null if the software mucal is not available.
	 * @throws Exception
	 */
	public static PressureBean getPressure(final IonChamberParameters bean) throws Exception {
		if(!bean.isUseGasProperties())
			throw new Exception("Gas parameters are not available to calculate pressure");

		PressureBean ret = new PressureBean();

		if (bean.getPercentAbsorption()==null || bean.getIonChamberLength()==null) {
			ret.setErrorMessage("No values for absorption or chamber length have been set.");
			return ret;
		}

		double muA = getAbsorptionCoeff( getGasAtomicNumber(bean), getWorkingEnergyInKev(bean) );

		String symbol = bean.getGasType();
		double transmission    = 1-(bean.getPercentAbsorption())/100d;
		double chamberLength      = bean.getIonChamberLength(); // chamber length [cm]
		double pA = Double.NaN;
		double pB = Double.NaN;

		String[] errorMessageText = null;
		if ("He".equals(symbol)) {
			pA = -1d*Math.log(transmission)/(muA*chamberLength);
			errorMessageText = checkPressure(pA, muA, "He");
			if (pA<0)
				pA = 0;
			ret.setPressure(pA);
			logger.debug("Pressure : "+bean.getGasType()+" = "+pA+" bar" );

		} else {

			double muB = muA;
			muA = getAbsorptionCoeff( getHeliumAtomicNumber(), getWorkingEnergyInKev(bean) );
			Double pT  = bean.getTotalPressure(); // total pressure in ion chamber (bars)
			if (pT==null){
				ret.setErrorMessage("No value for total pressure has been set.");
				return ret;
			}
			double muT = -1*Math.log(transmission)/(chamberLength*pT); // total absorption coeff required to get user specified transmission

			/* Calculate density of specified gas (rhoB), from total absorption and density (muT, rhoT) and absorption coeff for each gas (muA, muB) :
			 	mu = absorption coefficient [cm2/g], rho = mass density [g/cm3]:
			 		 		muT*rhoT	= muA*rhoA + muB*rhoB
			 		sub. for rhoA using : rhoA = rhoT - rhoB (i.e. total mass density) :
				 						= muA*(rhoT-rhoB) + muB*rhoB
						rhoT*(muT- muA)	= rhoB*(muB - muA)
		 		->				   rhoB	= rhoT*(muT - muA)/ (muB - muA)
			*/

			pB = pT*(muT - muA)/(muB - muA);
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

			logger.debug("Pressures : {} = {} bar, He = {} bar", bean.getGasType(), pB, pA);

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
		if (p>maxAllowedPressure) {
			return new String[]{"The pressure of '"+gasSymbol+"' is "+p+" which may be too large.",
								"Pressures should be limited to "+maxAllowedPressure+" bar or less."};
		}
		if (p<minAllowedPressure) {
			return new String[]{"'"+gasSymbol+"' would have a pressure of less than "+1000*minAllowedPressure+" mbar.",
								"Pressures of less than 1 mbar give a low accuracy, try a different mix."};
		}
		return null;
	}

	private static double getWorkingEnergyInKev(final IonChamberParameters bean) {
		final Double value = bean.getWorkingEnergy();
		return (value/1000.0d);
	}

	private static int getGasAtomicNumber(final IonChamberParameters bean) {
		final String symbol = bean.getGasType();
		return Element.getElement(symbol).getAtomicNumber();
	}

	private static int getHeliumAtomicNumber() {
		return Element.getElement("He").getAtomicNumber();
	}

	/**
	 * Return value of absorption coefficient using functions XrayLib.
	 * @param atomicNumber
	 * @param energyKev
	 * @return absorption coefficient (cm2/g)
	 */
	public static double getAbsorptionCoeffXrayLib(int atomicNumber, double energyKev) {
		logger.debug("Running Xraylib to calculate absorption coefficient :  atomic number = {}, working energy = {} keV", atomicNumber, energyKev);
		double density = Xraylib.ElementDensity(atomicNumber);
		double total = Xraylib.CS_Total(atomicNumber, energyKev);
		double absorptionCoeff = density*total;
		logger.debug("Xraylib absorption coefficient = {} cm2/g", absorptionCoeff);
		return absorptionCoeff;
	}

	public static boolean isUseMucal() {
		return useMucal;
	}

	public static void setUseMucal(boolean useMucal) {
		PressureCalculation.useMucal = useMucal;
	}

	public static double getAbsorptionCoeff(int atomicNumber, double workingEnergyKev ) throws Exception {
		if (useMucal) {
			return getAbsorptionCoeffMucal(atomicNumber, workingEnergyKev);
		} else {
			return getAbsorptionCoeffXrayLib(atomicNumber, workingEnergyKev);
		}
	}

}
