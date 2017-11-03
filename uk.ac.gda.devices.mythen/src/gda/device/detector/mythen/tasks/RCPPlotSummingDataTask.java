/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.mythen.data.DataConverter;
import gda.device.detector.mythen.data.MythenDataFileUtils;
import gda.device.detector.mythen.data.MythenDataFileUtils.FileType;
import gda.device.detector.mythen.data.MythenSum;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.gda.devices.mythen.epics.MythenDetector;
import uk.ac.gda.devices.mythen.event.PlotDataFileEvent;
/**
 * A Spring configurable {@link DataProcessingTask} to sum all the data files collected from the {@link MythenDetector} in a scan.
 * The summed data are then plotted either using an instance of {@link PlotServer} built-in GDA server or directly by the GDA client
 * from the data file using event notification to the registered observers.
 */
public class RCPPlotSummingDataTask implements DataProcessingTask, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(RCPPlotSummingDataTask.class);

	protected double step = 0.004;

	/**
	 * Sets the angle step to use when summing the data.
	 */
	public void setStep(double step) {
		this.step = step;
	}

	public double getStep() {
		return step;
	}

	protected String panelName;

	private String xAxisName;

	private String yAxisName;

	private boolean usePlotServer=true; //this is the old behaviour

	private Scriptcontroller eventAdmin;

	protected void sumProcessedData(Detector detector)  throws DeviceException {
		ArrayList<File> files=new ArrayList<File>();
		int numberOfModules;
		DataConverter dataConverter;
		File dataDirectory;
		String summedFilename;
		if (detector instanceof MythenDetector) {
			MythenDetector mydetector=(MythenDetector)detector;
			files=mydetector.getProcessedDataFilesForThisScan();
			numberOfModules=mydetector.getNumberOfModules();
			dataConverter=mydetector.getDataConverter();
			dataDirectory=mydetector.getDataDirectory();
			summedFilename = mydetector.buildFilename("summed", FileType.PROCESSED);
		} else {
			throw new RuntimeException("summing processed data is not supported for detcetor "+detector.getName());
		}
		logger.info(String.format("Going to sum %d dataset(s)", files.size()));

		// Build filename of each processed data file
		String[] filenames = new String[files.size()];
		for (int i=1; i<=files.size(); i++) {
			filenames[i-1] = files.get(i-1).getAbsolutePath();
		}

		// Load all processed data files
		logger.info("Loading processed data...");
		double[][][] allData = MythenDataFileUtils.readMythenProcessedDataFiles(filenames);
		logger.info("Done");

		// Sum the data
		logger.info("Summing data...");
		print("Summing data ...");
		double[][] summedData = MythenSum.sum(allData, numberOfModules, dataConverter.getBadChannelProvider(), step);
		logger.info("Done");
		// Save the summed data
		File summedDataFile = new File(dataDirectory, summedFilename);
		logger.info(String.format("Saving summed data to %s", summedDataFile.getAbsolutePath()));
		print("Saving summed data to "+ summedDataFile.getAbsolutePath());
		try {
			MythenDataFileUtils.saveProcessedDataFile(summedData, summedDataFile.getAbsolutePath());
			logger.info("Summed data saved successfully");
		} catch (IOException e) {
			final String msg = String.format(
				"Unable to save summed data to %s, but all individual data files have been saved successfully",
				summedDataFile);
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}

		// Register summed data file
		FileRegistrarHelper.registerFile(summedDataFile.getAbsolutePath());

		if (isUsePlotServer()) {
			// Plot summed data
			final int numChannels = summedData.length;
			double[] angles = new double[numChannels];
			double[] counts = new double[numChannels];
			for (int i = 0; i < numChannels; i++) {
				angles[i] = summedData[i][0];
				counts[i] = summedData[i][1];
			}
			String name2 = FilenameUtils.getName(summedDataFile.getAbsolutePath());
			Dataset anglesDataset = DatasetFactory.createFromObject(angles);
			anglesDataset.setName("angle");
			Dataset countsDataset = DatasetFactory.createFromObject(counts);
			countsDataset.setName(name2);
			try {
				// RCP plot panel
				SDAPlotter.plot(panelName, anglesDataset, countsDataset);
			} catch (Exception e) {
				logger.error("RCP plot failed.", e);
				throw new DeviceException("RCP plot failed.", e);
			}
		} else {
			if (getEventAdmin()!=null) {
				((ScriptControllerBase)getEventAdmin()).update(this, new PlotDataFileEvent(summedDataFile.getAbsolutePath(), true));
			}
		}
	}
	@Override
	public void run(Detector detector) throws DeviceException {
		sumProcessedData(detector);
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		if (isUsePlotServer()) {
			//for plot initiated from server
			if (getPanelName() == null) {
				throw new IllegalArgumentException("You have not specified which panel the data should be plotted in");
			}
		} else {
			//to tell RCP client data file name to plot
			if (getEventAdmin() == null) {
				throw new IllegalArgumentException("You have not specified a Scriptcontroller as EventAdmin.");
			}
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
	/**
	 * method to print message to the Jython Terminal console.
	 *
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
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
