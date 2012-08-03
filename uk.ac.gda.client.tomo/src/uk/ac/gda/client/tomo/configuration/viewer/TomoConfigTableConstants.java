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

package uk.ac.gda.client.tomo.configuration.viewer;

/**
 *
 */
public interface TomoConfigTableConstants {
	public static final int COL_SELECTION = 0;
	public static final int COL_PROPOSAL = COL_SELECTION + 1;
	public static final int COL_SAMPLE_DESCRIPTION = COL_PROPOSAL + 1;
	public static final int COL_MODULE = COL_SAMPLE_DESCRIPTION + 1;
	public static final int COL_ACQUISITION_TIME = COL_MODULE + 1;
	public static final int COL_FLAT_ACQ_TIME = COL_ACQUISITION_TIME + 1;
	public static final int COL_DETECTOR_DISTANCE = COL_FLAT_ACQ_TIME + 1;
	public static final int COL_ENERGY = COL_DETECTOR_DISTANCE + 1;
	public static final int COL_SAMPLE_WEIGHT = COL_ENERGY + 1;
	public static final int COL_RESOLUTION = COL_SAMPLE_WEIGHT + 1;
	public static final int COL_FRAMES_PER_PROJECTION = COL_RESOLUTION + 1;
	public static final int COL_CONTINUOUS_STEP = COL_FRAMES_PER_PROJECTION + 1;
	public static final int COL_RUN_TIME = COL_CONTINUOUS_STEP + 1;
	public static final int COL_EST_END_TIME = COL_RUN_TIME + 1;
	public static final int COL_TIME_DIVIDER = COL_EST_END_TIME + 1;
	public static final int COL_SHOULD_DISPLAY = COL_TIME_DIVIDER + 1;
	public static final int COL_PROGRESS = COL_SHOULD_DISPLAY + 1;

	public static final String SAMPLE_DESCRIPTION = "Description";
	public static final String PROPOSAL = "Visit Id";
	public static final String MODULE_NUMBER = "Module(mm)";
	public static final String ACQUISITION_TIME = "Acq time(s)";
	public static final String FLAT_ACQ_TIME = "Flat acq time(s)";
	public static final String DETECTOR_DISTANCE = "Detector distance(mm)";
	public static final String ENERGY = "Energy(keV)";
	public static final String SAMPLE_WEIGHT = "Weight (kgs)";
	public static final String RESOLUTION = "Resolution";
	public static final String FRAMES_PER_PROJECTION = "Frames Per Projection";
	public static final String CONTINUOUS_STEP = "Continuous/Step";
	public static final String RUN_TIME = "Run time(s)";
	public static final String EST_END_TIME = "Est. end time(s)";
	public static final String TIME_DIVIDER = "Time divider(%)";
	public static final String PROGRESS = "Progress";
	public static final String SELECTION = "Selection";
	public static final String SHOULD_DISPLAY = "Display?";
}
