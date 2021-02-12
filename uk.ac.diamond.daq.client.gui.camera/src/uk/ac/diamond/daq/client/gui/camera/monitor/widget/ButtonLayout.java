/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.monitor.widget;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Maps the {@link ClientImages} to a predefined GUI layout
 *
 * @author Maurizio Nagni
 */
public enum ButtonLayout {
	UNAVAILABLE(ClientMessages.CAMERA_UNAVAILABLE, ClientImages.STATE_ERROR),
	IDLE(ClientMessages.CAMERA_IDLE, ClientImages.STATE_IDLE),
	ACQUIRING(ClientMessages.CAMERA_ACQUIRING, ClientImages.STATE_ACTIVE),
	OTHER(ClientMessages.CAMERA_OTHER, ClientImages.STATE_WARNING);

	private final ClientMessages message;
	private final ClientImages image;

	ButtonLayout(ClientMessages message, ClientImages image) {
		this.message = message;
		this.image = image;
	}

	public ClientMessages getMessage() {
		return message;
	}

	public ClientImages getImage() {
		return image;
	}
}
