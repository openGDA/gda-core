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

package gda.example.viewer;

import gda.example.providers.ISampleAlignmentViewerProvider;

import org.eclipse.swt.widgets.Composite;

/**
 * Provides an interface for a sample alignment viewer
 */
public interface ISampleAlignmentViewer {

	/**
	 * Create the viewer's contents
	 * @param parent the parent composite
	 */
	public void createContents(Composite parent);
	
	/**
	 * Registers a listener with this viewer
	 * @param viewerListener IDeviceViewerListener to register
	 */
	void addListener(ISampleAlignmentViewerListener viewerListener);

	/**
	 * Removes a listener from this viewer
	 * @param viewerListener IDeviceViewerListener to remove
	 */
	void removeListener(ISampleAlignmentViewerListener viewerListener);
	
	/**
	 * Sets the content provider used by this viewer
	 * 
	 * @param viewerContentProvider the content provider
	 */
	void setContentProvider(ISampleAlignmentViewerProvider viewerContentProvider);

	/**
	 * Refreshes this viewer completely with information freshly obtained from
	 * this viewer's model.
	 */
	void refresh();

	/**
	 * Asks this viewer to set focus. Focus must be assigned to one of the controls contained in viewer.
	 */
	void setFocus();
}
