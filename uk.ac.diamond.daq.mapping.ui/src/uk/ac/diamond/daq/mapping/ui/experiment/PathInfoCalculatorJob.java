/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;


import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IPathInfoCalculator;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfoRequest;

public class PathInfoCalculatorJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(PathInfoCalculatorJob.class);

	private final IPathInfoCalculator<PathInfoRequest> pathInfoCalculator;

	private final Consumer<PathInfo> onComplete;

	private PathInfoRequest request;

	public PathInfoCalculatorJob(
			final IPathInfoCalculator<PathInfoRequest> pathInfoCalculator,
			final Consumer<PathInfo> onComplete) {
		super("Calculating scan path");
		setPriority(SHORT);
		this.pathInfoCalculator = Objects.requireNonNull(pathInfoCalculator);
		this.onComplete = Objects.requireNonNull(onComplete);
	}

	public void setPathInfoRequest(PathInfoRequest request) {
		this.request = request;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Calculating points for scan path", IProgressMonitor.UNKNOWN);

		try {
			logger.info("Starting calculation");
			Future<PathInfo> future = pathInfoCalculator.calculatePathInfoAsync(request);

			// Poll the asynchronous computation until finished, cancel it if requested
			// by the user
			while (!future.isDone()) {
				if (monitor.isCanceled()) {
					logger.info("Calculation cancelled");
					future.cancel(false);
					return Status.CANCEL_STATUS;
				}
			}
			monitor.done();

			// The consumer decides how to handle the path info event
			PathInfo pathInfo = future.get();
			logger.info("Calculation complete, {}", pathInfo);
			onComplete.accept(pathInfo);
		} catch (Exception e) {
			return new Status(IStatus.WARNING, "uk.ac.diamond.daq.mapping.ui", "Error calculating scan path", e);
		}
		return Status.OK_STATUS;
	}
}