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

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;

/**
 * Performs the work using shared objects and composites in this package.
 * <p>
 * This performs the interactions between these composites and the underlying detector object.
 */
public class FluorescenceDetectorCompositeController {

	private static Logger logger = LoggerFactory.getLogger(FluorescenceDetectorCompositeController.class);

	private FluorescenceDetector theDetector;
	private IWorkbenchPartSite mysite;
	private FluorescenceDetectorComposite fluorescenceDetectorComposite;
	private boolean continuousAquire;
	private Thread continuousThread;
	private SashFormPlotComposite sashFormPlot;
	private ObservableComponent roiObservableComponent = new ObservableComponent();

	private double[][] theData;

	public FluorescenceDetectorCompositeController(FluorescenceDetector theDetector, IWorkbenchPartSite site,
			FluorescenceDetectorComposite fluorescenceDetectorComposite) {
		this.theDetector = theDetector;
		this.mysite = site;
		this.fluorescenceDetectorComposite = fluorescenceDetectorComposite;
		sashFormPlot = fluorescenceDetectorComposite.getSashFormPlot();

		sashFormPlot.getRegionOnDisplay().addROIListener(new IROIListener() {

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
			}
		});
	}

	protected void updateRoiUI(ROIEvent evt) {
		roiObservableComponent.notifyIObservers(this, evt);
	}

	public FluorescenceDetector getDetector() {
		return theDetector;
	}

	public IWorkbenchPartSite getSite() {
		return mysite;
	}

	public SashFormPlotComposite getSashFormPlot() {
		return fluorescenceDetectorComposite.getSashFormPlot();
	}

	public void plot(final double[] theData) {
		// final List<Dataset> data = unpackDataSets(theData);

		String plotTitle;
		// if (updateTitle) {
		Date now = new Date();
		SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
		plotTitle = "Acquire at " + dt.format(now);
		// }

		// for (int i = 0; i < data.size(); i++) {
		// String name = getChannelName(theData);
		// if (data.size() > 1) {
		// name += " " + i;
		// }
		// name += " " + plotTitle;
		// data.get(i).setName(name);
		// }

		sashFormPlot.setDataSets(new DoubleDataset(theData));
		sashFormPlot.getPlottingSystem().setRescale(true);
		sashFormPlot.plotData();
		sashFormPlot.getPlottingSystem().setTitle(plotTitle);
		// calculateAndPlotCountTotals(true); TODO
		sashFormPlot.getPlottingSystem().setRescale(false);
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

		IProgressService service = (IProgressService) getSite().getService(IProgressService.class);
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
			// TODO persist a copy of data storeDataInWrapper(theData);

			if (monitor != null) {
				monitor.worked(1);
			}

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
			return;
		} catch (IOException e) {
			logAndDisplayErrorMessage("Exception writing out detector data",
					"Problem recording data. See log for details.", "Exception writing out detector data.", e);
		}

		if (monitor != null) {
			monitor.done();
		}
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
		String spoolDirPath = PathConstructor.createFromDefaultProperty();
		String snapshotPrefix = theDetector.getName() + "_snapshot";
		long snapShotNumber = new NumTracker(snapshotPrefix).incrementNumber();
		String fileName = snapshotPrefix + snapShotNumber + ".mca";
		File filePath = new File(spoolDirPath + "/" + fileName);
		String spoolFilePath = filePath.getAbsolutePath();
		save(theData, spoolFilePath);
		String msg = "Saved: " + spoolFilePath;
		sashFormPlot.appendStatus(msg, logger);
	}

	private void save(double[][] data, String filePath) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

			StringBuilder toWrite = new StringBuilder();
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					toWrite.append(data[i][j]);
					toWrite.append("\t");
				}
				toWrite.append("\n");
				writer.write(toWrite.toString());
				toWrite.delete(0, toWrite.length());
			}
			writer.close();
		} catch (IOException e) {
			logger.warn("Exception writing acquire data to xml file", e);
		}
	}

	public void dispose() {
		sashFormPlot.dispose();
	}

	public void replot() {

		if (theData != null) {

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					// getDetectorElementComposite().setEndMaximum(detectorData[0][0].length - 1);
					// plot(getDetectorList().getSelectedIndex(), true);
					plot(theData[fluorescenceDetectorComposite.getRegionsComposite().getSelectedDetectorChannel()]);
					// setEnabled(true);
					// lblDeadTime.setVisible(true);
					// deadTimeLabel.setVisible(true);
					sashFormPlot.getLeft().layout();
				}
			});
		}
	}

	public void updatePlottedRegion(int roiStart, int roiEnd) {
		sashFormPlot.getRegionOnDisplay().setROI(new RectangularROI(roiStart, 0, roiEnd - roiStart, 0, 0));
		sashFormPlot.getRegionOnDisplay().repaint();
	}

	public void addRoiObserver(IObserver anIObserver) {
		roiObservableComponent.addIObserver(anIObserver);
	}

	public void deleteRoiObserver(IObserver anIObserver) {
		roiObservableComponent.deleteIObserver(anIObserver);
	}

	public void updateBeanFromUI() {
		fluorescenceDetectorComposite.updateBeanFromUI();
	}

	public void applyConfigurationToDetector() {
		try {
			theDetector.applyConfigurationParameters((FluorescenceDetectorParameters) fluorescenceDetectorComposite.getBean());
			sashFormPlot.appendStatus("Successfully applied settings to detector", logger);
			
		} catch (DeviceException de) {
			logAndDisplayErrorMessage("Exception applying detector settings",
					"Hardware problem applying detector settings. See log for details.",
					"Exception applying detector settings.",
					de);
		} catch (Exception ex) {
			logAndDisplayErrorMessage("Exception applying detector settings",
					"Internal error while applying detector settings. See log for details.",
					"Exception applying detector settings.",
					ex);
		}
	}

	public void fetchConfigurationFromDetector() {
		// TODO
	}
}
