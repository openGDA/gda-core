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

package gda.util;

import gda.device.Detector;
import gda.device.Scannable;
import gda.factory.Finder;

/**
 * Only used by XAS scans and filewriters
 */
public class DarkCurrentReader {

	/**
	 * Returns the complete counts
	 * 
	 * @param bean
	 * @return complete data array from counter timer
	 * @throws Exception
	 */
	public static final DarkCurrentResults getDarkCurrent(final DarkCurrentBean bean) throws Exception {

		final Scannable shutter = (Scannable) Finder.getInstance().find(bean.getShutterName());
		String originalShutterPosition = shutter.getPosition().toString();
		Boolean openAtEnd = true;
		if (originalShutterPosition.equalsIgnoreCase("Closed")) {
			openAtEnd = false;
		} else {
			shutter.moveTo("Close");
		}

		final Detector timer = (Detector) Finder.getInstance().find(bean.getCounterTimerName());

		timer.setCollectionTime(bean.getDarkCollectionTime());
		// do not change the collection time - simply re-use the current time which the
		timer.collectData();

		Thread.sleep(Math.round(bean.getDarkCollectionTime()) * 1000); // Throws interrupted exception if the thread executing this method.
		while(timer.isBusy()){
			// TODO timeout of 2*bean.getDarkCollectionTime()
			Thread.sleep(100);
		}

		final Double[] counts = (Double[]) timer.readout();
		if (openAtEnd) {
			shutter.moveTo("Reset");
			shutter.moveTo("Open");
		}

		timer.setCollectionTime(bean.getOriginalCollectionTime());

		return new DarkCurrentResults(timer.getCollectionTime(), counts);
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
