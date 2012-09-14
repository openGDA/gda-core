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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;

/**
 * The tilt controller handles the implementation for the tilt alignment. For i12 a special sample is placed on the
 * sample stage and scans are taken to analyse the sample image's tilt angles. Matlab is used to analyse the flat field
 * and the correction angles.
 * 
 * @author rsr31645 - Ravi Somayaji
 */
public interface ITiltController extends ITomoHandler{

	/**
	 * @param monitor
	 * @param selectedCameraModule
	 * @param exposureTime
	 * @return {@link TiltPlotPointsHolder}
	 * @throws Exception 
	 */
	TiltPlotPointsHolder doTilt(IProgressMonitor monitor, CAMERA_MODULE selectedCameraModule, double exposureTime) throws Exception;
}