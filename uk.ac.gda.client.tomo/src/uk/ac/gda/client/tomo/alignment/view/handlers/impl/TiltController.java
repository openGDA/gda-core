/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.observable.IObservable;
import gda.scan.IScanDataPoint;
import gda.util.Sleep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.DoublePointList;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltController;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class TiltController implements ITiltController {

	private static final String MATLAB_MSG_IDENTIFIER = "matlab>";

	private static final String TILT_ALIGNMENT_TASK_NAME = "Tilt Alignment";

	private static final String PROJECTIONS_FOLDER_NAME = "projections";

	private static final String RESULT_FILE_LINE_2_CSV = "line_2.csv";

	private static final String RESULT_FILE_ELLIPSE_2_CSV = "ellipse_2.csv";

	private static final String RESULT_FILE_CENTERS_2_CSV = "centers_2.csv";

	private static final String RESULT_FILE_ELLIPSE_1_CSV = "ellipse_1.csv";

	private static final String RESULT_FILE_CENTERS_1_CSV = "centers_1.csv";

	private static final String SCAN_PROGRESS_MSG = "Scan Progress: %2.2f%%";

	private static final String END_OF_TILT_IDENTIFIER = "TiltReturn:";

	private static final String TILT_JYTHON_COMMAND = "tomoAlignment.doTiltAlignment(%1$d, %2$f)";

	private static final Logger logger = LoggerFactory.getLogger(TiltController.class);

	private IObservable tomoScriptController;

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}

	private boolean isComplete = false;

	@Override
	public TiltPlotPointsHolder doTilt(IProgressMonitor monitor, CAMERA_MODULE module, double exposureTime)
			throws Exception {
		final String[] folders = new String[2];
		final SubMonitor progress = SubMonitor.convert(monitor, 70);
		progress.beginTask(TILT_ALIGNMENT_TASK_NAME, 70);
		isComplete = false;
		IScanDataPointObserver tiltObserver = new IScanDataPointObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (arg instanceof PyException || arg instanceof Exception) {
					isComplete = true;
					// TODO : handle exceptions
				} else {
					if (source.equals(tomoScriptController)) {
						String msg = (String) arg;
						if (msg.startsWith(END_OF_TILT_IDENTIFIER)) {
							String folderNames = msg.substring(END_OF_TILT_IDENTIFIER.length());
							String[] folderNamesFromScript = folderNames.split(",");
							if (folderNamesFromScript.length == 2) {
								folders[0] = folderNamesFromScript[0].trim() + File.separator + PROJECTIONS_FOLDER_NAME;
								folders[1] = folderNamesFromScript[1].trim() + File.separator + PROJECTIONS_FOLDER_NAME;
							}
							isComplete = true;
						} else {
							if (!msg.startsWith(MATLAB_MSG_IDENTIFIER)) {
								progress.worked(1);
							}
							progress.subTask(msg);
						}
					} else if (arg instanceof IScanDataPoint) {
						IScanDataPoint scanDataPoint = (IScanDataPoint) arg;
						double currentPoint = scanDataPoint.getCurrentPointNumber();
						double numberOfPoints = scanDataPoint.getNumberOfPoints();
						double progressMsg = currentPoint / numberOfPoints * 100;
						progress.worked(1);
						progress.subTask(String.format(SCAN_PROGRESS_MSG, progressMsg));
					}
				}
			}
		};
		tomoScriptController.addIObserver(tiltObserver);
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(tiltObserver);
		InterfaceProvider.getCommandRunner().runCommand(
				String.format(TILT_JYTHON_COMMAND, module.getValue(), exposureTime));
		while (!isComplete) {
			Sleep.sleep(1000);
		}

		tomoScriptController.deleteIObserver(tiltObserver);
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(tiltObserver);
		progress.done();

		//
		// for testing
		folders[0] = "/dls/i12/data/2012/cm5706-2/default/7740/projections";
		folders[1] = "/dls/i12/data/2012/cm5706-2/default/7740/projections";
		//
		return getPlottablePoint(folders[0], folders[1]);
	}

	protected TiltPlotPointsHolder getPlottablePoint(String firstScanFolder, String secondScanFolder)
			throws IOException {
		TiltPlotPointsHolder tiltPlotPointsHolder = new TiltPlotPointsHolder();
		tiltPlotPointsHolder.setCenters1(getDoublePointList(firstScanFolder + File.separator
				+ RESULT_FILE_CENTERS_1_CSV));
		tiltPlotPointsHolder.setEllipse1(getDoublePointList(secondScanFolder + File.separator
				+ RESULT_FILE_ELLIPSE_1_CSV));
		tiltPlotPointsHolder.setCenters2(getDoublePointList(secondScanFolder + File.separator
				+ RESULT_FILE_CENTERS_2_CSV));
		tiltPlotPointsHolder.setEllipse2(getDoublePointList(secondScanFolder + File.separator
				+ RESULT_FILE_ELLIPSE_2_CSV));
		tiltPlotPointsHolder.setLine2(getDoublePointList(secondScanFolder + File.separator + RESULT_FILE_LINE_2_CSV));
		return tiltPlotPointsHolder;
	}

	private DoublePointList getDoublePointList(String fileName) throws IOException {
		DoublePointList pointList = new DoublePointList();

		File firstScanCentersFile = new File(fileName);
		if (firstScanCentersFile.exists()) {
			FileInputStream fis = new FileInputStream(firstScanCentersFile);
			InputStreamReader inpStreamReader = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(inpStreamReader);
			String rl = null;
			rl = br.readLine();
			while (rl != null) {
				StringTokenizer strTokenizer = new StringTokenizer(rl, ",");
				if (strTokenizer.countTokens() != 2) {
					throw new IllegalArgumentException("Invalid row in the table");
				}
				double x = Double.parseDouble(strTokenizer.nextToken());
				double y = Double.parseDouble(strTokenizer.nextToken());
				pointList.addPoint(x, y);
				rl = br.readLine();
			}
			fis.close();
			br.close();
			inpStreamReader.close();
		} else {
			throw new IOException("Unable to locate file:" + fileName);
		}
		return pointList;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
