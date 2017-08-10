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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.binding.IBeanService;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.widgets.selector.BeanSelectionEvent;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.richbeans.widgets.selector.GridListEditor.GRID_ORDER;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.factory.Findable;
import gda.factory.Finder;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.XspressDetector;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoCompositeDataStore;
import uk.ac.gda.exafs.ui.detector.wizards.ImportFluoDetROIWizard;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

/**
 * Provides control logic for a FluorescenceDetectorComposite. To use this class, create a new controller and then call setEditorUI() to provide a
 * FluorescenceDetectorComposite. If necessary (e.g. if using in an editor where the parameters object has already been created from a file) call
 * setEditingBean() to set the initial parameters. Finally call initialise() to start the controller.
 */
// TODO clarify creation logic for detector and parameters
public class FluorescenceDetectorCompositeController implements ValueListener, BeanSelectionListener, IROIListener {

	private static Logger logger = LoggerFactory.getLogger(FluorescenceDetectorCompositeController.class);

	// Essential references
	private FluorescenceDetectorParameters detectorParameters;
	private FluorescenceDetector theDetector;
	private FluorescenceDetectorComposite fluorescenceDetectorComposite;
	private IBeanController dataBindingController;
	private FluoCompositeDataStore dataStore;
	private FileDialog openDialog;
	private Thread continuousThread;

	// Controller state
	private double[][] theData;
	private double[][] dataLoadedFromFile;
	private static final String LIVE_DATA_NAME = "Acquired data";
	public static final String LOADED_DATA_NAME = "Data loaded from file";
	private String plotTitle;
	private boolean applyParametersBeforeAcquire = false;
	private volatile boolean continuousAquire; // changed to volatile, so changes to it are noticed by different threads
	private boolean updatingRoiPlotFromUI;
	private boolean updatingRoiUIFromPlot;

	/**
	 * Create a new FluorescenceDetectorCompositeController for the given FluorescenceDetectorComposite. Call at least one of setDetector() and
	 * setDetectorParameters() before calling initialise() to set up the controller.
	 */
	public FluorescenceDetectorCompositeController(FluorescenceDetectorComposite ui) {
		fluorescenceDetectorComposite = ui;
	}

	public void setApplyParametersBeforeAcquire(boolean applyParametersBeforeAcquire) {
		this.applyParametersBeforeAcquire = applyParametersBeforeAcquire;
	}

	/**
	 * Set the detector to be configured by this controller. Call this (if required) before calling initialise()
	 *
	 * @param detector
	 *            the FluorescenceDetector instance to be configured
	 */
	public void setDetector(FluorescenceDetector detector) {
		this.theDetector = detector;
	}

	/**
	 * Set the detector parameters bean to be edited by the FluorescenceDetectorComposite. Call this (if required) after GUI construction but before calling
	 * initialise()
	 */
	public void setDetectorParameters(FluorescenceDetectorParameters parameters) {
		detectorParameters = parameters;
		updateDataBindingController();
		checkDetectorMatchesParameters();

		if (fluorescenceDetectorComposite != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateUIFromBean();
					plotDataAndUpdateCounts();
					updatePlottedRegion();
				}
			});
		}
	}

	private void updateDataBindingController() {
		if (fluorescenceDetectorComposite != null && detectorParameters != null) {
			IBeanService service = ExafsActivator.getService(IBeanService.class);
			dataBindingController = service.createController(fluorescenceDetectorComposite, detectorParameters);
		}
	}

	private void checkDetectorMatchesParameters() {
		if (theDetector != null && detectorParameters != null) {
			if (!theDetector.getName().equals(detectorParameters.getDetectorName())) {
				logger.warn("Names do not match! Detector being configured: {}, detector named in parameters: {}", theDetector.getName(),
						detectorParameters.getDetectorName());
			}

			if (theDetector.getNumberOfElements() != detectorParameters.getDetectorList().size()) {
				logger.warn("Sizes do not match! Detector {} has {} elements but the parameters object has {} elements", theDetector.getName(),
						theDetector.getNumberOfElements(), detectorParameters.getDetectorList().size());
			}
		}
	}

	/**
	 * Call this method from the UI thread once the GUI has been fully constructed and the detector parameters object has been set (if desired; otherwise the
	 * current parameters will be fetched from the detector but this risks losing synchronisation between parameter objects if a different one is held by a
	 * containing editor)
	 */
	public void initialise() {

		// Make sure we have a detector and a parameters object if possible
		if (theDetector == null && detectorParameters != null) {
			String detectorName = detectorParameters.getDetectorName();
			logger.debug("No detector set; trying to get detector named in parameters: '{}'", detectorName);
			Findable namedObject = Finder.getInstance().find(detectorName);
			if (namedObject instanceof FluorescenceDetector) {
				theDetector = (FluorescenceDetector) namedObject;
			}
		}

		if (theDetector == null) {
			fluorescenceDetectorComposite.setDetectorName("No detector found!");
			logger.warn("No detector found");
			return;
		}

		if (detectorParameters == null) {
			fetchConfigurationFromDetector(); // initialises the dataBindingController as a side effect
			if (detectorParameters == null) {
				fluorescenceDetectorComposite.setDetectorName("No detector parameters available");
				logger.warn("No detector parameters returned by detector {}", theDetector.getName());
				return;
			}
		}

		// Set up the composite with information about the detector
		fluorescenceDetectorComposite.setDetectorName(theDetector.getName());
		setDetectorElementOrderPreference();
		fluorescenceDetectorComposite.setDetectorElementListSize(theDetector.getNumberOfElements());
		fluorescenceDetectorComposite.setMCASize(theDetector.getMCASize());
		fluorescenceDetectorComposite.setMaxNumberOfRois(theDetector.getMaxNumberOfRois());
		fluorescenceDetectorComposite.setOutputOptions( detectorParameters );
		fluorescenceDetectorComposite.setReadoutModeOptions( detectorParameters );

		// Add listeners
		try {
			dataBindingController.addValueListener(this);
		} catch (Exception ex) {
			logger.error("Error while adding value listener to UI", ex);
			displayErrorMessage("Data binding error", "Error initialising user interface. See log for details.");
		}

		fluorescenceDetectorComposite.addPlottedRegionListener(this);
		fluorescenceDetectorComposite.addBeanSelectionListener(this);

		fluorescenceDetectorComposite.addLoadButtonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				loadAcquireDataFromFile();
			}
		});

		fluorescenceDetectorComposite.addSaveButtonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				saveDataToFile(true);
			}
		});

		fluorescenceDetectorComposite.addAcquireButtonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (applyParametersBeforeAcquire) {
					applyConfigurationToDetector();
				}
				if (fluorescenceDetectorComposite.getContinuousModeSelection()) {
					continuousAcquire(fluorescenceDetectorComposite.getAcquisitionTime());
				} else {
					singleAcquire(fluorescenceDetectorComposite.getAcquisitionTime(), fluorescenceDetectorComposite.getAutoSaveModeSelection());
				}
			}
		});

		fluorescenceDetectorComposite.addApplySettingsButtonListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
					applyConfigurationToDetector();
			}
		});

		fluorescenceDetectorComposite.addContinuousModeButtonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (fluorescenceDetectorComposite.getContinuousModeSelection()) {
					fluorescenceDetectorComposite.setContinuousAcquireMode();
				} else {
					fluorescenceDetectorComposite.setSingleAcquireMode();
				}
			}
		});

		fluorescenceDetectorComposite.addRegionImportButtonSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				WizardDialog dialog = new WizardDialog(fluorescenceDetectorComposite.getShell(), new ImportFluoDetROIWizard(fluorescenceDetectorComposite
						.getRegionList(), fluorescenceDetectorComposite.getDetectorList(), detectorParameters.getClass()));
				dialog.create();
				dialog.open();
			}
		});

		// Listener for readout mode combo
		fluorescenceDetectorComposite.addReadoutModeListener(new ValueAdapter("readoutModeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				fluorescenceDetectorComposite.updateRoiWindowSettings();
			}
		} );

		ExafsActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED)) {
					setRegionEditableFromPreference();
				}
			}
		});

		fluorescenceDetectorComposite.addShowLoadedDataListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				fluorescenceDetectorComposite.showHideLoadedDataset();
			}
		});

		// setup the default dragging behaviour
		setRegionEditableFromPreference();

		// Hide/show output options
		setShowOutputOptionsFromPreference();

		// setup data store and fetch stored data, if any
		createDataStore();
		theData = dataStore.readDataFromFile();
		plotTitle = "Saved data";

		plotDataAndUpdateCounts();

		fluorescenceDetectorComposite.autoscaleAxes();

		fluorescenceDetectorComposite.setEnableShowLoadedDataCheckBox(dataLoadedFromFile!=null ? true : false);
	}

	/**
	 * Fetch the current configuration from the detector.
	 * <p>
	 * Be careful calling this when the FluorescenceDetectorComposite is being used in an editor - it will cause the detector parameters objects underlying the
	 * editor and the composite to be different and subject to synchronisation problems.
	 */
	public void fetchConfigurationFromDetector() {
		setDetectorParameters(theDetector.getConfigurationParameters());
	}

	private void updateUIFromBean() {
		try {
			dataBindingController.switchState(false);
			dataBindingController.beanToUI();
			dataBindingController.switchState(true);
		} catch (Exception ex) {
			logger.error("Error trying to update UI from bean", ex);
			displayErrorMessage("Data binding error", "Error trying to update user interface. See log for details.");
		}
	}

	private void setRegionEditableFromPreference() {
		fluorescenceDetectorComposite.setPlottedRegionMobile(ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED));
	}

	private void setShowOutputOptionsFromPreference() {
		boolean outputEditorShowOutputOptions = ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS);
		fluorescenceDetectorComposite.setShowOutputOptions(!outputEditorShowOutputOptions);
	}

	private void setDetectorElementOrderPreference() {
		int elementOrder = ExafsActivator.getDefault().getPreferenceStore().getInt(ExafsPreferenceConstants.DETECTOR_ELEMENT_ORDER);
		GRID_ORDER order = GRID_ORDER.LEFT_TO_RIGHT_TOP_TO_BOTTOM;
		if (elementOrder<GRID_ORDER.values().length) {
			order = GRID_ORDER.values()[elementOrder];
		}
		fluorescenceDetectorComposite.setDetectorElementOrder(order);
	}

	private void createDataStore() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		String fileName = varDir + "/" + theDetector.getName() + "_plot_data.xml";
		dataStore = new FluoCompositeDataStore(fileName);
	}

	/**
	 * Plot current data and update the count totals.
	 * <p>
	 * SWT thread safe.
	 */
	private void replot() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				plotDataAndUpdateCounts();
			}
		});
	}

	/**
	 * Plot current data and update the count totals.
	 * <p>
	 * Not thread safe, must be called from UI thread.
	 */
	private void plotDataAndUpdateCounts() {

		if (theData != null) {
			int element = getCurrentlySelectedElementNumber();

			double[] elementData = (element < theData.length) ? theData[element] : new double[] {/* empty */};
			Dataset dataset = DatasetFactory.createFromObject(elementData);
			dataset.setName(LIVE_DATA_NAME);

			double[] savedElementData = new double[] {};
			if ( dataLoadedFromFile != null ) {
				savedElementData = (element < dataLoadedFromFile.length) ? dataLoadedFromFile[element] : new double[] {/* empty */};
			}
			Dataset savedData = DatasetFactory.createFromObject(savedElementData);
			savedData.setName(LOADED_DATA_NAME);

			String elementName = "Element " + element;
			fluorescenceDetectorComposite.setXAxisLabel("Channel Number (" + elementName + ")");
			fluorescenceDetectorComposite.setElementName(elementName);

			fluorescenceDetectorComposite.setPlotTitle(plotTitle);
			fluorescenceDetectorComposite.plotDatasets(dataset, savedData);

			if (fluorescenceDetectorComposite.getAutoScaleOnAcquire())
				fluorescenceDetectorComposite.autoscaleAxes();

			calculateAndDisplayCountTotals();
		}
	}

	private int getCurrentlySelectedElementNumber() {
		return fluorescenceDetectorComposite.getSelectedDetectorElementIndex();
	}

	private void calculateAndDisplayCountTotals() {
		if (theData != null) {
			double enabledElementCounts = calculateEnabledElementTotal();
			fluorescenceDetectorComposite.setEnabledElementsCounts(enabledElementCounts);

			int currentElement = getCurrentlySelectedElementNumber();
			double currentElementCounts = 0;
			double regionCounts = 0;
			if (currentElement < theData.length) {
				currentElementCounts = calculateSingleElementTotal(theData[currentElement]);
				int regionStart, regionEnd;
				if (fluorescenceDetectorComposite.getReadoutModeIsRoi() ) {
					regionStart = fluorescenceDetectorComposite.getRegionStart();
					regionEnd = fluorescenceDetectorComposite.getRegionEnd();
				} else {
					regionStart = fluorescenceDetectorComposite.getWindowStart();
					regionEnd = fluorescenceDetectorComposite.getWindowEnd();
				}
				regionCounts = calculateRegionTotal(theData[currentElement], regionStart, regionEnd);
			}
			fluorescenceDetectorComposite.setSelectedElementCounts(currentElementCounts);
			fluorescenceDetectorComposite.setSelectedRegionCounts(regionCounts);
		}
	}

	private double calculateEnabledElementTotal() {
		double total = 0;
		for (DetectorElement element : detectorParameters.getDetectorList()) {
			if (element.getNumber() < theData.length && !element.isExcluded()) {
				total += calculateSingleElementTotal(theData[element.getNumber()]);
			}
		}
		return total;
	}

	private double calculateSingleElementTotal(double[] elementData) {
		double total = 0;
		for (double val : elementData) {
			total += val;
		}
		return total;
	}

	private double calculateRegionTotal(double[] elementData, int regionStart, int regionEnd) {
		// Correct bounds
		int start = Math.max(0, regionStart);
		int end = Math.min(elementData.length, regionEnd);

		double total = 0;
		for (int index = start; index < end; index++) {
			total += elementData[index];
		}
		return total;
	}

	/**
	 * Start/stop continuous acquisition
	 *
	 * @param collectionTime
	 */
	private synchronized void continuousAcquire(final double collectionTime) {
		if (continuousAquire) {
			stopContinuousAcquire();
		} else {
			startContinuousAcquire(collectionTime);
		}
	}

	private void stopContinuousAcquire() {
		logger.debug("Stopping detector");
		continuousAquire = false;
	}

	private void startContinuousAcquire(final double collectionTime) {
		try {
			continuousAquire = true;
			acquireStarted();
			continuousThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (continuousAquire) {
						if (fluorescenceDetectorComposite.isDisposed()) {
							break;
						}
						acquire(null, collectionTime, false);
					}
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							acquireFinished();
						}
					});
				}
			}, "Detector Live Runner");
			continuousThread.start();
		} catch (IllegalThreadStateException e) {
			logger.error("Problem starting continuous acquire thread.", e);
			displayErrorMessage("Exception starting continuous acquire", "Internal error while trying to start continuous acquisition. See log for details.");
		}
	}

	private void acquireStarted() {
		logAndAppendStatus("Continuous acquire started");
		fluorescenceDetectorComposite.showAcquireStarted();
	}

	private void acquireFinished() {
		logAndAppendStatus("Continuous acquire stopped");
		fluorescenceDetectorComposite.showAcquireFinished();
	}

	private void logAndAppendStatus(String message) {
		logger.info(message);
		fluorescenceDetectorComposite.appendStatus(message);
	}

	/**
	 * Acquire a single frame from the detector
	 *
	 * @param collectionTimeValue
	 *            the time to collect for (in milliseconds)
	 * @param writeToDisk
	 *            set <code>true</code> to save data automatically after collection
	 */
	private void singleAcquire(final double collectionTimeValue, final boolean writeToDisk) {

		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		try {
			service.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					acquire(monitor, collectionTimeValue, writeToDisk);
				}
			});
		} catch (InterruptedException cancellationIgnored) {
		} catch (InvocationTargetException ex) {
			logger.error("Error acquiring data from fluorescence detector: {}", theDetector, ex);
		}
	}

	private void acquire(IProgressMonitor monitor, final double collectionTimeValue, boolean writeToDisk) {
		int numWorkUnits = 2;

		if (monitor != null) {
			monitor.beginTask("Acquiring snapshot...", numWorkUnits);
		}

		try {
			theData = theDetector.getMCAData(collectionTimeValue);
			checkDataDimensionsMatchDetectorSize();
			dataStore.writeDataToFile(theData);

			if (monitor != null) {
				monitor.worked(1);
			}

			updatePlotTitle();
			replot();

			if (monitor != null) {
				monitor.worked(1);
			}

			if (writeToDisk) {
				saveDataToFile(false);
			} else if (monitor != null) {
				logAndAppendStatus("Data successfully acquired");
			}
		} catch (DeviceException de) {
			logger.error("Exception reading out detector data.", de);
			displayErrorMessage("Exception reading out detector data", "Hardware problem acquiring data. See log for details.");
		}

		if (monitor != null) {
			monitor.done();
		}
	}

	private void checkDataDimensionsMatchDetectorSize() {
		if (theData.length != theDetector.getNumberOfElements()) {
			logger.warn("Detector {} has returned data for {} elements, but should have {} elements", theDetector.getName(), theData.length,
					theDetector.getNumberOfElements());
		}

		if (theData[0].length != theDetector.getMCASize()) {
			logger.warn("Detector {} has returned {} channels of MCA data, but should have {} channels", theDetector.getName(), theData[0].length,
					theDetector.getMCASize());
		}
	}

	private void updatePlotTitle() {
		Date now = new Date();
		SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
		plotTitle = "Acquire at " + dt.format(now);
	}

	private void displayErrorMessage(final String uiTitle, final String uiMessage) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), uiTitle, uiMessage);
			}
		});
	}

	/**
	 * Apply the current parameters to the detector hardware
	 */
	public void applyConfigurationToDetector() {
		try {
			theDetector.applyConfigurationParameters(detectorParameters);
			logAndAppendStatus("Successfully applied settings to detector");

		} catch (DeviceException de) {
			logger.error("Exception applying detector settings.", de);
			displayErrorMessage("Exception applying detector settings", "Hardware problem applying detector settings. See log for details.");
		} catch (Exception ex) {
			logger.error("Exception applying detector settings.", ex);
			displayErrorMessage("Exception applying detector settings", "Internal error while applying detector settings. See log for details.");
		}
	}

	/**
	 * Load existing data from a file chosen by the user
	 */
	public void loadAcquireDataFromFile() {
		if (openDialog == null) {
			openDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
		}

		String dataDir = PathConstructor.createFromDefaultProperty();
		openDialog.setFilterPath(dataDir);
		openDialog.setFilterNames(new String[] { "*.mca" });
		final String filePath = openDialog.open();
		if (filePath != null) {
			FluoCompositeDataStore newStore = new FluoCompositeDataStore(filePath);
			dataLoadedFromFile = newStore.readDataFromFile();
			plotTitle = "Saved data";
			replot();
			fluorescenceDetectorComposite.autoscaleAxes();
			fluorescenceDetectorComposite.setEnableShowLoadedDataCheckBox(true);
		}
	}

	/**
	 * Save the current data to a file. The file name and location are chosen automatically.
	 * @param showDialogBox if true, display dialog box to allow user to set file name and location.
	 */
	public void saveDataToFile(boolean showDialogBox) {
		try {
			String snapshotPrefix = theDetector.getName() + "_snapshot";
			long snapShotNumber = new NumTracker(snapshotPrefix).incrementNumber();
			String fileName = snapshotPrefix + snapShotNumber + ".mca";

			String spoolDirPath = PathConstructor.createFromDefaultProperty();
			File filePath = new File(spoolDirPath + "/" + fileName);
			String outputFilename = filePath.getAbsolutePath();

			// Display dialog box to specify output directory (only if *not* using 'save on acquire')
			if (showDialogBox) {
				FileDialog dialog = new FileDialog(fluorescenceDetectorComposite.getShell(), SWT.SAVE);
				dialog.setFilterPath(filePath.getParent());
				dialog.setFileName(fileName);
				String[] filters = new String[] { "*.mca", "*.dat" };
				dialog.setFilterExtensions(filters);
				outputFilename = dialog.open();
			}

			if (outputFilename != null) {
				FluoCompositeDataStore newStore = new FluoCompositeDataStore(outputFilename);
				newStore.writeDataToFile(theData);
				newStore.writeDataToColumnFile(theData);

				String msg = "Saved: " + outputFilename;
				logAndAppendStatus(msg);
				msg = "Saved: " + newStore.getColumnFileName();
				logAndAppendStatus(msg);
		    }
		} catch (IOException ie) {
			logger.error("Exception writing out detector data.", ie);
			displayErrorMessage("Exception writing out detector data", "Problem recording data. See log for details.");
		}
	}

	@Override
	public void selectionChanged(BeanSelectionEvent selectionEvent) {
		Object selectedBean = selectionEvent.getSelectedBean();
		if (selectedBean instanceof IDetectorElement) {
			// Element changed - need to replot data and recalculate all counts
			replot();

			// Re-select the first region in the region list. (The first region is automatically selected anyway so this has no visible effect for the user.)
			// This is a workaround for a Rich Beans problem: bounds updaters are not fired on nested list editors, so changing the detector element selection
			// can leave the ROI start and end boxes with invalid bounds until another value change occurs. Calling setSelectedBean() (via setSelectedIndex())
			// on the region list causes a refresh at the correct nesting level. If there are no regions the call will throw IndexOutOfBoundsException, which we
			// silently ignore.
			try {
				fluorescenceDetectorComposite.getRegionList().setSelectedIndex(0);
			} catch (IndexOutOfBoundsException ignored) {
				// empty - ignore this exception
			}
		} else if (selectedBean instanceof DetectorROI) {
			// Region changed - need to redraw region on plot and recalculate region count
			updatePlottedRegion();
			calculateAndDisplayCountTotals();
		} else if (selectedBean != null) {
			logger.warn("Unexpected BeanSelectionEvent received from: {}", selectionEvent.getSource());
		}
	}

	@Override
	public void valueChangePerformed(ValueEvent event) {
		updateBeanFromUI();

		if (fluorescenceDetectorComposite.isApplyRoisToAllElements()) {
			// Apply current window range or ROI to all elements, depending on current readout mode.
			String readoutMode = fluorescenceDetectorComposite.getReadoutMode().getValue().toString();
			if ( XspressDetector.READOUT_ROIS.equals(readoutMode) )
				applyCurrentRegionsToAllElements();
			else
				applyCurrentWindowToAllElements();
		}

		calculateAndDisplayCountTotals(); // might want to only update the changed totals to speed up UI?

		// Update the plot with the new region, but check that this event has not originally been caused by an update
		// of the ROI UI from the plot to avoid a recursive loop of events.
		if (!updatingRoiUIFromPlot) {
			updatePlottedRegion();
		}
	}

	/**
	 * Apply window parameters of current detector element to all elements
	 *
	 */
	private void applyCurrentWindowToAllElements() {
		final int currentElementNumber = getCurrentlySelectedElementNumber();
		int windowStart = detectorParameters.getDetector(currentElementNumber).getWindowStart();
		int windowEnd = detectorParameters.getDetector(currentElementNumber).getWindowEnd();

		List<DetectorElement> elements = new ArrayList<DetectorElement>(detectorParameters.getDetectorList());
		// Do not overwrite the regions of the current element - this is important to avoid synchronisation issues with updates from the UI
		elements.remove(currentElementNumber);

		for (DetectorElement element : elements) {
			element.setWindow(windowStart, windowEnd);
		}
	}

	/**
	 * Apply list of ROI parameters of current detector element to all elements
	 */
	private void applyCurrentRegionsToAllElements() {
		final int currentElementNumber = getCurrentlySelectedElementNumber();
		List<DetectorROI> regionsToCopy = detectorParameters.getDetector(currentElementNumber).getRegionList();
		List<DetectorElement> elements = new ArrayList<DetectorElement>(detectorParameters.getDetectorList());

		// Do not overwrite the regions of the current element - this is important to avoid synchronisation issues with updates from the UI
		elements.remove(currentElementNumber);

		for (DetectorElement element : elements) {
			// Copy the regions list to avoid problems with shared objects
			List<DetectorROI> copiedRegions = new ArrayList<DetectorROI>(regionsToCopy.size());
			for (DetectorROI regionToCopy : regionsToCopy) {
				copiedRegions.add(new DetectorROI(regionToCopy));
			}
			element.setRegionList(copiedRegions);
		}
	}

	private void updateBeanFromUI() {
		try {
			dataBindingController.uiToBean();
		} catch (Exception ex) {
			logger.error("Error trying to update bean from UI", ex);
		}
	}

	private void updatePlottedRegion() {
		updatingRoiPlotFromUI = true;
		fluorescenceDetectorComposite.updatePlottedRegionFromUI();
		updatingRoiPlotFromUI = false;
	}

	@Override
	public String getValueListenerName() {
		return this.getClass().getName();
	}

	@Override
	public void roiSelected(ROIEvent event) {
		// ignore these events
	}

	@Override
	public void roiDragged(ROIEvent event) {
		// Ignore these events - might be nice but slows the UI down too much to update bean and counts while dragging
		// (In theory it should be possible to do this quickly, since the UI is quite smooth when changing the region
		// by dragging the adjuster bars on the RoiStart and RoiEnd boxes, but it's not an important enough feature to
		// worry about for now.)
	}

	@Override
	public void roiChanged(ROIEvent event) {
		updateRoiUIFromPlot(event);
	}

	private void updateRoiUIFromPlot(ROIEvent event) {
		// Update the UI with the new region bounds, but check that this event has not originally been caused by an
		// update of the plot from the UI to avoid a recursive loop of events.
		if (!updatingRoiPlotFromUI) {
			updatingRoiUIFromPlot = true;
			int start = (int) ((RectangularROI) event.getROI()).getPoint()[0];
			int end = (int) ((RectangularROI) event.getROI()).getEndPoint()[0];
			if (fluorescenceDetectorComposite.getReadoutModeIsRoi()) {
				fluorescenceDetectorComposite.setRegionStart(start);
				fluorescenceDetectorComposite.setRegionEnd(end);
			} else {
				fluorescenceDetectorComposite.setWindowStart(start);
				fluorescenceDetectorComposite.setWindowEnd(end);
			}

			updatingRoiUIFromPlot = false;
		}
	}
}
