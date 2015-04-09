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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.dawnsci.common.richbeans.beans.BeanController;
import org.dawnsci.common.richbeans.components.scalebox.NumberBox;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.event.ValueListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

/**
 * Performs the work using shared objects and composites in this package.
 * <p>
 * This performs the interactions between these composites and the underlying detector object.
 */
public class FluoDetectorCompositeController {

	private static Logger logger = LoggerFactory.getLogger(FluoDetectorCompositeController.class);

	private FluorescenceDetector theDetector;
	private FluorescenceDetectorComposite fluorescenceDetectorComposite;

	private FluorescenceDetectorParameters detectorParameters;
	private BeanController dataBindingController;
	private boolean continuousAquire;
	private Thread continuousThread;
	private SashFormPlotComposite sashFormPlot;
	private FluoCompositeDataStore dataStore;
	private int[][] theData;
	private String plotTitle;
	private FileDialog openDialog;

	public FluoDetectorCompositeController(FluorescenceDetectorComposite fluorescenceDetectorComposite,
			FluorescenceDetectorParameters detectorParameters, FluorescenceDetector theDetector) {

		this.detectorParameters = detectorParameters;
		this.theDetector = theDetector;
		this.fluorescenceDetectorComposite = fluorescenceDetectorComposite;
	}

	/**
	 * Call this method once the GUI has been fully constructed
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		dataBindingController = new BeanController(fluorescenceDetectorComposite, detectorParameters);
		dataBindingController.beanToUI();
		dataBindingController.switchUIOn();

		dataBindingController.addValueListener(new ValueAdapter() {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				applyCurrentRegionsToAllElements();
			}
		});

		sashFormPlot = fluorescenceDetectorComposite.getSashFormPlot();
		sashFormPlot.addRegionListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
				// ignore these events
			}

			@Override
			public void roiDragged(ROIEvent evt) {
				updateRoiUI(evt);
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				updateRoiUI(evt);

				// TODO need to make this work properly - currently changes don't reach bean
				updateBeanFromUI();
			}
		});

		ExafsActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED)) {
					setRegionEditableFromPreference();
				}
			}
		});

		// setup the default dragging behaviour
		setRegionEditableFromPreference();

		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		String fileName = varDir + "/" + theDetector.getName() + "_plot_data.xml";
		dataStore = new FluoCompositeDataStore(fileName);
	}

	private void setRegionEditableFromPreference() {
		// Bug in dawn stops this working correctly when preference is changed at runtime
		// See DAWNSCI-5843 for latest status on a possible fix
		// sashFormPlot.getRegionOnDisplay().setMobile(
		// ExafsActivator.getDefault().getPreferenceStore()
		// .getBoolean(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED));
		sashFormPlot.getRegionOnDisplay().setMobile(false); // TODO temporary while the binding from plot to bean is
															// failing
	}

	private void updateRoiUI(ROIEvent evt) {
		IROI roi = evt.getROI();
		final int start = (int) Math.round(roi.getPointX());
		final int end = (int) (start + Math.round(roi.getBounds().getLength(0)));

		updateNumberBoxIntegerValue(fluorescenceDetectorComposite.getRegionsComposite().getRoiStart(), start);
		updateNumberBoxIntegerValue(fluorescenceDetectorComposite.getRegionsComposite().getRoiEnd(), end);
	}

	private void updateNumberBoxIntegerValue(NumberBox numberBox, int value) {
		numberBox.off();
		numberBox.setIntegerValue(value);
		numberBox.on();
	}

	public FluorescenceDetector getDetector() {
		return theDetector;
	}

	public SashFormPlotComposite getSashFormPlot() {
		return fluorescenceDetectorComposite.getSashFormPlot();
	}

	public FluorescenceDetectorComposite getFluorescenceDetectorComposite() {
		return fluorescenceDetectorComposite;
	}

	private Shell getCurrentShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	private void plot(final int element) {
		Dataset dataset = DatasetFactory.createFromObject(theData[element]);
		sashFormPlot.setDataSets(dataset);
		sashFormPlot.getPlottingSystem().setRescale(true);
		sashFormPlot.plotData();
		sashFormPlot.getPlottingSystem().setTitle(plotTitle);
		sashFormPlot.getPlottingSystem().setRescale(false);
		sashFormPlot.getPlottingSystem().setShowLegend(false);

		updateElementNameLabel(element);
		calculateAndDisplayCountTotals(element);
		// TODO also add roi counts to roi listener
	}

	private void updateElementNameLabel(int element) {
		fluorescenceDetectorComposite.getRegionsComposite().getElementNameLabel().setValue("Element " + element);
	}

	private void calculateAndDisplayCountTotals(int element) {
		String totalCounts = getFormattedTotal(theData);
		fluorescenceDetectorComposite.getRegionsComposite().getTotalCountsLabel().setValue(totalCounts);

		String elementCounts = getFormattedTotal(theData[element]);
		fluorescenceDetectorComposite.getRegionsComposite().getElementCountsLabel().setValue(elementCounts);
	}

	private String getFormattedTotal(Object data) {
		Dataset dataset = DatasetFactory.createFromObject(data);
		Long totalCounts = (Long) dataset.typedSum(Dataset.INT64);
		return NumberFormat.getInstance().format(totalCounts.longValue());
	}

	/**
	 * Start/stops continuous acquire
	 * 
	 * @param collectionTime
	 */
	synchronized void continuousAcquire(final double collectionTime) {
		if (continuousAquire) {
			stopContinuousAcquire();
		} else {
			startContinuousAcquire(collectionTime);
		}
	}

	private void startContinuousAcquire(final double collectionTime) {
		try {
			continuousAquire = true;
			acquireStarted();
			continuousThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (continuousAquire) {
							if (sashFormPlot.getPlottingSystem().isDisposed()) {
								break;
							}
							acquire(null, collectionTime, false);
						}
					} catch (Exception e) {
						logger.error("Continuous acquire problem with detector.", e);
					} finally {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								acquireFinished();
							}
						});
					}
				}
			}, "Detector Live Runner");
			continuousThread.start();
		} catch (IllegalThreadStateException e) {
			logger.error("Problem starting continuous acquire thread.", e);
		}
	}

	private void stopContinuousAcquire() {
		logger.debug("Stopping detector");
		continuousAquire = false;
	}

	private void acquireStarted() {
		sashFormPlot.appendStatus("Continuous acquire started.", logger);
		fluorescenceDetectorComposite.getAcquireComposite().showAcquireStarted();
	}

	private void acquireFinished() {
		sashFormPlot.appendStatus("Continuous acquire stopped.", logger);
		fluorescenceDetectorComposite.getAcquireComposite().showAcquireFinished();
	}

	void singleAcquire(final double collectionTimeValue, final boolean writeToDisk) throws Exception {

		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		service.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					acquire(monitor, collectionTimeValue, writeToDisk);
				} catch (Exception e) {
					logger.error("Error performing single acquire", e);
				}
			}
		});
	}

	private void acquire(IProgressMonitor monitor, final double collectionTimeValue, boolean writeToDisk) {
		int numWorkUnits = 2;

		if (monitor != null) {
			monitor.beginTask("Acquiring snapshot...", numWorkUnits);
		}

		try {
			theData = theDetector.getMCData(collectionTimeValue);
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
				saveDataToFile();
			} else if (monitor != null) {
				sashFormPlot.appendStatus("Data successfully acquired.", logger);
			}
		} catch (DeviceException e) {
			logAndDisplayErrorMessage("Exception reading out detector data",
					"Hardware problem acquiring data. See log for details.", "Exception reading out detector data.", e);
		} catch (IOException e) {
			logAndDisplayErrorMessage("Exception writing out detector data",
					"Problem recording data. See log for details.", "Exception writing out detector data.", e);
		}

		if (monitor != null) {
			monitor.done();
		}
	}

	private void updatePlotTitle() {
		Date now = new Date();
		SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
		plotTitle = "Acquire at " + dt.format(now);
	}

	private void logAndDisplayErrorMessage(final String uiTitleMessage, final String uiMessage,
			final String logMessage, final Exception e) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						uiTitleMessage, uiMessage);
			}
		});
		logger.error(logMessage, e);
	}

	private void saveDataToFile() throws IOException {
		String snapshotPrefix = theDetector.getName() + "_snapshot";
		long snapShotNumber = new NumTracker(snapshotPrefix).incrementNumber();
		String fileName = snapshotPrefix + snapShotNumber + ".mca";

		String spoolDirPath = PathConstructor.createFromDefaultProperty();
		File filePath = new File(spoolDirPath + "/" + fileName);
		String spoolFilePath = filePath.getAbsolutePath();

		FluoCompositeDataStore newStore = new FluoCompositeDataStore(spoolFilePath);
		newStore.writeDataToFile(theData);

		String msg = "Saved: " + spoolFilePath;
		sashFormPlot.appendStatus(msg, logger);
	}

	public void replot() {
		if (theData == null) {
			loadDataFromStore();
		}

		if (theData != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					// getDetectorElementComposite().setEndMaximum(detectorData[0][0].length - 1);
					// plot(getDetectorList().getSelectedIndex(), true);
					int element = fluorescenceDetectorComposite.getRegionsComposite().getSelectedDetectorElement();
					plot(element);
					// setEnabled(true);
					// lblDeadTime.setVisible(true);
					// deadTimeLabel.setVisible(true);
					// sashFormPlot.getLeft().layout();
				}
			});
		}
	}

	private void loadDataFromStore() {
		theData = dataStore.readDataFromFile();
		if (theData != null) {
			plotTitle = "Saved data";
		}
	}

	public void updatePlottedRegion(int roiStart, int roiEnd) {
		if (sashFormPlot != null) {
			sashFormPlot.getRegionOnDisplay().setROI(new RectangularROI(roiStart, 0, roiEnd - roiStart, 0, 0));
			sashFormPlot.getRegionOnDisplay().repaint();
		}
	}

	private void updateBeanFromUI() {
		try {
			dataBindingController.uiToBean();
			dataBindingController.fireValueListeners();
		} catch (Exception ex) {
			logger.error("Error trying to update bean from UI", ex);
		}
	}

	private void updateUIFromBean() {
		try {
			dataBindingController.beanToUI();
		} catch (Exception ex) {
			logger.error("Error trying to update UI from bean", ex);
		}
	}

	public void applyConfigurationToDetector() {
		try {
			theDetector.applyConfigurationParameters(detectorParameters);
			sashFormPlot.appendStatus("Successfully applied settings to detector", logger);

		} catch (DeviceException de) {
			logAndDisplayErrorMessage("Exception applying detector settings",
					"Hardware problem applying detector settings. See log for details.",
					"Exception applying detector settings.", de);
		} catch (Exception ex) {
			logAndDisplayErrorMessage("Exception applying detector settings",
					"Internal error while applying detector settings. See log for details.",
					"Exception applying detector settings.", ex);
		}
	}

	public void fetchConfigurationFromDetector() {
		detectorParameters = theDetector.getConfigurationParameters();
		updateUIFromBean();
		applyCurrentRegionsToAllElements();
		replot();
	}

	public void loadAcquireDataFromFile() {
		if (openDialog == null) {
			openDialog = new FileDialog(getCurrentShell(), SWT.OPEN);
		}

		String dataDir = PathConstructor.createFromDefaultProperty();
		openDialog.setFilterPath(dataDir);
		openDialog.setFilterNames(new String[] { "*.mca" });
		final String filePath = openDialog.open();
		if (filePath != null) {

			FluoCompositeDataStore newStore = new FluoCompositeDataStore(filePath);
			theData = newStore.readDataFromFile();
			replot();

			final String msg = ("Loading map from " + filePath);
			Job job = new Job(msg) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					FluoCompositeDataStore newStore = new FluoCompositeDataStore(filePath);
					theData = newStore.readDataFromFile();
					replot();
					return Status.OK_STATUS;
				}

			};
			job.setUser(true);
			job.schedule();
		}
	}

	public void applyCurrentRegionsToAllElements() {

		final int currentIndex = fluorescenceDetectorComposite.getRegionsComposite().getSelectedDetectorElement();

		List<DetectorROI> regions = detectorParameters.getDetector(currentIndex).getRegionList();

		List<DetectorElement> elements = detectorParameters.getDetectorList();
		for (DetectorElement element : elements) {
			// TODO consider making a copy of the regions list if the detector classes ever support separate region
			// lists for each detector element
			element.setRegionList(regions);
		}
	}

	/**
	 * Add a value listener to all UI elements
	 */
	public void addValueListener(ValueListener listener) throws Exception {
		dataBindingController.addValueListener(listener);
	}

	/**
	 * Remove a value listener from all UI elements
	 */
	public void removeValueListener(ValueListener listener) throws Exception {
		dataBindingController.removeValueListener(listener);
	}
}
