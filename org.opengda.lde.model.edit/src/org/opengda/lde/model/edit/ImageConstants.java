/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package org.opengda.lde.model.edit;

/**
 *
 */
public class ImageConstants {

	public static final String ICON_CHECKED_STATE = "checked_state.png";
	public static final String ICON_UNCHECKED_STATE = "unchecked_state.png";

	public static final String ICON_RUN_READY = "waiting.gif";
	public static final String ICON_RUNNING = "running.gif";
	public static final String ICON_RUN_COMPLETE = "complete_status.gif";
//	public static final String ICON_RUN_PAUSED = "pause_status.gif";
	public static final String ICON_RUN_FAILURE = "progress_error.gif";
	public static final String ICON_INVALID = "invalid.png";
	public static final String ICON_ERROR = "errorstate.gif";

	public static String[] IMAGES = new String[] {ICON_CHECKED_STATE, ICON_UNCHECKED_STATE, 
			ICON_RUN_READY, ICON_RUNNING, ICON_RUN_COMPLETE, ICON_RUN_FAILURE, ICON_INVALID, ICON_ERROR
	};
}
