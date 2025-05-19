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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.XanesScanParameters;

/**
 * Creates a 2D tuple of energy and collection time for every point in a multi-region xanes scan.
 */
public class XanesScanPointCreator implements XasScanPointCreator {

	private Double finalEnergy;
	private double[][] regions;
	private Integer numberDetectors = 1;

	public XanesScanPointCreator() {
	}

	public XanesScanPointCreator(XanesScanParameters params) {
		setupScanPointCreator( params, this);
	}

	/**
	 * Operates this class when a bean (xml file from the gui) has been configured. This gives an example of how this
	 * class should be driven in a more manual mode.
	 *
	 * @param parameters
	 * @return PyTuple
	 * @throws Exception
	 */
	public static double[][] getEnergies(XanesScanParameters parameters) throws Exception {
		XanesScanPointCreator creator = new XanesScanPointCreator();
		setupScanPointCreator( parameters,creator );
		return creator.getEnergies();
	}

	public static Double[] getScanTimeArray(XanesScanParameters parameters) throws Exception {
		XanesScanPointCreator creator = new XanesScanPointCreator();
		setupScanPointCreator(parameters, creator);
		return creator.getTimes();
	}

	private static void setupScanPointCreator(XanesScanParameters parameters,XanesScanPointCreator creator )
	{
		List<Region> regions = parameters.getRegions();
		double[][] newregions = new double[regions.size()][3];
		for (int i = 0; i < regions.size(); i++){
			Region thisRegion = regions.get(i);
			newregions[i][0] = thisRegion.getEnergy();
			newregions[i][1] = thisRegion.getStep();
			newregions[i][2] = thisRegion.getTime();
		}
		creator.setFinalEnergy(parameters.getFinalEnergy());
		creator.setRegions(newregions);
	}

	@Override
	public Double[] getTimes() throws Exception {
		double[][] energies = getEnergies();
		return Stream.of(energies)
				.map(val->val[1])
				.toList()
				.toArray(new Double[] {});
		}

	@Override
	public double[][] getEnergies() throws Exception {
		checkAllValuesEntered();
		checkAllValuesConsistent();
		return calculateValues();
	}

	private double[][] calculateValues() {
		var scanTimes= new ArrayList<> ();
		double[][] allEnergies = new double[0][];
		for (int i = 0; i < regions.length -1 ; i++){
			double[][] thisRegion = ExafsScanPointCreator.createStepArray(regions[i][0], regions[i+1][0], regions[i][1], regions[i][2], false, numberDetectors);
			allEnergies = (double[][]) ArrayUtils.addAll(allEnergies, thisRegion);
			scanTimes.add(new ExafsScanRegionTime("region"+i, thisRegion.length, new double[]{regions[i][2]}));
		}
		double[][] finalRegion = ExafsScanPointCreator.createStepArray(regions[regions.length -1][0], finalEnergy, regions[regions.length -1][1], regions[regions.length -1][2], true, numberDetectors);
		allEnergies = (double[][]) ArrayUtils.addAll(allEnergies, finalRegion);
		scanTimes.add(new ExafsScanRegionTime("region"+(regions.length -1), finalRegion.length, new double[]{regions[regions.length -1][2]}));
		return allEnergies;
	}

	private void checkAllValuesConsistent() throws Exception {
		for (double[] region : regions) {
			// all regions must have 3 elements
			if (region.length != 3)
				throw new Exception("region length not 3. Each region must be: [energy, step, time]");
			// the first element of each region must be less than the final energy
			if (region[0] >= finalEnergy)
				throw new Exception("region energy >= final energy");
		}
	}

	private void checkAllValuesEntered() throws Exception {
		if (finalEnergy == null)
			throw new Exception("finalEnergy not set");
		if (regions == null)
			throw new Exception("regions not set");
		if (numberDetectors == null)
			throw new Exception("numberDetectors not set");
	}

	public double getFinalEnergy() {
		return finalEnergy;
	}

	public void setFinalEnergy(double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}

	public double[][] getRegions() {
		return regions;
	}

	public void setRegions(double[][] regions) {
		this.regions = regions;
	}

	public int getNumberDetectors() {
		return numberDetectors;
	}

	public void setNumberDetectors(int numberDetectors) {
		this.numberDetectors = numberDetectors;
	}
}
