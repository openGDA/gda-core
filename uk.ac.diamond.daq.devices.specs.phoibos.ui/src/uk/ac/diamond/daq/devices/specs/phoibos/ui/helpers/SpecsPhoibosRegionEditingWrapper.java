/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.helpers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.editors.SpecsRegionEditor;

/**
 * This class provides editing support for {@link SpecsRegionEditor}. It handles the update logic when using different
 * acquisition modes. and provides helper methods to allow the GUI to be reactive.
 * <p>
 * It also provide the logic for performing time estimations.
 *
 * @author James Mudd
 */
public class SpecsPhoibosRegionEditingWrapper implements PropertyChangeListener {

	private final SpecsPhoibosRegion region;
	private final double detectorEnergyWidth;
	private final int snapshotImageSizeX;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private String estimatedTime;

	public SpecsPhoibosRegionEditingWrapper(SpecsPhoibosRegion region, double detectorEnergyWidth,
			int snapshotImageSizeX) {
		this.region = region;
		this.detectorEnergyWidth = detectorEnergyWidth;
		this.snapshotImageSizeX = (snapshotImageSizeX != 0) ? snapshotImageSizeX : 1;
		setEstimatedTime(SpecsPhoibosTimeEstimator.estimateRegionTime(region, detectorEnergyWidth));
		region.addPropertyChangeListener(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Fire through the events received from the wrapped region
		pcs.firePropertyChange(evt);
		setEstimatedTime(SpecsPhoibosTimeEstimator.estimateRegionTime(region, detectorEnergyWidth));
		region.setEstimatedTimeInMs(SpecsPhoibosTimeEstimator.estimateRegionTimeMs(region, detectorEnergyWidth));
	}

	public void setAcquisitionMode(String acquisitionMode) {
		// Cache old dependent values
		final boolean oldSnapshotMode = isSnapshotMode();
		final boolean oldFixedEnergyMode = isFixedEnergyMode();
		// DO the update
		region.setAcquisitionMode(acquisitionMode);
		// fire dependent listeners
		pcs.firePropertyChange("snapshotMode", oldSnapshotMode, isSnapshotMode());
		pcs.firePropertyChange("fixedEnergyMode", oldFixedEnergyMode, isFixedEnergyMode());
		if (isSnapshotMode()) { // If the new mode is snapshot fix up the start and end energies
			setCentreEnergy(getCentreEnergy());
			setEnergyStep();
		}
	}

	private void setEnergyStep() {
		final double newEnergyStep = calculateEnergyStep();
		region.setStepEnergy(newEnergyStep);
		pcs.firePropertyChange("stepEnergy", getStepEnergy(), newEnergyStep);
	}

	private double calculateEnergyStep() {
		return getEnergyWidth() / snapshotImageSizeX;
	}

	public void setStartEnergy(double startEnergy) {
		// Cache old dependent values
		final double oldEnergyWidth = getEnergyWidth();
		// Do the real update
		region.setStartEnergy(startEnergy);
		region.setCentreEnergy(getCentreEnergy());
		// Fire dependent property listeners
		pcs.firePropertyChange("energyWidth", oldEnergyWidth, getEnergyWidth());
	}

	public void setEndEnergy(double endEnergy) {
		// Cache old dependent values
		final double oldEnergyWidth = getEnergyWidth();
		// Do the real update
		region.setEndEnergy(endEnergy);
		region.setCentreEnergy(getCentreEnergy());
		// Fire dependent property listeners
		pcs.firePropertyChange("energyWidth", oldEnergyWidth, getEnergyWidth());
	}

	public double getCentreEnergy() {
		return (getStartEnergy() + getEnergyWidth()) / 2.0;
	}

	public void setCentreEnergy(final double centreEnergy) {
		// Cache old dependent values
		setStartEnergy(calculateStartEnergy(centreEnergy, getPassEnergy()));
		setEndEnergy(calculateEndEnergy(centreEnergy, getPassEnergy()));
		region.setCentreEnergy(centreEnergy);
	}

	public void setPassEnergy(double passEnergy) {
		if (isSnapshotMode()) { // If its snapshot mode then the pass energy determines the width and step.
			// Cache old dependent values
			final double oldEnergyWidth = getEnergyWidth();
			// Hold the centre energy as if we move the start then get it again it will change
			final double centreEnergy = getCentreEnergy();
			region.setStartEnergy(calculateStartEnergy(centreEnergy, passEnergy));
			region.setEndEnergy(calculateEndEnergy(centreEnergy, passEnergy));
			// Fire dependent property listeners
			pcs.firePropertyChange("energyWidth", oldEnergyWidth, getEnergyWidth());
			setEnergyStep();
		}
		// Do the real update will fire the PCS for PE
		region.setPassEnergy(passEnergy);
	}

	private double calculateStartEnergy(final double centreEnergy, final double passEnergy) {
		return centreEnergy - ((passEnergy * detectorEnergyWidth) / 2);
	}

	private double calculateEndEnergy(final double centreEnergy, final double passEnergy) {
		return centreEnergy + ((passEnergy * detectorEnergyWidth) / 2);
	}

	public double getEnergyWidth() {
		return Math.abs(getEndEnergy() - getStartEnergy());
	}

	public boolean isSnapshotMode() {
		return getAcquisitionMode().equals("Snapshot");
	}

	public boolean isFixedEnergyMode() {
		return getAcquisitionMode().equals("Fixed Energy");
	}

	// ------ Pure delegated methods -------

	public double getStartEnergy() {
		return region.getStartEnergy();
	}

	public double getEndEnergy() {
		return region.getEndEnergy();
	}

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

	public double getStepEnergy() {
		return region.getStepEnergy();
	}

	public void setStepEnergy(double stepEnergy) {
		region.setStepEnergy(stepEnergy);
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

	public boolean isEnabled() {
		return region.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		region.setEnabled(enabled);
	}

	public boolean isBindingEnergy() {
		return region.isBindingEnergy();
	}

	public void setBindingEnergy(boolean bindingEnergy) {
		region.setBindingEnergy(bindingEnergy);
	}

	public int getSlices() {
		return region.getSlices();
	}

	public void setSlices(int slices) {
		region.setSlices(slices);
	}

	public String getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(String estimatedTime) {
		String oldValue = getEstimatedTime();
		this.estimatedTime = estimatedTime;
		pcs.firePropertyChange("estimatedTime", oldValue, estimatedTime);
	}
}
