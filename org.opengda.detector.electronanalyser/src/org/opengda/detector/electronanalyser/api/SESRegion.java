/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.api;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Objects;

import org.eclipse.emf.ecore.util.EcoreUtil;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.diamond.osgi.services.ServiceProvider;

@JsonIgnoreProperties({"runMode", "ADCMask", "discriminatorLevel"})
public class SESRegion implements PropertyChangeListener, Serializable {

	private transient SESSettingsService settings = ServiceProvider.getService(SESSettingsService.class);
	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = 1491165026561874272L;

	//Needed for property change support
	public static final String NAME = "name";
	public static final String ENABLED = "enabled";
	public static final String REGION_ID = "regionId";
	public static final String LENS_MODE = "lensMode";
	public static final String PASS_ENERGY = "passEnergy";
	public static final String SLICES = "slices";
	public static final String ITERATIONS = "iterations";
	public static final String ACQUISTION_MODE =  "acquisitionMode";
	public static final String EXCITATION_ENERGY_SOURCE = "excitationEnergySource";
	public static final String ENERGY_MODE = "energyMode";
	public static final String LOW_ENERGY = "lowEnergy";
	public static final String HIGH_ENERGY = "highEnergy";
	public static final String FIX_ENERGY = "fixEnergy";
	public static final String STEP_TIME = "stepTime";
	public static final String TOTAL_STEPS = "totalSteps";
	public static final String TOTAL_TIME = "totalTime";
	public static final String ENERGY_STEP = "energyStep";
	public static final String EXPOSURE_TIME = "exposureTime";
	public static final String FIRST_X_CHANNEL = "firstXChannel";
	public static final String LAST_X_CHANNEL = "lastXChannel";
	public static final String FIRST_Y_CHANNEL = "firstYChannel";
	public static final String LAST_Y_CHANNEL = "lastYChannel";
	public static final String DETECTOR_MODE = "detectorMode";
	public static final String STATUS = "status";

	public static final String KINETIC = "Kinetic";
	public static final String BINDING = "Binding";

	public static final String SWEPT = "Swept";
	public static final String FIXED = "Fixed";

	public static final String PULSE_COUNTING = "Pulse Counting";
	public static final String ADC = "ADC";

	public enum Status {READY, INVALID, RUNNING, COMPLETED, ABORTED}

	private String name = "New_Region";
	@JsonAlias({"Enabled"})
	private boolean enabled = false;
	private String regionId = EcoreUtil.generateUUID();

	private String lensMode = settings.getDefaultLensModeForSESRegion();
	public int passEnergy = 5;

	//Acquisition configuration
	private int slices = 1;
	private int iterations = 1;
	private String acquisitionMode = SWEPT;

	//Excitation energy
	private String excitationEnergySource = settings.getDefaultExcitationEnergySourceForSESRegion();
	private String energyMode = KINETIC;

	//Spectrum energy range
	private double lowEnergy = 8;
	private double highEnergy = 10;
	private double fixEnergy = 9;

	//Step
	private double stepTime = 1;
	private double totalSteps = 15;
	private double totalTime = 15;
	private double energyStep = 200;
	private double exposureTime = 1.0;

	//Detector
	private int firstXChannel = 1;
	private int lastXChannel = 1000;
	private int firstYChannel = 101;
	private int lastYChannel = 800;
	private String detectorMode = ADC;

	private Status status = Status.READY;

	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public SESRegion() {
		// Public no-arg constructor for general use. Needed as default constructor is hidden by copy-constructor.
	}

	/**
	 * Copy constructor for creating a new region from an existing one
	 *
	 * @param region To be copied
	 */
	public SESRegion(SESRegion copy) {
		this.name = copy.getName();
		this.enabled =  copy.isEnabled();
		this.regionId = copy.getRegionId();
		this.lensMode = copy.getLensMode();
		this.passEnergy = copy.getPassEnergy();
		this.slices = copy.getSlices();
		this.iterations = copy.getIterations();
		this.acquisitionMode = copy.getAcquisitionMode();
		this.excitationEnergySource = copy.getExcitationEnergySource();
		this.energyMode = copy.getEnergyMode();
		this.lowEnergy = copy.getLowEnergy();
		this.highEnergy = copy.getHighEnergy();
		this.fixEnergy = copy.getFixEnergy();
		this.stepTime = copy.getStepTime();
		this.totalSteps = copy.getTotalSteps();
		this.totalTime = copy.getTotalTime();
		this.energyStep = copy.getEnergyStep();
		this.exposureTime = copy.getExposureTime();
		this.firstXChannel = copy.getFirstXChannel();
		this.lastXChannel = copy.getLastXChannel();
		this.firstYChannel = copy.getFirstYChannel();
		this.lastYChannel = copy.getLastYChannel();
		this.detectorMode = copy.getDetectorMode();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(evt);
	}

	@JsonIgnore
	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public String getExcitationEnergySource() {
		return excitationEnergySource;
	}

	@JsonProperty()
	public void setExcitationEnergySource(String excitationEnergySource) {
		final String oldValue = this.excitationEnergySource;
		this.excitationEnergySource = excitationEnergySource;
		propertyChangeSupport.firePropertyChange(EXCITATION_ENERGY_SOURCE, oldValue, excitationEnergySource);
	}

	@JsonAlias({"excitationEnergy", EXCITATION_ENERGY_SOURCE })
	public void setLegacyExcitationEnergy(double excitationEnergy) {
		final String oldValue = this.excitationEnergySource;
		this.excitationEnergySource = settings.convertLegacyExcitationEnergyToExcitationEnergySourceName(excitationEnergy);
		propertyChangeSupport.firePropertyChange(EXCITATION_ENERGY_SOURCE, oldValue, excitationEnergySource);
	}

	public String getDetectorMode() {
		return detectorMode;
	}

	public void setDetectorMode(String detectorMode) {
		String oldValue = this.detectorMode;
		this.detectorMode = detectorMode;
		propertyChangeSupport.firePropertyChange(DETECTOR_MODE, oldValue, detectorMode);
	}

	@JsonIgnore
	public boolean isDetectorModePulseCounting() {
		return getDetectorMode().equals(PULSE_COUNTING);
	}

	@JsonIgnore
	public boolean isDetectorModeADC() {
		return getDetectorMode().equals(ADC);
	}

	public double getEnergyStep() {
		return energyStep;
	}

	public void setEnergyStep(double energyStep) {
		double oldValue = this.energyStep;
		this.energyStep = energyStep;
		propertyChangeSupport.firePropertyChange(ENERGY_STEP, oldValue, energyStep);
	}

	public int getLastXChannel() {
		return lastXChannel;
	}

	public void setLastXChannel(int lastXChannel) {
		int oldValue = this.lastXChannel;
		this.lastXChannel = lastXChannel;
		propertyChangeSupport.firePropertyChange(LAST_X_CHANNEL, oldValue, lastXChannel);
	}

	public int getFirstXChannel() {
		return firstXChannel;
	}

	public void setFirstXChannel(int firstXChannel) {
		int oldValue = this.firstXChannel;
		this.firstXChannel = firstXChannel;
		propertyChangeSupport.firePropertyChange(FIRST_X_CHANNEL, oldValue, firstXChannel);
	}

	public int getFirstYChannel() {
		return firstYChannel;
	}

	public void setFirstYChannel(int firstYChannel) {
		int oldValue = this.firstYChannel;
		this.firstYChannel = firstYChannel;
		propertyChangeSupport.firePropertyChange(FIRST_Y_CHANNEL, oldValue, firstYChannel);
	}

	public int getLastYChannel() {
		return lastYChannel;
	}

	public void setLastYChannel(int lastYChannel) {
		int oldValue = this.lastYChannel;
		this.lastYChannel = lastYChannel;
		propertyChangeSupport.firePropertyChange(LAST_Y_CHANNEL, oldValue, lastYChannel);
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		String oldValue = this.regionId;
		this.regionId = regionId;
		propertyChangeSupport.firePropertyChange(REGION_ID, oldValue, regionId);
	}

	public double getStepTime() {
		return stepTime;
	}

	public void setStepTime(double stepTime) {
		double oldValue = this.stepTime;
		this.stepTime = stepTime;
		propertyChangeSupport.firePropertyChange(STEP_TIME, oldValue, stepTime);
	}

	public double getTotalSteps() {
		return totalSteps;
	}

	public void setTotalSteps(double totalSteps) {
		double oldValue = this.totalSteps;
		this.totalSteps = totalSteps;
		propertyChangeSupport.firePropertyChange(TOTAL_STEPS, oldValue, totalSteps);
	}

	public double getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(double totalTime) {
		double oldValue = this.totalTime;
		this.totalTime = totalTime;
		propertyChangeSupport.firePropertyChange(TOTAL_TIME, oldValue, totalTime);
	}

	public String getEnergyMode() {
		return energyMode;
	}

	@JsonIgnore
	public boolean isEnergyModeKinetic() {
		return getEnergyMode().equals(KINETIC);
	}

	@JsonIgnore
	public boolean isEnergyModeBinding() {
		return getEnergyMode().equals(BINDING);
	}

	public void setEnergyMode(String energyMode) {
		String oldValue = this.energyMode;
		this.energyMode = energyMode;
		propertyChangeSupport.firePropertyChange(ENERGY_MODE, oldValue, energyMode);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		propertyChangeSupport.firePropertyChange(NAME, oldValue, name);
	}

	public String getAcquisitionMode() {
		return acquisitionMode;
	}

	@JsonIgnore
	public boolean isAcquisitionModeSwept() {
		return getAcquisitionMode().equals(SWEPT);
	}

	@JsonIgnore
	public boolean isAcquisitionModeFixed() {
		return getAcquisitionMode().equals(FIXED);
	}

	public void setAcquisitionMode(String acquisitionMode) {
		String oldValue = this.acquisitionMode;
		this.acquisitionMode = acquisitionMode;
		propertyChangeSupport.firePropertyChange(ACQUISTION_MODE, oldValue, acquisitionMode);
	}

	public String getLensMode() {
		return lensMode;
	}

	public void setLensMode(String lensMode) {
		String oldValue = this.lensMode;
		this.lensMode = lensMode;
		propertyChangeSupport.firePropertyChange(LENS_MODE, oldValue, lensMode);
	}

	public double getLowEnergy() {
		return lowEnergy;
	}

	public void setLowEnergy(double startEnergy) {
		double oldValue = this.lowEnergy;
		this.lowEnergy = startEnergy;
		propertyChangeSupport.firePropertyChange(LOW_ENERGY, oldValue, startEnergy);
	}

	public double getHighEnergy() {
		return highEnergy;
	}

	public void setHighEnergy(double highEnergy) {
		double oldValue = this.highEnergy;
		this.highEnergy = highEnergy;
		propertyChangeSupport.firePropertyChange(HIGH_ENERGY, oldValue, highEnergy);
	}

	public int getPassEnergy() {
		return passEnergy;
	}

	public void setPassEnergy(int passEnergy) {
		int oldValue = this.passEnergy;
		this.passEnergy = passEnergy;
		propertyChangeSupport.firePropertyChange(PASS_ENERGY, oldValue, passEnergy);
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		int oldValue = this.iterations;
		this.iterations = iterations;
		propertyChangeSupport.firePropertyChange(ITERATIONS, oldValue, iterations);
	}

	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		double oldValue = this.exposureTime;
		this.exposureTime = exposureTime;
		propertyChangeSupport.firePropertyChange(EXPOSURE_TIME, oldValue, exposureTime);
	}


	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		boolean oldValue = this.enabled;
		this.enabled = enabled;
		propertyChangeSupport.firePropertyChange(ENABLED, oldValue, enabled);
	}

	public int getSlices() {
		return slices;
	}

	public void setSlices(int slices) {
		int oldValue = this.slices;
		this.slices = slices;
		propertyChangeSupport.firePropertyChange(SLICES, oldValue, slices);
	}

	public double getFixEnergy() {
		return fixEnergy;
	}

	public void setFixEnergy(double fixEnergy) {
		double oldValue = this.fixEnergy;
		this.fixEnergy = fixEnergy;
		propertyChangeSupport.firePropertyChange(FIX_ENERGY, oldValue, fixEnergy);
	}


	@Override
	public String toString() {
		return this.getClass().getSimpleName() +
		"[" +
			NAME + "=" + getName() + "," +
			ENABLED + "=" + isEnabled() + "," +
			REGION_ID + "=" + getRegionId() + "," +
			LENS_MODE + "=" + getLensMode() + "," +
			PASS_ENERGY + "=" + getPassEnergy() + "," +
			SLICES + "=" + getSlices() + "," +
			ITERATIONS + "=" + getIterations() + "," +
			ACQUISTION_MODE + "=" + getAcquisitionMode() + "," +
			EXCITATION_ENERGY_SOURCE + "=" + getExcitationEnergySource() + "," +
			ENERGY_MODE + "=" + getEnergyMode() + "," +
			LOW_ENERGY + "=" + getLowEnergy() + "," +
			HIGH_ENERGY + "=" + getHighEnergy() + "," +
			FIX_ENERGY + "=" + getFixEnergy() + "," +
			STEP_TIME + "=" + getStepTime() + "," +
			TOTAL_STEPS + "=" + getTotalSteps() + "," +
			TOTAL_TIME + "=" + getTotalTime() + "," +
			ENERGY_STEP + "=" + getEnergyStep() + "," +
			EXPOSURE_TIME + "=" + getExposureTime() + "," +
			FIRST_X_CHANNEL + "=" + getFirstXChannel() + "," +
			LAST_X_CHANNEL + "=" + getLastXChannel() + "," +
			FIRST_Y_CHANNEL + "=" + getFirstYChannel() + "," +
			LAST_Y_CHANNEL + "=" + getLastYChannel() + "," +
			DETECTOR_MODE + "=" + getDetectorMode() +
		"]";
	}

	@JsonIgnore
	public Status getStatus() {
		return status;
	}

	@JsonIgnore
	public void setStatus(Status status) {
		Status oldValue = this.status;
		this.status= status;
		propertyChangeSupport.firePropertyChange(STATUS, oldValue, status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(acquisitionMode, detectorMode, enabled, energyMode, energyStep, excitationEnergySource, exposureTime, firstXChannel, firstYChannel,
				fixEnergy, highEnergy, iterations, lastXChannel, lastYChannel, lensMode, lowEnergy, name, passEnergy, regionId, slices, status, stepTime,
				totalSteps, totalTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SESRegion other = (SESRegion) obj;
		return Objects.equals(acquisitionMode, other.acquisitionMode) && Objects.equals(detectorMode, other.detectorMode) && enabled == other.enabled
				&& Objects.equals(energyMode, other.energyMode) && Double.doubleToLongBits(energyStep) == Double.doubleToLongBits(other.energyStep)
				&& Objects.equals(excitationEnergySource, other.excitationEnergySource)
				&& Double.doubleToLongBits(exposureTime) == Double.doubleToLongBits(other.exposureTime) && firstXChannel == other.firstXChannel
				&& firstYChannel == other.firstYChannel && Double.doubleToLongBits(fixEnergy) == Double.doubleToLongBits(other.fixEnergy)
				&& Double.doubleToLongBits(highEnergy) == Double.doubleToLongBits(other.highEnergy) && iterations == other.iterations
				&& lastXChannel == other.lastXChannel && lastYChannel == other.lastYChannel && Objects.equals(lensMode, other.lensMode)
				&& Double.doubleToLongBits(lowEnergy) == Double.doubleToLongBits(other.lowEnergy) && Objects.equals(name, other.name)
				&& passEnergy == other.passEnergy && Objects.equals(regionId, other.regionId) && slices == other.slices && status == other.status
				&& Double.doubleToLongBits(stepTime) == Double.doubleToLongBits(other.stepTime)
				&& Double.doubleToLongBits(totalSteps) == Double.doubleToLongBits(other.totalSteps)
				&& Double.doubleToLongBits(totalTime) == Double.doubleToLongBits(other.totalTime);
	}


}