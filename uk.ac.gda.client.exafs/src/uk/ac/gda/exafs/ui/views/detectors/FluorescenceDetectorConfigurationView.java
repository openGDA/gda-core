/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.detectors;

import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;

/**
 * Configures the regions of interest of a fluorescence detector. Subclasses should override createPartControl() to
 * create the FluorescenceDetectorComposite.
 * <p>
 * There should be a view for each detector instance on the beamline. However all views implementing this interface can
 * share the same command handlers.
 */
public abstract class FluorescenceDetectorConfigurationView extends ViewPart {

	protected FluorescenceDetectorComposite fluorescenceDetectorComposite;

	public FluorescenceDetectorConfigurationView() {
		super();
	}

	@Override
	public void setFocus() {
		fluorescenceDetectorComposite.setFocus();
	}

	@Override
	public void dispose() {
		fluorescenceDetectorComposite.dispose();
		super.dispose();
	}

	/**
	 * Apply the Regions as displayed in the view to the detector
	 */
	public void applyConfigurationToDetector() {
		fluorescenceDetectorComposite.getController().applyConfigurationToDetector();
	}

	/**
	 * Update the regions displayed in the view from the detector
	 */
	public void fetchConfigurationFromDetector() {
		fluorescenceDetectorComposite.getController().fetchConfigurationFromDetector();
	}
}
