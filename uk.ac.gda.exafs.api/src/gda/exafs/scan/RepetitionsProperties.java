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

package gda.exafs.scan;

/**
 * The properties to use to control repetitions in the XAS scan scripts.
 */
public abstract class RepetitionsProperties {

	/**
	 * Set this to true to inform a script that the repetition loop should pause after the current repetition has
	 * finished.
	 */
	public static String PAUSE_AFTER_REP_PROPERTY = "uk.ac.gda.exafs.pauseafterrepetition";

	/**
	 * Set to true before aborting the current scan. The scan script will then know that the scan has been aborted
	 * because the only the current repetition has been aborted , but that the repetition loop should continue.
	 */
	public static String SKIP_REPETITION_PROPERTY = "uk.ac.gda.exafs.skiprepetition";
	
	
	/**
	 * Used by XAS scan scripts to store the number of repetitions in the current loop. This value may be changed outside of the
	 * script to alter the number of repetitions that loop will do.
	 */
	public static String NUMBER_REPETITIONS_PROPERTY = "uk.ac.gda.exafs.numberofrepetitions";

}
