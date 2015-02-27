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

import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Views which are used to configure the Regions of Interest of detectors implementing {@link FluorescenceDetector}
 * <p>
 * There should be a view for each detector instance on the beamline. However all views implementing this interface can
 * share the same command handlers.
 */
public interface FluorescenceConfigurationView {

	/**
	 * Apply the Regions as displayed in the view to the detector
	 */
	public void applyConfigurationToDetector();

	/**
	 * Update the regions displayed in the view from the detector
	 */
	public void fetchConfigurationFromDetector();

}
