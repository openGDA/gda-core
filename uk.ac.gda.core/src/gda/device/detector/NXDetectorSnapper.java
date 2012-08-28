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

package gda.device.detector;

import gda.device.DetectorSnapper;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXFileWriterPlugin;

public class NXDetectorSnapper implements DetectorSnapper {


	private final NXFileWriterPlugin fileWriter;

	private final NXCollectionStrategyPlugin collectionStrategy;

	/**
	 * Controls whether the driver does callbacks with the array data to registered plugins. 0=No, 1=Yes. 
	 * Setting this to 0 can reduce overhead in the case that the driver is being used only to control the device, and not to make the data available to plugins or to EPICS clients.
	 */
	private boolean disableCallbacks = false;

	
	public NXDetectorSnapper(NXCollectionStrategyPlugin collectionStrategy, NXFileWriterPlugin fileWriter) {
		this.collectionStrategy = collectionStrategy;
		this.fileWriter = fileWriter;
	}

	public NXFileWriterPlugin getFileWriter() {
		return fileWriter;
	}
	
	public NXCollectionStrategyPlugin getCollectionStrategy() {
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
		getCollectionStrategy().setGenerateCallbacks(!isDisableCallbacks());
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
		return new String[] {getFileWriter().getFullFileName()};
	}

}
