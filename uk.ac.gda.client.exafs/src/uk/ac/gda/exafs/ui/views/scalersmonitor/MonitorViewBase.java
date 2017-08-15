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
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.Dataset;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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

	/** Class to set the I0, It, Iref... values in textboxes at top of view */
	protected ScalersMonitorConfig displayData;

	protected IAxis dtAxis;

	protected IAxis primaryAxis;

	protected Double maxFluoRate;

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

	@Override
	public void createPartControl(Composite parent) {

		setupGui(parent);

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

	/**
	 * Setup gui with textboxes for the rates and plot to display channel values.
	 * Common code refactored from {@link XspressMonitorView#createPartControl(Composite)} and
	 * {@link XmapMonitorView#createPartControl(Composite)}
	 * @since 6/6/2017
	 * @param parent
	 */
	protected void setupGui(Composite parent) {
		Group grpCurrentCountRates = new Group(parent, SWT.BORDER);
		grpCurrentCountRates.setText("Current count rates");
		grpCurrentCountRates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpCurrentCountRates.setLayout(new GridLayout());

		setupDisplayData(grpCurrentCountRates);

		myPlotter.createPlotPart(grpCurrentCountRates, "Rates", null, PlotType.XY, null);
		myPlotter.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		primaryAxis = myPlotter.getSelectedYAxis();
		primaryAxis.setTitle("Counts (Hz)");
		dtAxis = myPlotter.createAxis("Deadtime (%)", true, SWT.RIGHT);
		maxFluoRate = Double.valueOf(LocalProperties.get("gda.exafs.ui.views.scalersMonitor.maxFluoRate", "500000"));
	}

	/**
	 * Setup gui that displays the rate values (in textboxes)
	 * May be overridden by child classes to customise the name and number columns etc.
	 * @param parent
	 */
	protected void setupDisplayData(Composite parent) {
		displayData = new ScalersMonitorConfig(parent);
		displayData.createControls();
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
  	* If another detector rates view is running, user is notified and collection will not begin.
  	* This is function is called every time the 'start' button is pressed.
 	* @return true if collection is allowed to start, false otherwise
 	*/
	private boolean checkRunCollectionAllowed() {
		if (dataProvider.getCollectionIsRunning()){
			MessageDialog.openInformation(Display.getDefault().getActiveShell(),
					"Detector rate collection",
					"Another detector rates view is already running. You will need to stop that one before this one can be started.");
			return false;
		}

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
				btnRunPause.setImageDescriptor(pauseImage);
				runCollection();
				setRunButtonImage(runImage);
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

		if (dataProvider.getCollectionIsRunning()) {
			logger.debug("Collection already running in another thread");
			return;
		}

		dataProvider.setCollectionIsRunning(true);

		while(runMonitoring && dataProvider.getCollectionAllowed()) {
			// set the collection time
			dataProvider.setCollectionTime(collectionTime);

			final Double[] ionChamberValues;
			final Double[] fluoStats;
			long timeAtCollectionStart;
			try {
				timeAtCollectionStart = System.currentTimeMillis();
				logger.trace("reading from ionchambers");
				ionChamberValues = getIonChamberValues();
				logger.trace("reading from fluo detector");
				fluoStats = getFluoDetectorCountRatesAndDeadTimes();
			} catch(final Exception e1) {
				logger.debug("getFluoDetectorCountRatesAndDeadTimes exception" + e1.getMessage() + " stopping collection of detector rates.");
				runMonitoring = false;
				final String errorMessage = " view will have to stop collecting.\nError occurred while getting detector values: "+e1.getMessage();
				logger.error("Problem collecting detctor rates", e1);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
							MessageDialog.openError(
								PlatformUI.getWorkbench().getDisplay().getActiveShell(),
								"Detector Rates Error",	getPartName() + errorMessage);
					}
				});
				continue;
			}

			// If data returned is null, collection is probably not allowed (e.g. switched off by script)
			if (!dataProvider.getCollectionAllowed() || ionChamberValues==null || fluoStats==null) {
				runMonitoring=false;
				continue;
			}

			// Update display (in gui thread)
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateDisplayedData(fluoStats, ionChamberValues);
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
		dataProvider.setCollectionIsRunning(false);
	}

	/**
	 * Collect the data from the fluorescence detector
	 *
	 * @return Double[]
	 * @throws DeviceException
	 */
	protected abstract Double[] getFluoDetectorCountRatesAndDeadTimes() throws DeviceException;

	/**
	 * Collect the data from the ion chambers
	 *
	 * @return Double[]
	 * @throws DeviceException
	 */
	protected abstract Double[] getIonChamberValues() throws Exception;

	/**
	 * Runs in the UI thread, so updates to UI objects should only be made here.
	 *
	 * @param statsValues
	 *            - stats values from the fluo detector
	 * @param ionchamberValues
	 *            - values from the ion chambers
	 */
	protected void updateDisplayedData(Double[] statsValues, Double[] ionchamberValues) {
		displayData.setI0(ionchamberValues[0]);
		displayData.setIt(ionchamberValues[1]);
		displayData.setIref(ionchamberValues[2]);
		double ItI0 = Math.log(ionchamberValues[0] / ionchamberValues[1]);
		displayData.setItI0(ItI0);
		double IrefIt = Math.log(ionchamberValues[2] / ionchamberValues[1]);
		displayData.setIrefIt(IrefIt);

		updateDisplayDataFFValues(statsValues, ionchamberValues);

		double[] rates = getRatesFromStats(statsValues);
		double[] dts = getDeadtimePercentFromStats(statsValues);
		updatePlot(rates, dts);
	}

	/**
	 * Extract rate information from stats array produced by fluo detector
	 * (Refactored from {@link XspressMonitorView}, {@link XmapMonitorView}.
	 * @since 6/6/2017
	 * @param stats
	 * @return rate
	 */
	protected double[] getRatesFromStats(Double[] stats) {
		double[] rates = new double[numElements];
		for (int element = 0; element < numElements; element++) {
			rates[element] = stats[element * 3]; // Hz
		}
		return rates;
	}

	/**
	 * Extract deadtime information from stats array produced by fluo detector
	 * (Refactored from {@link XspressMonitorView}, {@link XmapMonitorView}.
	 * @since 6/6/2017
	 * @param stats
	 * @return rate
	 */
	protected double[] getDeadtimePercentFromStats(Double[] stats) {
		double[] dts = new double[numElements];
		for (int element = 0; element < numElements; element++) {
			dts[element] = (stats[element * 3 + 1] - 1) * 100; // %
		}
		return dts;
	}

	/**
	 * Update the FF and FFI0 values in {@link #displayData}.
	 * @param statsValues
	 * @param deadtimeValues
	 */
	protected abstract void updateDisplayDataFFValues(Double[] statsValues, Double[] deadtimeValues);

	/**
	 * Update plot part of gui to show supplied rate and deadtime values.
	 * Common code refactored from
	 * {@link XspressMonitorView#createPartControl(Composite)} and
	 * {@link XmapMonitorView#createPartControl(Composite)}
	 * @since 6/6/2017
	 * @param rates
	 * @param dts
	 */
	protected void updatePlot(double[] rates, double[] dts) {
		if (myPlotter==null) {
			return;
		}

		Dataset dsRates = DatasetFactory.createFromObject(rates);
		dsRates.setName("Rates (Hz)");

		Dataset dsDeadTime = DatasetFactory.createFromObject(dts);
		dsDeadTime.setName("Deadtime (%)");

		Dataset x = DatasetFactory.createLinearSpace(DoubleDataset.class, 0, numElements-1, numElements);
		x.setName("Element");

		myPlotter.clear();

		// rates plot
		myPlotter.setSelectedYAxis(primaryAxis);
		ILineTrace ratesTrace = myPlotter.createLineTrace("Rates (Hz)");
		ratesTrace.setTraceType(TraceType.HISTO);
		ratesTrace.setLineWidth(5);
		ratesTrace.setTraceColor(new Color(null, 0, 0, 128));
		ratesTrace.setData(x, dsRates);
		myPlotter.addTrace(ratesTrace);
		myPlotter.getSelectedXAxis().setRange(0, numElements);
		myPlotter.getSelectedXAxis().setTitle("Element");
		int minRangeValue = numElements==1 ? -1 : 0;
		myPlotter.getSelectedYAxis().setRange(minRangeValue, maxFluoRate);

		// deadtime plot
		myPlotter.setSelectedYAxis(dtAxis);
		ILineTrace deadTimeTrace = myPlotter.createLineTrace("Red (%)");
		deadTimeTrace.setLineWidth(1);
		deadTimeTrace.setTraceColor(new Color(null, 255, 0, 0));
		deadTimeTrace.setData(x, dsDeadTime);
		myPlotter.addTrace(deadTimeTrace);
		myPlotter.getSelectedYAxis().setShowMajorGrid(false);
		myPlotter.getSelectedYAxis().setRange(0, 100);

		myPlotter.setSelectedYAxis(primaryAxis);
		myPlotter.setShowLegend(false);
		myPlotter.repaint(false);
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
		setRunMonitoring(false); //to make sure collection thread stops
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
		logger.debug("partClosed");
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = false;
			setRunMonitoring(false); //to make sure collection thread stops
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
			setRunMonitoring(false); //to make sure collection thread stops
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
