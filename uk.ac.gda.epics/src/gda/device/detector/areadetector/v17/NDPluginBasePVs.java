/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import gda.epics.PV;

/**
 * Represents an NDPluginDriver. Name reflects the unfortunately named {@link NDPluginBase}.
 * <p>
 * Documentation from: http://cars9.uchicago.edu/software/epics/pluginDoc.html#NDPluginDriver
 */
public interface NDPluginBasePVs {

	/**
	 * Enable (1) or disable (0) callbacks from the driver to this plugin. If callbacks are disabled then the plugin
	 * will normally be idle and consume no CPU resources.
	 * 
	 * @return EnableCallbacks / EnableCallbacks_RBV pair.
	 */
	PV<Boolean> getEnableCallbacksPVPair();

	/**
	 * 0 = callbacks from the driver do not block; the NDArray data is put on a queue and the callback processes in its
	 * own thread. 1 = callbacks from the driver block; the callback processes in the driver callback thread.
	 * 
	 * @return BlockingCallbacks / BlockingCallbacks_RBV pair.
	 */
	PV<Boolean> getBlockingCallbacksPVPair();

	/**
	 * Counter that increments by 1 each time an NDArray callback occurs when NDPluginDriverBlockingCallbacks=0 and the
	 * plugin driver queue is full, so the callback cannot be processed.
	 * 
	 * @return DroppedArrays / DroppedArrays_RBV pair.
	 */
	PV<Boolean> getDroppedArraysPVPair();

	/**
	 * Counter that increments by 1 each time an NDArray callback occurs when NDPluginDriverBlockingCallbacks=0 and the
	 * plugin driver queue is full, so the callback cannot be processed.
	 * 
	 * @return ArrayCounter / ArrayCounter pair.
	 */
	PV<Boolean> getArrayCounterPVPair();

	/**
	 * Asyn port name for NDArray driver that will make callbacks to this plugin. This port can be changed at run time,
	 * connecting the plugin to a different NDArray driver.
	 * 
	 * @return NDArrayPort / NDArrayPort_RBV pair.
	 */
	PV<String> getNDArrayPortPVPair();

}
