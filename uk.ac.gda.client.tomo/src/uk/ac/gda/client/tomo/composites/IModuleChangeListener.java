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

package uk.ac.gda.client.tomo.composites;

import java.lang.reflect.InvocationTargetException;

import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;

/**
 * Change listener interface so that action can be taken upon change to the modules
 */
public interface IModuleChangeListener {
	/**
	 * propagates the new module to all the module change listeners.
	 * 
	 * @param oldModule
	 * @param newModule
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	void moduleChanged(CAMERA_MODULE oldModule, CAMERA_MODULE newModule) throws InterruptedException,
			InvocationTargetException;
}