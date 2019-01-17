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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for a section of an {@link AbstractSectionsView}
 */
public interface IMappingSection {

	/**
	 * Initialise the section
	 *
	 * @param mappingView
	 *            the parent view of this section
	 */
	void initialize(AbstractSectionsView mappingView);

	/**
	 * Create the controls for this section
	 *
	 * @param parent
	 *            the parent Composite for this section
	 */
	void createControls(Composite parent);

	/**
	 * Perform any cleanup necessary for this section
	 */
	void dispose();

	/**
	 * Controls whether this section should be shown in the mapping view
	 * <p>
	 * For example, the processing section is not shown if there are no processing files.
	 *
	 * @return true if the section should be shown
	 */
	boolean shouldShow();

	/**
	 * Controls whether this section should be preceded by a separator
	 *
	 * @return true to show a separator before this section
	 */
	boolean createSeparator();

	/**
	 * Save the state of this view
	 *
	 * @param persistedState
	 *            Eclipse's persisted state for the view
	 */
	void saveState(Map<String, String> persistedState);

	/**
	 * Load the state of this view from Eclipse's persisted state
	 *
	 * @param persistedState
	 *            Eclipse's persisted state for the view
	 */
	void loadState(Map<String, String> persistedState);

	/**
	 * Updates this section - usually based on the mapping bean.
	 */
	void updateControls();

	/**
	 * Do whatever is required when this section gets focus
	 */
	void setFocus();
}