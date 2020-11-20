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

package uk.ac.diamond.daq.client.gui.camera;

import java.io.Serializable;

import org.eclipse.swt.widgets.Combo;

import uk.ac.gda.client.properties.CameraProperties;

/**
 * Represents an camera item in a {@link Combo}. <br/>
 * <bold>IMPORTANT</bold> For now instances of this class are created by
 * {@link CameraHelper#getCameraComboItems()} consequently the
 * {@link #getIndex()} returns the same index as in the client configuration
 * file.
 *
 * @author Maurizio Nagni
 */
public class CameraComboItem implements Serializable {

	private static final long serialVersionUID = 5926542162079849239L;

	/**
	 * The item index in the combo.
	 */
	private final int index;
	/**
	 * The label used in the combo
	 */
	private final String name;

	CameraComboItem(CameraProperties properties) {
		this.name = properties.getName();
		this.index = properties.getIndex();
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}
}