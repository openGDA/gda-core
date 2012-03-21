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

package gda.exafs.scan;
import gda.util.Converter;
import gda.util.Element;
import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.XasScanParameters;


import gda.util.Formatter;
import gda.util.XafsRegion;

/**
 * This class generates an Jython native tuple of energies to step through in an Exafs step scan(I18 method).
 */

public class ExafsScanPointCreator2 {
	public ExafsScanPointCreator2() {
		super();
	}

	private double edgeEnergy;
	private XafsRegion preEdgeRegion;
	private double kStart;
	private String internalUnits=Converter.EV;
	private String externalUnits = Converter.KEV;
	public String getInternalUnits() {
		return internalUnits;
	}

	public void setInternalUnits(String internalUnits) {
		this.internalUnits = internalUnits;
	}

	public String getExternalUnits() {
		return externalUnits;
	}

	public void setExternalUnits(String externalUnits) {
		this.externalUnits = externalUnits;
	}

	
	public XafsRegion getPreEdgeRegion() {
		return preEdgeRegion;
	}

	public void setPreEdgeRegion(XafsRegion preEdgeRegion) {
		this.preEdgeRegion = preEdgeRegion;
	}

	public double getkStart() {
		return kStart;
	}

	public void setkStart(double kStart) {
		this.kStart = kStart;
	}

	public double getSmallestAllowedStep() {
		return smallestAllowedStep;
	}

	public void setSmallestAllowedStep(double smallestAllowedStep) {
		this.smallestAllowedStep = smallestAllowedStep;
	}

	public double getkStep() {
		return kStep;
	}

	public void setkStep(double kStep) {
		this.kStep = kStep;
	}

	public double getkEnd() {
		return kEnd;
	}

	public void setkEnd(double kEnd) {
		this.kEnd = kEnd;
	}

	public XafsRegion getEdgeRegion() {
		return edgeRegion;
	}

	public void setEdgeRegion(XafsRegion edgeRegion) {
		this.edgeRegion = edgeRegion;
	}

	public double getkStartTime() {
		return kStartTime;
	}

	public void setkStartTime(double kStartTime) {
		this.kStartTime = kStartTime;
	}

	public double getkEndTime() {
		return kEndTime;
	}

	public void setkEndTime(double kEndTime) {
		this.kEndTime = kEndTime;
	}

	public int getnKPoints() {
		return nKPoints;
	}

	public void setnKPoints(int nKPoints) {
		this.nKPoints = nKPoints;
	}

	private double smallestAllowedStep;
	private double kStep;
	private double kEnd;
	private XafsRegion edgeRegion;
	private double kStartTime;
	private double kEndTime;
	private int kWeighting = 1;
	public int getkWeighting() {
		return kWeighting;
	}

	public void setkWeighting(int kWeighting) {
		this.kWeighting = kWeighting;
	}

	private int nKPoints;
	private double kTotalTime;
	private int numberDetectors =1;

	public int getNumberDetectors() {
		return numberDetectors;
	}

	public void setNumberDetectors(int numberDetectors) {
		this.numberDetectors = numberDetectors;
	}

	private static final Logger logger = LoggerFactory.getLogger(ExafsScanPointCreator2.class);
	
	/**
	 * Operates this class when a bean (xml file from the gui) has been configured. This gives an example of how this
	 * class should be driven in a more manual mode.
	 * 
	 * @param parameters
	 * @return PyTuple
	 * @throws Exception
	 */
	public static PyTuple calculateEnergies(XasScanParameters parameters) throws Exception {
		if(parameters.getExafsStepType().equalsIgnoreCase("E"))
		{
			logger.error("K values must be in per angstroms");
			throw new Exception("K values must be specified in per angstroms");
		}
		ExafsScanPointCreator2 creator = new ExafsScanPointCreator2();
		
		//This class expects the edge energy to be set in KeV
		if(parameters.getEdgeEnergy() == null)
			creator.setDefaultScan(Element.getElement(parameters.getElement()).getEdgeEnergyInkeV(parameters.getEdge()));
		else
			creator.setDefaultScan(parameters.getEdgeEnergy()/1000.0);
		//TODO need to set the start and end energies of the regions
		creator.getPreEdgeRegion().setTime(parameters.getPreEdgeTime());
		creator.getEdgeRegion().setTime(parameters.getEdgeTime());
		creator.setkStartTime(parameters.getExafsFromTime());
		creator.setkEndTime(parameters.getExafsToTime());
		creator.setkWeighting(parameters.getKWeighting().intValue());
		creator.reCalculate();
		//number of detectors is 1.
		return ExafsScanPointCreator.convert2DDoubleArray(creator.calculateValues(), creator.getNumberDetectors());
	}

	public void setDefaultScan(double energy) {
		//twoD angle from the mono
		//Converter.setTwoD(6.2695);
		/* with energy in keV these are suitable preEdgeValues */
		double start = energy - 0.150;
		double end = energy - 0.020;
		int steps = 23;
		double time = 1000.0;
		double increment = (end - start) / steps;
		double edgestep;
		double valueOne;
		double valueTwo;

		edgeEnergy = energy;
		Converter.setEdgeEnergy(edgeEnergy);
		preEdgeRegion = new XafsRegion("Pre Edge", start, end, increment, time, steps,internalUnits, externalUnits);

		preEdgeRegion.setDisplayUnits(externalUnits);
		preEdgeRegion.setLastRegion(false);

		logger.info("the preegde is " + preEdgeRegion);
		start = Converter.convert(end, externalUnits, externalUnits);

		/* kStart should be smallest value above 3.0 which makes kStep */
		/* 0.2 mDeg, but 3.0 will do for now */
		/* the end of the edge region should be kStart in keV and this is */
		/* where conversion trouble starts */

		/* this will calculate a negative edgestep */

		edgestep = Converter.convert(energy + 0.001, externalUnits, externalUnits)
				- Converter.convert(energy, externalUnits, externalUnits);
/*
		if (Math.abs(edgestep) < 0.2)
			edgestep = -0.2;*/

		kStart = 2.9;

		do {
			kStart += 0.1;
			valueOne = Converter.convert(kStart, Converter.PERANGSTROM, externalUnits);
			valueTwo = Converter.convert(kStart + 0.05, Converter.PERANGSTROM, externalUnits);
			
		} while (Math.abs(valueOne - valueTwo) < smallestAllowedStep);

		kStep = 0.04;
		kEnd = 12.0;

		end = Converter.convert(kStart, Converter.PERANGSTROM, externalUnits);

		steps = (int) ((end - start) / edgestep);

		edgeRegion = new XafsRegion("Edge", start, end, increment, time, steps,internalUnits, externalUnits);
		// Reset the number of steps to cause the region to recalculate its
		// increment to the correct accuracy.
		edgeRegion.setNumberOfSteps(steps);
		edgeRegion.setDisplayUnits(externalUnits);
		edgeRegion.setLastRegion(false);

		// Recalculate the preEdge region to match up with the recalculated
		// edgeRegion
		preEdgeRegion.setEnd(edgeRegion.getStart());
		calculateKPoints();
		logger.info("the egde is " + edgeRegion);
		kStart = Formatter.getFormattedDouble(kStart, Converter.PERANGSTROM);
		kStep = Formatter.getFormattedDouble(kStep, Converter.PERANGSTROM);
		kEnd = Formatter.getFormattedDouble(kEnd, Converter.PERANGSTROM);

		kStartTime = 1000.0;
		kEndTime = 1000.0;
		kWeighting = 3;
		logger.info("the k values are  " + kStart + " " + kEnd + " " + kStep);
	}

	private void calculateKPoints() {
		double startAngle;
		double angle;
		double gotK;
		double step;
		double hopedfork;
		double nextAngle;

		startAngle = Converter.convert(kStart, Converter.PERANGSTROM, internalUnits);

		angle = startAngle;
		nKPoints = 0;
		kTotalTime = 0.0;

		// The calculation has to be done in this way only to avoid steps in
		// angle
		// which are actually smaller than the smallest allowed step.
		do {
			gotK = Converter.convert(angle, internalUnits, Converter.PERANGSTROM);

			if (nKPoints == 0)
				kStart = gotK;

			hopedfork = kStart + nKPoints * kStep;

			nextAngle = Converter.convert(gotK + kStep, Converter.PERANGSTROM, internalUnits);

			step = nextAngle - angle;

			if (Math.abs(step) < smallestAllowedStep)
				step = (step / Math.abs(step)) * smallestAllowedStep;

			nKPoints++;
			kTotalTime = kTotalTime + countTimeForK(gotK);
			angle = angle + step;
		}
		// NB should stop if hopedfork is equal to or just less than (by
		// rounding
		// errors) kEnd, otherwise number of points increases by one each time
		// round.
		while (kEnd - hopedfork > 0.001 && nKPoints < 2000);

		kEnd = gotK;

		logger.debug("calculateTheStepScanningWay - nKPoints " + nKPoints + " kEnd " + kEnd);

		// Recalculate the edge region with actual angle of starting
		edgeRegion.setEnd(Converter.convert(kStart, Converter.PERANGSTROM, externalUnits));
	}

	
	/**
	 * Calculates the countTime for a given k value.
	 * 
	 * @param k
	 * @return the countTime
	 */
	private double countTimeForK(double k) {
		double time;

		time = Math.pow((k - kStart), kWeighting);
		time = time / Math.pow(kEnd - kStart, kWeighting);
		time = time * (kEndTime - kStartTime);
		time = Math.rint(kStartTime + time);

		return (time);
	}
	
	private double[][] convertTimes(double[][] exafsEnergies, Double fromTime, Double toTime) {
		double start = exafsEnergies[0][0];
		double end = exafsEnergies[(exafsEnergies.length - 1)][0];
		double currentEnergy = start;
		double a = 0.0;
		double b = Math.pow(end-start,kWeighting);
		double c =toTime -fromTime;
		double time =0.0;
		for (int i = 0; i < exafsEnergies.length; i++) {
			currentEnergy = exafsEnergies[i][0];
			for (int j = 1; j <= numberDetectors; j++) {
				a = Math.pow(currentEnergy -start, kWeighting);
				time = fromTime + (a * c)/b;
				exafsEnergies[i][j] = time;
			}
			
			}
		return exafsEnergies;
	}

	public double[][] calculateValues()
	{
		double[][] preEdgeEnergies = ExafsScanPointCreator.createStepArray(preEdgeRegion.getStart() * 1000.0, preEdgeRegion.getEnd()*1000.0, preEdgeRegion.getIncrement()* 1000.0, preEdgeRegion.getTime(), false, numberDetectors);
		double[][] edgeEnergies = ExafsScanPointCreator.createStepArray(edgeRegion.getStart()* 1000.0, edgeRegion.getEnd()* 1000.0, edgeRegion.getIncrement()* 1000.0, edgeRegion.getTime(), false, numberDetectors);
		double[][] kArray = ExafsScanPointCreator.createStepArray(kStart, kEnd, kStep, kStartTime, true, numberDetectors);
		for (int i = 0; i < kArray.length; i++) {
			kArray[i][0] = Converter.convert(kArray[i][0], Converter.PERANGSTROM, Converter.EV);
		}
		kArray = convertTimes(kArray, kStartTime, kEndTime);
		double[][] allEnergies = preEdgeEnergies;
		allEnergies = (double[][]) ArrayUtils.addAll(allEnergies,edgeEnergies);
		allEnergies = (double[][]) ArrayUtils.addAll(allEnergies, kArray);
		
		return allEnergies;
		
	}


	public void reCalculate() {
		edgeRegion.setStart(preEdgeRegion.getEnd());
		calculateKPoints();
		logger.debug("reCalculate kStart is now " + kStart);
		kStart = Formatter.getFormattedDouble(kStart, Converter.PERANGSTROM);
		kStep = Formatter.getFormattedDouble(kStep, Converter.PERANGSTROM);
		kEnd = Formatter.getFormattedDouble(kEnd, Converter.PERANGSTROM);
	}
	
	public static void main(String args[])
	{
		ExafsScanPointCreator2 e = new ExafsScanPointCreator2();
		e.setDefaultScan(7.111142);
	}
}
