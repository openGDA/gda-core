/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.simulator;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.alignment.view.handlers.impl.TiltController;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class TiltTestController extends TiltController {
	@Override
	public TiltPlotPointsHolder doTilt(IProgressMonitor monitor, CAMERA_MODULE module, double exposureTime)
			throws Exception {
		return getPlottablePoint("/dls/i12/data/2012/cm5706-2/default/5793/projections/", "/dls/i12/data/2012/cm5706-2/default/5793/projections/");
	}
}
