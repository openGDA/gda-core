/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.hrpd.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//import org.eclipse.ui.PlatformUI;

/**
 * This class is used to minimise the number of runnables being added to the stack
 * to process scan data points. It has reduced the listeners to a client side list
 * which is processes as one Runnable.
 * 
 * It is also required as the graph may not have been initialised when a scan is run.
 * This class is always listening however and if a listener adds themselves later, 
 * they are updated with all points instead of just the latest.
 * 
 * It currently keeps the ScanDataPoints in memory. 
 */
public class ScanDataFileEventService {

	private static ScanDataFileEventService staticInstance;
	
	/**
	 * @return ScanDataPointUtils
	 * @throws Exception 
	 */
	public static ScanDataFileEventService getInstance() throws Exception {
		if (staticInstance==null) staticInstance = new ScanDataFileEventService();
		return staticInstance;
	}
	
	private List<ScanDataFile> currentDataFiles;
	
	/**
	 * Called once to add a listener for scan data points. This also means that the 
	 * scan data is built up even if the UI as not been initialised.
	 */
	private ScanDataFileEventService() throws Exception {
		
		currentDataFiles = new ArrayList<ScanDataFile>(89); 
		
		//JythonServerFacade.getCurrentInstance().addIScanDataFileObserver(new IScanDataFileObserver() {
//			@Override
//			public String getName() {
//				return JythonTerminal.NAME;
//			}
//			@Override
//			public void update(Object theObserved, final Object info) {
//				if (info instanceof BatonChanged) return;
				//PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						if (!(info instanceof JythonServerStatus)  &&
//							    !(info instanceof ScanDataFile)) return;
//							
//						if (info instanceof JythonServerStatus) {
//							final JythonServerStatus status = (JythonServerStatus)info;
//							if (status.scanStatus == Jython.IDLE) {
//								fireScanStopped();
//							} else if (status.scanStatus == Jython.PAUSED) {
//								fireScanPaused();
//							} else {
//								fireScanStarted();
//							}
//							
//						} else if (info instanceof ScanDataFile) {
//							currentDataFiles.add((ScanDataFile)info);
//							fireScanDataFile(new ScanDataFileEvent(currentDataFiles, (ScanDataFile)info));
//						}
//					}
//
//				});
//			}
//		});
	}

	protected Collection<ScanPlotListener> listeners;
	protected boolean running = false;
	
	/**
	 * @param l
	 */
	public void addScanPlotListener(ScanPlotListener l) {
		if (listeners==null) listeners = new ArrayList<ScanPlotListener>(3);
		listeners.add(l);
	}
	
	protected void fireScanPaused() {
		for (ScanPlotListener l : listeners) l.scanPaused();
	}

	protected void fireScanStopped() {
		running = false;
		for (ScanPlotListener l : listeners) l.scanStopped();
	}

	protected void fireScanStarted() {
		if (currentDataFiles!=null) currentDataFiles.clear();
		running = true;
		for (ScanPlotListener l : listeners) l.scanStarted();
	}
	
	protected void fireScanDataFile(ScanDataFileEvent e) {
		for (ScanPlotListener l : listeners) l.scanDataFileChanged(e);
	}

	/**
	 * @return Returns the running.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 
	 * @return Returns the currentDataPoints.
	 */
	public List<ScanDataFile> getCurrentDataFiles() {
		return Collections.unmodifiableList(currentDataFiles);
	}

}
