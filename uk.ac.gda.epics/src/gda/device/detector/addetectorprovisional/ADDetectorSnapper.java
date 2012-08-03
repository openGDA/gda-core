/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.addetectorprovisional;

import gda.device.detector.addetector.filewriter.FileWriter;
import gda.device.detector.addetector.triggering.ADTriggeringStrategy;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDFile;

public class ADDetectorSnapper implements DetectorSnapper {


	private final FileWriter fileWriter;

	private final ADTriggeringStrategy collectionStrategy;

	/**
	 * Controls whether the driver does callbacks with the array data to registered plugins. 0=No, 1=Yes. 
	 * Setting this to 0 can reduce overhead in the case that the driver is being used only to control the device, and not to make the data available to plugins or to EPICS clients.
	 */
	private boolean disableCallbacks = false;

	private final ADBase adBase;
	
	
	public ADDetectorSnapper(ADBase adBase, ADTriggeringStrategy collectionStrategy, FileWriter fileWriter) {
		this.collectionStrategy = collectionStrategy;
		this.fileWriter = fileWriter;
		this.adBase = adBase;
	}

	public FileWriter getFileWriter() {
		return fileWriter;
	}
	
	public ADTriggeringStrategy getCollectionStrategy() {
		return collectionStrategy;
	}

	public boolean isDisableCallbacks() {
		return disableCallbacks;
	}

	public void setDisableCallbacks(boolean disableCallbacks) {
		this.disableCallbacks = disableCallbacks;
	}

	
	@Override
	public void prepareForAcquisition(double collectionTime) throws Exception {
		// NOTE: copied from ADDetector.atScanStart()

		//disable or enable callbacks if required by class variable first to allow collectionStrategy or fileWriter to turn back on needed
		adBase.setArrayCallbacks( isDisableCallbacks() ? 0 :1);
		int numberImagesPerCollection = getCollectionStrategy().getNumberImagesPerCollection(collectionTime);
		getCollectionStrategy().prepareForCollection(collectionTime, 1);
		getFileWriter().prepareForCollection(numberImagesPerCollection);

	}

	@Override
	public double getAcquireTime() throws Exception {
		return getCollectionStrategy().getAcquireTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getCollectionStrategy().getAcquirePeriod();
	}

	/**
	 * Acquire and return a list of resulting files.
	 * @throws Exception 
	 */
	@Override
	public String[] acquire() throws Exception {
		getCollectionStrategy().collectData();
		return new String[] {getFileWriter().getFullFileName_RBV()};
	}

}
