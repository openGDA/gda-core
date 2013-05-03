/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import gda.util.Converter;
import gda.util.Element;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyArray;
import org.python.core.PyFloat;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import uk.ac.gda.beans.exafs.XasScanParameters;

/**
 * This class generates an Jython native tuple of energies to step through in an Exafs step scan. The result can be used
 * in a concurrent scan.
 * <p>
 * Has the option to provides steps of constant K in the exafs region.
 * <p>
 * TODO: provide the option to return appropriate time when using constants K steps.
 */
public class ExafsScanPointCreator {

	private Double initialEnergy;
	private Double aEnergy;
	private Double bEnergy;
	private Double cEnergy;
	private Double finalEnergy;

	private Double preEdgeStep;
	private Double edgeStep;
	private Double exafsStep;

	private Double preEdgeTime;
	private Double edgeTime;
	private Double exafsTime;

	private Double exafsFromTime;
	private Double exafsToTime;

	private Double edgeEnergy;

	private boolean exafsConstantEnergyStep = true;
	private boolean exafsConstantTime = true;

	private Integer numberDetectors = 1;
	private Double kWeighting;
	private ArrayList<ExafsScanRegionTime> scanTimes;


	/**
	 * Operates this class when a bean (xml file from the gui) has been configured. This gives an example of how this
	 * class should be driven in a more manual mode.
	 * 
	 * @param parameters
	 * @return PyTuple
	 * @throws Exception
	 */
	public static PyTuple calculateEnergies(XasScanParameters parameters) throws Exception {

		ExafsScanPointCreator creator = new ExafsScanPointCreator();
		setupScanPointCreator(parameters, creator);
		return creator.getEnergies();
	}
	
	public static ArrayList<ExafsScanRegionTime> getScanTimes(XasScanParameters parameters) throws Exception{
		ExafsScanPointCreator creator = new ExafsScanPointCreator();
		setupScanPointCreator(parameters, creator);
		creator.getEnergies();
		return creator.scanTimes;
	}

	public static Double[] getScanTimeArray(XasScanParameters parameters) throws Exception {
		ExafsScanPointCreator creator = new ExafsScanPointCreator();
		setupScanPointCreator(parameters, creator);
		return creator.getScanTimes();
	}

	private static void setupScanPointCreator(XasScanParameters parameters,ExafsScanPointCreator creator ) throws Exception
	{
		final Double[] abc = getABC(parameters);
		creator.setInitialEnergy(parameters.getInitialEnergy());
		creator.setaEnergy(abc[0]);
		creator.setbEnergy(abc[1]);
		creator.setcEnergy(abc[2]);
		creator.setFinalEnergy(parameters.getFinalEnergy());

		creator.setPreEdgeStep(parameters.getPreEdgeStep());
		creator.setEdgeStep(parameters.getEdgeStep());
		creator.setExafsStep(parameters.getExafsStep());

		creator.setPreEdgeTime(parameters.getPreEdgeTime());
		creator.setEdgeTime(parameters.getEdgeTime());
		creator.setExafsTime(parameters.getExafsTime());
		creator.setExafsToTime(parameters.getExafsToTime());
		creator.setExafsFromTime(parameters.getExafsFromTime());
		creator.setkWeighting(parameters.getKWeighting());
		// creator.setKStart(parameters.getKStart());

		// ksteps in exafs region?
		creator.setExafsConstantEnergyStep(parameters.getExafsStepType().equals("E"));
		// varying time in exafs region?
		creator.setExafsConstantTime(parameters.getExafsTime() != null);
		// set the edge Energy if it is null
		if (parameters.getEdgeEnergy() == null) {
			creator.setEdgeEnergy(Element.getElement(parameters.getElement()).getEdgeEnergy(parameters.getEdge()));
		}
		else
			creator.setEdgeEnergy(parameters.getEdgeEnergy());
	}
	
	private static Double[] getABC(XasScanParameters parameters) throws Exception {
		// define the steps in the edge region
		Double[] abc = null;
		try {
			if (parameters.isABGiven()) {
				abc = gda.exafs.scan.ExafsScanRegionCalculator.calculateABC(parameters.getElement(), parameters
						.getEdge(), parameters.getEdgeEnergy(), parameters.getA(), parameters.getB(),
						parameters.getC(), true);
			} else {
				abc = gda.exafs.scan.ExafsScanRegionCalculator.calculateABC(parameters.getElement(), parameters
						.getEdge(), parameters.getEdgeEnergy(), parameters.getGaf1(), parameters.getGaf2(), parameters
						.getGaf3(), false);
			}
			return abc;
		} catch (Exception e) {
			throw new Exception("Exception while calculating exafs data points: " + e.getMessage());
		}
	}

	public void setkWeighting(Double kWeighting) {
		this.kWeighting = kWeighting;
	}

	public ExafsScanPointCreator() {
	}

	/**
	 * @return an array of the energies this scan will step through
	 * @throws ExafsScanPointCreatorException
	 */
	private double[][] getScanEnergies() throws ExafsScanPointCreatorException {

		checkAllValuesEntered();

		checkAllValuesConsistent();

		return calculateValues();
	}

	/**
	 * @return a native Jython array of the energies this scan will step through
	 * @throws Exception
	 */
	public PyTuple getEnergies() throws Exception {
		return convert2DDoubleArray(getScanEnergies(), numberDetectors);
	}
	
	public Double[] getScanTimes() throws Exception {
		double[][] energies = getScanEnergies();
		Double[] times = new Double[energies.length];
		for (int i = 0; i < energies.length; i++){
			times[i] = energies[i][1];
		}
		return times;
	}

	protected static PyTuple convert2DDoubleArray(double[][] doublearray, int numDetectors) {

		PyObject[] floatarray = new PyObject[doublearray.length];

		for (int i = 0; i < doublearray.length; i++) {
			PyArray thisElement = new PyArray(PyFloat.class, numDetectors + 1);
			for (int j = 0; j < numDetectors + 1; j++) {
				thisElement.__setitem__(j, new PyFloat(doublearray[i][j]));
			}
			floatarray[i] = thisElement;
		}

		return new PyTuple(floatarray);

	}

	private double[][] calculateValues() throws ExafsScanPointCreatorException {

		// Rather than using two dimensional arrays, this method should use pojo objects and collections
		// this is much easier to deal with.
		scanTimes= new ArrayList<ExafsScanRegionTime> ();
		double[][] preEdgeEnergies = createStepArray(initialEnergy, aEnergy, preEdgeStep, preEdgeTime, false,
				numberDetectors);
		scanTimes.add(new ExafsScanRegionTime("PreEdge", preEdgeEnergies.length, new double[]{preEdgeTime}));
		double[][] abEnergies = convertABSteps(aEnergy, bEnergy, preEdgeStep, edgeStep, preEdgeTime);
		scanTimes.add(new ExafsScanRegionTime("AbEdge", abEnergies.length, new double[]{preEdgeTime}));
		
		double[][] bcEnergies = createStepArray(bEnergy+edgeStep, cEnergy, edgeStep, edgeTime, false, numberDetectors);
		scanTimes.add(new ExafsScanRegionTime("BcEnergy", bcEnergies.length, new double[]{edgeTime}));
		// if varying time the temporarily set the exafs time to a fixed value
		if (!exafsConstantTime) {
			exafsTime = exafsFromTime;
		}

		double[][] exafsEnergies;
		if (exafsConstantEnergyStep) {
			exafsEnergies = createStepArray(cEnergy, finalEnergy, exafsStep, exafsTime, true, numberDetectors);
		} else {
			exafsEnergies = calculateExafsEnergiesConstantKStep();
		}
		// now change all the exafs times if they vary
		if (!exafsConstantTime) {
			exafsTime = null;
			exafsEnergies = convertTimes(exafsEnergies, exafsFromTime, exafsToTime);
		}

		// Smooth out the transition between edge and exafs region.
		final double[][][] newRegions = createEdgeToExafsSteps(cEnergy, exafsEnergies, edgeStep, exafsTime);
		final double[][] edgeToExafsEnergies = newRegions[0];
		if(edgeToExafsEnergies != null)
		{
			scanTimes.add(new ExafsScanRegionTime("EdgetoExafs", edgeToExafsEnergies.length, new double[]{exafsTime}));
		}
		exafsEnergies = newRegions[1];

		// The edgeToExafsEnergies replaces the first EXAFS_SMOOTH_COUNT

		// merge arrays
		double []exafsRegionTimeArray = new double[exafsEnergies.length];
		int  k =0;
		for(double[] exafsEnergy : exafsEnergies)
		{
			exafsRegionTimeArray[k++ ] = exafsEnergy[1];
		}
		scanTimes.add(new ExafsScanRegionTime("Exafs", 1,  exafsRegionTimeArray));
		double[][] allEnergies = (double[][]) ArrayUtils.addAll(preEdgeEnergies, abEnergies);
		allEnergies = (double[][]) ArrayUtils.addAll(allEnergies, bcEnergies);
		if (edgeToExafsEnergies != null)
			allEnergies = (double[][]) ArrayUtils.addAll(allEnergies, edgeToExafsEnergies);
		allEnergies = (double[][]) ArrayUtils.addAll(allEnergies, exafsEnergies);
		return allEnergies;
	}

	private double[][] convertABSteps(Double aEnergy, Double bEnergy, Double preEdgeStep, Double edgeStep,
			Double stepTime) throws ExafsScanPointCreatorException {

		// double[][] abEnergies = createStepArray(aEnergy, bEnergy, preEdgeStep, stepTime, false);
		double[] steps = ExafsScanRegionCalculator.calculateVariableStepRegion(aEnergy, bEnergy, preEdgeStep, edgeStep);

		// // replace steps in abEnergies with those from steps
		// for (int i = 0; i < abEnergies.length; i++) {
		// abEnergies[i+1][0] = steps[i];
		// }

		return createArrayFromEnergySteps(aEnergy, steps, stepTime);
	}

	private double[][] createArrayFromEnergySteps(double a, double[] steps, Double stepTime) {

		double[][] array = new double[steps.length + 1][numberDetectors + 1];

		array[0] = createElement(a, stepTime, numberDetectors);

		for (int i = 0; i < steps.length; i++) {
			array[i + 1] = createElement(steps[i], stepTime, numberDetectors);
		}

		return array;
	}

	private static final int EXAFS_SMOOTH_COUNT = 10;

	private double[][][] createEdgeToExafsSteps(final Double cEnergy, double[][] exafsEnergies, final Double edgeStep,
			final Double stepTime) throws ExafsScanPointCreatorException {

		// If too few points in exafs, we do nothing
		if (exafsEnergies.length < EXAFS_SMOOTH_COUNT) {
			return new double[][][] { null, exafsEnergies };
		}

		int i = 0;
		double kStep = exafsEnergies[i + 1][0] - exafsEnergies[i][0];
		int num = dn(cEnergy, exafsEnergies[i][0], edgeStep, kStep);

		while (num < EXAFS_SMOOTH_COUNT) {
			num = dn(cEnergy, exafsEnergies[i][0], edgeStep, kStep);
			if (num > EXAFS_SMOOTH_COUNT)
				break;
			kStep = exafsEnergies[i + 1][0] - exafsEnergies[i][0];
			i += 1;
		}

		double[] steps = ExafsScanRegionCalculator.calculateVariableStepRegion(cEnergy, exafsEnergies[i][0], edgeStep,
				kStep);

		exafsEnergies = (double[][]) ArrayUtils.subarray(exafsEnergies, i + 1, exafsEnergies.length);

		return new double[][][] { createArrayFromEnergySteps(cEnergy, steps, stepTime), exafsEnergies };

	}

	/**
	 * Gets the energy after which k is considered constant
	 * 
	 * @param scanParametersOrXanesParameters
	 * @return energy Value
	 * @throws Exception
	 */
	public static double getStartOfConstantKRegion(Object scanParametersOrXanesParameters) throws Exception {

		if (scanParametersOrXanesParameters instanceof XasScanParameters) {
			final Double[] abc = getABC((XasScanParameters) scanParametersOrXanesParameters);
			final Double energyStep = ((XasScanParameters) scanParametersOrXanesParameters).getExafsStep();

			double kStart = abc[2];
			for (int i = 0; i < EXAFS_SMOOTH_COUNT; i++)
				kStart += energyStep;

			return kStart;
		}

		throw new Exception(scanParametersOrXanesParameters.getClass().getName() + " is not yet supported!");
	}

	private static int dn(final double a, final double b, final double aStep, final double bStep) {
		return ((int) ((2.0 * (b - a)) / (aStep + bStep))) + 1;
	}

	private double[][] convertTimes(double[][] exafsEnergies, Double fromTime, Double toTime) {
		// loop over the exafsEnergies array and change each time element to a number varying smootly from exafsFromTime
		// to exafsToTime
		/*
		 * double timeStep = (toTime - fromTime) / (exafsEnergies.length - 1); for (int i = 0; i < exafsEnergies.length;
		 * i++) { for (int j = 1; j <= numberDetectors; j++) { exafsEnergies[i][j] = fromTime + (timeStep * i); } }
		 */
		// Calculating the weighted time. when kweighting is 1, the system is linear
		// for a cubic system use kweighting = 2 or 3.
		double start = exafsEnergies[0][0];
		double end = exafsEnergies[(exafsEnergies.length - 1)][0];
		double currentEnergy = start;
		double a = 0.0;
		double b = Math.pow(end - start, kWeighting);
		double c = toTime - fromTime;
		double time = 0.0;
		for (int i = 0; i < exafsEnergies.length; i++) {
			currentEnergy = exafsEnergies[i][0];
			for (int j = 1; j <= numberDetectors; j++) {
				a = Math.pow(currentEnergy - start, kWeighting);
				time = fromTime + (a * c) / b;
				exafsEnergies[i][j] = time;
			}

		}
		return exafsEnergies;
	}
	
	private double[][] calculateExafsEnergiesConstantKStep() {
		// so want to loop from edgeRegionHighEnergy to finalEnergy in k steps of size exafsStep

		double lowK = evToK(cEnergy);
		double highK = evToK(finalEnergy);
		// this.kStart = lowK;

		double[][] kArray = createStepArray(lowK, highK, exafsStep, exafsTime, true, numberDetectors);

		// convert from k to energy in each element
		for (int i = 0; i < kArray.length; i++) {
			kArray[i][0] = kToEv(kArray[i][0]);
		}

		return kArray;
	}

	/**
	 * evForK converts energy in eV in k value (in inverse angstroms)
	 * 
	 * @param energy
	 *            energy to convert
	 * @return k in inverse Angstroms
	 */
	private double evToK(double energy) {
		//This uses 7% of CPU in quick xafs
		//double val = 0.512316746*Math.sqrt(energy-edgeEnergy);    
		//return val;
		Converter.setEdgeEnergy(edgeEnergy / 1000.0);
		return Converter.convert(energy, Converter.EV, Converter.PERANGSTROM);
	}

	/**
	 * kToEv converts energy in k to a value in ev
	 * 
	 * @param value
	 *            value to convert
	 * @return energy in eV
	 */
	private double kToEv(double value) {
		//This uses 7% of CPU in quick xafs
		//double val =  edgeEnergy+((value*value)/0.512316746);  
		//return val;
		Converter.setEdgeEnergy(edgeEnergy / 1000.0);
		return Converter.convert(value, Converter.PERANGSTROM, Converter.EV);
	}

	// /**
	// * timeForK calculates the appropriate counting time for a particular k value
	// *
	// * @param k
	// * double
	// * @return double
	// */
	// private double timeForK(double k) {
	// double a = Math.pow(k - start, kWeighting);
	// double b = Math.pow(stop - start, kWeighting);
	// double c = (kEndTime - kStartTime);
	// double time = kStartTime + (a * c) / b;
	// return time;
	// }

	protected static double[][] createStepArray(double low, double high, double step, double time,
			boolean ensureUseHighEnergy, int numDetectors) {
		Long numSteps = Math.round((high - low) / step);

		if (numSteps == 0) {
			return new double[][] { createElement(low, time, numDetectors) };
		}

		double[][] array = new double[numSteps.intValue()][numDetectors + 1];

		for (int i = 0; i < numSteps; i++) {
			array[i] = createElement(low + i * step, time, numDetectors);
		}

		// room for one more?
		if (array[array.length - 1][0] + step < high) {
			double[][] extraarray = new double[1][numDetectors + 1];
			extraarray[0] = createElement(array[array.length - 1][0] + step, time, numDetectors);
			array = (double[][]) ArrayUtils.addAll(array, extraarray);
		}

		if (ensureUseHighEnergy && array[array.length - 1][0] != high) {
			double[][] extraarray = new double[1][numDetectors + 1];
			extraarray[0] = createElement(high, time, numDetectors);
			array = (double[][]) ArrayUtils.addAll(array, extraarray);
		}

		return array;
	}

	private static double[] createElement(double energy, double time, int numDetectors) {
		double[] element = new double[numDetectors + 1];
		element[0] = energy;
		for (int j = 1; j < numDetectors + 1; j++) {
			element[j] = time;
		}
		return element;
	}

	private void checkAllValuesEntered() throws ExafsScanPointCreatorException {

		final double aNearest = getNearestAEnergy();
		setaEnergy(aNearest);

		if (initialEnergy == null) {
			throw new ExafsScanPointCreatorException("initialEnergy not set");
		}
		if (aEnergy == null) {
			throw new ExafsScanPointCreatorException("A/Gaf1 Energy not set");
		}
		if (bEnergy == null) {
			throw new ExafsScanPointCreatorException("B/Gaf2 Energy not set");
		}
		if (preEdgeStep == null) {
			throw new ExafsScanPointCreatorException("preEdgeStep not set");
		}
		if (preEdgeTime == null) {
			throw new ExafsScanPointCreatorException("preEdgeTime not set");
		}
		if (cEnergy == null) {
			throw new ExafsScanPointCreatorException("cEnergy not set");
		}
		if (edgeStep == null) {
			throw new ExafsScanPointCreatorException("edgeStep not set");
		}
		if (edgeTime == null) {
			throw new ExafsScanPointCreatorException("edgeTime not set");
		}
		if (finalEnergy == null) {
			throw new ExafsScanPointCreatorException("finalEnergy not set");
		}
		if (exafsStep == null) {
			throw new ExafsScanPointCreatorException("exafsStep not set");
		}
		if (exafsConstantTime && exafsTime == null) {
			throw new ExafsScanPointCreatorException("exafsTime not set");
		} else if (!exafsConstantTime && (exafsFromTime == null || exafsToTime == null)) {
			throw new ExafsScanPointCreatorException("exafsFromTime and exafsToTime need to be set if varying exafs time to be used");
		}
		if (numberDetectors == null) {
			throw new ExafsScanPointCreatorException("numberDetectors not set");
		}

		if (!exafsConstantEnergyStep && edgeEnergy == null) {
			throw new ExafsScanPointCreatorException("edgeEnergy not set when doing constant K in exafs region");
		}

	}

	private double getNearestAEnergy() {

		// We try to calculate the nearest point to A which fits the step sizes
		double value = getInitialEnergy();
		double ret = value;
		while (value < getaEnergy()) {
			double step = getPreEdgeStep();
			// avoid infinite loop
			if (step <= 0.0){
				step = 1.0;
			}
			value += step;
			if (value > getaEnergy())
				break;
			ret = value;
		}
		return ret;
	}

	private void checkAllValuesConsistent() throws ExafsScanPointCreatorException {
		if (initialEnergy > aEnergy) {
			throw new ExafsScanPointCreatorException("initialEnergy higher than edgeRegionLowEnergy");
		}
		if (cEnergy < aEnergy) {
			throw new ExafsScanPointCreatorException("edgeRegionLowEnergy higher than edgeRegionHighEnergy");
		}
		if (cEnergy > finalEnergy) {
			throw new ExafsScanPointCreatorException("edgeRegionHighEnergy higher than finalEnergy");
		}
		if (preEdgeStep > aEnergy - initialEnergy) {
			throw new ExafsScanPointCreatorException("preEdgeStep too big");
		}
		if (edgeStep > cEnergy - aEnergy) {
			throw new ExafsScanPointCreatorException("edgeStep too big");
		}
		if (exafsStep > finalEnergy - cEnergy) {
			throw new ExafsScanPointCreatorException("exafsStep too big");
		}
	}

	/**
	 * @return Returns the initialEnergy.
	 */
	public double getInitialEnergy() {
		return initialEnergy;
	}

	/**
	 * @param initialEnergy
	 *            The initialEnergy to set.
	 */
	public void setInitialEnergy(double initialEnergy) {
		this.initialEnergy = initialEnergy;
	}

	/**
	 * @return Returns the preEdgeStep.
	 */
	public double getPreEdgeStep() {
		return preEdgeStep;
	}

	/**
	 * @param preEdgeStep
	 *            The preEdgeStep to set.
	 */
	public void setPreEdgeStep(double preEdgeStep) {
		this.preEdgeStep = preEdgeStep;
	}

	/**
	 * @return Returns the cEnergy.
	 */
	public double getcEnergy() {
		return cEnergy;
	}

	/**
	 * @param cEnergy
	 *            The cEnergy to set.
	 */
	public void setcEnergy(double cEnergy) {
		this.cEnergy = cEnergy;
	}

	/**
	 * @return Returns the edgeStep.
	 */
	public double getEdgeStep() {
		return edgeStep;
	}

	/**
	 * @param edgeStep
	 *            The edgeStep to set.
	 */
	public void setEdgeStep(double edgeStep) {
		this.edgeStep = edgeStep;
	}

	/**
	 * @return Returns the finalEnergy.
	 */
	public double getFinalEnergy() {
		return finalEnergy;
	}

	/**
	 * @param finalEnergy
	 *            The finalEnergy to set.
	 */
	public void setFinalEnergy(double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}

	/**
	 * @return Returns the exafsStep.
	 */
	public double getExafsStep() {
		return exafsStep;
	}

	/**
	 * @param exafsStep
	 *            The exafsStep to set.
	 */
	public void setExafsStep(double exafsStep) {
		this.exafsStep = exafsStep;
	}

	/**
	 * @return Returns the exafsConstantEnergyStep.
	 */
	public boolean isExafsConstantEnergyStep() {
		return exafsConstantEnergyStep;
	}

	/**
	 * @param exafsConstantEnergyStep
	 *            - if false then exfas region will be in steps of constant k
	 */
	public void setExafsConstantEnergyStep(boolean exafsConstantEnergyStep) {
		this.exafsConstantEnergyStep = exafsConstantEnergyStep;
	}

	/**
	 * @param edgeEnergy
	 *            The edgeEnergy to set.
	 */
	public void setEdgeEnergy(Double edgeEnergy) {
		this.edgeEnergy = edgeEnergy;
	}

	/**
	 * @return Returns the edgeEnergy.
	 */
	public Double getEdgeEnergy() {
		return edgeEnergy;
	}

	/**
	 * @return Returns the preEdgeTime.
	 */
	public Double getPreEdgeTime() {
		return preEdgeTime;
	}

	/**
	 * @param preEdgeTime
	 *            The preEdgeTime to set.
	 */
	public void setPreEdgeTime(Double preEdgeTime) {
		this.preEdgeTime = preEdgeTime;
	}

	/**
	 * @return Returns the edgeTime.
	 */
	public Double getEdgeTime() {
		return edgeTime;
	}

	/**
	 * @param edgeTime
	 *            The edgeTime to set.
	 */
	public void setEdgeTime(Double edgeTime) {
		this.edgeTime = edgeTime;
	}

	/**
	 * @return Returns the exafsTime.
	 */
	public Double getExafsTime() {
		return exafsTime;
	}

	/**
	 * @param exafsTime
	 *            The exafsTime to set.
	 */
	public void setExafsTime(Double exafsTime) {
		this.exafsTime = exafsTime;
	}

	/**
	 * @param numberDetectors
	 *            The numberDetectors to set.
	 */
	public void setNumberDetectors(int numberDetectors) {
		this.numberDetectors = numberDetectors;
	}

	/**
	 * @return Returns the numberDetectors.
	 */
	public int getNumberDetectors() {
		return numberDetectors;
	}

	/**
	 * @return Returns the exafsConstantTime.
	 */
	public boolean isExafsConstantTime() {
		return exafsConstantTime;
	}

	/**
	 * @param exafsConstantTime
	 *            The exafsConstantTime to set.
	 */
	public void setExafsConstantTime(boolean exafsConstantTime) {
		this.exafsConstantTime = exafsConstantTime;
	}

	/**
	 * @return Returns the exafsFromTime.
	 */
	public Double getExafsFromTime() {
		return exafsFromTime;
	}

	/**
	 * @param exafsFromTime
	 *            The exafsFromTime to set.
	 */
	public void setExafsFromTime(Double exafsFromTime) {
		this.exafsFromTime = exafsFromTime;
	}

	/**
	 * @return Returns the exafsToTime.
	 */
	public Double getExafsToTime() {
		return exafsToTime;
	}

	/**
	 * @param exafsToTime
	 *            The exafsToTime to set.
	 */
	public void setExafsToTime(Double exafsToTime) {
		this.exafsToTime = exafsToTime;
	}

	/**
	 * @param initialEnergy
	 *            The initialEnergy to set.
	 */
	public void setInitialEnergy(Double initialEnergy) {
		this.initialEnergy = initialEnergy;
	}

	/**
	 * @param edgeRegionLowEnergy
	 *            The edgeRegionLowEnergy to set.
	 */
	public void setEdgeRegionLowEnergy(Double edgeRegionLowEnergy) {
		this.aEnergy = edgeRegionLowEnergy;
	}

	/**
	 * @param preEdgeStep
	 *            The preEdgeStep to set.
	 */
	public void setPreEdgeStep(Double preEdgeStep) {
		this.preEdgeStep = preEdgeStep;
	}

	/**
	 * @param edgeRegionHighEnergy
	 *            The edgeRegionHighEnergy to set.
	 */
	public void setEdgeRegionHighEnergy(Double edgeRegionHighEnergy) {
		this.cEnergy = edgeRegionHighEnergy;
	}

	/**
	 * @param edgeStep
	 *            The edgeStep to set.
	 */
	public void setEdgeStep(Double edgeStep) {
		this.edgeStep = edgeStep;
	}

	/**
	 * @param finalEnergy
	 *            The finalEnergy to set.
	 */
	public void setFinalEnergy(Double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}

	/**
	 * @param exafsStep
	 *            The exafsStep to set.
	 */
	public void setExafsStep(Double exafsStep) {
		this.exafsStep = exafsStep;
	}

	/**
	 * @param numberDetectors
	 *            The numberDetectors to set.
	 */
	public void setNumberDetectors(Integer numberDetectors) {
		this.numberDetectors = numberDetectors;
	}

	/**
	 * @return Returns the aEnergy.
	 */
	public Double getaEnergy() {
		return aEnergy;
	}

	/**
	 * @param aEnergy
	 *            The aEnergy to set.
	 */
	public void setaEnergy(Double aEnergy) {
		this.aEnergy = aEnergy;
	}

	/**
	 * @return Returns the bEnergy.
	 */
	public Double getbEnergy() {
		return bEnergy;
	}

	/**
	 * @param bEnergy
	 *            The bEnergy to set.
	 */
	public void setbEnergy(Double bEnergy) {
		this.bEnergy = bEnergy;
	}

	@Override
	public String toString() {
		String string = "initialEnergy " + initialEnergy + " steps of " + preEdgeStep + " to edgeRegionLowEnergy "
				+ aEnergy + " then varying steps to " + bEnergy + " then steps of " + edgeStep
				+ " to edgeRegionHighEnergy " + cEnergy;

		if (exafsConstantEnergyStep) {
			string += " then steps of " + exafsStep + "  to finalEnergy " + finalEnergy;
		} else {
			string += " then steps of constant K to finalEnergy " + finalEnergy;
		}

		if (!exafsConstantTime && exafsFromTime != null && exafsToTime != null) {
			string += " with time ranging from " + exafsFromTime + " to " + exafsToTime;
		}

		return string;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aEnergy == null) ? 0 : aEnergy.hashCode());
		result = prime * result + ((bEnergy == null) ? 0 : bEnergy.hashCode());
		result = prime * result + ((edgeEnergy == null) ? 0 : edgeEnergy.hashCode());
		result = prime * result + ((cEnergy == null) ? 0 : cEnergy.hashCode());
		result = prime * result + ((edgeStep == null) ? 0 : edgeStep.hashCode());
		result = prime * result + ((edgeTime == null) ? 0 : edgeTime.hashCode());
		result = prime * result + (exafsConstantEnergyStep ? 1231 : 1237);
		result = prime * result + (exafsConstantTime ? 1231 : 1237);
		result = prime * result + ((exafsFromTime == null) ? 0 : exafsFromTime.hashCode());
		result = prime * result + ((exafsStep == null) ? 0 : exafsStep.hashCode());
		result = prime * result + ((exafsTime == null) ? 0 : exafsTime.hashCode());
		result = prime * result + ((exafsToTime == null) ? 0 : exafsToTime.hashCode());
		result = prime * result + ((finalEnergy == null) ? 0 : finalEnergy.hashCode());
		result = prime * result + ((initialEnergy == null) ? 0 : initialEnergy.hashCode());
		result = prime * result + ((numberDetectors == null) ? 0 : numberDetectors.hashCode());
		result = prime * result + ((preEdgeStep == null) ? 0 : preEdgeStep.hashCode());
		result = prime * result + ((preEdgeTime == null) ? 0 : preEdgeTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExafsScanPointCreator other = (ExafsScanPointCreator) obj;
		if (aEnergy == null) {
			if (other.aEnergy != null)
				return false;
		} else if (!aEnergy.equals(other.aEnergy))
			return false;
		if (bEnergy == null) {
			if (other.bEnergy != null)
				return false;
		} else if (!bEnergy.equals(other.bEnergy))
			return false;
		if (edgeEnergy == null) {
			if (other.edgeEnergy != null)
				return false;
		} else if (!edgeEnergy.equals(other.edgeEnergy))
			return false;
		if (cEnergy == null) {
			if (other.cEnergy != null)
				return false;
		} else if (!cEnergy.equals(other.cEnergy))
			return false;
		if (edgeStep == null) {
			if (other.edgeStep != null)
				return false;
		} else if (!edgeStep.equals(other.edgeStep))
			return false;
		if (edgeTime == null) {
			if (other.edgeTime != null)
				return false;
		} else if (!edgeTime.equals(other.edgeTime))
			return false;
		if (exafsConstantEnergyStep != other.exafsConstantEnergyStep)
			return false;
		if (exafsConstantTime != other.exafsConstantTime)
			return false;
		if (exafsFromTime == null) {
			if (other.exafsFromTime != null)
				return false;
		} else if (!exafsFromTime.equals(other.exafsFromTime))
			return false;
		if (exafsStep == null) {
			if (other.exafsStep != null)
				return false;
		} else if (!exafsStep.equals(other.exafsStep))
			return false;
		if (exafsTime == null) {
			if (other.exafsTime != null)
				return false;
		} else if (!exafsTime.equals(other.exafsTime))
			return false;
		if (exafsToTime == null) {
			if (other.exafsToTime != null)
				return false;
		} else if (!exafsToTime.equals(other.exafsToTime))
			return false;
		if (finalEnergy == null) {
			if (other.finalEnergy != null)
				return false;
		} else if (!finalEnergy.equals(other.finalEnergy))
			return false;
		if (initialEnergy == null) {
			if (other.initialEnergy != null)
				return false;
		} else if (!initialEnergy.equals(other.initialEnergy))
			return false;
		if (numberDetectors == null) {
			if (other.numberDetectors != null)
				return false;
		} else if (!numberDetectors.equals(other.numberDetectors))
			return false;
		if (preEdgeStep == null) {
			if (other.preEdgeStep != null)
				return false;
		} else if (!preEdgeStep.equals(other.preEdgeStep))
			return false;
		if (preEdgeTime == null) {
			if (other.preEdgeTime != null)
				return false;
		} else if (!preEdgeTime.equals(other.preEdgeTime))
			return false;
		return true;
	}
}
