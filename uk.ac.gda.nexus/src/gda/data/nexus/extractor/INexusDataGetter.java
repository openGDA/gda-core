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

package gda.data.nexus.extractor;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.dawnsci.nexus.NexusException;


/**
 *
 */
public interface INexusDataGetter {
	/**
	 * @param name 
	 * @param nxClass 
	 * @param getData
	 * @return @see gda.data.nexus.extractor.NexusGroupData
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	public NexusGroupData getDataForCurrentProcessedGroup(String name, String nxClass, boolean getData) throws NexusException, NexusExtractorException;
	
	/**
	 * @param attrName name of attribute e.g. target
	 * @return value of target attribute if present else null
	 * @throws NexusException 
	 * @throws NexusExtractorException 
	 */
	public NexusGroupData getAttributeOfCurrentProcessedGroup(String attrName)throws NexusException, NexusExtractorException;
	
	
	/**
	 * @return The id of the source e.g. path to the file
	 * @throws MalformedURLException 
	 */
	public URL getSourceId() throws MalformedURLException;

}
