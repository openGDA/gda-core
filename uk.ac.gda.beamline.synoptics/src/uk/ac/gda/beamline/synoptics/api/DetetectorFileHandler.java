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

package uk.ac.gda.beamline.synoptics.api;

import uk.ac.gda.beamline.synoptics.views.DetectorFilePlotView;

/**
 * An interface for file handlers that can extract data to display on a {@link DetectorFilePlotView}.
 */
public interface DetetectorFileHandler {

	/** Check if this handler can deal with the given filename */
	boolean canHandle(String filename);

	/**
	 * Extract the data from the given file and plot it on the given plotView
	 * @return true if file was successfully plotted
	 */
	boolean plot(String filename, DetectorFilePlotView plotView, boolean newPlot);

}
