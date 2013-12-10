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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.xspress.XspressDetector;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.exafs.ui.detector.Acquire;
import uk.ac.gda.exafs.ui.detector.Data;

public class XspressAcquire extends Acquire {
	private static final Logger logger = LoggerFactory.getLogger(XspressAcquire.class);
	private String detectorFileLocation;
	private String originalResolutionGrade;
	private String originalReadoutMode;
	private int[][][] mcaData;
	private String acquireFileLabelText;
	
	public void writeToDisk(String xspressSaveDir, SashFormPlotComposite sashPlotFormComposite, Data plotData, int[][][] detectorData) throws Exception{
		String xspressSaveFullDir = PathConstructor.createFromProperty(xspressSaveDir);
		if (xspressSaveFullDir == null || xspressSaveFullDir.length() == 0)
			throw new Exception("Error saving data. Xspress device spool dir is not defined in property " + xspressSaveDir);
		long snapShotNumber = new NumTracker("Xspress_snapshot").incrementNumber();
		String fileName = "xspress_snap_" + snapShotNumber + ".mca";
		File detectorFile = new File(xspressSaveFullDir + "/" + fileName);
		detectorFileLocation = detectorFile.getAbsolutePath();
		plotData.save(detectorData, detectorFileLocation);
		sashPlotFormComposite.appendStatus("Xspress snapshot saved to " + detectorFile, logger);
	}
	
	public String getDetectorFileLocation(){
		return detectorFileLocation;
	}
	
	protected void acquire(XspressDetector xspressDetector, IProgressMonitor monitor, double collectionTimeValue, SashFormPlotComposite sashPlotFormComposite, String uiReadoutMode, String uiResolutionGrade) {
		if (monitor != null)
			monitor.beginTask("Acquire xspress data", 100);
		try {
			originalResolutionGrade = xspressDetector.getResGrade();
			originalReadoutMode = xspressDetector.getReadoutMode();
		} catch (DeviceException e) {
			logger.error("Cannot get current resolution grade", e);
			return;
		}
		sashPlotFormComposite.appendStatus("Collecting a single frame of MCA data with resolution grade set to '" + uiResolutionGrade + "'.", logger);
		try {
			xspressDetector.setAttribute("readoutModeForCalibration", new String[] { uiReadoutMode, uiResolutionGrade });
			mcaData = xspressDetector.getMCData((int) collectionTimeValue);
		} catch (DeviceException e) {
			sashPlotFormComposite.appendStatus("Cannot read out xspress detector data", logger);
			logger.error("Cannot read out xspress detector data", e);
		}
		
		if (monitor != null)
			monitor.done();
		sashPlotFormComposite.appendStatus("Collected data from detector successfully.", logger);
		try {
			xspressDetector.setResGrade(originalResolutionGrade);
			xspressDetector.setReadoutMode(originalReadoutMode);
		} catch (DeviceException e) {
			sashPlotFormComposite.appendStatus("Cannot reset res grade, detector may be in an error state.", logger);
			logger.error("Cannot reset res grade, detector may be in an error state", e);
		}
		sashPlotFormComposite.appendStatus("Reset detector to resolution grade '" + originalResolutionGrade + "'.", logger);
	}
	
	public int[][][] getMcaData(){
		return mcaData;
	}
	
	public String getOriginalResolutionGrade(){
		return originalResolutionGrade;
	}
	
	public String getOriginalReadoutMode(){
		return originalReadoutMode;
	}
	
	public void saveMca(SashFormPlotComposite sashPlotFormComposite, String xspressSaveDir, Data plotData){
		try {
			writeToDisk(xspressSaveDir, sashPlotFormComposite, plotData, mcaData);
			acquireFileLabelText = "Saved: " + detectorFileLocation;
		} catch (Exception e) {
			sashPlotFormComposite.appendStatus("Cannot write xspress detector data to disk", logger);
			logger.error("Cannot write xspress detector data to disk.", e);
			return;
		}
	}

	public String getAcquireFileLabelText() {
		return acquireFileLabelText;
	}

}