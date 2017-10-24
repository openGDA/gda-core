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

package uk.ac.gda.devices.edxd;

import java.util.ArrayList;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.analysis.RCPPlotter;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.xmap.edxd.EDXDController;
import gda.device.detector.xmap.edxd.EDXDElement;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalDatabase.LocalDatabaseException;
import uk.ac.diamond.daq.persistence.jythonshelf.ObjectShelfException;

/**
 * This class describes the EDXD detector on I12, it is made up of 24 subdetectors
 */
public class NexusEDXDController extends DetectorBase implements Configurable, NexusDetector {

	// Setup the logging facilities
	transient private static final Logger logger = LoggerFactory.getLogger(NexusEDXDController.class);
	private String edxdControllerName;
	private EDXDController edxdController;
	protected static final String EDXD_PLOT = "EDXD Plot";
	protected static final int TOTAL_NUMBER_OF_TRACE_DATASETS = 10;
	/**
	 * Basic constructor, nothing done in here, waiting for configure
	 */
	public NexusEDXDController() {
	}

	/**
	 * @return String
	 */
	public String getEdxdControllerName() {
		return edxdControllerName;
	}

	/**
	 * @param deviceName
	 */
	public void setEdxdControllerName(String deviceName) {
		this.edxdControllerName = deviceName;
	}

	@Override
	public void configure() throws FactoryException {

		super.configure();
		if((edxdController = (EDXDController)Finder.getInstance().find(edxdControllerName) )!= null){
			logger.debug("edxd controller found " );
		}

		setInputNames(new String[] {});
		setExtraNames(new String[] {"edxd_mean_live_time","edxd_max_dead_time","edxd_min_dead_time","edxd_mean_dead_time","edxd_total_counts"});

	}

     	@Override
     	public void collectData() throws DeviceException {

		edxdController.collectData();
	}

	@Override
	public String getDescription() throws DeviceException {
		return "The EDXD Detector for I12";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "I12 EDXD Detector";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Multi channel MCA";
	}

	@Override
	public int getStatus() throws DeviceException {
		return edxdController.getStatus();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}


	@Override
	public NexusTreeProvider readout() throws DeviceException {
		edxdController.verifyData();


		NXDetectorData data = new NXDetectorData(this);

		// Quick integrator to calculate the total counts and values for the dead_time stats
		int totalCounts = 0;
		double dead_time_max = -Double.MAX_VALUE;
		double dead_time_min = Double.MAX_VALUE;
		double dead_time_mean = 0.0;
		int dead_time_mean_elements = 0;
		double live_time_mean = 0.0;

		int noOfSubDetectors = edxdController.getNumberOfElements();
		// one final thing for the use of plotting
		DoubleDataset[] plotds = new DoubleDataset[noOfSubDetectors];


		// populate the data item from the elements
		for( int i = 0; i < noOfSubDetectors; i++) {

			EDXDElement det = edxdController.getSubDetector(i);

			// add the data
			double[] detData = det.readoutDoubles();
			double thisSum = 0;
			for (double item : detData) {
				thisSum+= item;
			}
			totalCounts += thisSum;

			plotds[i] = DatasetFactory.createFromObject(DoubleDataset.class, detData);
			plotds[i].getName();

			data.addData(det.getName(), new NexusGroupData(det.getDataDimensions(), plotds[i].getData()), "counts", 1);

			// add the energy Axis
			double[] energy = det.getEnergyBins();
			data.addAxis(det.getName(),"edxd_energy", new NexusGroupData(energy), 2, 2, "keV", false);

			// add the q Axis
			double[] q = det.getQMapping();
			data.addAxis(det.getName(),"edxd_q", new NexusGroupData(q), 2, 1, "units", false);

			double[] elive_time = {det.getEnergyLiveTime()};
			data.addElement(det.getName(),"edxd_energy_live_time", new NexusGroupData(elive_time), "seconds", true);

			double[] tlive_time = {det.getTriggerLiveTime()};
			data.addElement(det.getName(),"edxd_trigger_live_time", new NexusGroupData(tlive_time), "seconds", true);

			double[] real_time = {det.getRealTime()};
			data.addElement(det.getName(),"edxd_real_time", new NexusGroupData(real_time), "seconds", true);

			int[] events = {det.getEvents()};
			data.addElement(det.getName(),"edxd_events", new NexusGroupData(events), "counts", true);

			double[] input_count_rate = {det.getRealTime()};
			data.addElement(det.getName(),"edxd_input_count_rate", new NexusGroupData(input_count_rate), "counts/second", true);

			double[] output_count_rate = {det.getRealTime()};
			data.addElement(det.getName(),"edxd_output_count_rate", new NexusGroupData(output_count_rate), "counts/second", true);

			// simple deadtime calculation for now, which is simply based on the 2 rates
			double[] dead_time = {(1.0-(output_count_rate[0]/input_count_rate[0]))*real_time[0]};
			data.addElement(det.getName(),"edxd_dead_time", new NexusGroupData(dead_time), "seconds", true);

			// now calculate the deadtime statistics
			if(dead_time[0] > dead_time_max )
				dead_time_max = dead_time[0];
			if(dead_time[0] < dead_time_min )
				dead_time_min = dead_time[0];
			dead_time_mean += dead_time[0];
			live_time_mean += tlive_time[0];
			dead_time_mean_elements++;


		}

		data.setPlottableValue("edxd_mean_live_time", live_time_mean/dead_time_mean_elements);
		data.setPlottableValue("edxd_max_dead_time", dead_time_max);
		data.setPlottableValue("edxd_min_dead_time", dead_time_min);
		data.setPlottableValue("edxd_mean_dead_time", dead_time_mean/dead_time_mean_elements);
		data.setPlottableValue("edxd_total_counts", (double) totalCounts);

		// now perform the plotting
		try {
			updatePlots(plotds);
		} catch (Exception e) {
			throw new DeviceException("Could not update plots", e);
		}

		return data;


	}


	/**
	 * Sets the number of bins that are used by the xmap.
	 * @param NumberOfBins a number up to 16k
	 * @return the number of bins which are actualy set.
	 * @throws DeviceException
	 */
	public int setBins(int NumberOfBins) throws DeviceException {
		return edxdController.setBins(NumberOfBins);
	}


	/**
	 *
	 * @return The number of bins the xmap is curently set to
	 * @throws DeviceException
	 */
	public int getBins() throws DeviceException {
		return edxdController.getBins();
	}

	/**
	 * Sets the dynamic range of the detector
	 * @param dynamicRange the dynamic range in KeV
	 * @return the actual value which has been set
	 * @throws DeviceException
	 */
	public double setDynamicRange(double dynamicRange) throws DeviceException {
		return edxdController.setDynamicRange(dynamicRange);
	}

	/**
	 * Sets the energy per bin
	 * @param binWidth in eV
	 * @throws DeviceException
	 */
	public void setBinWidth(double binWidth) throws DeviceException {
		edxdController.setBinWidth(binWidth);
	}

	/**
	 * Simple method which sets up the EDXD with some basic assumptions
	 * @param maxEnergy The maxim energy expected in KeV
	 * @param numberOfBins the number of bins that are wanted, i.e the resolution
	 * @throws DeviceException
	 */
	public void setup(double maxEnergy, int numberOfBins ) throws DeviceException {
		edxdController.setup(maxEnergy, numberOfBins);
	}


	// All the saving and loading settings

	/**
	 * Save all the xmap settings, with no description
	 * @param name
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 * @throws DeviceException
	 */
	public void saveCurrentSettings(String name) throws ObjectShelfException, LocalDatabaseException, DeviceException {
		saveCurrentSettings(name,"No Description");
	}

	/**
	 * Save all the xmap settings along with the given description
	 * @param name
	 * @param description
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 * @throws DeviceException
	 */
	public void saveCurrentSettings(String name, String description) throws ObjectShelfException, LocalDatabaseException, DeviceException {
		edxdController.saveCurrentSettings(name, description);
	}

	/**
	 * Loads a setting from the persistance
	 * @param name
	 * @return The description of the configuration loaded
	 * @throws DeviceException
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 */
	public String loadSettings(String name) throws DeviceException, ObjectShelfException, LocalDatabaseException {

		return edxdController.loadSettings(name);

	}

	/**
	 * This method shows all the savefiles for the EDXD detector
	 * @return a string containing all the files
	 * @throws LocalDatabaseException
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 * @throws ObjectShelfException
	 */
	public String listSettings() throws ObjectShelfException, LocalDatabaseException{

		return edxdController.listSettings();

	}

	// Setters for the various settings accross the board

	/**
	 * Set the preampGain for all elements
	 * @param preampGain
	 * @throws DeviceException
	 */
	public void setPreampGain(double preampGain) throws DeviceException {
		edxdController.setPreampGain(preampGain);
	}

	/**
	 * Set the peakTime for all elements
	 * @param peakTime
	 * @throws DeviceException
	 */
	public void setPeakTime(double peakTime) throws DeviceException {
		edxdController.setPeakTime(peakTime);
	}

	/**
	 * Set the triggerThreshold for all elements
	 * @param triggerThreshold
	 * @throws DeviceException
	 */
	public void setTriggerThreshold(double triggerThreshold) throws DeviceException {
		edxdController.setTriggerThreshold(triggerThreshold);
	}


	/**
	 * Set the baseThreshold for all elements
	 * @param baseThreshold
	 * @throws DeviceException
	 */
	public void setBaseThreshold(double baseThreshold) throws DeviceException {
		edxdController.setBaseThreshold(baseThreshold);
	}


	/**
	 * Set the baseLength for all elements
	 * @param baseLength
	 * @throws DeviceException
	 */
	public void setBaseLength(int baseLength) throws DeviceException {
		edxdController.setBaseLength(baseLength);
	}

	/**
	 * Set the energyThreshold for all elements
	 * @param energyThreshold
	 * @throws DeviceException
	 */
	public void setEnergyThreshold(double energyThreshold) throws DeviceException {
		edxdController.setEnergyThreshold(energyThreshold);
		}

	/**
	 * Set the resetDelay for all elements
	 * @param resetDelay
	 * @throws DeviceException
	 */
	public void setResetDelay(double resetDelay) throws DeviceException {
		edxdController.setResetDelay(resetDelay);
	}

	/**
	 * Set the gapTime for all elements
	 * @param gapTime
	 * @throws DeviceException
	 */
	public void setGapTime(double gapTime) throws DeviceException {
		edxdController.setGapTime(gapTime);
	}

	/**
	 * Set the triggerPeakTime for all elements
	 * @param triggerPeakTime
	 * @throws DeviceException
	 */
	public void setTriggerPeakTime(double triggerPeakTime) throws DeviceException {
		edxdController.setTriggerPeakTime(triggerPeakTime);
	}

	/**
	 * Set the triggerGapTime for all elements
	 * @param triggerGapTime
	 * @throws DeviceException
	 */
	public void setTriggerGapTime(double triggerGapTime) throws DeviceException {
		edxdController.setTriggerGapTime(triggerGapTime);
	}

	/**
	 * Set the maxWidth for all elements
	 * @param maxWidth
	 * @throws DeviceException
	 */
	public void setMaxWidth(double maxWidth) throws DeviceException {
		edxdController.setMaxWidth(maxWidth);
	}

	/**
	 * Get one of teh elements specifialy by name
	 * @param index
	 * @return An teh EDXDElement requested
	 */
	public EDXDElement getSubDetector(int index) {
		return edxdController.getSubDetector(index);
	}

	 /**
	 *Change the data collection mode to resume / clear
	 * @param resume
	 * @throws DeviceException
	 */
	public void setResume(boolean resume)throws DeviceException
	    {
		 edxdController.setResume(resume);
	    }

	// Spectra Monitoring and Plotting

	private boolean plotAllSpectra = false;

	private Integer traceOneSpectra = null;

	private boolean newTrace = true;

	private ArrayList<DoubleDataset> traceDataSets = new ArrayList<DoubleDataset>();





	/**
	 *
	 */
	public void monitorAllSpectra() {
		plotAllSpectra = true;
	}

	/**
	 * Monitors a specific spectra
	 * @param detectorNumber
	 */
	public void monitorSpectra(int detectorNumber) {
		plotAllSpectra = false;
		traceOneSpectra = detectorNumber;
		newTrace = true;
	}

	/**
	 * Stops monitoring the detector
	 */
	public void stopMonitoring() {
		plotAllSpectra = false;
		traceOneSpectra = null;
		newTrace = true;
	}

	/**
	 * Clears the trace if there is a specific detector being traced
	 */
	public void clearTrace() {
		newTrace = true;
	}


	/**
	 * Get data as double array from the mca element
	 * @param mcaNumber
	 * @return double[]
	 * @throws DeviceException
	 */
	public double[] getData(int mcaNumber) throws DeviceException {
		 return edxdController.getData(mcaNumber);
	}

	/**
	 * Acquires a single image for viewing only
	 * @param aquisitionTime The time to collect for
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("static-access")
	public void acquire(double aquisitionTime) throws DeviceException, InterruptedException {
		edxdController.setCollectionTime(aquisitionTime);
		edxdController.collectData();

		while (edxdController.getStatus() == Detector.BUSY) {

			InterfaceProvider.getTerminalPrinter().print("Acquiring");
			Thread.sleep(1000);
		}

		InterfaceProvider.getTerminalPrinter().print("Done");

		edxdController.verifyData();

		// now the data is acquired, plot it out to plot2 for the time being.
		int  noElem = edxdController.getNumberOfElements();
		DoubleDataset[] data = new DoubleDataset[noElem];

		for( int i = 0; i < noElem; i++) {

			EDXDElement det = edxdController.getSubDetector(i);

			// add the data
			data[i] = DatasetFactory.createFromObject(DoubleDataset.class, det.readoutDoubles());
			data[i].setName(det.getName());
		}
		Dataset yaxis = DatasetFactory.createFromObject(edxdController.getSubDetector(0).getEnergyBins());
		yaxis.setName("Energy");

		try {
			RCPPlotter.plot(EDXD_PLOT, yaxis, data);
		} catch (Exception e) {
			throw new DeviceException("Could not plot to {}", EDXD_PLOT, e);
		}


	}


	@SuppressWarnings("static-access")
	private void updatePlots(DoubleDataset[] plotds) throws Exception {
		if(plotAllSpectra) {
			Dataset yAxis = DatasetFactory.createFromObject(edxdController.getSubDetector(0).getEnergyBins());
			yAxis.setName("Energy");
			RCPPlotter.plot(EDXD_PLOT, yAxis, plotds);
		} else {

			if(traceOneSpectra!=null) {
				Dataset yAxis = DatasetFactory.createFromObject(edxdController.getSubDetector(traceOneSpectra).getEnergyBins());
				yAxis.setName("Energy");
				if (newTrace) {
					traceDataSets.clear();
					newTrace=false;
				}
				traceDataSets.add(plotds[traceOneSpectra]);
				while (traceDataSets.size() > TOTAL_NUMBER_OF_TRACE_DATASETS) {
					traceDataSets.remove(0);
				}
				DoubleDataset[] plotValues = new DoubleDataset[traceDataSets.size()];
				for(int i = 0; i < traceDataSets.size(); i++) {
					plotValues[i] = traceDataSets.get(i);
				}
				RCPPlotter.stackPlot(EDXD_PLOT, yAxis, plotValues);
			}

		}

	}

	/**
	 * Set the acquisition time
	 * @param collectionTime
	 * @throws DeviceException
	 */
	public void setAquisitionTime(double collectionTime)throws DeviceException {
		edxdController.setAquisitionTime(collectionTime);
	}

	/**
	 * Start Data collection. Uses the existing collection mode
	 * @throws DeviceException
	 */
	public void start() throws DeviceException {
		edxdController.start();
	}



	@Override
	public void stop() throws DeviceException {
		edxdController.stop();
	}

	/**
	 * @throws DeviceException
	 */
	public void activateROI() throws DeviceException{
		edxdController.activateROI();
	}


	/**
	 * @throws DeviceException
	 */
	public void deactivateROI() throws DeviceException{
		edxdController.deactivateROI();
	}

	/**
	 * @return int
	 * @throws DeviceException
	 */
	public int getMaxAllowedROIs() throws DeviceException {
		return edxdController.getMaxAllowedROIs();
	}

}



