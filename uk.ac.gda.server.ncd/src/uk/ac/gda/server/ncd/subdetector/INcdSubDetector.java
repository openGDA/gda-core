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

package uk.ac.gda.server.ncd.subdetector;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;


/**
 * A class to represent a physical detector for NCD.
 */
public interface INcdSubDetector extends Device {
	
	public static final String descriptionLabel = "description";

	public void clear() throws DeviceException;

	public void start() throws DeviceException;

	public void stop() throws DeviceException;
	
	public String getDetectorType() throws DeviceException;
	
	/**
	 * add detector data for frames to the returned data type
	 * @param frames
	 * @param dataTree
	 * @throws DeviceException
	 */
	void writeout(int frames, NXDetectorData dataTree) throws DeviceException;
	
	public int getMemorySize() throws DeviceException;
	
	public List<DataDimension> getSupportedDimensions() throws DeviceException;
	
	public void setDataDimensions(int[] detectorSize) throws DeviceException;
	
	public int[] getDataDimensions() throws DeviceException;
	
	/**
	 * Get the size of pixels in x (for 1d detectors) or x and y (for 2d detectors)
	 * @return pixel size in metres
	 */
	public double getPixelSize() throws DeviceException;

    public void atScanStart() throws DeviceException;
    
    public void atScanEnd() throws DeviceException;
    
    public void setTimer(Timer timer) throws DeviceException;
    
    public DetectorProperties getDetectorProperties() throws DeviceException;
}