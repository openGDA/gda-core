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

package uk.ac.gda.exafs.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.views.detectors.FluorescenceConfigurationView;

/**
 * Applies the {@link FluorescenceDetectorParameters} in a {@link FluorescenceConfigurationView} to the {@link FluorescenceDetector} the view talks to
 */
public class ApplyDetectorConfigurationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// look at the active part, so this handler can be used for multiple views / editors which are interacting with Fluorescence detectors
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();

		if (activePart instanceof FluorescenceConfigurationView) {
			((FluorescenceConfigurationView) activePart).applyConfigurationToDetector();
		} else {
			throw new ExecutionException(
					"ApplyDetectorConfigurationCommand executed outside the context of a detector configuration view");
		}
		return null;
	}

}
