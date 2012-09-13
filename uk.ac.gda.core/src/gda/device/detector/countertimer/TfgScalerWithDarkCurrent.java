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

package gda.device.detector.countertimer;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDAServer;
import gda.device.timer.Tfg;
import gda.factory.Finder;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TfgScalerWithDarkCurrent extends TfgScaler {

	private static final Logger logger = LoggerFactory.getLogger(TfgScalerWithDarkCurrent.class);

	private boolean darkCurrentRequired = true;

	// default is 1s, but properly should be the longest collection period in a scan
	private Double darkCurrentCollectionTime = 1.0;

	private DarkCurrentResults darkCurrent;
	
	private boolean useReset = true;

	/**
	 * Reads and sets the darkCurrent
	 * 
	 * @throws DeviceException
	 */
	public void acquireDarkCurrent() throws Exception {
		darkCurrent = null;

		// Deal with DummyDASever - Arg DummyDASever should
		// have a way of reading dark currents but it does not.
		// For now we test and set dark current.
		if (((Tfg) timer).getDaServer() instanceof DummyDAServer) {
			darkCurrent = new DarkCurrentResults(1.0, new Double[] { 150d, 230d, 135d });
			return;
		}

		// Read dark current
		final DarkCurrentBean bean = new DarkCurrentBean();
		bean.setCounterTimerName(this.getName());
		bean.setShutterName(LocalProperties.get("gda.exafs.darkcurrent.shutter", "shutter1"));
		bean.setOriginalCollectionTime(getCollectionTime());
		bean.setDarkCollectionTime(darkCurrentCollectionTime);

		// value.
		this.darkCurrent = collectDarkCurrent(bean);
		for (Double dkValue : getDarkCurrent()) {
			String comment = String.format(getName() + " dark current measured to be: %.0f over %.1f s", dkValue,
					getDarkCurrentCollectionTime());
			logger.info(comment);
		}
	}

	private gda.device.detector.countertimer.TfgScalerWithDarkCurrent.DarkCurrentResults collectDarkCurrent(
			final DarkCurrentBean bean) throws Exception {

		final Scannable shutter = (Scannable) Finder.getInstance().find(bean.getShutterName());
		String originalShutterPosition = shutter.getPosition().toString();
		Boolean openAtEnd = true;
		if (originalShutterPosition.equalsIgnoreCase("Close")) {
			openAtEnd = false;
		} else {
			shutter.moveTo("Close");
		}

		setCollectionTime(bean.getDarkCollectionTime());
		// do not change the collection time - simply re-use the current time which the
		collectData();

		Thread.sleep(Math.round(bean.getDarkCollectionTime()) * 1000); // Throws interrupted exception if the thread
																		// executing this method.
		while (isBusy()) {
			// TODO timeout of 2*bean.getDarkCollectionTime()
			Thread.sleep(100);
		}

		double[] countsDbl = super.readout();
		Double[] counts = ArrayUtils.toObject(countsDbl);
		if (openAtEnd) {
			if (useReset) {
				shutter.moveTo("Reset");
			}
			shutter.moveTo("Open");
		}

		setCollectionTime(bean.getOriginalCollectionTime());

		return new DarkCurrentResults(bean.getDarkCollectionTime(), counts);
	}

	/**
	 * Reads the dark current.
	 */
	@Override
	public void atScanStart() throws DeviceException {

		if (isDarkCurrentRequired()) {
			try {
				acquireDarkCurrent();
			} catch (Exception e) {
				throw new DeviceException("Cannot read dark current", e);
			}
		}
		super.atScanStart();
	}
	
	@Override
	public void atScanEnd() throws DeviceException {
		// reset time to default
		darkCurrentCollectionTime = 1.0;
		super.atScanEnd();
	}

	public void setDarkCurrent(DarkCurrentResults darkCurrent) {
		this.darkCurrent = darkCurrent;
	}

	public Double[] getDarkCurrent() {
		if (darkCurrent == null){
			return new Double[]{};
		}
		return darkCurrent.getCounts();
	}

	public boolean isDarkCurrentRequired() {
		return darkCurrentRequired;
	}

	public void setDarkCurrentRequired(boolean darkCurrentRequired) {
		this.darkCurrentRequired = darkCurrentRequired;
	}

	public void setDarkCurrentCollectionTime(Double darkCurrentCollectionTime) {
		this.darkCurrentCollectionTime = darkCurrentCollectionTime;
	}

	public Double getDarkCurrentCollectionTime() {
		return darkCurrentCollectionTime;
	}

	public boolean isUseReset() {
		return useReset;
	}

	/**
	 * True by default, set to false if the shutter used to collect the dark current should not have reset called before opening.
	 * 
	 * @param useReset
	 */
	public void setUseReset(boolean useReset) {
		this.useReset = useReset;
	}

	protected double[] adjustForDarkCurrent(double[] values, Double collectionTime) {

		if (!isDarkCurrentRequired() || darkCurrent == null || darkCurrent.timeInS <= 0.0) {
			return values;
		}

		int channel = 0;
		int darkChannelOffset = 0;
		if (timeChannelRequired) {
			channel = 1;
			darkChannelOffset = -1;
		}

		for (; channel < values.length; channel++) {
			values[channel] = adjustChannelForDarkCurrent(values[channel], darkCurrent.getCounts()[channel
					+ darkChannelOffset], collectionTime);
		}

		return values;
	}

	protected Double adjustChannelForDarkCurrent(Double rawCounts, Double dkCounts, Double collectionTime) {
		if (dkCounts > 0){
			rawCounts = rawCounts - (collectionTime / darkCurrent.getTimeInS()) * dkCounts;
		}
		rawCounts = rawCounts > 0 ? rawCounts : 0;
		return rawCounts;
	}

	/**
	 * Parameters for dark current collection
	 */
	public static final class DarkCurrentBean {

		private double originalCollectionTime = 1d;
		private double darkCollectionTime = 1d;
		private String shutterName;
		private String counterTimerName;

		public String getShutterName() {
			return shutterName;
		}

		public void setShutterName(String shutterName) {
			this.shutterName = shutterName;
		}

		public String getCounterTimerName() {
			return counterTimerName;
		}

		public void setCounterTimerName(String sounterTimerName) {
			this.counterTimerName = sounterTimerName;
		}

		public double getOriginalCollectionTime() {
			return originalCollectionTime;
		}

		public void setOriginalCollectionTime(double originalCollectionTime) {
			this.originalCollectionTime = originalCollectionTime;
		}

		public void setDarkCollectionTime(double darkCollectionTime) {
			this.darkCollectionTime = darkCollectionTime;
		}

		public double getDarkCollectionTime() {
			return darkCollectionTime;
		}

	}

	public static final class DarkCurrentResults {

		Double timeInS = 1d;
		Double[] counts;

		public DarkCurrentResults(Double timeInS, Double[] counts) {
			super();
			this.timeInS = timeInS;
			this.counts = counts;
		}

		public Double getTimeInS() {
			return timeInS;
		}

		public void setTimeInS(Double timeInS) {
			this.timeInS = timeInS;
		}

		public Double[] getCounts() {
			return counts;
		}

		public void setCounts(Double[] counts) {
			this.counts = counts;
		}
	}
}
