/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.viewer;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Extends {@link ThreeStateDisplay} to add a fourth (grey) state
 */
public class FourStateDisplay extends ThreeStateDisplay {

	private static final Image ICON_GREY = new Image(Display.getDefault(), FourStateDisplay.class.getResourceAsStream(ICONS_DIR + "grey.png"));

	private final String messageGrey;

	public FourStateDisplay(Composite parent, String messageGreen, String messageYellow, String messageRed, String messageGrey) {
		super(parent, messageGreen, messageYellow, messageRed);
		this.messageGrey = messageGrey;
	}

	/**
	 * Display grey icon and default message
	 */
	public void setGrey() {
		setGrey(messageGrey);
	}

	/**
	 * Display grey icon and specified message
	 */
	public void setGrey(String message) {
		setImage(ICON_GREY);
		setMessage(message);
	}
}
