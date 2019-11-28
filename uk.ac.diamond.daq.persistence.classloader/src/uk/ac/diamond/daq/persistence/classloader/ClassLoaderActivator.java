/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.persistence.classloader;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class ClassLoaderActivator extends Plugin {
	private static ClassLoaderActivator instance;
	private BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		this.bundleContext = context;
		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

	static <T> T getService(Class<T> serviceClass) {
		BundleContext bundleContext = instance.getBundle().getBundleContext();
		return bundleContext.getService(bundleContext.getServiceReference(serviceClass));
	}

	public static ClassLoaderActivator getInstance() {
		return instance;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}
}
