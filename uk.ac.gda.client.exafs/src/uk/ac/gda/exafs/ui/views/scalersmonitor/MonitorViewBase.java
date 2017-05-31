/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.scalersmonitor;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProvider;
import gda.device.detector.DetectorMonitorDataProviderInterface;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;
import uk.ac.gda.client.CommandQueueViewFactory;

/**
 * Base class for view showing live data from an Ion chambers / fluorescence detector pairing.
 * <p>
 * Should be able to continue to show data during a scan by reading from the detectors but not operating them.
 */
public abstract class MonitorViewBase extends ViewPart implements IPartListener2, IObserver {

	protected static final Logger logger = LoggerFactory.getLogger(MonitorViewBase.class);
	protected final String ALREADY_RUNNING_MSG = "Scan/script and/or detectors already running.";

	protected volatile boolean runMonitoring = false;

	protected int numElements;

	protected IPlottingSystem<Composite> myPlotter;

	protected boolean amVisible = true;

	private Action btnRunPause;

	private Action collectionTimeAction;

	private ImageDescriptor pauseImage;

	private ImageDescriptor runImage;

	protected DetectorMonitorDataProviderInterface dataProvider;

	protected double collectionTime;


	@Override
	public void init(IViewSite site) throws PartInitException {
		try {
			myPlotter = PlottingFactory.createPlottingSystem();
			JythonServerFacade.getInstance().addIObserver(this);
		} catch (Exception e) {
			throw new PartInitException("Exception creating PlottingSystem", e);
		}
		super.init(site);
	}

	@Override
	public void update(final Object source, final Object arg) {
		if (source instanceof JythonServerFacade && arg instanceof JythonServerStatus) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					int newstatus = ((JythonServerStatus) arg).scanStatus;
					if (newstatus == Jython.IDLE) {
						btnRunPause.setEnabled(true);
					} else {
						btnRunPause.setEnabled(false);
					}
				}
			});
		}
	}

	/**
	 * Collect the data from the fluorescence detector
	 *
	 * @return Double[]
	 * @throws DeviceException
	 */
	protected abstract Double[] getFluoDetectorCountRatesAndDeadTimes() throws DeviceException;

	/**
	 * Runs in the UI thread, so updates to UI objects should only be made here.
	 *
	 * @param values
	 *            - values from the ion chambers
	 * @param xspressStats
	 *            - values from the fluo detector
	 */
	protected abstract void updateDisplay(Double[] values, Double[] xspressStats);

	/**
	 * Collect the data from the ion chambers
	 *
	 * @return Double[]
	 * @throws DeviceException
	 */
	protected abstract Double[] getIonChamberValues() throws Exception;

	@Override
	public void createPartControl(Composite parent) {
		getSite().getPage().addPartListener(this);
		createToolbar();
		collectionTime = Double.valueOf(LocalProperties.get("gda.exafs.ui.views.scalersMonitor.collectionTime", "1.0"));
		dataProvider = Finder.getInstance().find("detectorMonitorDataProvider");

		// Display a warning message if the server is not setup with the 'data provider' object,
		// Put details in log file of how to make it work.
		if (dataProvider==null) {
			logger.warn("No data provider object was found on the server.\n"+
					"Server needs to have an instance of DetectorMonitorDataProvider called "+
					"'detectorMonitorDataProvider' present and configured with ion chambers, xspress2 detectors etc. "+
					"for the Detector Rates View to work (see b18 config for example).\n"+
					"This needs to be RMI exported from the server and imported to the client "+
					"using the DetectorMonitorDataProviderInterface.");

			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Problem runnning detector rates",
					"Detector rates cannot run - no data provider object was found on the server.\nContact gda support - see log file for more details.");

			// Disable the run button to prevent data collection from running and getting further errors
			btnRunPause.setEnabled(false);
		}
	}

	private void createToolbar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
		pauseImage = GDAClientActivator.getImageDescriptor("icons/control_stop_blue.png");
		runImage = GDAClientActivator.getImageDescriptor("icons/control_play_blue.png");
		btnRunPause = new Action(null, SWT.NONE) {
			@Override
			public void run() {
				if (btnRunPause.getImageDescriptor().equals(pauseImage)) {
					setRunMonitoring(false);
					btnRunPause.setImageDescriptor(runImage);
				} else {
					// Check to see if collection is allowed to take place
					if (!checkRunCollectionAllowed()) {
						return;
					}
					setRunMonitoring(true);
					// create new collection thread and start it
					Thread collectionThread = new Thread(createCollectionRunnable());
					collectionThread.start();
					btnRunPause.setImageDescriptor(pauseImage);
				}
			}
		};
		btnRunPause.setId(CommandQueueViewFactory.ID + ".runpause");
		btnRunPause.setImageDescriptor(runImage);
		manager.add(btnRunPause);

		// Add button to allow collection time to be changed
		collectionTimeAction = getChangeCollectionTimeAction();
		manager.add(collectionTimeAction);
	}

	private Action getChangeCollectionTimeAction() {
		final Action changeCollectionTimeAction = new Action(null, SWT.NONE) {
			@Override
				public void run() {
					InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Set collection time",
							"Set frame length to use when collecting data [seconds]", String.valueOf(collectionTime),
							new DoubleValidator());
					if (dlg.open() == Window.OK) {
						// User clicked OK; update the label with the input
						Double userInputDouble = Double.valueOf(dlg.getValue());
						if (userInputDouble == null || userInputDouble < 0) {
							logger.info("Problem converting user input {}", dlg.getValue());
						} else {
							collectionTime = userInputDouble;
						}
					}
				}
		};
		changeCollectionTimeAction.setId(CommandQueueViewFactory.ID + ".setcollectiontime");
		changeCollectionTimeAction.setToolTipText("Set the collection time");

		// Get clock image from icons directory in current plugin
		String bundleName = FrameworkUtil.getBundle(getClass()).getSymbolicName(); // name of plugin
		ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(bundleName, "icons/clock.png");
		changeCollectionTimeAction.setImageDescriptor(image);
		return changeCollectionTimeAction;
	}

	/**
	 * Validator used to check floating point number input (collection time dialog box)
	 */
	class DoubleValidator implements IInputValidator {
		/**
		 * Validates a string to make sure it's an integer > 0. Returns null for no error, or string with error message
		 *
		 * @param newText
		 * @return String
		 */
		@Override
		public String isValid(String newText) {
			Double value = null;
			try {
				value = Double.valueOf(newText);
			} catch (NumberFormatException nfe) {
				// swallow, value==null
			}
			if (value == null || value < 0) {
				return "Text should be a number > 0";
			}
			return null;
		}
	}

	private void setRunButtonImage(ImageDescriptor image) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				btnRunPause.setImageDescriptor(runImage);
			}
		});
	}

	/**
  	* Check to see it collection is allowed to take place by {@link #dataProvider}. If collection
  	* is currently not allowed, a dialog box will be displayed to allow user to override the setting
  	* and allow collection to proceed. <p>
  	* This is function is called every time the 'start' button is pressed.
 	* @return true if collection is allowed to start, false otherwise
 	*/
	private boolean checkRunCollectionAllowed() {
		if (!dataProvider.getCollectionAllowed()) {
			boolean allow = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					"Detector rate collection",
					"Server is currently set to prevent collection of data for detector rate view.\n"+
					"Do you want to change it to be allowed?");
			if (allow==true) {
				dataProvider.setCollectionAllowed(true);
			}
		}
		return dataProvider.getCollectionAllowed();
	}

	/**
	 * @return new Runnable that calls the {@link #runCollection()} function.
	 */
	protected Runnable createCollectionRunnable() {
		Runnable newThread = new Runnable() {
			@Override
			public void run() {
				logger.debug("Collection thread started");
				runCollection();
				logger.debug("Collection thread finished");
			}
		};
		return newThread;
	}

	/**
	 * Run data collection. Collection runs until either :
	 * <li> {@link #runMonitoring} is set to false
	 * <li> {@link #dataProvider} returns false for {@link DetectorMonitorDataProvider#getCollectionAllowed()}
	 * <li> An Exception is thrown during collection (e.g. due to scan/script also running or detector throwing an exception)
	 * <p>
	 * This function is executed in Runnable object (by {@link #createCollectionRunnable()}) which is run in a background
	 * thread when the 'start' button is pressed.
	 */
	public void runCollection() {

		final long guiRefreshIntervalMs = (long) collectionTime*1000;

		// set the collection time
		dataProvider.setCollectionTime(collectionTime);

		while(runMonitoring && dataProvider.getCollectionAllowed()) {
			final Double[] values;
			final Double[] xspressStats;
			long timeAtCollectionStart;
			try {
				timeAtCollectionStart = System.currentTimeMillis();
				logger.trace("reading from ionchambers");
				values = getIonChamberValues();
				logger.trace("reading from fluo detector");
				xspressStats = getFluoDetectorCountRatesAndDeadTimes();
			} catch(final Exception e1) {
				logger.debug("getFluoDetectorCountRatesAndDeadTimes exception" + e1.getMessage() + " stopping collection of detector rates.");
				runMonitoring = false;
				final String errorMessage = " view will have to stop collecting.\nError occurred while getting detector values: "+e1.getMessage();

				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
							MessageDialog.openError(
								PlatformUI.getWorkbench().getDisplay().getActiveShell(),
								"Detector Rates Error",	getPartName() + errorMessage);
					}
				});
				runMonitoring = false;
				continue;
			}

			// If data returned is null, collection is probably not allowed (e.g. switched off by script)
			if (!dataProvider.getCollectionAllowed() || values==null || xspressStats==null) {
				runMonitoring=false;
				continue;
			}

			// Update display (in gui thread)
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateDisplay(values, xspressStats);
				}
			});

			// Calls to getIonChamberValues() and getFluoDetectorCountRatesAndDeadTimes() block until completion
			// for real hardware, so sleep in GUI thread is only really needed in dummy mode.
			// Or if GUI refresh interval is larger than collection time.

			// Sleep for a bit until next collection should start.
			long timeInLoop = System.currentTimeMillis() - timeAtCollectionStart;
			long timeUntilNextCollection = guiRefreshIntervalMs - timeInLoop;
			if (timeUntilNextCollection > 0) {
				try {
					Thread.sleep(timeUntilNextCollection);
				} catch (InterruptedException e) {
					logger.error("Exception while waiting at end of collection - exiting loop", e);
					runMonitoring = false;
					continue;
				}
			}
		}
		// reset image back to 'start' arrow
		setRunButtonImage(runImage);
	}

	public void setRunMonitoring(boolean runMonitoring) {
		this.runMonitoring = runMonitoring;
	}

	public boolean isRunMonitoring() {
		return runMonitoring;
	}

	@Override
	public void dispose() {
		amVisible = false;
		myPlotter.dispose();
		super.dispose();
		logger.debug("dispose");
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// ignore
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = true;
			logger.debug("partBroughtToTop");
		}
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = false;
			logger.debug("partClosed");
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// ignore
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = false;
			logger.debug("partHidden");
		}
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// ignore
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = true;
			logger.debug("partOpened");
		}
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = true;
			logger.debug("partVisible");
		}
	}

}
