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

package uk.ac.gda.exafs.ui.views.detectors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorComposite;
import uk.ac.gda.exafs.ui.composites.detectors.FluorescenceDetectorCompositeController;

/**
 * Configures the regions of interest of a fluorescence detector. If this view is opened with a secondary ID, it is
 * assumed to be the name of a detector and the view will connect to that detector. This allows multiple instances of
 * the view to be opened simultaneously to configure different detectors. If the view is opened without a secondary ID
 * (e.g. from the Show View menu) it will ask the GDA Finder for all objects which implement the FluorescenceDetector
 * interface, and prompt the user to choose which detector to configure if several are available.
 */
public class FluorescenceDetectorConfigurationView extends ViewPart {

	public static final String ID = "uk.ac.gda.exafs.ui.views.detectors.FluorescenceDetectorConfigurationView";

	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorConfigurationView.class);

	private FluorescenceDetectorComposite fluorescenceDetectorComposite;
	private FluorescenceDetectorCompositeController controller;

	/** This will be set if view is being opened by {@link FluorescenceDetectorViewFactory} */
	private String detectorName = "";

	public FluorescenceDetectorConfigurationView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		String secondaryId = getViewSite().getSecondaryId();
		if (!detectorName.isEmpty()) {
			secondaryId = detectorName;
		}

		if (secondaryId == null) {
			// No secondary ID - view is probably being opened from the Show View menu
			// Find a detector, then close this view and reopen with a secondary ID to specify the chosen detector
			FluorescenceDetector detector = findDetectorToConfigure();
			if (detector != null) {
				reopenViewWithSecondaryId(detector.getName());
			}
		} else {
			// A detector name has been provided in the secondary ID - create the view
			setPartName(secondaryId + " Configuration");
			FluorescenceDetector detector = findNamedDetector(secondaryId);
			fluorescenceDetectorComposite = new FluorescenceDetectorComposite(parent, SWT.NONE);
			controller = new FluorescenceDetectorCompositeController(fluorescenceDetectorComposite);
			controller.setDetector(detector);
			controller.initialise();
		}
	}

	/**
	 * Choose a FluorescenceDetector to be configured by this view
	 */
	private FluorescenceDetector findDetectorToConfigure() {
		List<FluorescenceDetector> allDetectors = findAllFluorescenceDetectors();
		return chooseOneDetectorFromList(allDetectors);
	}

	/**
	 * Get a list of all FluorescenceDetector instances known to the Finder
	 */
	private List<FluorescenceDetector> findAllFluorescenceDetectors() {

		// Set up a list to be filled with results from the Finder
		final List<FluorescenceDetector> allDetectors = new ArrayList<FluorescenceDetector>();
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			// Finding all instances with the Finder can take a while. Set up a task to do this using the Eclipse
			// Progress Service to allow it to be cancelled if necessary
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InterruptedException {
					monitor.beginTask("Finding all available fluorescence detectors", IProgressMonitor.UNKNOWN);
					monitor.subTask("This might take a little while the first time it's done");

					// The only way to remain responsive is to set up a completely separate thread to call the Finder,
					// and wait for it to finish
					Thread finderThread = new Thread(new Runnable() {
						@Override
						public void run() {
							allDetectors.addAll(Finder.getInstance().listFindablesOfType(FluorescenceDetector.class));
						}
					});
					finderThread.start();

					// While waiting for the Finder thread to finish, check for cancellation periodically
					while (finderThread.isAlive()) {
						if (monitor.isCanceled()) {
							throw new InterruptedException("Task cancelled");
							// The Finder thread will continue in the background and terminate normally
						}
						Thread.sleep(50);
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			logger.error("Error while trying to get all FluorescenceDetectors from the Finder", e);
		} catch (InterruptedException e) {
			// Cancelled by user - no detectors will be found or selected so don't show the view
			cancelViewOpening();
		}
		return allDetectors;
	}

	/**
	 * Cancel opening the view. Can't actually stop the creation process, so instead hide the view as soon as possible
	 * after its creation is finished
	 */
	private void cancelViewOpening() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getSite().getPage().hideView(FluorescenceDetectorConfigurationView.this);
			}
		});
	}

	/**
	 * Get a single detector from the given list. If there is more than one item in the list, the user is prompted to
	 * choose one. If the list contains only a single item, that item is returned. If the list is empty, this method
	 * returns <code>null</code>
	 */
	private FluorescenceDetector chooseOneDetectorFromList(List<FluorescenceDetector> allDetectors) {
		if (allDetectors.size() == 0) {
			return null;
		} else if (allDetectors.size() == 1) {
			return allDetectors.get(0);
		} else {
			return getDetectorChoiceFromUser(allDetectors);
		}
	}

	/**
	 * Ask the user to choose a detector from a list dialog
	 */
	private FluorescenceDetector getDetectorChoiceFromUser(List<FluorescenceDetector> detectors) {
		// Sort the detector list alphabetically by name
		Collections.sort(detectors, new Comparator<FluorescenceDetector>() {
			@Override
			public int compare(FluorescenceDetector o1, FluorescenceDetector o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		// Set up the dialog to get a detector choice from the user
		ListDialog dialog = new ListDialog(getSite().getShell());
		dialog.setContentProvider(ArrayContentProvider.getInstance());
		dialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FluorescenceDetector) {
					return ((FluorescenceDetector) element).getName();
				}
				return null;
			}
		});
		dialog.setInput(detectors.toArray());
		dialog.setTitle("Choose a fluorescence detector to configure");

		// Show the dialog and handle the response
		FluorescenceDetector detector = null;
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length != 1) {
				// Shouldn't get more than one result from a ListDialog - log the error and continue
				logger.warn("Expected one choice from list dialog. {} choices returned", result.length);
			}
			if (result.length > 0 && result[0] instanceof FluorescenceDetector) {
				// A FluorescenceDetector was chosen - return it
				detector = (FluorescenceDetector) result[0];
			} else {
				logger.warn("Object returned from list dialog is not a FluorescenceDetector instance, or result list is empty");
			}
		} else {
			// User clicked cancel and so doesn't want to open the view - hide it immediately
			cancelViewOpening();
		}
		return detector;
	}

	/**
	 * Close this view and open again with the secondary ID specified
	 */
	private void reopenViewWithSecondaryId(final String secondaryId) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = getSite().getPage();
				page.hideView(FluorescenceDetectorConfigurationView.this);
				try {
					page.showView(FluorescenceDetectorConfigurationView.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					logger.error("Error activating view with secondary ID {}", secondaryId, e);
				}
			}
		});
	}

	/**
	 * Get a named FluorescenceDetector from the Finder
	 */
	private FluorescenceDetector findNamedDetector(String detectorName) {
		return Finder.getInstance().find(detectorName);
	}

	@Override
	public void setFocus() {
		if (fluorescenceDetectorComposite != null) {
			fluorescenceDetectorComposite.setFocus();
		}
	}

	/**
	 * Apply the Regions as displayed in the view to the detector
	 */
	public void applyConfigurationToDetector() {
		if (controller != null) {
			controller.applyConfigurationToDetector();
		} else {
			logger.warn("Controller does not exist, cannot apply detector configuration");
		}
	}

	/**
	 * Update the regions displayed in the view from the detector
	 */
	public void fetchConfigurationFromDetector() {
		if (controller != null) {
			controller.fetchConfigurationFromDetector();
		} else {
			logger.warn("Controller does not exist, cannot fetch detector configuration");
		}
	}

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}
}
