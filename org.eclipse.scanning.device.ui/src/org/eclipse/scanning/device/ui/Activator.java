/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.device.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.scanning.api.IServiceResolver;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Activator extends AbstractUIPlugin implements IServiceResolver {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.scanning.device.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private BundleContext context;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.context = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	private static IPreferenceStore store;
	public static IPreferenceStore getStore() {
		if (plugin!=null) return plugin.getPreferenceStore();
		if (store==null) store = new PreferenceStore();
		return store;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		if (plugin==null) {
			// create an image outside of eclipse, used for tests
			final ImageData data = new ImageData("../"+PLUGIN_ID+"/"+path);
			return new ImageDescriptor() {
				@Override
				public ImageData getImageData() {
					return data;
				}
			};
		}

		final ImageRegistry imageRegistry = getDefault().getImageRegistry();
		ImageDescriptor descriptor = imageRegistry.getDescriptor(path);
		if (descriptor == null) {
			descriptor = imageDescriptorFromPlugin(PLUGIN_ID, path);
			imageRegistry.put(path, descriptor);
		}
		return descriptor;
	}

	public static Image getImage(final String path) {
		ImageDescriptor imageDescriptor = getImageDescriptor(path); // loads the ImageDescriptor into the image registry
		if (plugin == null) {
			return imageDescriptor.createImage();
		}
		return getDefault().getImageRegistry().get(path);
	}

	@Override
	public <T> T getService(Class<T> serviceClass) {
		if (context==null) return null;
		final ServiceReference<T> ref = context.getServiceReference(serviceClass);
		if (ref == null) return null;
		return context.getService(ref);
	}

	@Override
	public <T> Collection<T> getServices(Class<T> serviceClass) throws InvalidSyntaxException {
		if (context==null) return Collections.emptySet();
		final Collection<ServiceReference<T>> refs = context.getServiceReferences(serviceClass, null);
		if (refs==null) return Collections.emptySet();
		final Collection<T> ret = new LinkedHashSet<T>(refs.size());
		for (ServiceReference<T> ref : refs) ret.add(context.getService(ref));
		return ret;
	}

}
