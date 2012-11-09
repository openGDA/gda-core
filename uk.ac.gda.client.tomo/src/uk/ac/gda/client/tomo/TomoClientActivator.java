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

package uk.ac.gda.client.tomo;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TomoClientActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.gda.client.tomo"; //$NON-NLS-1$

	// The shared instance
	private static TomoClientActivator plugin;

	/**
	 * The constructor
	 */
	public TomoClientActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		// getImageRegistry().dispose();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static TomoClientActivator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		for (String imgPath : ImageConstants.IMAGES) {
			reg.put(imgPath, imageDescriptorFromPlugin(PLUGIN_ID, imgPath));
		}
	}

	private static HashMap<Integer, Integer> resolutionProjections;

	public static HashMap<Integer, Integer> getResolutionProjections() {
		if (resolutionProjections == null) {
			resolutionProjections = new HashMap<Integer, Integer>();
			resolutionProjections.put(1, 6000);
			resolutionProjections.put(2, 3000);
			resolutionProjections.put(4, 3000);
			resolutionProjections.put(8, 1800);
		}
		return resolutionProjections;
	}

	private static HashMap<Integer, Integer> resolutionBinning;

	public static HashMap<Integer, Integer> getResolutionBinning() {
		if (resolutionBinning == null) {
			resolutionBinning = new HashMap<Integer, Integer>();
			resolutionBinning.put(1, 1);
			resolutionBinning.put(2, 1);
			resolutionBinning.put(4, 4);
			resolutionBinning.put(8, 8);
		}
		return resolutionBinning;
	}

}
