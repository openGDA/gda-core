/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.preference;

public class DeprecatedPreferenceConstants {

	public static final String IGNORE_DATASET_FILTERS = "ignore.data.set.filters";
	public static final String SHOW_XY_COLUMN         = "show.xy.column.in.nexus.editor";
	public static final String SHOW_DATA_SIZE         = "show.data.size.in.nexus.editor";
	public static final String SHOW_DIMS              = "show.dims.in.nexus.editor";
	public static final String SHOW_SHAPE             = "show.shape.in.nexus.editor";
	public static final String DATA_FORMAT            = "data.format.editor.view";
	public static final String PLAY_SPEED             = "data.format.slice.play.speed";

	public static final int PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM = 0;
	public static final int PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM = 1;
	/**
	 * 0 if DatasetPlotter is chosen or 1 if AbstractPlottingSystem
	 */
	public static final String PLOT_VIEW_PLOTTING_SYSTEM = "plotView.plottingsystem";
}
