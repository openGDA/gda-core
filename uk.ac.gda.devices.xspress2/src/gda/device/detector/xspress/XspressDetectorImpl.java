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

package gda.device.detector.xspress;

import gda.device.DeviceException;
import gda.factory.FactoryException;

public interface XspressDetectorImpl {

	public void configure() throws FactoryException;
	public int getNumberOfDetectors() throws DeviceException;
	public void setCollectionTime(int time) throws DeviceException;
	public void setWindows(int detector, int winStart, int winEnd) throws DeviceException;
	public void clear() throws DeviceException;
	public void start() throws DeviceException;
	public void stop() throws DeviceException;
	public void close() throws DeviceException;
	public void reconfigure() throws DeviceException;
	public int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException;
	public int[] readoutMca(int detector, int startFrame, int numberOfFrames, int mcaSize) throws DeviceException;
}
