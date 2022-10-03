/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;

/**
 * This wraps a standard SpecsPhoibosRegion to allow you to set centerEnergy and pass energy instead of start and end
 * energy.
 *
 * @author James Mudd
 */
public class SpecsFixedRegionWrapper {

	private final SpecsPhoibosRegion region;
	private final double detectorEnergyWidth;

	public SpecsFixedRegionWrapper(SpecsPhoibosRegion region, double detectorEnergyWidth) {
		this.region = region;
		this.detectorEnergyWidth = detectorEnergyWidth;
		// It only makes sense in snapshot mode so ensure the region is set like that
		region.setAcquisitionMode("Snapshot");
	}

	private double calculateStartEnergy(final double centreEnergy, final double passEnergy) {
		return centreEnergy - ((passEnergy * detectorEnergyWidth) / 2 );
	}

	private double calculateEndEnergy(final double centreEnergy, final double passEnergy) {
		return centreEnergy + ((passEnergy * detectorEnergyWidth) / 2 );
	}

	public void setPassEnergy(final double passEnergy) {
		// Set the pass energy on the underlying region
		region.setPassEnergy(passEnergy);
		// Update the start and end energy for the new PE
		final double centreEnergy = getCentreEnergy();

		region.setStartEnergy(calculateStartEnergy(centreEnergy, passEnergy));
		region.setEndEnergy(calculateEndEnergy(centreEnergy, passEnergy));
	}

	public double getCentreEnergy() {
		return (getStartEnergy() + getEndEnergy()) / 2.0;
	}

	public void setCentreEnergy(final double centreEnergy) {
		final double passEnergy = getPassEnergy();

		region.setStartEnergy(calculateStartEnergy(centreEnergy, passEnergy));
		region.setEndEnergy(calculateEndEnergy(centreEnergy, passEnergy));
	}

	public SpecsPhoibosRegion getRegion() {
		return region;
	}

	// Delegate all other methods through;
	public String getName() {
		return region.getName();
	}

	public void setName(String name) {
		region.setName(name);
	}

	public String getAcquisitionMode() {
		return region.getAcquisitionMode();
	}

	public String getPsuMode() {
		return region.getPsuMode();
	}

	public void setPsuMode(String psuMode) {
		region.setPsuMode(psuMode);
	}

	public String getLensMode() {
		return region.getLensMode();
	}

	public void setLensMode(String lensMode) {
		region.setLensMode(lensMode);
	}

	public double getStartEnergy() {
		return region.getStartEnergy();
	}

	public double getEndEnergy() {
		return region.getEndEnergy();
	}

	public double getPassEnergy() {
		return region.getPassEnergy();
	}

	public int getIterations() {
		return region.getIterations();
	}

	public void setIterations(int iterations) {
		region.setIterations(iterations);
	}

	public double getExposureTime() {
		return region.getExposureTime();
	}

	public void setExposureTime(double exposureTime) {
		region.setExposureTime(exposureTime);
	}

	public int getValues() {
		return region.getValues();
	}

	public void setValues(int values) {
		region.setValues(values);
	}

	@Override
	public String toString() {
		return region.toString();
	}

}
