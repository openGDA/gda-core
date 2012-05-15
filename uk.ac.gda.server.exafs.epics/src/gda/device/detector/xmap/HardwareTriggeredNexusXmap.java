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

package gda.device.detector.xmap;

import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.device.detector.NexusDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.Configurable;

public interface HardwareTriggeredNexusXmap extends XmapDetector,HardwareTriggerableDetector,
		PositionCallableProvider, Configurable, NexusDetector {

	public String getHDFFileName()throws DeviceException;
	public void waitForFile(String fileName)throws DeviceException, InterruptedException;
	public Xmap getXmap();
	public boolean isInBufferMode() throws Exception;
}
