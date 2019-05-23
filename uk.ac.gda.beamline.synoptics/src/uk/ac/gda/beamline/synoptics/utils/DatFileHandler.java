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

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.gda.beamline.synoptics.views.DetectorFilePlotView;

public class DatFileHandler extends FileNameFilteringFileHandler {
	private static final Logger logger = LoggerFactory.getLogger(DatFileHandler.class);
	private Set<String> plottedNames = new HashSet<>();

	@Override
	public boolean plot(String filename, DetectorFilePlotView plotView, boolean newPlot) {
		if (newPlot) {
			plottedNames.clear();
		}
		if (!plottedNames.contains(filename)) {
			plottedNames.add(filename);
			try {
				IDataHolder data = LoaderFactory.getData(filename);
				List<ILazyDataset> datasets = data.getList();
				logger.debug("Found {} datasets", datasets.size());
				ILazyDataset xData = datasets.remove(0);

				String title = new File(filename).getName();
				if (newPlot) plotView.clearPlots();
				datasets.forEach(ds -> updatePlot(plotView, false, xData, title, ds));
				return true;
			} catch (Exception e) {
				logger.error("Could not load scan data", e);
			}
		} else {
			return true;
		}
		return false;
	}

	private void updatePlot(DetectorFilePlotView plotView, boolean newPlot, ILazyDataset xData, String title, ILazyDataset yData) {
		try {
			plotView.updatePlot(slice(xData), slice(yData), title, xData.getName(), yData.getName(), newPlot, PlotType.XY);
		} catch (DatasetException e) {
			logger.error("Could not plot data ({}/{})", xData, yData, e);
		}
	}

	private IDataset slice(ILazyDataset data) throws DatasetException {
		return data.getSlice((Slice)null);
	}
}
