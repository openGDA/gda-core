/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server;

import java.util.Optional;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
*/
public class GDAServerActivator extends Plugin {

	public static final String PLUGIN_ID = "uk.ac.diamond.daq.server";

	// The shared instance
	private static GDAServerActivator plugin;
	private static BundleContext context;

	public GDAServerActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		GDAServerActivator.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		GDAServerActivator.context = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GDAServerActivator getDefault() {
		return plugin;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public static <T> Optional<T> getService(Class<T> serviceClass) {
		if (context == null) return Optional.empty();
		return Optional.of(serviceClass)
				.map(context::getServiceReference)
				.map(context::getService);
	}
}
