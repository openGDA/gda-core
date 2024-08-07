/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.scanning.device.ui.vis;


import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * @deprecated for removal in 9.28 Use instead:
 *             {@link uk.ac.diamond.daq.mapping.ui.experiment.PathInfoCalculatorJob} Only use as part of defunct Scanning
 *             Perspective, useful parts of code extracted to mapping packages and improved upon
 */
@Deprecated(since="9.26", forRemoval=true)
class PathInfoCalculatorJob extends Job {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(PathInfoCalculatorJob.class);


	static final int MAX_POINTS_IN_ROI = 100000; // 100,000

	// Services
	private IPointGeneratorService pointGeneratorFactory;
	private IValidatorService      vservice;

	// Model
	private IScanPointGeneratorModel scanPathModel;
	private List<ScanRegion> scanRegions;

	// Controller
	private PlottingController controller;


	public PathInfoCalculatorJob(final PlottingController controller) {
		super("Calculating scan path");
		logger.deprecatedClass("GDA 9.28", "uk.ac.diamond.daq.mapping.ui.experiment.PathInfoCalculatorJob");
		this.controller = controller;
		setSystem(true);
		setUser(false);
		setPriority(Job.INTERACTIVE);
		this.pointGeneratorFactory = ServiceProvider.getService(IPointGeneratorService.class);
		this.vservice              = ServiceProvider.getService(IValidatorService.class);
	}

	protected void schedule(IScanPointGeneratorModel model, List<ScanRegion> scanRegions) {
		this.scanPathModel = model;
		this.scanRegions   = scanRegions;
		schedule();
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {

		if (scanPathModel==null) return Status.CANCEL_STATUS;
		if (scanRegions==null || scanRegions.isEmpty())    {
			setPathVisible(false);
			return Status.CANCEL_STATUS;
		}

		{ // Put trace out of scope, drawing the line should not depend on it.
			final IImageTrace trace = controller.getImageTrace();
			if (trace!=null) {
				if (!trace.hasTrueAxes()) throw new IllegalArgumentException(getClass().getSimpleName()+" should act on true axis images!");
			}
		}

		monitor.beginTask("Calculating points for scan path", IProgressMonitor.UNKNOWN);

		PathInfo pathInfo = new PathInfo();
		if (!(scanPathModel instanceof IBoundingBoxModel)) {
			setPathVisible(false);
			return Status.CANCEL_STATUS;// No path to draw.
		}
		IBoundingBoxModel boxModel = (IBoundingBoxModel) scanPathModel;
		String xAxisName = boxModel.getxAxisName();
		String yAxisName = boxModel.getyAxisName();
		try {
			vservice.validate(scanPathModel); // Throws exception if invalid.

			final List<IROI> rois = pointGeneratorFactory.findRegions(scanPathModel, scanRegions); // Out of the regions defined finds in the ones for this model.
			if (rois==null || rois.isEmpty()) {
				setPathVisible(false);
				return Status.CANCEL_STATUS;// No path to draw.
			}

			final IPointGenerator<CompoundModel> pointGen = pointGeneratorFactory.createGenerator(scanPathModel, rois);
			double lastX = Double.NaN;
			double lastY = Double.NaN;
			for (IPosition point : pointGen) {

				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				pathInfo.pointCount++;

				double[] pnt = new double[]{point.getDouble(xAxisName), point.getDouble(yAxisName)};

				if (pathInfo.pointCount > 1) {
					double thisXStep = Math.abs(pnt[0] - lastX);
					double thisYStep = Math.abs(pnt[1] - lastY);
					double thisAbsStep = Math.sqrt(Math.pow(thisXStep, 2) + Math.pow(thisYStep, 2));
					if (thisXStep > 0) {
						pathInfo.smallestXStep = Math.min(pathInfo.smallestXStep, thisXStep);
					}
					if (thisYStep > 0) {
						pathInfo.smallestYStep = Math.min(pathInfo.smallestYStep, thisYStep);
					}
					pathInfo.smallestAbsStep = Math.min(pathInfo.smallestAbsStep, thisAbsStep);
				}

				lastX = pnt[0];
				lastY = pnt[1];
				if (pathInfo.size() <= MAX_POINTS_IN_ROI) {
					pathInfo.add(Double.valueOf(lastX), Double.valueOf(lastY));
				}
			}
			monitor.done();

			// Update the plot, waiting until it has suceeded before
			// returning and allowing this job to run again.
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					controller.plot(pathInfo);
				}
			});

		} catch (ModelValidationException mve) {
			return Status.CANCEL_STATUS;

		} catch (Exception e) {
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Error calculating scan path", e);
		}
		return Status.OK_STATUS;
	}

	private void setPathVisible(boolean b) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				controller.setPathVisible(b);
			}
		});
	}

}