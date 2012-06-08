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

package gda.rcp.views;

import gda.rcp.GDAClientActivator;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class GdaImages {
	
	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();
	
	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;
	static {
		try {
			fgIconBaseURL = new URL(GDAClientActivator.getDefault().getBundle().getEntry("/"), "icons/" ); //$NON-NLS-1$ //$NON-NLS-2$
			
			createImage("", "user_gray.png");
			createImage("", "computer_go.png");
			createImage("", "control_pause_blue.png");
			createImage("", "delete.png");
			createImage("", "arrow_right.png");
		} catch (MalformedURLException e) {
		}
	}	
	
	private static final String NAME_PREFIX =
		GDAClientActivator.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();	
	
	public static final String VIEWER = "viewer/"; //$NON-NLS-1$

	public static final String IMG_LIBRARY = NAME_PREFIX + "library_obj.gif"; //$NON-NLS-1$	
	public static final ImageDescriptor DESC_IMG_LIBRARY_MACRO = createImage(VIEWER, IMG_LIBRARY);

	public static final String IMG_SW_BOX = NAME_PREFIX + "movingBox.gif"; //$NON-NLS-1$	
	public static final ImageDescriptor IMG_SW_BOX_MACRO = createImage(VIEWER, IMG_SW_BOX);
	
	public static final String IMG_GREEN_DOT = NAME_PREFIX + "public_co.gif"; //$NON-NLS-1$	
	public static final ImageDescriptor IMG_GREEN_DOT_MACRO = createImage(VIEWER, IMG_GREEN_DOT);


	private static ImageDescriptor createImage(String prefix, String name) {
		return createImage(imageRegistry, prefix, name);
	}

	private static ImageDescriptor createImage(
		ImageRegistry registry,
		String prefix,
		String name) {
		ImageDescriptor result =
			ImageDescriptor.createFromURL(
				makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}

	public static Image getImage(String key) {
		return imageRegistry.get(key);
	}

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 */
	public static void setImageDescriptors(
		IAction action,
		String type,
		String relPath) {
		relPath = relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type + "/", relPath)); //$NON-NLS-1$
		action.setHoverImageDescriptor(create("c" + type + "/", relPath)); //$NON-NLS-1$
		action.setImageDescriptor(create("e" + type + "/", relPath)); //$NON-NLS-1$
	}

	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}	
}
