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

package uk.ac.gda.exafs.ui.preferences;


/**
 * Constant definitions for plug-in preferences.
 * 
 * To access use: ExafsActivator.getDefault().getPreferenceStore()
 */
public class ExafsPreferenceConstants {

	public static final String A_ELEMENT_LINK = "exafs.element.a.link.Preference";

	public static final String B_ELEMENT_LINK = "exafs.element.b.link.Preference";

	public static final String C_ELEMENT_LINK = "exafs.element.c.link.Preference";
	
	public static final String C_MIRRORS_B_LINK = "exafs.element.c.mirrors.b.link.Preference";

	public static final String INITIAL_ENERGY_ELEMENT_LINK = "exafs.element.initial.energy.link.Preference";

	public static final String FINAL_ENERGY_ELEMENT_LINK = "exafs.element.final.energy.link.Preference";

	public static final String SAMPLE_ELEMENTS = "exafs.reference.sample.Elements";
	
	public static final String EXAFS_GRAPH_EDITABLE = "exafs.editor_graph.editable.Preference";
	/**
	 * Set in plugin_customisation.ini to never display the gas properties options in the detector editor
	 */
	//true for I18
	public static final String NEVER_DISPLAY_GAS_PROPERTIES = "exafs.editor.hideGasProperties.Preference";
	
	/**
	 * Set in plugin_customisation.ini to never display the gas fill period in the detector editor
	 */
	// true for I18
	public static final String DISPLAY_GAS_FILL_PERIOD = "exafs.editor.displayGasFillPeriod.Preference";
	
	/**
	 * If true then add options to add additional metadata in the outputeditor
	 */
	// true for B18
	public static final String SHOW_METADATA_EDITOR = "exafs.editor.showMetadataEditor.Preference";
	
	/**
	 * In the plotting perspective, hide the LnI0ItScanPlotView view
	 */
	// true for I18
	public static final String HIDE_LnI0ItScanPlotView = "exafs.editor.hide.LnI0ItScanPlotView.Preference";
	
	/**
	 * In the plotting perspective, show the B18ScalersMonitorView instead of the ScalersMonitorView
	 */
	public static final String SHOW_B18ScalersMonitorView = "exafs.editor.show.B18ScalersMonitorView.Preference";
	
	public static final String XAS_MAX_ENERGY = "exafs.editor.maxEnergy.Preference";
	
	public static final String XAS_MIN_ENERGY = "exafs.editor.minEnergy.Preference";
	
	/**
	 * Add the soft x-ray option to the detectors editor
	 */
	public static final String DETECTORS_SOFT_XRAY = "exafs.detectoreditor.showSoftXray.Preference";
	
	/**
	 *  Only show the fluorescence option in the detectors editor
	 */
	public static final String DETECTORS_FLUO_ONLY = "exafs.detectoreditor.fluoOnly.Preference";
	
	/**
	 * Only show the static data collection option in the Vortex editor
	 */
	public static final String VORTEX_STATIC_ONLY = "exafs.vortexeditor.showStaticOptionOnly.Preference";
	
	/**
	 * Add a mythen option in the Fluo detector composite
	 */
	public static final String SHOW_MYTHEN = "exafs.fluoEditor.showMythen.Preference";
	
	public static final String HIDE_WORKING_ENERGY = "exafs.detectoreditor.hideWorkingEnergy.Preference";
	
	public static final String EXAFS_FINAL_ANGSTROM = "exafs.final_energy.angstrom.Preference";
	
	/**
	 * When true, only show options relating to the XES spectrometer in various editors and dialogs.
	 */
	public static final String XES_MODE_ENABLED = "exafs.xes_mode_enabled.Preference";
}
