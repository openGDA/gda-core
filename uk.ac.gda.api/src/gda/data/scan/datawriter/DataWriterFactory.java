/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.scan.datawriter;

import gda.factory.FactoryException;
import gda.factory.Findable;

/**
 * Classes implementing this interface provide pre-configured DataWriters for scans
 */
public interface DataWriterFactory extends Findable {

	/**
	 * Creates a new instance of a DataWriter
	 * 
	 * @return a DataWriter
	 * @throws FactoryException
	 */
	public DataWriter createDataWriter() throws FactoryException;
}
