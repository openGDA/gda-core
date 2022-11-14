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

package uk.ac.diamond.daq.experiment.api.ui;

import java.beans.PropertyChangeListener;

/**
 * Implementors of this interface will be compatible with
 * ListWithCustomEditor ElementEditor.
 *
 */
public interface EditableWithListWidget {

	/**
	 * This is the label shown in the list
	 */
	String getLabel();

	/**
	 * Called when the 'add' button is pressed
	 */
	EditableWithListWidget createDefault();

	static final String REFRESH_PROPERTY = "refreshRequest";

	/**
	 * Any event fired through your type's property change support
	 * will trigger a list refresh
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Listener is removed from an instance
	 * when said instance is deleted from the list
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);
}
