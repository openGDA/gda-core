/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui;

public class SamplePlateConstants {

	// IEventBroker topic constants
	public static final String TOPIC_TAKE_SNAPSHOT = "uk/ac/diamond/daq/sample/plate/management/ui/topicTakeSnapshot";
	public static final String TOPIC_CLEAR_SNAPSHOT = "uk/ac/diamond/daq/sample/plate/management/ui/topicClearSnapshot";
	public static final String TOPIC_PICK_POSITION = "uk/ac/diamond/daq/sample/plate/management/ui/topicPickPosition";
	public static final String TOPIC_ADD_ANNOTATION = "uk/ac/diamond/daq/sample/plate/management/ui/topicAddAnnotation";
	public static final String TOPIC_UPDATE_POSITION_ANNOTATION = "uk/ac/diamond/daq/sample/plate/management/ui/topicUpdatePositionAnnotation";
	public static final String TOPIC_UPDATE_LABEL_ANNOTATION = "uk/ac/diamond/daq/sample/plate/management/ui/topicUpdateLabelAnnotation";
	public static final String TOPIC_DELETE_ANNOTATION = "uk/ac/diamond/daq/sample/plate/management/ui/topicDeleteAnnotation";
	public static final String TOPIC_RETURN_CALIBRATED_AXES = "uk/ac/diamond/daq/sample/plate/management/ui/topicReturnCalibratedAxes";

	private SamplePlateConstants() {
		// Prevent instances
	}

}
