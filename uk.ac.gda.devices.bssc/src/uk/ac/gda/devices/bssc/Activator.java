/*-
 * Copyright © 2011 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury & Rutherford Laboratory
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

package uk.ac.gda.devices.bssc;

import java.util.Hashtable;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.dataset.IDatasetMathsService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import uk.ac.diamond.scisoft.analysis.osgi.DatasetMathsServiceImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.gda.devices.bssc"; //$NON-NLS-1$
	public static final String DATA_ANALYSIS_IMAGE_ID = "icons/DataAnalysisResult.png";

	// The shared instance
	private static Activator plugin;

	private BundleContext context;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
		plugin = this;
		Hashtable<String, String> props = new Hashtable<String, String>(1);
		props = new Hashtable<String, String>(1);
		props.put("description", "A service which replaces concrete classes in the scisoft.analysis plugin.");
		// The following service is required for dataset plotting in the BioSAXS Results perspective
		context.registerService(IDatasetMathsService.class, new DatasetMathsServiceImpl(), props);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		super.initializeImageRegistry(registry);
		Bundle bundle = Platform.getBundle(PLUGIN_ID);

		ImageDescriptor bioISISImage = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(
				DATA_ANALYSIS_IMAGE_ID), null));
		registry.put(DATA_ANALYSIS_IMAGE_ID, bioISISImage);
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
	public static Activator getDefault() {
		return plugin;
	}
}