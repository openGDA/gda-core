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
import org.springframework.beans.factory.InitializingBean;

import gda.analysis.Plotter;
import gda.device.detector.mythen.data.MythenProcessedDataset;

/**
 * An {@link AtPointEndTask} which plots the last frame in each scan.
 */
public class PlotLastPointTask implements AtPointEndTask, InitializingBean {

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

		Plotter.plot(panelName, channelsDataset, countsDataset);
	}

	@Override
	public void run(String filename, MythenProcessedDataset processedData, boolean clearFirst) {
		double[] angles = processedData.getAngleArray();
		double[] counts = processedData.getCountArray();

		Dataset channelsDataset = DatasetFactory.createFromObject(angles);
		channelsDataset.setName("angle");
		Dataset countsDataset = DatasetFactory.createFromObject(counts);
		countsDataset.setName(filename);
		if (clearFirst) {
			Plotter.plot(panelName, channelsDataset, countsDataset);
		} else {
			Plotter.plotOver(panelName, channelsDataset, countsDataset);
		}
	}

}
