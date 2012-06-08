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

package gda.rcp.views;

import gda.factory.Findable;
import gda.factory.Finder;
import gda.rcp.GDAClientActivator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Abstract class to aid creation of views from views configured in Spring
 */
public class ViewFactory implements IExecutableExtensionFactory, IExecutableExtension {
	String nameOfFindableExecutableExtension="";
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		if( propertyName.equals("class") && data instanceof String){
			nameOfFindableExecutableExtension = (String)data;
		}

	}

	@Override
	public Object create() throws CoreException {
		Findable findable = Finder.getInstance().find(nameOfFindableExecutableExtension);
		if( findable == null || !(findable instanceof FindableExecutableExtension))
			throw new CoreException(new Status(IStatus.ERROR, GDAClientActivator.PLUGIN_ID,
					"Unable to find a FindableExecutableExtension called '" + nameOfFindableExecutableExtension + "'"));
		return ((FindableExecutableExtension)findable).create();
	}

}