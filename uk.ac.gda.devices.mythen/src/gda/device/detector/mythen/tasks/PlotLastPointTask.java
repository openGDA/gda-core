/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.tasks;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.mythen.data.MythenProcessedDataset;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 * An {@link AtPointEndTask} which plots the last frame in each scan.
 */
public class PlotLastPointTask implements AtPointEndTask, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(PlotLastPointTask.class);

	private String panelName;

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (panelName == null) {
			throw new IllegalStateException("You have not specified which panel the data should be plotted in");
		}
	}

	@Override
	public void run(String filename, MythenProcessedDataset processedData) {
		double[] angles = processedData.getAngleArray();
		double[] counts = processedData.getCountArray();

		Dataset channelsDataset = DatasetFactory.createFromObject(angles);
		channelsDataset.setName("angle");
		Dataset countsDataset = DatasetFactory.createFromObject(counts);
		countsDataset.setName(filename);

		try {
			SDAPlotter.plot(panelName, channelsDataset, countsDataset);
		} catch (Exception e) {
			logger.error("Error plotting to '{}'",panelName , e);
		}
	}

	@Override
	public void run(String filename, MythenProcessedDataset processedData, boolean clearFirst) {
		double[] angles = processedData.getAngleArray();
		double[] counts = processedData.getCountArray();

		Dataset channelsDataset = DatasetFactory.createFromObject(angles);
		channelsDataset.setName("angle");
		Dataset countsDataset = DatasetFactory.createFromObject(counts);
		countsDataset.setName(filename);
		try {
			if (clearFirst) {
				SDAPlotter.plot(panelName, channelsDataset, countsDataset);
			} else {
				SDAPlotter.clearPlot(panelName);
				SDAPlotter.plot(panelName, channelsDataset, countsDataset);
			}
		} catch (Exception e) {
			logger.error("Error plotting to '{}'", panelName, e);
		}
	}

}
