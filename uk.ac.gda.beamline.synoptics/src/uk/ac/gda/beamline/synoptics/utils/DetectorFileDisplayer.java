/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.utils;

import java.util.Collection;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beamline.synoptics.api.DetectorFileHandler;
import uk.ac.gda.beamline.synoptics.api.PlottingFileProcessor;
import uk.ac.gda.beamline.synoptics.views.DetectorFilePlotView;

/**
 * Utility to take a file and display in on a {@link DetectorFilePlotView} using a
 * configurable list of file handlers to extract the data.
 */
public class DetectorFileDisplayer implements PlottingFileProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DetectorFileDisplayer.class);

	private Collection<DetectorFileHandler> fileHandlers;
	private String viewId;
	private PlotType plotType;
	private boolean newPlot;
	private DetectorFilePlotView plotView;

	@Override
	public void processFile(String filename) {
		logger.trace("Processing file {}", filename);
		openView();
		for (DetectorFileHandler handler: fileHandlers) {
			if (handler.canHandle(filename)) {
				if (handler.plot(filename, plotView, newPlot)) {
					return;
				}
			}
		}
		logger.warn("No handlers available for file '{}'", filename);
	}

	private void openView() {
		if (plotView == null || plotView.isDisposed()) {
			plotView = null; // In case it was disposed
			Display.getDefault().syncExec(() -> { // sync to ensure view is opened
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart showView = null;
					try {
						showView = page.showView(viewId);
					} catch (PartInitException e) {
						logger.error("Unable to show view plot view '{}'", e);
					}
					if (showView != null && showView instanceof DetectorFilePlotView) {
						plotView = (DetectorFilePlotView) showView;
					}
					page.activate(plotView);
			});
		}
	}

	@Override
	public boolean isNewPlot() {
		return newPlot;
	}

	@Override
	public void setNewPlot(boolean value) {
		newPlot = value;
	}

	@Override
	public void setPlotType(PlotType type) {
		plotType = type;
	}

	@Override
	public PlotType getPlotType() {
		return plotType;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public void setFileHandlers(Collection<DetectorFileHandler> fileHandlers) {
		this.fileHandlers = fileHandlers;
	}

}
