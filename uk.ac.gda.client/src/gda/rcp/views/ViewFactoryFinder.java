/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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
import org.eclipse.ui.part.ViewPart;
import org.springframework.util.StringUtils;

/**
 * Class that can be used in an Eclipse view extension to locate a factory
 * object that can create an Eclipse view.
 * 
 * <p>To use this:</p>
 * 
 * <ol>
 * <li>Add a bean to your Spring context that implements {@link Findable} (so it can be found by the Finder) and
 * {@link IViewFactory}. ({@link FindableViewFactoryBase} can be used as a starting point.)
 * <li>Define a view extension with the class name "{@code gda.rcp.views.ViewFactoryFinder:yourBeanName}".</li>
 * </ol>
 */
public class ViewFactoryFinder implements IExecutableExtensionFactory, IExecutableExtension {
	
	private String factoryBeanName;
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (propertyName.equals("class") && data instanceof String) {
			factoryBeanName = (String) data;
		}
	}
	
	@Override
	public Object create() throws CoreException {
		
		if (!StringUtils.hasText(factoryBeanName)) {
			final String msg = String.format("View factory bean name not specified");
			throw new CoreException(new Status(IStatus.ERROR, GDAClientActivator.PLUGIN_ID, msg));
		}
		
		Findable findable = Finder.getInstance().find(factoryBeanName);
		if (findable == null) {
			final String msg = String.format("No view factory bean with the name %s", StringUtils.quote(factoryBeanName));
			throw new CoreException(new Status(IStatus.ERROR, GDAClientActivator.PLUGIN_ID, msg));
		}
		
		if (!(findable instanceof IViewFactory)) {
			final String msg = String.format("Bean %s is not a view factory", StringUtils.quote(factoryBeanName));
			throw new CoreException(new Status(IStatus.ERROR, GDAClientActivator.PLUGIN_ID, msg));
		}
		
		IViewFactory viewFactory = (IViewFactory) findable;
		ViewPart view = viewFactory.createView();
		return view;
	}

}