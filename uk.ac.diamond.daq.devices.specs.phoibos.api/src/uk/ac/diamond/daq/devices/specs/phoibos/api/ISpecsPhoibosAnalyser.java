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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import java.util.Set;

import gda.factory.Findable;
import gda.observable.IObservable;

/**
 * This is the interface used to expose the SPECS Phoibos analyser over RMI.
 * <p>
 * It is intended to only contain the methods needed to make a rich GUI to interact with the analyser
 *
 * @author James Mudd
 */
public interface ISpecsPhoibosAnalyser extends Findable, IObservable {

	double getDwellTime();

	void setDwellTime(double collectionTime);

	void setLensMode(String value);

	void setAcquisitionMode(String value);

	void setPassEnergy(double value);

	double getPassEnergy();

	Set<String> getLensModes();

	void setPsuMode(String psuMode);

	Set<String> getPsuModes();

	String getPsuMode();

	String getLensMode();

	double[] getEnergyAxis();

	double[] getYAxis();

	double[] getSpectrum();

	int getSlices();

	double[][] getImage();

	/**
	 * Configures the analyser to acquire the region specified.
	 *
	 * @param region The region to set
	 */
	void setRegion(SpecsPhoibosRegion region);

	/**
	 * Get the current analyser settings and builds a region from them.
	 *
	 * @return The region the analyser is currently configured for
	 */
	SpecsPhoibosRegion getRegion();

	/**
	 * Starts the analyser acquiring in continuous mode, this is intended for use in alignment. This is non blocking
	 */
	void startContinuous();

	void stopAcquiring();

	double getCenterEnergy();

	double getDetectorEnergyWidth();

	String getYUnits();

	Set<String> getAcquisitionModes();

	void setSequence(SpecsPhoibosSequence sequence);

	/**
	 * Converts binding energy to kinetic energy according to:
	 * <br>
	 * KE = hν - BE - Φ
	 * <br>
	 * where hν = photon energy and Φ = analyser work function
	 *
	 * @param bindingEnergy to convert
	 * @return The equivalent binding energy
	 */
	double toKineticEnergy(double bindingEnergy);

	/**
	 * Converts kinetic energy to binding energy according to:
	 * <br>
	 * BE = hν - KE - Φ
	 * <br>
	 * where hν = photon energy and Φ = analyser work function
	 *
	 * @param kineticEnergy to convert
	 * @return The equivalent binding energy
	 */
	double toBindingEnergy(double kineticEnergy);

	/**
	 * Converts binding energy to kinetic energy according to:
	 * <br>
	 * KE = hν - BE - Φ
	 * <br>
	 * where hν = photon energy and Φ = analyser work function
	 *
	 * @param bindingEnergy to convert
	 * @return The equivalent binding energy
	 */
	double[] toKineticEnergy(double[] bindingEnergy);

	/**
	 * Converts kinetic energy to binding energy according to:
	 * <br>
	 * BE = hν - KE - Φ
	 * <br>
	 * where hν = photon energy and Φ = analyser work function
	 *
	 * @param kineticEnergy to convert
	 * @return The equivalent binding energy
	 */
	double[] toBindingEnergy(double[] kineticEnergy);

}