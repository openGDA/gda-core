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

import static java.util.stream.Collectors.joining;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.IMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.gda.beamline.synoptics.views.DetectorFilePlotView;

public class NexusScanFileHandler extends FileNameFilteringFileHandler {
	private static final Logger logger = LoggerFactory.getLogger(NexusScanFileHandler.class);
	private static final String X_ATTR = "@primary";
	private static final String Y_ATTR = "@signal";

	private Collection<String> plottedNames = new HashSet<>();

	@Override
	public boolean plot(String filename, DetectorFilePlotView plotView, boolean newPlot) {
		if (newPlot) {
			plottedNames.clear();
		}
		if (!plottedNames.contains(filename)) {
			plottedNames.add(filename);
			try {
				IDataHolder data = LoaderFactory.getData(filename);
				IMetadata meta = data.getMetadata();
				DoubleDataset xData = null;
				DoubleDataset yData = null;
				for (String name: meta.getDataNames()) {
					if (xData == null && "1".equals(meta.getMetaValue(name + X_ATTR))) {
						logger.debug("Using {} for x axis", name);
						xData = ((DoubleDataset) data.getLazyDataset(name).getSlice(new Slice(0, null, null)));
					} else if (yData == null && "1".equals(meta.getMetaValue(name + Y_ATTR))) {
						logger.debug("Using {} for y axis", name);
						yData = ((DoubleDataset) data.getLazyDataset(name).getSlice((Slice)null));
					}
				}
				if (xData == null || yData == null) {
					throw new IllegalStateException("Could not determine datasets");
				}
				plotView.updatePlot(xData, yData, makeTitle(), xData.getName(), yData.getName(), newPlot, PlotType.XY);
				return true;
			} catch (Exception e) {
				logger.error("Could not load scan data", e);
			}
		} else {
			return true;
		}
		return false;
	}

	private String makeTitle() {
		return plottedNames.stream().map(this::relativeName).collect(joining(", "));
	}

	private String relativeName(String filename) {
		String visitDirectory = InterfaceProvider.getPathConstructor().getClientVisitDirectory();
		Path visit = Paths.get(visitDirectory);
		Path relativize = visit.relativize(Paths.get(filename));
		return relativize.toString();
	}
}
