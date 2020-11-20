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

package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.swt.widgets.Composite;

/**
 * The minimal object to interact with the plotting system image. While the
 * {@link #getPlottingSystem()} returns the GUI graphical elements,
 * {@link #getImageTrace()} returns the image displayed in the plotting system
 *
 * @author Maurizio Nagni
 */
public interface CameraPlotter {

	/**
	 * @return the GUI plotting system
	 */
	IPlottingSystem<Composite> getPlottingSystem();

	/**
	 * @return the image displayed in the plotting system
	 */
	IImageTrace getImageTrace();

}