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

/**
 * Called by ADDetector;s Detector hooks (in the order triggered by ConcurrentScan)
 */
public interface ADTriggeringStrategy {

	public void prepareForCollection(double collectionTime, int numImages) throws Exception;
	
	public double getAcquireTime() throws Exception;

	public double getAcquirePeriod()  throws Exception;

	public void collectData() throws Exception;

	public void endCollection() throws Exception;

	public void stop() throws Exception;

	public void atCommandFailure() throws Exception;
	
	public int getStatus() throws Exception;
	
	public void waitWhileBusy() throws InterruptedException, Exception;
	
	/*
	 * Returns the number of images required to achieve the desired collectionTime
	 * This is number of images per ScanDataPoint
	 */
	public int getNumberImagesPerCollection(double collectionTime);
}
