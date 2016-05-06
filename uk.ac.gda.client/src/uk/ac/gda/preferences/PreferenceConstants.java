/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.preferences;

public class PreferenceConstants {

	public static final String KEEP_BATON = "gda.client.baton.retain";

	public static final String BATON_REQUEST_TIMEOUT = "gda.client.baton.timeout";

	public static final String MAX_SIZE_CACHED_DATA_POINTS = "gda.client.max.size.cached.points";

	public static final String DASHBOARD_FORMAT = "gda.client.dashboard.format";

	public static final String DASHBOARD_BOUNDS = "gda.client.dashboard.bounds";

	public static final String DASHBOARD_DESCRIPTION = "gda.client.dashboard.dummy";

	public static final String NEW_WORKSPACE = "gda.client.new.workspace";

	public static final String HIDE_SCAN_THRESHOLD = "uk.ac.gda.client.liveplot.threshold";

	/**
	 * Comma separated list of integers used to construct Color(int rgb). Def = PlotColorUtility.getDefaultColour(nr);
	 * Values converted to Integer using Integer.valueof(s,16)., i.e. using radix 16 e.g. FF0000 = red
	 */
	public static final String GDA_CLIENT_PLOT_COLORS = "gda.client.plot.colors";

	/**
	 * Integer value for line width. Def = PlotColorUtility.getDefaultLineWidth(0);
	 */
	public static final String GDA_CLIENT_PLOT_LINEWIDTH = "gda.client.plot.linewidth";

	/**
	 * Integer value for min. scan plot update period in ms. Default=500;
	 */
	public static final String GDA_CLIENT_PLOT_PERIOD_MS = "gda.client.plot.period.ms";

	/**
	 * Boolean value to control whether XYPlotView hides previous scan when displaying new scan. Default is True
	 */
	public static final String GDA_CLIENT_PLOT_AUTOHIDE_LAST_SCAN = "gda.client.plot.autohide.lastscan";

	/**
	 * Comma separated list of integers for line style. Def = PlotColorUtility.getDefaultStyle(nr);
	 */
	public static final String GDA_CLIENT_PLOT_LINESTYLES = "gda.client.plot.linestyles";

	/**
	 * Boolean value to control whether Error Bar is shown in plot or not. Default is False
	 */
	public static final String GDA_CLIENT_PLOT_ERRORBAR = "gda.client.plot.errorbar";

	/**
	 * Name of preference to set TRUE if project referring to the folder where data is written to by scans is to be
	 * created on startup. Default is TRUE
	 */
	public static final String GDA_DATA_PROJECT_CREATE_ON_STARTUP = "gda.data_project_create_on_startup";

	/**
	 * Name of preference to containing regular expression to use to define folders to exclude from the data project If
	 * empty no excludes are set Default is xml
	 */
	public static final String GDA_DATA_PROJECT_FILTER = "gda.data_project_filter";

	/**
	 * Name of preference to containing boolean to indicate if preference GDA_DATA_PROJECT_FILTER specifies an exclude
	 * list Default is true
	 */
	public static final String GDA_DATA_PROJECT_FILTER_IS_EXCLUDE = "gda.data_project_filter_is_exclude";

	/**
	 * Name of preference containing name of the data project referring to the folder where data is written to by scans
	 * Default is Data
	 */
	public static final String GDA_DATA_PROJECT_NAME = "gda.data_project_name";

	/**
	 * Name of preference to set TRUE if the application makes use of the ScanDataPointEventService Default is TRUE
	 */
	public static final String GDA_USE_SCANDATAPOINT_SERVICE = "gda.use_scandatapoint_service";

	/**
	 * Name of preference to set TRUE if launch configurations e.g. Run/Debug are to be disabled Default is TRUE
	 */
	public static final String GDA_DISABLE_LAUNCH_CONFIGS = "gda.disable_launch_configs";

	/**
	 * Name of preference to set TRUE if the XYPlotView is to be opened when a scan is started Default is TRUE
	 */
	public static final String GDA_OPEN_XYPLOT_ON_SCAN_START = "gda.open_xyplot_on_scan_start";

	/**
	 * Name of preference containing ID of view to open at the start of a scan Default is empty - in which case
	 * XYPlotView is opened
	 */
	public static final String GDA_OPEN_XYPLOT_ON_SCAN_START_ID = "gda.open_xyplot_on_scan_start_id";

	/**
	 * Comma separated list of the names of the LoggingScriptControllers whose logs are to be displayed in the
	 * ScriptControllerLogView.
	 */
	public static final String GDA_LOGGINGSCRIPTCONTROLLERS = "gda.loggingscriptcontrollers.to_observe";

	/**
	 * set to true if text should be shown in the command queue view toolbar instead of images.
	 * <p>
	 * This would help avoid confusion with similar icons in other views which do similar but different jobs.
	 */
	public static final String GDA_COMMAND_QUEUE_SHOW_TEXT = "gda.commandqueue.showtext";

	/**
	 * If set to true then the Command Queue will enable/disable buttons in the Jython toolbar depending on the state of
	 * the Command Queue.
	 * <p>
	 * This minimises the available buttons for the user to press and so simplifies the choice of
	 * buttons when the user wants to pause/stop the experiment.
	 */
	public static final String GDA_COMMAND_QUEUE_DISABLE_JYTHON_CONTROLS = "gda.commandqueue.disablejythoncontrols";

}
