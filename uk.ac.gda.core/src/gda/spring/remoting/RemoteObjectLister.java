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

package gda.spring.remoting;

import java.util.Map;

/**
 * A service that returns the names of available remote objects together with
 * the interface that each object implements.
 */
public interface RemoteObjectLister {
	
	/**
	 * Returns a map of the available objects. The keys are object names, and
	 * the values are class names.
	 * 
	 * @return map of available objects
	 */
	public Map<String, String> getAvailableObjects();

}
