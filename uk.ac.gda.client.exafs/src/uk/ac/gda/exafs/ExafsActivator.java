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

package uk.ac.gda.exafs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Activator for Exafs Plugin
 */
public class ExafsActivator extends AbstractUIPlugin {

	// The plug-in ID - corresponds to name in plugin.xml file
	/**
	 *
	 */
	public static final String PLUGIN_ID = "uk.ac.diamond.gda.rcp";

	// The shared instance
	private static ExafsActivator plugin;

	/**
	 * The constructor
	 */
	public ExafsActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ExafsActivator getDefault() {
		return plugin;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Method to get a service.
	 *
	 * @param serviceClass
	 * @return any loaded OSGi service which the bundle can see.
	 */
	public static <T> T getService(Class<T> serviceClass) {
		ServiceReference<T> ref = plugin.getBundle().getBundleContext().getServiceReference(serviceClass);
		return plugin.getBundle().getBundleContext().getService(ref);
	}
	private static IPreferenceStore store = new PreferenceStore();

	public static IPreferenceStore getStore() {
		if (plugin!=null) return plugin.getPreferenceStore();
		return store;
	}
}
