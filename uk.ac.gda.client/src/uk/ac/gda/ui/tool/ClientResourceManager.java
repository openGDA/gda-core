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

package uk.ac.gda.ui.tool;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

/**
 * A centralised service for client fonts
 *
 * @author Maurizio Nagni
 */
public class ClientResourceManager {

	private static Map<FontDescriptor, Font> fontMap = new HashMap<>();

	private static ClientResourceManager instance;
	private static final String FONT = "Arial";

	private ResourceManager resourceManager;

	private static FontDescriptor labelFontDescriptor = FontDescriptor.createFrom(FONT, 10, SWT.NORMAL);
	private static FontDescriptor textFontDescriptor = FontDescriptor.createFrom(FONT, 10, SWT.NORMAL);
	private static FontDescriptor buttonFontDescriptor = FontDescriptor.createFrom(FONT, 10, SWT.NORMAL);
	private static FontDescriptor textItalicFontDescriptor = FontDescriptor.createFrom(FONT, 10, SWT.ITALIC);
	private static FontDescriptor groupFontDescriptor = FontDescriptor.createFrom(FONT, 10, SWT.BOLD);

	private ClientResourceManager() {

	}

	public static synchronized ClientResourceManager getInstance() {
		if (instance == null) {
			instance = new ClientResourceManager();
		}
		return instance;
	}

	private synchronized ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return resourceManager;
	}

	/**
	 * Returns the Font described by the given FontDescriptor.
	 *
	 * @param descriptor
	 *            description of the font to create
	 * @return the Font described by the given descriptor
	 * @throws DeviceResourceException
	 *             if unable to create the font
	 */
	public synchronized Font getFont(final FontDescriptor descriptor) {
		return getFromFontMap(descriptor);
	}

	/**
	 * Returns the default Font for Text control
	 *
	 * @return the default Font for Text control
	 */
	public synchronized Font getTextDefaultFont() {
		return getFromFontMap(textFontDescriptor);
	}

	/**
	 * Returns the default Font for Italic Text control
	 *
	 * @return the default Font for Italic Text control
	 */
	public synchronized Font getTextDefaultItalicFont() {
		return getFromFontMap(textItalicFontDescriptor);
	}

	/**
	 * Returns the default Font for Label control
	 *
	 * @return the default Font for Label control
	 */
	public Font getLabelDefaultFont() {
		return getFromFontMap(labelFontDescriptor);
	}

	/**
	 * Returns the default Font for Group control
	 *
	 * @return the default Font for Group control
	 */
	public Font getGroupDefaultFont() {
		return getFromFontMap(groupFontDescriptor);
	}

	/**
	 * Returns the default Font for Button control
	 *
	 * @return the default Font for Button control
	 */
	public Font getButtonDefaultFont() {
		return getFromFontMap(buttonFontDescriptor);
	}

	/**
	 * The name of the default Font for the client
	 *
	 * @return the client Font name
	 */
	public static String getDefaultFont() {
		return FONT;
	}

	private Font getFromFontMap(FontDescriptor descriptor) {
		return fontMap.computeIfAbsent(descriptor, this.getResourceManager()::createFont);
	}
}
