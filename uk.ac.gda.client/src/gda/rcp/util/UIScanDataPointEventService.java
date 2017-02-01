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

package gda.rcp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerStatus;
import gda.jython.batoncontrol.BatonChanged;
import gda.rcp.GDAClientActivator;
import gda.scan.IScanDataPoint;
import uk.ac.gda.ClientManager;
import uk.ac.gda.preferences.PreferenceConstants;

/**
 * This class is used to minimise the number of runnables being added to the stack to process scan data points. It has
 * reduced the listeners to a client side list which is processes as one Runnable. It is also required as the graph may
 * not have been initialised when a scan is run. This class is always listening however and if a listener adds
 * themselves later, they are updated with all points instead of just the latest. It currently keeps the ScanDataPoints
 * in memory. NOTE: This class is not designed for listeners not updating UI. It is only for user interface which
 * requires updates when the scan data point is notified. Currently if you have many scan data points coming through,
 * all will be notified on the UI thread in order as asyncExec. You may need to disconnect from the
 * UIScanDataPointEventService in this scenario. Also instead you may need to update the UI with a timer to stop too
 * many points coming through.
 */
public final class UIScanDataPointEventService {

	private static final Logger logger = LoggerFactory.getLogger(UIScanDataPointEventService.class);

	private static UIScanDataPointEventService staticInstance;

	public static UIScanDataPointEventService getInstance() {
		if (staticInstance == null)
			staticInstance = new UIScanDataPointEventService();
		return staticInstance;
	}

	private List<IScanDataPoint> currentDataPoints;
	private IScanDataPointObserver scanDataPointObserver;
	private Collection<ScanPlotListener> listeners;
	private boolean running = false;

	/**
	 * Called once to add a listener for scan data points. This also means that the scan data is built up even if the UI
	 * as not been initialised. If the class thinks that it is in testing mode (because normal GDA env variables are not
	 * present) it will need calling to connect once the InterfaceProvider has been set.
	 */
	private UIScanDataPointEventService() {

		currentDataPoints = new ArrayList<IScanDataPoint>(89);
		if (ClientManager.isClient())
			connect();
	}

	/**
	 * Can be called by tests that may change the IScanDataPointProvider
	 */
	public void reconnect() {
		dispose();
		connect();
	}

	/**
	 * Normally connects automatically, do not need to call connect.
	 */
	public void connect() {

		if (scanDataPointObserver != null)
			throw new RuntimeException("The ScanDataPointEventService is already connected.");

		this.scanDataPointObserver = new IScanDataPointObserver() {

			@Override
			public void update(Object theObserved, final Object info) {

				try {
					if (PlatformUI.getWorkbench().getDisplay().isDisposed())
						return;
				} catch (IllegalStateException e) {
					// ignore exception - this means that an event has arrived before the workbench has had a chance to
					// be created. So simply return;
					return;
				}

				if (info instanceof BatonChanged)
					return;

				if (!(info instanceof JythonServerStatus) && !(info instanceof IScanDataPoint)) {
					return;
				}

				try {
					if (info instanceof JythonServerStatus) {
						final JythonServerStatus status = (JythonServerStatus) info;
						if (status.scanStatus == Jython.IDLE) {
							fireScanStopped();
						} else if (status.scanStatus == Jython.PAUSED) {
							fireScanPaused();
						} else {
							fireScanStarted();
						}
					} else if (info instanceof IScanDataPoint) {
						final IScanDataPoint currentPoint = (IScanDataPoint) info;
						cachePoint(currentPoint);
						fireScanDataPoint(new ScanDataPointEvent(currentDataPoints, currentPoint));
					}
				} catch (Throwable th) {
					logger.error("Error in ScanDataPointEventService", th);
				}
			}
		};

		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(scanDataPointObserver);
	}

	private void cachePoint(IScanDataPoint currentPoint) {

		if (currentDataPoints != null && currentDataPoints.size() > 0
				&& !currentPoint.getUniqueName().equals(currentDataPoints.get(0).getUniqueName())) {

			currentDataPoints.clear();
		}

		final int maxSize = GDAClientActivator.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS);
		currentDataPoints.add(currentPoint);

		if (currentDataPoints.size() > maxSize) {
			currentDataPoints.remove(0);
		}
	}

	/**
	 * Called to remove listener from server and clear out local listeners.
	 */
	public void dispose() {
		if (ClientManager.isClient()) {
			InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(scanDataPointObserver);
		}
		scanDataPointObserver = null;
		if (listeners != null)
			listeners.clear();
		if (currentDataPoints != null)
			currentDataPoints.clear();
	}

	public void addScanPlotListener(ScanPlotListener l) {
		if (listeners == null) {
			listeners = new ArrayList<ScanPlotListener>(3);
		}
		listeners.add(l);
	}

	protected void fireScanPaused() {

		if (listeners == null) {
			logger.debug("ScanDataPointEventService has empty list of listeners");
			return;
		}

		for (final ScanPlotListener l : listeners) {
			try {
				l.scanPaused();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	protected void fireScanStopped() {
		running = false;
		if (listeners == null) {
			logger.debug("ScanDataPointEventService has empty list of listeners");
			return;
		}
		for (final ScanPlotListener l : listeners) {
			try {
				l.scanStopped();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected void fireScanStarted() {
		running = true;

		if (listeners == null) {
			logger.debug("ScanDataPointEventService has empty list of listeners");
			return;
		}

		for (final ScanPlotListener l : listeners) {
			try {
				l.scanStarted();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected void fireScanDataPoint(final ScanDataPointEvent e) {
		if (listeners == null) {
			return;
		}

		for (final ScanPlotListener l : listeners) {
			try {
				l.scanDataPointChanged(e);
			} catch (Exception ne) {
				logger.error(ne.getMessage(), ne);
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public List<IScanDataPoint> getCurrentDataPoints() {
		return Collections.unmodifiableList(currentDataPoints);
	}

	public void removeScanPlotListener(ScanPlotListener l) {
		if (listeners != null) {
			while (listeners.remove(l)) {
			}
		}
	}
}