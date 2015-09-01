/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.CollectionStrategyBeanInterface;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

abstract public class AbstractADTriggeringStrategy implements CollectionStrategyBeanInterface{

	private ADBase adBase;
	private boolean propertiesSet = false;

	private double readoutTime = 0.1; // TODO: Should default to 0, change setReadoutTime javadoc if this changes.
	
	private boolean readAcquisitionTime = true;
	private String acquisitionTimeUnit="s";

	private boolean readAcquisitionPeriod = false;
	private String acquisitionPeriodUnit="s";

	private Boolean generateCallbacks = null;
	
	/**
	 * Each exposure lasts for acc_expo_time. Exp per Image is set to int(collection_time/acc_expo_time + 0.5)
	 */
	boolean accumlationMode = false;
	
	/**
	 * @see gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy#accumlationMode
	 */
	public boolean isAccumlationMode() {
		return accumlationMode;
	}

	/**
	 * @see gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy#accumlationMode
	 */
	public void setAccumlationMode(boolean accumlationMode) {
		this.accumlationMode = accumlationMode;
	}

	/**
	 * @see gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy#accumlationMode
	 * 
	 * default value is 10ms
	 */
	double acc_expo_time=0.01; 
	
	/**
	 * 
	 * @see gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy#accumlationMode
	 */
	public double getAcc_expo_time() {
		return acc_expo_time;
	}

	/**
	 * 
	 * @see gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy#accumlationMode
	 */
	public void setAcc_expo_time(double acc_expo_time) {
		this.acc_expo_time = acc_expo_time;
	}

	private String timeFormat = "%.2f"; 
	
	/**
	 * Sets the required readout/dwell time (t_period - t_acquire).
	 * <p>
	 * Defaults to 0.1 (Should be 0)
	 * 
	 * @param readoutTime
	 */
	public void setReadoutTime(double readoutTime) {
		this.readoutTime = readoutTime;
	}
	
	public void setReadAcquisitionTime(boolean readAcquisitionTime) {
		this.readAcquisitionTime = readAcquisitionTime;
	}

	public void setReadAcquisitionPeriod(boolean readAcquisitionPeriod) {
		this.readAcquisitionPeriod = readAcquisitionPeriod;
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		this.generateCallbacks = b;
		
	}

	@Override
	public boolean isGenerateCallbacks() {
		return generateCallbacks;
	}
	/**
	 * Get the required readout/dwell time (t_period - t_acquire).
	 */
	public double getReadoutTime() {
		return readoutTime;
	}

	public void setAdBase(ADBase adBase) {
		errorIfPropertySetAfterBeanConfigured("adBase");
		this.adBase = adBase;
	}

	public ADBase getAdBase() {
		return adBase;
	}
	
	public boolean isReadAcquisitionTime() {
		return readAcquisitionTime;
	}
	
	public boolean isReadAcquisitionPeriod() {
		return readAcquisitionPeriod;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		throw new UnsupportedOperationException("Must be operated via prepareForCollection(collectionTime, numberImagesPerCollection)");
	}

	/**
	 * IMPORTANT: Implementations must call enableOrDisableCallbacks()
	 */
	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		enableOrDisableCallbacks();
	}

	protected final void enableOrDisableCallbacks() throws Exception {
		if (generateCallbacks != null) {
			getAdBase().setArrayCallbacks(isGenerateCallbacks() ? 1 : 0);
		}
	}

	@Override @Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		double expoTime = isAccumlationMode() ? acc_expo_time : collectionTime;
		if (getReadoutTime() < 0) {
			getAdBase().setAcquirePeriod(0.0);
		} else {
			getAdBase().setAcquirePeriod(expoTime + getReadoutTime());
		}
		getAdBase().setAcquireTime(expoTime);
		getAdBase().setNumExposures(isAccumlationMode() ? (int)(collectionTime / acc_expo_time + 0.5) : 1);
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if( adBase == null)
			throw new RuntimeException("adBase is not set");
		propertiesSet = true;
	}
	
	@Override
	public String getName() {
		return "driver";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}
	
	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}
	
	@Override
	public List<String> getInputStreamNames() {
		List<String> fieldNames = new ArrayList<String>();
		if (isReadAcquisitionTime()) {
			fieldNames.add("count_time");
		}
		if (isReadAcquisitionPeriod()) {
			fieldNames.add("period");
		}
		return fieldNames;
	}

	public List<String> getInputStreamUnits() {
		List<String> fieldUnits = new ArrayList<String>();
		
		if (isReadAcquisitionTime()) {
			if (getAcquisitionTimeUnit()!=null) {
				fieldUnits.add(getAcquisitionTimeUnit());
			}
		}
		if (isReadAcquisitionPeriod()) {
			if (getAcquisitionPeriodUnit()!=null) {
				fieldUnits.add(getAcquisitionPeriodUnit());
			}
		}
		return fieldUnits;
	}
	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();
		if (isReadAcquisitionTime()) {
			formats.add(getTimeFormat());
		}
		if (isReadAcquisitionPeriod()) {
			formats.add(getTimeFormat());
		}
		return formats;
	}

	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		List<Double> times = new ArrayList<Double>();
		if (isReadAcquisitionTime()) {
			try {
				times.add(getAcquireTime());
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
		if (isReadAcquisitionPeriod()) {
			try {
				times.add(getAcquirePeriod());
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
		//need to add NumExposures for accumulationMode
		Vector<NXDetectorDataAppender> vector = new Vector<NXDetectorDataAppender>();
		if (getInputStreamUnits().isEmpty()) {
			vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), times));
		} else {
			vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), times, getInputStreamUnits()));
		}
		return vector;
	}
	@Override
	public boolean requiresAsynchronousPlugins() {
		return false; //This is fine for software triggered cameras
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public String getAcquisitionPeriodUnit() {
		return acquisitionPeriodUnit;
	}

	public void setAcquisitionPeriodUnit(String acquisitionPeriodUnit) {
		this.acquisitionPeriodUnit = acquisitionPeriodUnit;
	}

	public String getAcquisitionTimeUnit() {
		return acquisitionTimeUnit;
	}

	public void setAcquisitionTimeUnit(String acquisitionTimeUnit) {
		this.acquisitionTimeUnit = acquisitionTimeUnit;
	}

	/**
	 * This function can be used to enforce the condition that properties are not changed after Bean initialisation completes.
	 *
	 * @param description used in thrown exceptions
	 */
	public void errorIfPropertySetAfterBeanConfigured(String description) {
		if (propertiesSet) throw new IllegalAccessError("Attempt to set property " + description  + " in bean "+ getName() + "after Bean configured!");
	}
}
