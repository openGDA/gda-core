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

import java.nio.file.Paths;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.gda.beamline.synoptics.views.DetectorFilePlotView;

/**
 * DetectorFileHandler to plot images from HDF files to a {@link DetectorFilePlotView}
 * <p>
 * Filters can be added to only handle certain files based on file name and the
 * path to the data is configurable.
 */

public class HdfImagePlotter extends FileNameFilteringFileHandler {
	private static final Logger logger = LoggerFactory.getLogger(HdfImagePlotter.class);
	private String dataPath;

	@Override
	public boolean plot(String filename, DetectorFilePlotView plotView, boolean newPlot) {
		if (canHandle(filename)) {
			String name = Paths.get(filename).getFileName().toString();
			IDataset image = null;
			try {
				image = LoaderFactory.getDataSet(filename, dataPath, null);
				plotView.updateImagePlot(image, name);
				return true;
			} catch (Exception e) {
				logger.error("Error loading nxs file: " + filename, e);
			}
		}
		return false;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
}
