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

package gda.device.detector;

/**
 * For slow detectors which can be readout (likely through the core plugin's PositionCallableProvider system) while for
 * example motors are being moved. The detector may report that its not busy, but still be reading out. ConcurrentScan will call this hook on all Scannables at the current level that
 * implement this interface before triggering Scannables to move and Detectors to collect data.
 */
public interface DetectorWithReadout {

	/**
	 *  This method waits for the detector to finish
	 * reading out and become ready to collectData.
	 * @throws InterruptedException 
	 */
	public void waitForReadoutCompletion() throws InterruptedException;

}
