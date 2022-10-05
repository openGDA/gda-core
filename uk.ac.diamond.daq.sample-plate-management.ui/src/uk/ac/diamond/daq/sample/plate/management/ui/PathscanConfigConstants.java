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
public class PathscanConfigConstants {

	// IEventBroker topic constants
	public static final String TOPIC_BUILD_SCRIPT = "uk/ac/diamond/daq/sample/plate/management/ui/topicBuildScript";
	public static final String TOPIC_BUILD_AND_RUN_SCRIPT = "uk/ac/diamond/daq/sample/plate/management/ui/topicBuildAndRunScript";
	public static final String TOPIC_GENERATE_SUMMARY = "uk/ac/diamond/daq/sample/plate/management/ui/topicGenerateSummary";
	public static final String TOPIC_SELECT_SCAN = "uk/ac/diamond/daq/sample/plate/management/ui/topicSelectScan";
	public static final String TOPIC_SYNC_SUMMARY = "uk/ac/diamond/daq/sample/plate/management/ui/topicSyncSummary";
	public static final String TOPIC_OPEN_SPECS = "uk/ac/diamond/daq/sample/plate/management/ui/topicOpenSpecs";
	public static final String TOPIC_RESIZE_SCROLL = "uk/ac/diamond/daq/sample/plate/management/ui/topicResizeScroll";

	public static final String NO_ANALYSER = "***No sequence***";
	public static final String TEST_VISIT = "test_visit";

	private PathscanConfigConstants() {
		// Prevent instances
	}
}