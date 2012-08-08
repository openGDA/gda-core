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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;

abstract public class AbstractADTriggeringStrategy implements ADTriggeringStrategy, InitializingBean{

	private final ADBase adBase;

	private double readoutTime = 0.1; // TODO: Should default to 0
	
	private boolean readAcquisitionTime = true;

	private boolean readAcquisitionPeriod = false;
	
	AbstractADTriggeringStrategy(ADBase adBase) {
		this.adBase = adBase;
	}
	
	/**
	 * Sets the required readout/dwell time (t_period - t_acquire).
	 * <p>
	 * Defaults to 0.
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

	/**
	 * Get the required readout/dwell time (t_period - t_acquire).
	 */
	public double getReadoutTime() {
		return readoutTime;
	}
	
	protected ADBase getAdBase() {
		return adBase;
	}
	
	public boolean isReadAcquisitionTime() {
		return readAcquisitionTime;
	}
	
	public boolean isReadAcquisitionPeriod() {
		return readAcquisitionPeriod;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		if (getReadoutTime() < 0) {
			getAdBase().setAcquirePeriod(0.0);
		} else {
			getAdBase().setAcquirePeriod(collectionTime + getReadoutTime());
		}
		getAdBase().setAcquireTime(collectionTime);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if( adBase == null)
			throw new RuntimeException("adBase is not set");
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
	public List<String> getInputStreamFieldNames() {
		List<String> fieldNames = new ArrayList<String>();
		if (isReadAcquisitionTime()) {
			fieldNames.add("count_time");
		}
		if (isReadAcquisitionPeriod()) {
			fieldNames.add("period");
		}
		return fieldNames;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();
		if (isReadAcquisitionTime()) {
			formats.add("%.2f");
		}
		if (isReadAcquisitionPeriod()) {
			formats.add("%.2f");
		}
		return formats;
	}

	@Override
	public Vector<Double[]> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		Double[] times = new Double[0];
		if (isReadAcquisitionTime()) {
			try {
				ArrayUtils.add(times, getAcquireTime());
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
		if (isReadAcquisitionPeriod()) {
			try {
				ArrayUtils.add(times, getAcquirePeriod());
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
		Vector<Double[]> vector = new Vector<Double[]>();
		vector.add(times);
		return new Vector<Double[]>();
	}

}
