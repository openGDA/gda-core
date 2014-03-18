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

package gda.device.detector.areadetector.v17;

/** 
 * The ADCommon interface allows beans whose classes implement either the ADBase or GetPluginBaseAvailable
 * interfaces to be referenced by beans which could take either as their input.
 * 
 * For instance an NDFile could be told to take it's input either direct from the camera (an ADBaseImpl)
 * or from an NDProcessImpl, thus configure its NDArrayPort from it's inputs actual PortName_RBV, rather
 * than having the port name string hard coded into the bean.
 */
public interface ADCommon {

	/**
	 * Get the port name used to connect this NDPluginBase/ADBase to downstream plugins by their
	 * 
	 */
	String getPortName_RBV() throws Exception;

	/* Comparing ADBase & NDPluginBase interfaces, possible candidates to move in here are:
	 * 
	 * getArrayRate_RBV
	 * getArrayCounter_RBV
	 * getArrayCounter
	 * getArraySize_RBV
	 * getColorMode_RBV
	 * getDataType_RBV
	 * getNDAttributesFile
	 * reset
	 * setArrayCounter
	 * setNDAttributesFile
	 */
}
