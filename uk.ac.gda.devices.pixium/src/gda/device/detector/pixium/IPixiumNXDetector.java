/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.pixium;

import gda.device.DeviceException;

public interface IPixiumNXDetector {

	public abstract void includeEarlyFrames() throws Exception;

	public abstract void excludeEarlyFrames() throws Exception;

	public abstract void setBaseExposure(double expTime) throws Exception;

	public abstract double getBaseExposure() throws Exception;

	public abstract void setBaseAcquirePeriod(double acqTime) throws Exception;

	public abstract double getBaseAcquirePeriod() throws Exception;

	public abstract void setExposuresPerImage(int numExp) throws Exception;

	public abstract int getExposuresPerImage() throws Exception;

	public abstract void setNumImages(int numImg) throws Exception;

	public abstract int getNumImages() throws Exception;

	public abstract void setPUMode(int mode) throws Exception;

	public abstract int getPUMode() throws Exception;

	public abstract void calibrate() throws Exception;

	public abstract void acquire(double collectionTime, int numImages) throws Exception;

	public abstract void acquire(double collectionTime) throws Exception;

	public abstract void stop() throws DeviceException;

}