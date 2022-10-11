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

import org.eclipse.dawnsci.nexus.NexusException;


/**
 * see the package-info.java file for information on this class
 */
public interface INexusTreeProcessor {
	/**
	 * 
	 */
	public enum RESPONSE {
		/**
		 * Carry on with this element and its children
		 */
		GO_INTO,
		/**
		 * Stop processing
		 */
		NO_MORE,
		/**
		 * Skip this element and its children
		 */
		SKIP_OVER,
		/**
		 * Skip this element and its children but get attributes
		 */
		SDS_ATTR;
	}

	/**
	 * @param name
	 * @param nxClass
	 * @param nexusDataGetter
	 * @return @see RESPONSE
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	public RESPONSE beginElement(String name, String nxClass, INexusDataGetter nexusDataGetter) throws NexusException,
			NexusExtractorException;

	/**
	 * 
	 */
	public void endElement();
}
