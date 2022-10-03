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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A bean representing a SPECS Phoibos region.
 *
 * it supports {@link Serializable} to allow it to be passed over RMI and has property change support for dynamic
 * editing in GUIs.
 *
 * @author James Mudd *
 */
public class SpecsPhoibosRegion implements Serializable {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = 4498165026561874272L;
	private static final String SCANNABLE_VALUES_NAME = "ScannableValues";

	private String name = "region";
	private String acquisitionMode = "Fixed Transmission";
	private String psuMode = "3.5kV";
	private String lensMode = "SmallArea";
	private double startEnergy = 800.0;
	private double endEnergy = 850.0;
	private double stepEnergy = 0.1;
	private double passEnergy = 5.0;
	private int iterations = 1;
	private double exposureTime = 1.0;
	/** Used to enable and disable regions within a sequence */
	// TODO Is this a property of the region or the sequence? Might be better in the sequence
	private boolean enabled = true;
	/** Is the region defined in binding energy or kinetic energy */
	private boolean bindingEnergy = false; //
	/** Only used in Snapshot breaks up into multiple regions */
	private int values = 1;
	/** Non-energy channels */
	private int slices = 100;

	private double centreEnergy;

	private final transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	// Optional scannables
	private final List<SpecsPhoibosScannableValue> scannableValues = new LinkedList<>();
	private final transient PropertyChangeListener scannableValueListener = pcs::firePropertyChange;


	public SpecsPhoibosRegion() {
		// Public no-arg constructor for general use. Needed as default constructor is hidden by copy-constructor.
	}

	/**
	 * Copy constructor for creating a new region from an existing one
	 *
	 * @param region To be copied
	 */
	public SpecsPhoibosRegion(SpecsPhoibosRegion region) {
		this.name = region.getName();
		this.acquisitionMode = region.getAcquisitionMode();
		this.psuMode = region.getPsuMode();
		this.lensMode = region.getLensMode();
		this.startEnergy = region.getStartEnergy();
		this.endEnergy = region.getEndEnergy();
		this.stepEnergy = region.getStepEnergy();
		this.passEnergy = region.getPassEnergy();
		this.iterations = region.getIterations();
		this.exposureTime = region.getExposureTime();
		this.enabled = region.isEnabled();
		this.bindingEnergy = region.isBindingEnergy();
		this.values = region.getValues();
		this.slices = region.getSlices();
		this.centreEnergy = region.getCentreEnergy();

		for (SpecsPhoibosScannableValue scannableValue : region.getScannableValues()) {
			addScannableValue(new SpecsPhoibosScannableValue(scannableValue));
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		pcs.firePropertyChange("name", oldValue, name);
	}

	public String getAcquisitionMode() {
		return acquisitionMode;
	}

	public void setAcquisitionMode(String acquisitionMode) {
		String oldValue = this.acquisitionMode;
		this.acquisitionMode = acquisitionMode;
		pcs.firePropertyChange("acquisitionMode", oldValue, acquisitionMode);
	}

	public String getPsuMode() {
		return psuMode;
	}

	public void setPsuMode(String psuMode) {
		String oldValue = this.psuMode;
		this.psuMode = psuMode;
		pcs.firePropertyChange("psuMode", oldValue, psuMode);
	}

	public String getLensMode() {
		return lensMode;
	}

	public void setLensMode(String lensMode) {
		String oldValue = this.lensMode;
		this.lensMode = lensMode;
		pcs.firePropertyChange("lensMode", oldValue, lensMode);
	}

	public double getStartEnergy() {
		return startEnergy;
	}

	public void setStartEnergy(double startEnergy) {
		double oldValue = this.startEnergy;
		this.startEnergy = startEnergy;
		pcs.firePropertyChange("startEnergy", oldValue, startEnergy);
	}

	public double getEndEnergy() {
		return endEnergy;
	}

	public void setEndEnergy(double endEnergy) {
		double oldValue = this.endEnergy;
		this.endEnergy = endEnergy;
		pcs.firePropertyChange("endEnergy", oldValue, endEnergy);
	}

	public double getStepEnergy() {
		return stepEnergy;
	}

	public void setStepEnergy(double stepEnergy) {
		double oldValue = this.stepEnergy;
		this.stepEnergy = stepEnergy;
		pcs.firePropertyChange("stepEnergy", oldValue, stepEnergy);
	}

	public double getPassEnergy() {
		return passEnergy;
	}

	public void setPassEnergy(double passEnergy) {
		double oldValue = this.passEnergy;
		this.passEnergy = passEnergy;
		pcs.firePropertyChange("passEnergy", oldValue, passEnergy);
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		int oldValue = this.iterations;
		this.iterations = iterations;
		pcs.firePropertyChange("iterations", oldValue, iterations);
	}

	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		double oldValue = this.exposureTime;
		this.exposureTime = exposureTime;
		pcs.firePropertyChange("exposureTime", oldValue, exposureTime);
	}

	public int getValues() {
		return values;
	}

	public void setValues(int values) {
		int oldValue = this.values;
		this.values = values;
		pcs.firePropertyChange("values", oldValue, values);
	}


	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		boolean oldValue = this.enabled;
		this.enabled = enabled;
		pcs.firePropertyChange("enabled", oldValue, enabled);
	}

	public boolean isBindingEnergy() {
		return bindingEnergy;
	}

	public void setBindingEnergy(boolean bindingEnergy) {
		boolean oldValue = this.bindingEnergy;
		this.bindingEnergy = bindingEnergy;
		pcs.firePropertyChange("bindingEnergy", oldValue, bindingEnergy);
	}

	public int getSlices() {
		return slices;
	}

	public void setSlices(int slices) {
		int oldValue = this.slices;
		this.slices = slices;
		pcs.firePropertyChange("slices", oldValue, slices);
	}

	public SpecsPhoibosScannableValue getScannableValue(String scannableName) {
		return scannableValues
				.stream()
				.filter(optionalScannable -> optionalScannable.getScannableName().equals(scannableName))
				.findFirst()
				.orElse(null);
	}

	public List<SpecsPhoibosScannableValue> getScannableValues() {
		return scannableValues;
	}

	public List<SpecsPhoibosScannableValue> getEnabledScannableValues() {
		return scannableValues
				.stream()
				.filter(SpecsPhoibosScannableValue::isEnabled)
				.collect(Collectors.toList());
	}

	public SpecsPhoibosScannableValue addScannableValue(String scannableName) {
		if (getScannableValue(scannableName) != null) {
			throw new IllegalArgumentException("Region already contains a scannable value with this name.");
		}

		SpecsPhoibosScannableValue scannableValue = new SpecsPhoibosScannableValue(scannableName);

		scannableValue.addPropertyChangeListener(scannableValueListener);
		List<SpecsPhoibosScannableValue> oldValue = new LinkedList<>(scannableValues);
		scannableValues.add(scannableValue);
		scannableValue.addPropertyChangeListener(scannableValueListener);
		pcs.firePropertyChange(SCANNABLE_VALUES_NAME, oldValue, scannableValues);

		return scannableValue;
	}

	public void addScannableValue(SpecsPhoibosScannableValue scannableValue) {
		if (getScannableValue(scannableValue.getScannableName()) != null) {
			throw new IllegalArgumentException("Region already contains a scannable value with this name.");
		}

		scannableValue.addPropertyChangeListener(scannableValueListener);
		List<SpecsPhoibosScannableValue> oldValue = new LinkedList<>(scannableValues);
		scannableValues.add(scannableValue);
		scannableValue.addPropertyChangeListener(scannableValueListener);
		pcs.firePropertyChange(SCANNABLE_VALUES_NAME, oldValue, scannableValues);
	}

	public String getScannableValueDescription() {
		if (scannableValues == null) {
			return "";
		}

		StringBuilder description = new StringBuilder();
		for (SpecsPhoibosScannableValue scannableValue : scannableValues
				.stream()
				.filter(SpecsPhoibosScannableValue::isEnabled)
				.toArray(SpecsPhoibosScannableValue[]::new)) {

			description.append(scannableValue.getScannableName() + ":" + scannableValue.getScannableValue() + ".");
		}

		return description.toString();
	}

	public void updateScannableValueListeners() {
		for (SpecsPhoibosScannableValue scannableValue : scannableValues) {
			scannableValue.removePropertyChangeListener(scannableValueListener);
			scannableValue.addPropertyChangeListener(scannableValueListener);
		}
	}

	public double getCentreEnergy() {
		return centreEnergy;
	}

	public void setCentreEnergy(double energy) {
		double oldValue = this.centreEnergy;
		this.centreEnergy = energy;
		pcs.firePropertyChange("centreEnergy", oldValue, energy);
	}


	@Override
	public String toString() {
		return "SpecsPhoibosRegion [name=" + name + ", acquisitionMode=" + acquisitionMode + ", psuMode=" + psuMode
				+ ", lensMode=" + lensMode + ", startEnergy=" + startEnergy + ", endEnergy=" + endEnergy
				+ ", stepEnergy=" + stepEnergy + ", passEnergy=" + passEnergy + ", iterations=" + iterations
				+ ", exposureTime=" + exposureTime + ", enabled=" + enabled + ", bindingEnergy=" + bindingEnergy
				+ ", values=" + values + ", slices=" + slices + ", centreEnergy=" + centreEnergy +"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acquisitionMode == null) ? 0 : acquisitionMode.hashCode());
		result = prime * result + (bindingEnergy ? 1231 : 1237);
		result = prime * result + (enabled ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(endEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(exposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + iterations;
		result = prime * result + ((lensMode == null) ? 0 : lensMode.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		temp = Double.doubleToLongBits(passEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((psuMode == null) ? 0 : psuMode.hashCode());
		result = prime * result + slices;
		temp = Double.doubleToLongBits(startEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stepEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + values;
		temp = Double.doubleToLongBits(centreEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + scannableValues.hashCode();
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
		SpecsPhoibosRegion other = (SpecsPhoibosRegion) obj;
		if (acquisitionMode == null) {
			if (other.acquisitionMode != null)
				return false;
		} else if (!acquisitionMode.equals(other.acquisitionMode))
			return false;
		if (bindingEnergy != other.bindingEnergy)
			return false;
		if (enabled != other.enabled)
			return false;
		if (Double.doubleToLongBits(endEnergy) != Double.doubleToLongBits(other.endEnergy))
			return false;
		if (Double.doubleToLongBits(exposureTime) != Double.doubleToLongBits(other.exposureTime))
			return false;
		if (iterations != other.iterations)
			return false;
		if (lensMode == null) {
			if (other.lensMode != null)
				return false;
		} else if (!lensMode.equals(other.lensMode))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(passEnergy) != Double.doubleToLongBits(other.passEnergy))
			return false;
		if (psuMode == null) {
			if (other.psuMode != null)
				return false;
		} else if (!psuMode.equals(other.psuMode))
			return false;
		if (slices != other.slices)
			return false;
		if (Double.doubleToLongBits(startEnergy) != Double.doubleToLongBits(other.startEnergy))
			return false;
		if (Double.doubleToLongBits(stepEnergy) != Double.doubleToLongBits(other.stepEnergy))
			return false;
		if (values != other.values)
			return false;
		if (!scannableValues.equals(other.getScannableValues()))
			return false;
		if (Double.doubleToLongBits(centreEnergy) != Double.doubleToLongBits(other.centreEnergy))
			return false;
		return true;
	}
}