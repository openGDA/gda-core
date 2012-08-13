/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.rcp.GDAClientActivator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;


/**
 * Version of XmapMonitorView which does not display the results from I0,It,Iref but the next channel from the TFG
 * instead.
 */
public class XmapI1MonitorView extends ViewPart implements Runnable, IPartListener2 {

	public static final String ID = "uk.ac.gda.exafs.ui.views.xmapi1monitor"; //$NON-NLS-1$

	protected static final Logger logger = LoggerFactory.getLogger(XmapI1MonitorView.class);

	protected volatile boolean runMonitoring = false;
	protected volatile boolean keepOnTrucking = true;
	protected volatile double refreshRate = 1.0; // seconds

	protected boolean amVisible = true;
	protected XmapI1MonitorViewData displayData;
	private volatile Thread updateThread;

	private ImageDescriptor pauseImage;

	private ImageDescriptor runImage;

	private Action btnRunPause;

	public XmapI1MonitorView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		{
			Group grpCurrentCountRates = new Group(parent, SWT.BORDER);
			grpCurrentCountRates.setText("Current count rates");
			grpCurrentCountRates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			grpCurrentCountRates.setLayout(new GridLayout());

			displayData = new XmapI1MonitorViewData(grpCurrentCountRates);
		}

		// create a thread for this object and start it
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

	private void createThread() {
		keepOnTrucking = true;
		updateThread = uk.ac.gda.util.ThreadManager.getThread(this);
		updateThread.start();
	}

	@Override
	public void setFocus() {
		// Set the focus
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

				final Double i1;
				final Double[] xmapStats;
				try {
					i1 = updateValues();
					xmapStats = getXmapCountRatesAndDeadTimes();
				} catch (DeviceException e1) {
					logger.error(e1.getMessage(), e1);
					runMonitoring = false;
					continue;
				}

				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						displayData.setI1(i1);
						updateXmapGrid(xmapStats, i1);
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

	protected void updateXmapGrid(Double[] xmapStats, Double i1) {

		double rate = xmapStats[0]; // Hz
		double dt = (xmapStats[1] - 1) * 100; // %
		Double FF = xmapStats[0];
		displayData.setDeadTime(dt);
		displayData.setRate(rate);
		displayData.setTotalCounts(FF);
		displayData.setFFI1(xmapStats[2] / i1);
	}

	protected Double[] getXmapCountRatesAndDeadTimes() throws DeviceException {
		XmapDetector xmap = (XmapDetector) Finder.getInstance().find("xmapMca");
		return (Double[]) xmap.getAttribute("liveStats");
	}

	protected Double updateValues() throws DeviceException {

		String xmapName = LocalProperties.get("gda.exafs.xmapName", "xmapMca");
		XmapDetector xmap = (XmapDetector) Finder.getInstance().find(xmapName);
		String ionchambersName = LocalProperties.get("gda.exafs.i1Name", "I1");
		CounterTimer ionchambers = (CounterTimer) Finder.getInstance().find(ionchambersName);

		// only collect new data outside of scans else will readout the last data collected
		try {
			if (JythonServerFacade.getInstance().getScanStatus() == Jython.IDLE && !xmap.isBusy()
					&& !ionchambers.isBusy()) {
				xmap.collectData();
				ionchambers.setCollectionTime(1);
				ionchambers.collectData();
			}
			xmap.waitWhileBusy();
			ionchambers.waitWhileBusy();
		} catch (InterruptedException e) {
			throw new DeviceException(e);
		}

		// read the latest frame
		int currentFrame = ionchambers.getCurrentFrame();
		if (currentFrame % 2 != 0) {
			currentFrame--;
		}
		if (currentFrame > 0) {
			currentFrame /= 2;
			currentFrame--;
		}

		double[] ion_results = (double[]) ionchambers.readout();
		Double collectionTime = (Double) ionchambers.getAttribute("collectionTime");
		return ion_results[0] /= collectionTime;
	}

	@Override
	public void dispose() {
		keepOnTrucking = false;
		amVisible = false;
		super.dispose();
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// ignore
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = true;
			logger.info("partBroughtToTop");
		}
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = false;
			logger.info("partClosed");
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
			logger.info("partHidden");
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
			logger.info("partOpened");
		}
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (partRef.getPartName().equals(this.getPartName())) {
			amVisible = true;
			logger.info("partVisible");
		}
	}
}
