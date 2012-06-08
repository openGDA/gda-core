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

import gda.data.PlottableDetectorData;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;

public interface GDANexusDetectorData  extends NexusTreeProvider, PlottableDetectorData {

	/**
	 * returns the names detectors tree
	 * @param detName if null or empty it returns the first 
	 * @return the NexusTree associated with the named detector
	 * 
	 **/
	public INexusTree getDetTree(String detName);
	
	/**
	 * @param detName
	 * @param dataName name of the child whose data is to be returned. If null or empty the first detector entry is used
	 * @param className class name of the child whose data is to be returned e.g. NexusExtractor.SDSClassName
	 * @return NexusGroupData
	 */	
	public NexusGroupData getData(String detName, String dataName, String className);

	public String[] getOutputFormat();

	public GDANexusDetectorData mergeIn(GDANexusDetectorData data);
}
