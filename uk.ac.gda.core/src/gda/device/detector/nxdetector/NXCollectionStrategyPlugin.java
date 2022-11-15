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

package gda.device.detector.nxdetector;

import gda.scan.ScanInformation;

/**
 * Called by ADDetector;s Detector hooks (in the order triggered by ConcurrentScan)
 */
public interface NXCollectionStrategyPlugin extends NXPluginBase {

	public double getAcquireTime() throws Exception;

	public double getAcquirePeriod() throws Exception;

	@Deprecated(since="GDA 8.26")
	// This does not need to be on the interface. It duplicates some of the behaviour in prepareForCollection
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception;

	void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception;

	public void collectData() throws Exception;

	public int getStatus() throws Exception;

	public void waitWhileBusy() throws InterruptedException, Exception;

	public void setGenerateCallbacks(boolean b);

	public boolean isGenerateCallbacks();

	/**
	 * Returns the number of images required to achieve the desired collectionTime (seconds) This is number of images
	 * per ScanDataPoint. May also ignore the collection time.
	 */
	public int getNumberImagesPerCollection(double collectionTime) throws Exception;

	/**
	 * @return Return true if all other NXPlugins in the NXDetector must support asynchronous read , i.e. their
	 *         supportsAsynchronousRead method returns true.
	 *         <p>
	 *         For example in a trajectory scan the data for the plugins must be stored in some form of hardware cache
	 *         that can be read out asynchronously to the scan.
	 */
	public boolean requiresAsynchronousPlugins();
}
