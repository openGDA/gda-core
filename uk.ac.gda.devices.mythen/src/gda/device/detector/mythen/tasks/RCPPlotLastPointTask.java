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

package gda.device.detector.mythen.tasks;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.data.PathConstructor;
import gda.device.detector.mythen.data.MythenProcessedDataset;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.gda.devices.mythen.event.PlotDataFileEvent;

public class RCPPlotLastPointTask implements AtPointEndTask, InitializingBean {

	private String panelName;
	private String xAxisName;
	private String yAxisName;
	private static final Logger logger = LoggerFactory.getLogger(RCPPlotLastPointTask.class);
	private boolean usePlotServer=true; // the default existing behaviour must not be changed
	private Scriptcontroller eventAdmin;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (isUsePlotServer()) {
			if (getPanelName() == null) {
				throw new IllegalStateException("You have not specified which panel the data should be plotted in");
			}
		} else {
			if (getEventAdmin()==null) {
				throw new IllegalStateException("You have not specified a Scriptcontroller as event admin.");
			}
		}
	}

	@Override
	public void run(String filename, MythenProcessedDataset processedData) {
		if (isUsePlotServer()) {
			double[] angles = processedData.getAngleArray();
			double[] counts = processedData.getCountArray();

			IDataset channelsDataset = DatasetFactory.createFromObject(angles);
			channelsDataset.setName("angle");
			IDataset countsDataset = DatasetFactory.createFromObject(counts);
			countsDataset.setName(filename);

			try {
				SDAPlotter.plot(panelName, channelsDataset, countsDataset);
			} catch (Exception e) {
				logger.error("Exception throwed on RCPPlotter.plot to panel " + panelName, e);
			}
		} else {
			if (FilenameUtils.getExtension(filename) == "") {
				filename=filename+".dat";
			}

			String fullPathName = Paths.get(PathConstructor.createFromDefaultProperty(),filename).toString();
			((ScriptControllerBase)getEventAdmin()).update(this, new PlotDataFileEvent(fullPathName, true));
		}

	}

	@Override
	public void run(String filename, MythenProcessedDataset processedData, boolean clearFirst) {
		if (isUsePlotServer()) {
			// do data plot using plot server.
			double[] angles = processedData.getAngleArray();
			double[] counts = processedData.getCountArray();

			IDataset channelsDataset = DatasetFactory.createFromObject(angles);
			channelsDataset.setName(getxAxisName());
			IDataset countsDataset = DatasetFactory.createFromObject(counts);
			countsDataset.setName(filename);

			try {
				if (clearFirst) {
					SDAPlotter.plot(panelName, channelsDataset, new IDataset[] { countsDataset }, getxAxisName(),
							getyAxisName());
				} else {
					SDAPlotter.addPlot(panelName, "", new IDataset[] { channelsDataset },
							new IDataset[] { countsDataset }, getxAxisName(), getyAxisName());
				}
			} catch (Exception e) {
				logger.error("Exception throwed on RCPPlotter.plot to panel " + panelName, e);
			}
		} else {
			//send event to client to do data plot
			String fullPathName=Paths.get(PathConstructor.createFromDefaultProperty(),filename).toString();
			((ScriptControllerBase)getEventAdmin()).update(this, new PlotDataFileEvent(fullPathName, clearFirst));
		}
	}

	public String getPanelName() {
		return panelName;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	public String getxAxisName() {
		return xAxisName;
	}

	public void setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}

	public String getyAxisName() {
		return yAxisName;
	}

	public void setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}

	public boolean isUsePlotServer() {
		return usePlotServer;
	}

	public void setUsePlotServer(boolean usePlotServer) {
		this.usePlotServer = usePlotServer;
	}

	public Scriptcontroller getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(Scriptcontroller eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

}
