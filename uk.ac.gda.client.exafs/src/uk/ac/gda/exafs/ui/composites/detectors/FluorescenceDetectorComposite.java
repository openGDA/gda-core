/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors;

import java.text.NumberFormat;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.trace.ColorOption;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.richbeans.widgets.selector.GridListEditor;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorAcquireComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorCountsComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorElementConfig;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorElementsComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorOutputPreferenceComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorReadoutModeComposite;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorRegionsComposite;

/**
 * Composite for enabling a user to set the regions of interest for energy sensitive detectors.
 */
public class FluorescenceDetectorComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorComposite.class);

	private SashFormPlotComposite sashFormPlot;
	private FluoDetectorReadoutModeComposite readoutModeComposite;
	private FluoDetectorAcquireComposite acquireComposite;
	private FluoDetectorElementsComposite elementsComposite;
	private FluoDetectorCountsComposite countsComposite;
	private FluoDetectorRegionsComposite regionsComposite;
	private FluoDetectorOutputPreferenceComposite outputPrefComposite;
	private int mcaSize;



	/**
	 * Create a new FluorescenceDetectorComposite. After the relevant values are available, this should be followed by calls to setDetectorElementListSize()
	 * <b>(once only)</b> and setMCASize().
	 *
	 * @param parent
	 * @param style
	 */
	public FluorescenceDetectorComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		try {
			sashFormPlot = new SashFormPlotComposite(this, null);
			sashFormPlot.getPlottingSystem().setRescale(false);
			sashFormPlot.getPlottingSystem().setShowLegend(false);
			sashFormPlot.getSashForm().setWeights(new int[] { 30, 70 });
			sashFormPlot.getRegionOnDisplay().setShowPosition(true); // This has no effect but would be nice!
			sashFormPlot.setXAxisLabel("Channel number");
			sashFormPlot.setYAxisLabel("Counts");

			addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					sashFormPlot.dispose();
				}
			});

			GridDataFactory horizontalGrabGridData = GridDataFactory.fillDefaults().grab(true, false);

			readoutModeComposite = new FluoDetectorReadoutModeComposite(sashFormPlot.getLeft(), SWT.NONE);
			horizontalGrabGridData.applyTo(readoutModeComposite);

			acquireComposite = new FluoDetectorAcquireComposite(sashFormPlot.getLeft(), SWT.NONE);
			horizontalGrabGridData.applyTo(acquireComposite);

			countsComposite = new FluoDetectorCountsComposite(sashFormPlot.getLeft(), SWT.NONE);
			horizontalGrabGridData.applyTo(countsComposite);

			elementsComposite = new FluoDetectorElementsComposite(sashFormPlot.getLeft(), SWT.NONE);
			horizontalGrabGridData.applyTo(elementsComposite);

			regionsComposite = new FluoDetectorRegionsComposite(sashFormPlot.getLeft(), SWT.NONE, elementsComposite);
			horizontalGrabGridData.applyTo(regionsComposite);

			outputPrefComposite = new FluoDetectorOutputPreferenceComposite(sashFormPlot.getLeft(), SWT.NONE );
			horizontalGrabGridData.applyTo(outputPrefComposite);

			sashFormPlot.computeSizes();

		} catch (Exception ex) {
			// Creating the PlottingSystem in SashFormPlotComposite can throw Exception
			logger.warn("Exception creating FluorescenceDetectorComposite", ex);
			Label errorMessageLabel = new Label(this, SWT.NONE);
			errorMessageLabel.setText("Error - see log for details");
		}
	}


	/**
	 * @return the start value of window for currently-selected element
	 */
	public int getWindowStart() {
		return (int) regionsComposite.getWindowStart().getNumericValue();
	}

	/**
	 * Set the start value of window for the currently-selected element
	 *
	 * @param windowStart
	 */
	public void setWindowStart(int windowStart) {
		regionsComposite.getWindowStart().setIntegerValue(windowStart);
	}

	/**
	 * @return the end value of window for currently-selected element
	 */
	public int getWindowEnd() {
		return (int) regionsComposite.getWindowEnd().getNumericValue();
	}

	/**
	 * Set the end value of window for the currently-selected element
	 *
	 * @param windowEnd
	 */
	public void setWindowEnd(int windowEnd) {
		regionsComposite.getWindowEnd().setIntegerValue(windowEnd);
	}

	/**
	 * For access by BeanUI only. This name must match the field name in XspressParameters.
	 */
	public BooleanWrapper getOnlyShowFF() {
		return outputPrefComposite.getOnlyShowFF();
	}

	/**
	 * For access by BeanUI only. This name must match the field name in XspressParameters.
	 */
	public BooleanWrapper getShowDTRawValues() {
		return outputPrefComposite.getShowDTRawValues();
	}

	/**
	 * For access by BeanUI only. This name must match the field name in Vortex/XspressParameters.
	 */
	public BooleanWrapper getSaveRawSpectrum() {
		return outputPrefComposite.getSaveRawSpectrum();
	}

	public void addReadoutModeListener(ValueAdapter listener) {
		readoutModeComposite.getReadoutCombo().addValueListener(listener);
	}

	/**
	 * Set readout mode of the detector
	 *
	 * @param readoutMode
	 */
	public void setReadoutMode( String readoutMode ) {
		readoutModeComposite.setReadoutMode(readoutMode);
	}

	/**
	 * For access by BeanUI only. This name must match the field name in XspressParameters.
	 */
	public ComboWrapper getReadoutMode() {
		return readoutModeComposite.getReadoutCombo();
	}

	/**
	 * For access by BeanUI only. This name must match the field name in XspressParameters.
	 */
	public ComboWrapper getResGrade() {
		return readoutModeComposite.getResolutionGradeCombo();
	}

	/**
	 * For access by BeanUI only. This name must match the field name in XspressParameters.
	 */
	public ComboWrapper getRegionType() {
		return readoutModeComposite.getRegionTypeCombo();
	}

	/**
	 * Call this once after construction when the number of detector elements is known
	 *
	 * @param size
	 */
	public void setDetectorElementListSize(int size) {
		elementsComposite.configureDetectorElementTable(size, regionsComposite);
		sashFormPlot.computeSizes();
	}

	/**
	 * Call this before {@link FluoDetectorElementsComposite#configureDetectorElementTable}
	 * to set the display order of the detector elements in the composite.
	 *
	 * @param order
	 */
	public void setDetectorElementOrder(GridListEditor.GRID_ORDER order) {
		elementsComposite.setDetectorElementOrder(order);
	}

	/**
	 * Call this before {@link FluoDetectorElementsComposite#configureDetectorElementTable}
	 * to set the config that controls the order of the detector elements in the composite.
	 *
	 * @param config
	 */
	public void setDetectorElementConfiguration(FluoDetectorElementConfig config) {
		elementsComposite.setElementConfiguration(config);
	}

	/**
	 * Call this after construction when the number of MCA channels is known
	 *
	 * @param mcaSize
	 */
	public void setMCASize(int mcaSize) {
		this.mcaSize = mcaSize;
		regionsComposite.getRoiEnd().setMaximum(mcaSize);
		regionsComposite.getWindowEnd().setMaximum(mcaSize);
	}

	/**
	 * Call this after construction when the maximum number of ROIs per channel is known
	 *
	 * @param maxNumberOfRois
	 */
	public void setMaxNumberOfRois(int maxNumberOfRois) {
		getRegionList().setMaxItems(maxNumberOfRois);
	}

	// TODO move the label outside the acquire composite?
	public void setDetectorName(String name) {
		if (name != null && name.length() > 0) {
			acquireComposite.setDetectorNameLabel("Detector: " + name);
		} else {
			acquireComposite.setDetectorNameLabel("");
		}
	}

	/**
	 * For access by BeanUI only. This name must match the field name in VortexParameters.
	 */
	public IFieldWidget getCollectionTime() {
		return acquireComposite.getCollectionTime();
	}

	/**
	 * For access by BeanUI only. This name must match the field name in FluorescenceDetectorParameters.
	 */
	public ListEditor getDetectorList() {
		return elementsComposite.getDetectorList();
	}

	/**
	 * For access by BeanUI only. This name must match the field name in XspressParameters.
	 */
	public IFieldWidget getEditIndividualElements() {
		return regionsComposite.getApplyToAllCheckbox(); // the apply to all box has reversed boolean mode
	}

	/**
	 * For access by the controller.
	 */
	public boolean isApplyRoisToAllElements() {
		return regionsComposite.getApplyToAllCheckbox().getButton().getSelection();
	}

	/**
	 * For access by the controller <em>only</em> for the purpose of initialising the Import Region wizard.
	 * <p>
	 * It would be better to avoid breaking encapsulation like this if possible but that might not be possible, or would certainly require the wizard to be
	 * rewritten.
	 */
	public ListEditor getRegionList() {
		return regionsComposite.getRegionList();
	}

	/**
	 * Add a listener which will be notified when a selection is changed in one of the list editors
	 *
	 * @param listener
	 */
	public void addBeanSelectionListener(BeanSelectionListener listener) {
		getDetectorList().addBeanSelectionListener(listener);
		getRegionList().addBeanSelectionListener(listener);
	}

	/**
	 * @return the acquisition time (in milliseconds)
	 */
	public double getAcquisitionTime() {
		return acquireComposite.getCollectionTime().getNumericValue();
	}

	/**
	 * Add a listener which will be notified when the load button is pressed
	 *
	 * @param listener
	 */
	public void addLoadButtonListener(SelectionListener listener) {
		acquireComposite.getLoadButton().addSelectionListener(listener);
	}

	/**
	 * Add a listener which will be notified when the save button is pressed
	 *
	 * @param listener
	 */
	public void addSaveButtonListener(SelectionListener listener) {
		acquireComposite.getSaveButton().addSelectionListener(listener);
	}

	/**
	 * Add a listener which will be notified when the acquire button is pressed
	 *
	 * @param listener
	 */
	public void addAcquireButtonListener(SelectionListener listener) {
		acquireComposite.getAcquireButton().addSelectionListener(listener);
	}

	/**
	 * Add a listener which will be notified when the 'Apply settings' button is pressed
	 *
	 * @param listener
	 */
	public void addApplySettingsButtonListener(SelectionListener listener) {
		acquireComposite.getApplySettingsButton().addSelectionListener(listener);
	}

	/**
	 * @return <code>true</code> if data should be automatically saved after acquisition
	 */
	public boolean getAutoSaveModeSelection() {
		return acquireComposite.getAutoSaveCheckBox().getSelection();
	}

	/**
	 * @return <code>true</code> if data should be acquired continuously
	 */
	public boolean getContinuousModeSelection() {
		return acquireComposite.getLiveCheckBox().getSelection();
	}

	/**
	 * Add a listener which will be notified when the continuous acquisition checkbox is clicked
	 *
	 * @param listener
	 */
	public void addContinuousModeButtonListener(SelectionListener listener) {
		acquireComposite.getLiveCheckBox().addSelectionListener(listener);
	}

	/**
	 * Update the view to show that continuous acquisition mode is selected
	 */
	public void setContinuousAcquireMode() {
		acquireComposite.setContinuousAcquireMode();
	}

	/**
	 * Update the view to show that single acquisition mode is selected
	 */
	public void setSingleAcquireMode() {
		acquireComposite.setSingleAcquireMode();
	}

	/**
	 * @return the index number of the currently-selected detector element
	 */
	public int getSelectedDetectorElementIndex() {
		return elementsComposite.getSelectedElementIndex();
	}

	/**
	 * Set the element name to be displayed below the detector element list
	 *
	 * @param label
	 */
	public void setElementName(String label) {
		elementsComposite.getElementNameLabel().setValue(label);
	}

	/**
	 * Set the total counts for all enabled detector elements
	 *
	 * @param enabledElementCounts
	 */
	public void setEnabledElementsCounts(double enabledElementCounts) {
		countsComposite.getEnabledElementsCounts().setValue(formatDoubleValue(enabledElementCounts));
		countsComposite.layout(true, true);
	}

	/**
	 * Set the total counts for the currently-selected detector element
	 *
	 * @param elementCounts
	 */
	public void setSelectedElementCounts(double elementCounts) {
		countsComposite.getSelectedElementCounts().setValue(formatDoubleValue(elementCounts));
		countsComposite.layout(true, true);
	}

	/**
	 * Set the counts for the currently-selected region
	 *
	 * @param regionCounts
	 */
	public void setSelectedRegionCounts(double regionCounts) {
		countsComposite.getSelectedRegionCounts().setValue(formatDoubleValue(regionCounts));
		countsComposite.layout(true, true);
	}

	private String formatDoubleValue(double val) {
		return NumberFormat.getIntegerInstance().format(val);
	}

	/**
	 * Add a listener which will be notified when the import button is pressed
	 *
	 * @param listener
	 */
	public void addRegionImportButtonSelectionListener(SelectionListener listener) {
		regionsComposite.getImportButton().addSelectionListener(listener);
	}

	/**
	 * @return the start value of the currently-selected region
	 */
	public int getRegionStart() {
		return (int) regionsComposite.getRoiStart().getNumericValue();
	}

	/**
	 * Set the start value of the currently-selected region
	 *
	 * @param regionStart
	 */
	public void setRegionStart(int regionStart) {
		regionsComposite.getRoiStart().setIntegerValue(regionStart);
	}

	/**
	 * @return the end value of the currently-selected region
	 */
	public int getRegionEnd() {
		return (int) regionsComposite.getRoiEnd().getNumericValue();
	}

	/**
	 * Set the end value of the currently-selected region
	 *
	 * @param regionEnd
	 */
	public void setRegionEnd(int regionEnd) {
		regionsComposite.getRoiEnd().setIntegerValue(regionEnd);
	}

	public boolean getReadoutModeIsRoi() {
		return regionsComposite.getReadoutModeIsRoi();
	}

	/**
	 * Update the region on the plot from the current settings in the UI
	 */
	public void updatePlottedRegionFromUI() {
		boolean editingRoi = regionsComposite.getReadoutModeIsRoi();
		int start = editingRoi ? getRegionStart(): getWindowStart();
		int end = editingRoi ? getRegionEnd(): getWindowEnd();
		sashFormPlot.getRegionOnDisplay().setROI(new RectangularROI(start, 0, end - start, 0, 0));
		sashFormPlot.getRegionOnDisplay().repaint();
	}

	/**
	 * Set whether the region on the plot can be moved by dragging
	 *
	 * @param mobile
	 */
	public void setPlottedRegionMobile(boolean mobile) {
		sashFormPlot.getRegionOnDisplay().setMobile(mobile);
		sashFormPlot.getRegionOnDisplay().setTrackMouse(false);
	}

	/**
	 * Add a listener which will be notified when the plotted region is changed
	 *
	 * @param listener
	 */
	public void addPlottedRegionListener(IROIListener listener) {
		sashFormPlot.addRegionListener(listener);
	}

	/**
	 * Update the view to indicate that continuous acquisition is in progress
	 */
	public void showAcquireStarted() {
		acquireComposite.showAcquireStarted();
	}

	/**
	 * Update the view to indicate that continuous acquisition has finished
	 */
	public void showAcquireFinished() {
		acquireComposite.showAcquireFinished();
	}

	/**
	 * Append text to the status panel
	 *
	 * @param text
	 */
	public void appendStatus(String text) {
		sashFormPlot.appendStatus(text);
	}

	/**
	 * Rescale the plot
	 * <p>
	 * In this implementation, the plotting system is told to scale its axes automatically, and the X-axis is manually
	 * rescaled afterwards so it is completely filled by the available MCA data.
	 */
	public void autoscaleAxes() {
		sashFormPlot.getPlottingSystem().autoscaleAxes();
		if (mcaSize > 0) {
			sashFormPlot.getPlottingSystem().getSelectedXAxis().setRange(0, mcaSize);
		}
	}

	/**
	 * Set the label for the X-axis of the plot
	 *
	 * @param xAxislabel
	 */
	public void setXAxisLabel(String xAxislabel) {
		sashFormPlot.setXAxisLabel(xAxislabel);
	}

	/**
	 * Set the label for the Y-axis of the plot
	 *
	 * @param yAxislabel
	 */
	public void setYAxisLabel(String yAxislabel) {
		sashFormPlot.setYAxisLabel(yAxislabel);
	}

	/**
	 * Set the title shown above the plot
	 *
	 * @param plotTitle
	 */
	public void setPlotTitle(String plotTitle) {
		sashFormPlot.setPlotTitle(plotTitle);
	}

	/**
	 * Plot the given dataset
	 *
	 * @param dataset
	 */
	public void plotDataset(IDataset dataset) {
		sashFormPlot.setDatasets(dataset);
		sashFormPlot.plotData();
	}

	/**
	 * Plot the given datasets, colour them appropriately and show the legend.
	 *
	 * @param datasets
	 * @since 8/9/2016
	 */
	public void plotDatasets(IDataset... datasets) {
		sashFormPlot.setDatasets(datasets);
		sashFormPlot.getPlottingSystem().setShowLegend(true);
		sashFormPlot.getPlottingSystem().setColorOption(ColorOption.BY_NAME);
		sashFormPlot.plotData();
		showHideLoadedDataset();
	}

	public void showHideLoadedDataset() {
		ITrace trace = sashFormPlot.getPlottingSystem().getTrace(FluorescenceDetectorCompositeController.LOADED_DATA_NAME);
		if (trace != null)
			trace.setVisible( getShowDataLoadedFromFile() );
	}

	/**
	 *  Hide/show output option checkboxes for detector type.
	 *
	 * @param detectorParams
	 * @since 1/7/2016
	 */
	public void setOutputOptions(FluorescenceDetectorParameters detectorParams ) {
		boolean enableFFDTButtons = true;
		if ( detectorParams instanceof Xspress3Parameters ) {
			enableFFDTButtons = false;
		}
		GridUtils.setVisibleAndLayout(outputPrefComposite.getOnlyShowFF(), enableFFDTButtons );
		GridUtils.setVisibleAndLayout(outputPrefComposite.getShowDTRawValues(), enableFFDTButtons );
	}


	/**
	 * Hide/show the 'output options' part of the gui.
	 * @param showOutputOptions
	 */
	public void setShowOutputOptions(boolean showOutputOptions) {
		GridUtils.setVisibleAndLayout(outputPrefComposite, showOutputOptions);
	}

	/**
	 * Setup GUI for specified readout mode of detector.
	 * i.e. shows/hides different parts of GUI as appropriate for XSpress2/3
	 * This function is called during initial setup of GUI, using settings from Xml file.
	 *
	 * @parameter detectorParams
	 * @since 1/7/2016
	 */
	public void setReadoutModeOptions(FluorescenceDetectorParameters detectorParams) {
		if (detectorParams instanceof XspressParameters) {
			XspressParameters xspressParams = (XspressParameters) detectorParams;
			readoutModeComposite.setReadoutMode( xspressParams.getReadoutMode() );
			regionsComposite.updateControlVisibility( xspressParams.getReadoutMode() );
		} else if ( detectorParams instanceof Xspress3Parameters || detectorParams instanceof VortexParameters ) {
			// Xspress3 gui is for 'region of interest' only...
			readoutModeComposite.setReadoutMode( XspressParameters.READOUT_MODE_REGIONSOFINTEREST  );
			regionsComposite.updateControlVisibility( XspressParameters.READOUT_MODE_REGIONSOFINTEREST );
			readoutModeComposite.getReadoutCombo().setEnabled(false);
			// Remove resolution grade controls
			readoutModeComposite.showResGradeRegionControls(false);
		}
	}

	/**
	 * Update 'Readout Mode' controls : hide/show resolution grade combo boxes for currently selected readout mode
	 *
	 */
	public void updateRoiWindowSettings() {
		String readoutMode = readoutModeComposite.getReadoutMode();
		regionsComposite.updateControlVisibility( readoutMode );
	}

	/**
	 * @return true if 'autoscale on acquire' checkbox is checked.
	 * @since 8/9/2016
	 */
	public boolean getAutoScaleOnAcquire() {
		return acquireComposite.getAutoScaleOnAcquireCheckBox().getSelection();
	}

	public boolean getShowDataLoadedFromFile() {
		return acquireComposite.getShowDataLoadedFromFileCheckBox().getSelection();
	}

	public void addShowLoadedDataListener(SelectionListener listener) {
		acquireComposite.getShowDataLoadedFromFileCheckBox().addSelectionListener(listener);
	}

	public void setEnableShowLoadedDataCheckBox(boolean enabled) {
		acquireComposite.getShowDataLoadedFromFileCheckBox().setEnabled(enabled);
	}

}
