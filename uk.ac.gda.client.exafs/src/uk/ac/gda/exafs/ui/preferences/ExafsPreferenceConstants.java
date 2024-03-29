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
	 * If true, then hide the Fill Gas button in the Detector Parameters editor.
	 * <p>
	 * False by default, true for I20.
	 */
	public static final String HIDE_GAS_FILL_CONTROLS = "exafs.editor.hideGasFillControls.Preference";

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

	/**
	 * If SHOW_MYTHEN value true, and this value false, then operate the diffraction detector at the ends of the scan.
	 * <p>
	 * If SHOW_MYTHEN value true, and this value true, then collect diffraction images alongside fluorescence data.
	 */
	public static final String DIFFRACTION_COLLECTED_CONCURRENTLY = "exafs.fluoEditor.collectDiffractionConcurrently.Preference";


	public static final String HIDE_WORKING_ENERGY = "exafs.detectoreditor.hideWorkingEnergy.Preference";

	public static final String EXAFS_FINAL_ANGSTROM = "exafs.final_energy.angstrom.Preference";

	/**
	 * When set to 'true' the 'Exafs step type' is set to 'k' in XasScanParametersUIEditor and the 'Exafs step type' combo box is disabled.
	 */
	public static final String EXAFS_ONLY_ALLOW_K_STEP_TYPE = "exafs.only.allow.kstep.type.Preference";

	/**
	 * When true, only show options relating to the XES spectrometer in various editors and dialogs.
	 */
	public static final String XES_MODE_ENABLED = "exafs.xes_mode_enabled.Preference";

	/**
	 * When false, mca windows in xspress and vortex editors cannot be dragged with the mouse
	 */
	public static final String DETECTOR_OVERLAY_ENABLED = "exafs.editor.overlay.Preference";

	/**
	 * When true then the Fluo detector output options should be shown in the output parameters editor and not the
	 * individual detector editors.  The beans will also have to have matching information.
	 */
	public static final String DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS = "exafs.outputeditor.showFluoOptions.preference";

	/**
	 * Order of detector elements in {@link uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorElementsComposite}, i.e. value correspond to enum value
	 * in {@link org.eclipse.richbeans.widgets.selector.GridListEditor.GRID_ORDER} :<p>
	 * <li>0 = LEFT_TO_RIGHT_TOP_TO_BOTTOM
	 * <li>1 = TOP_TO_BOTTOM_RIGHT_TO_LEFT
	 * <li>2 = CUSTOM_MAP
	 */
	public static final String DETECTOR_ELEMENT_ORDER = "exafs.editor.detectorElementOrder.preference";

	/**
	 * When set to 'true', show GUI controls in FluorescenceDetectorComposite for setting the
	 * deadtime correction (DTC) energy (for xspress2, xspress4 detectors only)
	 */
	public static final String DETECTOR_SHOW_DTC_ENERGY = "exafs.editor.showDtcEnergy.preference";

	/**
	 * When set to 'true', set 'show on acquire' checkbox in FluorescenceDetectorComposite to true when view
	 * is opened.
	 */
	public static final String DETECTOR_SAVE_ON_ACQUIRE = "exafs.editor.saveOnAcquireCheckbox.preference";

	/**
	 * Names of scannables whose positions should be recorded in MCA data files generated from FLuorescence
	 * detector view. This can be a list of several scannables separated by whitespace, comma or semicolon.
	 */
	public static final String DETECTOR_MCA_FILE_SCANNABLES = "exafs.editor.mca.file.extra.scannables.preference";

	/** Width of scaler/ROI window to use when doing 'set window from line' in fluorescence detector view */
	public static final String DETECTOR_WINDOW_HALFWIDTH = "exafs.editor.detectorWindow.halfWidth";

	/**
	 * Set to true to allow detector parameters to be updated with latest filename when the detector
	 * configuration XML file is saved to a new file.
	 */
	public static final String DETECTOR_PARAMS_UPDATE_ON_SAVEAS = "exafs.editor.detectorParams.update.on.saveAs";

	public static final String HIDE_DEFAULT_GAS_MIXTURES_BUTTON = "exafs.hideDefaultGasMixturesButton.preference";

	/**
	 * When true, QEXAFS scans are the default scan type and new scans are always qexafs when first created
	 */
	public static final String QEXAFS_IS_DEFAULT_SCAN_TYPE = "exafs.editor.qexafsdefaultscan.preference";

	/**
	 * When true, XANES scans are the default scan type and new scans are always XANES when first created
	 */
	public static final String XANES_IS_DEFAULT_SCAN_TYPE = "exafs.editor.xanesdefaultscan.preference";

	/**
	 * The order which the scan xml tabs in Experiment perspective should appear (default is Scan, Detector, Sample, Output)
	 */
	public static final String SCAN_TAB_ORDER = "exafs.editor.scan.tab.order";

	/**
	 * The type of scan xml tab which should be selected when opening settings for a scan in Experiment perspective.
	 * (default is 'Scan')
	 */
	public static final String SELECTED_SCAN_TAB = "exafs.editor.selected.scan.tab";


	/** When true, show the 'get energy from scan' button in the Ion chambers pressure calculation/fill view */
	public static final String IONCHAMBERS_SHOW_ENERGY_FROM_SCAN_BUTTON = "exafs.ionchambersview.show.energy.from.scan.button";

	/**
	 * Whether to try and select the same type of settings tab when switch between different scans in Experiment explorer
	 * (e.g. if 'detector' tab is selected, the detector tab will also be selected in the next scan that is opened)
	 * Set to 'true' to enable this behaviour.
	 */
	public static final String AUTO_SELECT_TAB = "exafs.editor.auto.select.scan.tab";

}
