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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
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
public abstract class MonitorViewBase extends ViewPart implements Runnable, IPartListener2, IObserver {

	protected static final Logger logger = LoggerFactory.getLogger(MonitorViewBase.class);
	protected final String ALREADY_RUNNING_MSG = "Scan/script and/or detectors already running.";

	protected volatile boolean runMonitoring = false;

	protected volatile double refreshRate = 1.0; // seconds

	protected int numElements;

	protected IPlottingSystem<Composite> myPlotter;

	protected boolean amVisible = true;

	protected volatile Thread updateThread;

	protected volatile boolean keepOnTrucking = true;

	Action btnRunPause;

	private ImageDescriptor pauseImage;

	ImageDescriptor runImage;

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
		createThread();
		createToolbar();
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
					setRunMonitoring(true);
					btnRunPause.setImageDescriptor(pauseImage);
				}
			}
		};
		btnRunPause.setId(CommandQueueViewFactory.ID + ".runpause");
		btnRunPause.setImageDescriptor(runImage);
		manager.add(btnRunPause);
	}

	/*
	 * Check to see if a scan or script is running.
	 * @since 20/5/2016
	 */
	public boolean getScriptOrScanIsRunning() {
		return JythonServerFacade.getInstance().getScanStatus() != Jython.IDLE ||
			   JythonServerFacade.getInstance().getScriptStatus() != Jython.IDLE;
	}

	@Override
	public void run() {
		while (keepOnTrucking) {
			if (!runMonitoring || !amVisible) {
				try {
					if (!keepOnTrucking) {
						return;
					}
					Thread.sleep(1000);
					if (!keepOnTrucking) {
						return;
					}
				} catch (InterruptedException e) {
					// end the thread
					return;
				}
			} else {
				final Double[] values;
				final Double[] xspressStats;
				try {
					logger.trace("reading from ionchambers");
					values = getIonChamberValues();
					logger.trace("reading from fluo detector");
					xspressStats = getFluoDetectorCountRatesAndDeadTimes();
				} catch (final Exception e1) {
					logger.debug("getFluoDetectorCountRatesAndDeadTimes exception" + e1.getMessage() + " stopping collection of detector rates.");
					setRunMonitoring(false);
					final String errorMessage = " view will have to stop collecting.\nError occurred while getting detector values: "
												+ e1.getMessage();
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							btnRunPause.setImageDescriptor(runImage);
							if (e1.getMessage().compareTo(ALREADY_RUNNING_MSG) != 0) {
								MessageDialog.openError(
										PlatformUI.getWorkbench().getDisplay().getActiveShell(),
										"Detector Rates Error",
										getPartName() + errorMessage);
							}
						}
					});
					btnRunPause.setImageDescriptor(runImage);
					runMonitoring = false;
					continue; // stop the loop until the error can be fixed
				}

				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateDisplay(values, xspressStats);
					}
				});

				try {
					if (!keepOnTrucking) {
						return;
					}
					Thread.sleep((long) (refreshRate * 1000));
					if (!keepOnTrucking) {
						return;
					}
				} catch (InterruptedException e) {
					// end the thread
					return;
				}
			}
		}
	}

	protected void createThread() {
		keepOnTrucking = true;
		updateThread = uk.ac.gda.util.ThreadManager.getThread(this);
		updateThread.start();
	}

	/**
	 * Extending classes should use this when creating their datasets to create an empty data set to force the y-axis to
	 * be a fixed scale. This gets around the limitation of the current graphing and its use should be reviewed when the
	 * new graphing is available in Spring 2012.
	 *
	 * @param maxValue
	 * @return DoubleDataset
	 */
	protected DoubleDataset createFullRangeDataset(double maxValue) {
		return DatasetFactory.createFromObject(DoubleDataset.class, new double[] { 0, maxValue });
	}

	public void setRunMonitoring(boolean runMonitoring) {
		this.runMonitoring = runMonitoring;
	}

	public boolean isRunMonitoring() {
		return runMonitoring;
	}

	public double getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(double refreshRate) {
		if (refreshRate > 0 && refreshRate < 5.0) {
			this.refreshRate = refreshRate;
		}
	}

	@Override
	public void dispose() {
		keepOnTrucking = false;
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

	protected int findOrderOfMagnitude(double value) {
		if (value < 0) {
			value *= -1;
		}

		if (value < 10 || Double.isInfinite(value)) {
			return 1;
		}

		int oom = 0;

		do {
			value /= 10;
			oom++;
		} while (value > 10);

		return oom;
	}

}
