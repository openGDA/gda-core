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

package uk.ac.gda.common.rcp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/*
 * A class that can be used by an Activator to provide support for services. Particularly useful
 * when selecting a service from a collection of services based on the String value of a property.
 *
 * namedServiceProvider = new NamedServiceProvider(bundleContext);
 * namedServiceProvider.getNamedService(clzz, "SERVICE_NAME", name);
 *
 * in bundle stop call namedServiceProvider.close()
 */
public class NamedServiceProvider {

	private BundleContext bundleContext;

	public NamedServiceProvider(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		if (bundleContext == null)
			throw new IllegalStateException("BundleContext is null");
	}

	public void close() {
		if( !serviceTrackers.isEmpty()){
			for (Entry<String, ServiceTracker<?, ?>> st : serviceTrackers.entrySet()) {
				st.getValue().close();
			}
			serviceTrackers.clear();
		}
		bundleContext = null;
	}

	Map<String, ServiceTracker<?, ?>> serviceTrackers = new HashMap<String, ServiceTracker<?, ?>>();

	public <T> T getNamedService(Class<T> clzz, final String propertyKey, final String name) {
		if (bundleContext == null)
			throw new IllegalStateException("BundleContext is null");
		boolean checkName = name !=null && !name.isEmpty();
		String serviceClassAndName = clzz.getName();
		if(checkName){
			serviceClassAndName += (":" + propertyKey + ":" + name);
		}
		@SuppressWarnings("unchecked")
		ServiceTracker<T, T> tracker = (ServiceTracker<T, T>) serviceTrackers.get(serviceClassAndName);
		if (tracker == null) {
			ServiceTrackerCustomizer<T, T> customizer = null;
			if( checkName){
				customizer = new ServiceTrackerCustomizer<T, T>() {

					@Override
					public T addingService(ServiceReference<T> reference) {
						if (reference.getProperty(propertyKey).equals(name))
							return bundleContext.getService(reference);
						return null;
					}

					@Override
					public void modifiedService(ServiceReference<T> reference, T service) {
					}

					@Override
					public void removedService(ServiceReference<T> reference, T service) {
					}
				};
			}
			tracker = new ServiceTracker<T, T>(bundleContext, clzz.getName(), customizer);
			tracker.open(true);
			serviceTrackers.put(serviceClassAndName, tracker);
		}
		return tracker.isEmpty() ? null : tracker.getService();
	}

}
