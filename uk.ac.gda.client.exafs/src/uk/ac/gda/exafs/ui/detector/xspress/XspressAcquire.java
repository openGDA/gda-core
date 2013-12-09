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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.exafs.ui.detector.Acquire;
import uk.ac.gda.exafs.ui.detector.Data;

public class XspressAcquire extends Acquire {
	private static final Logger logger = LoggerFactory.getLogger(XspressAcquire.class);
	private String detectorFileLocation;
	
	public XspressAcquire() {
		// TODO Auto-generated constructor stub
	}
	
	public void writeToDisk(String xspressSaveDir, SashFormPlotComposite sashPlotFormComposite, Data plotData, double[][][] detectorData) throws Exception{
		String spoolDirPath = PathConstructor.createFromProperty(xspressSaveDir);
		if (spoolDirPath == null || spoolDirPath.length() == 0)
			throw new Exception("Error saving data. Xspress device spool dir is not defined in property " + xspressSaveDir);
		long snapShotNumber = new NumTracker("Xspress_snapshot").incrementNumber();
		String fileName = "xspress_snap_" + snapShotNumber + ".mca";
		File detectorFile = new File(spoolDirPath + "/" + fileName);
		detectorFileLocation = detectorFile.getAbsolutePath();
		plotData.save(detectorData, detectorFileLocation);
		sashPlotFormComposite.appendStatus("Xspress snapshot saved to " + detectorFile, logger);
	}
	
	public String getDetectorFileLocation(){
		return detectorFileLocation;
	}
	

}
