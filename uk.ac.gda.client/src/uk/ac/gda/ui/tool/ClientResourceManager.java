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

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

public class ClientResourceManager {

	private static ClientResourceManager instance;
	private static final String FONT = "Arial";

	private ResourceManager resourceManager;
	private Font groupFont;
	private Font labelFont;
	private Font textFont;
	private Font buttonFont;

	private ClientResourceManager() {

	}

	public static synchronized ClientResourceManager getInstance() {
		if (instance == null) {
			instance = new ClientResourceManager();
		}
		return instance;
	}

	public synchronized ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return resourceManager;
	}

	public synchronized Font getTextDefaultFont() {
		if (textFont == null) {
			textFont = getFont(FontDescriptor.createFrom(FONT, 10, SWT.NORMAL));
		}
		return textFont;
	}

	public synchronized Font getTextDefaultItalicFont() {
		if (textFont == null) {
			textFont = getFont(FontDescriptor.createFrom(FONT, 10, SWT.ITALIC));
		}
		return textFont;
	}

	public synchronized Font getFont(final FontDescriptor fontDescriptor) {
		return getResourceManager().createFont(fontDescriptor);
	}

	public Font getLabelDefaultFont() {
		if (labelFont == null) {
			labelFont = getFont(FontDescriptor.createFrom(FONT, 10, SWT.NORMAL));
		}
		return labelFont;
	}

	public Font getGroupDefaultFont() {
		if (groupFont == null) {
			groupFont = getFont(FontDescriptor.createFrom(FONT, 10, SWT.BOLD));
		}
		return groupFont;
	}

	public Font getButtonDefaultFont() {
		if (buttonFont == null) {
			buttonFont = getFont(FontDescriptor.createFrom(FONT, 10, SWT.NORMAL));
		}
		return buttonFont;
	}

	public static String getDefaultFont() {
		return FONT;
	}

}
