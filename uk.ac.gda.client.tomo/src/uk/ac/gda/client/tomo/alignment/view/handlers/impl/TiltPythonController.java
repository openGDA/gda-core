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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;

import java.awt.geom.Point2D.Double;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.DoublePointList;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltController;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.tomography.tilt.TiltParameters;

public class TiltPythonController implements ITiltController {

	private static final String TOMO_ALIGNMENT_TILT_COMMAND = "tomoAlignment.tiltAlignment(%d, %f)";
	private static final Logger logger = LoggerFactory.getLogger(TiltPythonController.class);

	private IObservable tomoScriptController;

	@Override
	public void dispose() {
		// do nothing
	}

	private List<Double> preTiltPoints;
	private List<Double> postTiltPoints;
	private List<Double> preTiltEllipsePoints;
	private List<Double> postTiltEllipsePoints;

	private String errorMessage;

	private IObserver tiltAlignmentObserver = new IObserver() {

		@Override
		public void update(Object source, Object arg) {
			logger.debug("Script message:{}", arg);
			if (arg instanceof TiltParameters) {

				TiltParameters tiltParameters = (TiltParameters) arg;
				if (tiltParameters.getErrorMessage() != null) {
					errorMessage = tiltParameters.getErrorMessage();
				} else {

					preTiltPoints = tiltParameters.getPreTiltPoints();

					postTiltPoints = tiltParameters.getPostTiltPoints();
					preTiltEllipsePoints = tiltParameters.getPreTiltEllipsePoints();
					postTiltEllipsePoints = tiltParameters.getPostTiltEllipsePoints();

				}
				setComplete(true);
			} else {
				progress.subTask(String.valueOf(arg));
				progress.worked(2);
			}
		}
	};
	private boolean isComplete;
	private SubMonitor progress;

	private synchronized void setComplete(boolean flag) {
		this.isComplete = flag;
	}

	private synchronized boolean isComplete() {
		return isComplete;
	}

	@Override
	public TiltPlotPointsHolder doTilt(IProgressMonitor monitor, CAMERA_MODULE selectedCameraModule, double exposureTime)
			throws Exception {
		progress = SubMonitor.convert(monitor);
		progress.beginTask("Tilt", 30);
		progress.worked(1);
		progress.subTask("Tilt Alignment");
		errorMessage = null;
		setComplete(false);

		clearAllPoints();
		tomoScriptController.addIObserver(tiltAlignmentObserver);

		JythonServerFacade.getInstance().runCommand(
				String.format(TOMO_ALIGNMENT_TILT_COMMAND, selectedCameraModule.getValue(), exposureTime));

		while (!isComplete()) {
			Thread.sleep(500);
		}

		tomoScriptController.deleteIObserver(tiltAlignmentObserver);
		TiltPlotPointsHolder tiltPlotPointsHolder = new TiltPlotPointsHolder();
		if (preTiltPoints != null) {
			DoublePointList preTiltCentres = new DoublePointList();
			for (Double point : preTiltPoints) {
				preTiltCentres.addPoint(point.x, point.y);
			}
			DoublePointList preTiltEllipsePoints = new DoublePointList();
			for (Double point : this.preTiltEllipsePoints) {
				preTiltEllipsePoints.addPoint(point.x, point.y);
			}
			tiltPlotPointsHolder.setCenters1(preTiltCentres);
			tiltPlotPointsHolder.setEllipse1(preTiltEllipsePoints);
		}
		if (postTiltPoints != null) {
			DoublePointList postTiltCenters = new DoublePointList();
			for (Double point : postTiltPoints) {
				postTiltCenters.addPoint(point.x, point.y);
			}
			DoublePointList postTiltEllipsePoints = new DoublePointList();
			for (Double point : this.postTiltEllipsePoints) {
				postTiltEllipsePoints.addPoint(point.x, point.y);
			}
			tiltPlotPointsHolder.setCenters2(postTiltCenters);
			tiltPlotPointsHolder.setEllipse2(postTiltEllipsePoints);

		}

		if (errorMessage != null) {
			throw new Exception(errorMessage);
		}
		progress.done();
		return tiltPlotPointsHolder;
	}

	private void clearAllPoints() {
		preTiltPoints = Collections.emptyList();
		postTiltPoints = Collections.emptyList();
		preTiltEllipsePoints = Collections.emptyList();
		postTiltEllipsePoints = Collections.emptyList();
	}

	public IObservable getTomoScriptController() {
		return tomoScriptController;
	}

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}
}
