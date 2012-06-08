/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

package gda.gui.scanplot;

import gda.TestHelpers;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.scan.IScanDataPoint;
import gda.scan.IScanStepId;
import gda.scan.LocalStepId;
import gda.scan.ScanDataPoint;
import gda.scan.ScanDataPointClient;
import gda.scan.ScanDataPointServer;
import gda.scan.ScanDataPointVar;
import gda.util.simpleServlet.FindableSimpleServlet;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 *
 * first sets of lines is a nested scan the first scannable has 3 positions, the second 2 positions
 * and the 3rd 100, so there are 6 combinations of first and second scannables. 
 * There are 2 extra scannables being monitored so the total number of lines per combination is 4
 * making 4*6 lines = 24.
 * So x is the 3rd scannable 
 * second set of lines is simple a scan of x with Y being read at each point so the additional
 * number of lines is 1
 * 
 * following sets of lines are repeats of the first and second.
 * 
 * An example of using this class is XYPlotViewTest:
 * 
 * 		ScanDataPointHandlerTester addDataQueue = new ScanDataPointHandlerTester(this);
		for(int i=0; i< 3; i++){
			File f =  new File(scratchFolder + File.separator + Integer.toString(i)+".nxs");
			gda.util.FileUtil.copy(testdataFile, f);
			addDataQueue.startAddingData(f.getAbsolutePath());
			while( addDataQueue.isAddingData()){
				delay(1000);
			}
			switch(i){
			case 0:
				'test' results
 */
public class ScanDataPointHandlerTester implements Runnable {
	
	private final ScanDataPointHandler scanPlot;
	private final int size;

	private boolean killed = false;
	private Thread thread = null;
	private Boolean addData = false;
	private Object obj = new Object();
	private String currentFileName;
	Integer numberOfRestarts = 1;


	/**
	 * @param scanPlot
	 */
	public ScanDataPointHandlerTester(ScanDataPointHandler scanPlot) {
		this.scanPlot = scanPlot;
		this.size     = 100;
		Factory testFactory = TestHelpers.createTestFactory("ScanDataPointHandlerTester");
		FindableSimpleServlet ss = new FindableSimpleServlet();
		ss.setName(ScanDataPointClient.SCAN_DATA_STORE);
		testFactory.addFindable(ss);
		Finder.getInstance().addFactory(testFactory);
	}

	/**
	 * @param currentFileName
	 */
	public void startAddingData(String currentFileName) {
		this.currentFileName = currentFileName;
		synchronized (obj) {
			addData = true;
			if (thread == null) {
				thread = uk.ac.gda.util.ThreadManager.getThread(this);
				thread.start();
			}
			obj.notifyAll();
		}
	}

	/**
	 * 
	 */
	public void stopAddingData() {
		synchronized (obj) {
			addData = false;
			obj.notifyAll();
		}
	}

	/**
	 * 
	 */
	public void dispose() {
		killed = true;
	}

	@SuppressWarnings("null")
	@Override
	public void run() {
		boolean childScan = false;
		Integer pointNumber = 0;
		int[] xLoops = null;
		Integer numYs = null;

		Integer xCounters[] = null;
		DummyScannable xScannables[]=null;
		DummyScannable yScannables[]=null;
		Integer totalPointsWithChild = 1;
		Vector<Scannable> scannables = null;
		while (!killed) {
			try {
				boolean addDataNow = false;
				synchronized (obj) {
					addDataNow = addData;
					if (!addDataNow) {
						obj.wait();
						numberOfRestarts++;
						pointNumber = 0;
					}
				}
				if (addDataNow) {
					if (pointNumber == 0) {
						childScan = numberOfRestarts % 2 == 1;
						xLoops = childScan ? new int[] { 3, 2, size } : new int[] { size };
						numYs = childScan ? 2 : 1;

						xCounters = new Integer[xLoops.length];
						xScannables = new DummyScannable[xLoops.length];
						yScannables = new DummyScannable[numYs];
						totalPointsWithChild = 1;
						for (int i = 0; i < xLoops.length; i++) {
							totalPointsWithChild *= xLoops[i];
						}
						Arrays.fill(xCounters, 0);
						scannables = new Vector<Scannable>();

						String name = "";
						Double xVal = (double) pointNumber;
						for (int i = 0; i < xLoops.length; i++) {
							name += "X";
							xScannables[i] = new DummyScannable(name + Integer.toString(i), xCounters[i]);
							scannables.add(xScannables[i]);
						}
						name = "";
						for (int i = 0; i < numYs; i++) {
							name += "Y";
							Double yVal = (((double) i + 1) / 5.0)
							* Math.sin(Math.PI * ((xVal + numberOfRestarts * 20) / 180));
							yScannables[i] = new DummyScannable(name + Integer.toString(i), yVal);
							scannables.add(yScannables[i] );
						}

					}

					ScanDataPoint scanDataPoint = new ScanDataPoint();
					scanDataPoint.setUniqueName("scanName" + numberOfRestarts.toString());
					if (scannables != null) {
						// do the getPosition/readout here as work should not be done inside the SDP.
						// This should be the only place these methods are called in the scan.
						for (Scannable scannable : scannables) {
							scanDataPoint.addScannable(scannable);
							scanDataPoint.addScannablePosition(scannable.getPosition(),scannable.getOutputFormat());
						}
					}
					
					scanDataPoint.setHasChild(childScan);
					scanDataPoint.setCurrentPointNumber(pointNumber);
					scanDataPoint.setNumberOfPoints(childScan ? totalPointsWithChild : xLoops[0]);
					scanDataPoint.setInstrument("instrument");
					scanDataPoint.setCommand("blah blah");
					scanDataPoint.setScanIdentifier(numberOfRestarts.toString());
					scanDataPoint.setCurrentFilename(currentFileName);
					scanDataPoint.setNumberOfChildScans(childScan ? xLoops.length-1 : 0);
					if( childScan ){
						List<IScanStepId> scanStepIds = new Vector<IScanStepId>(); 
						for( int i=0; i< xCounters.length;i++){
							scanStepIds.add(  new LocalStepId(Integer.toString(i) + "=" + xCounters[i].toString() + ","));
						}
						scanDataPoint.setStepIds(scanStepIds);
					}
					scanDataPoint.setScanDimensions(xLoops);
					{
						ScanDataPointVar var = ScanDataPointServer.getToken(scanDataPoint);
						IScanDataPoint pt = ScanDataPointClient.convertToken(var);
						scanPlot.handlePoint(pt);
					}
					Thread.sleep(50);
					pointNumber++;
					boolean atEnd = false;
					if (scanDataPoint.getHasChild()) {
						for (int i = xLoops.length - 1; i >= 0; i--) {
							xCounters[i]++;
							if (xCounters[i] == xLoops[i]) {
								// reset to 0 add 1 to previous
								for (int j = i; j < xLoops.length; j++) {
									xCounters[j] = 0;
								}
								if (i == 0) {
									atEnd = true;
									break;
								}
								// go to next
							} else {
								break;
							}
						}
					} else {
						xCounters[0]++;
						if (xCounters[0] == xLoops[0])
							atEnd = true;
						for (int i = 1; i < xLoops.length; i++) {
							xCounters[i]++;
						}
					}
					for(int i=0; i< xLoops.length; i++){
						if (xScannables[i]!=null) xScannables[i].asynchronousMoveTo(xCounters[i]);
					}
					Double xVal = (double) pointNumber;
					for (int i = 0; i < numYs; i++) {
						Double yVal = (((double) i + 1) / 5.0)
								* Math.sin(Math.PI * ((xVal + numberOfRestarts * 20) / 180));
						if (yScannables[i]!=null)  yScannables[i].asynchronousMoveTo(yVal);
					}					
					
					if (atEnd) {
						pointNumber = 0;
//						numberOfRestarts++;
						addData = false;
					}
				}
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}
	
	public boolean isAddingData(){
		return addData;
	}

}

