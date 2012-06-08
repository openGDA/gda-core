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

package gda.device.detector.xmap;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.Detector;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.McaGroupType;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A controller class  XMAP MCA with Epics interface
 *
 */
public class EpicsXmapController  extends DeviceBase implements XmapController , InitializationListener{

	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private gda.device.detector.xmap.EpicsXmapController.AcqStatusListener acqlistener;

	private gda.device.detector.xmap.EpicsXmapController.RealTimeListener rtimelistener;
	private String name;
	private String deviceName;
	private Channel noOfBins;
	private Channel clearStart;
	private Channel start;
	private Channel stop;
	private Channel statrate;
	private Channel statproc;
	private Channel readrate;
	private Channel readproc;
	private Channel treal;
	private Channel tacq;
	private boolean acquisitionDone;
	private int numberOfBins=1024;
	private int numberOfROIs=1;
	private int actualNumberOfROIs;
	@Override
	public int getNumberOfROIs() {
		return numberOfROIs;
	}

	@Override
	public void setNumberOfROIs(int numberOfROIs) {
		this.numberOfROIs = numberOfROIs;
	}

	@Override
	public void setNumberOfMca(int numberOfMca) {
		this.numberOfMca = numberOfMca;
	}

	private int numberOfMca = 4;
	private static final Logger logger = LoggerFactory.getLogger(EpicsXmapController.class);
	private Vector<String> statusUpdateRates = new Vector<String>();
	private Vector<String> readUpdateRates = new Vector<String>();
	private EpicsMCASimple[] mcaArray;
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			
			this.controller = EpicsController.getInstance();
			this.channelManager = new EpicsChannelManager(this);
			acqlistener = new AcqStatusListener();
			rtimelistener = new RealTimeListener();
			
			// EPICS interface version 3 for phase II beamlines (excluding I22).
			if (getDeviceName() != null) {
				try {
					McaGroupType mcaConfig = Configurator.getConfiguration(getDeviceName(),
							gda.epics.interfaces.McaGroupType.class);
					createChannelAccess(mcaConfig);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for device: " + getDeviceName(), e);
				}

			}
			// Nothing specified in Server XML file
			else {
				logger.error("Missing EPICS interface configuration for the ETL sintillator detector " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the ETL sintillator detector"
						+ getName());
			}

		}// end of if (!configured)
	}
	
	private void createChannelAccess(McaGroupType mcaConfig) throws FactoryException {
		try {
			channelManager.createChannel(mcaConfig.getERASE().getPv(), false);
			channelManager.createChannel(mcaConfig.getACQ().getPv(), false);
			clearStart = channelManager.createChannel(mcaConfig.getERASESTART().getPv(), false);
			noOfBins = channelManager.createChannel(mcaConfig.getNBINS().getPv(), false);
			start = channelManager.createChannel(mcaConfig.getSTART().getPv(), false);
			stop = channelManager.createChannel(mcaConfig.getSTOP().getPv(), false);
			//dwell = channelManager.createChannel(mcaConfig.getTDWELL().getPv(), false);
			statrate=channelManager.createChannel(mcaConfig.getSTATRATE().getPv(), false);
			//hack to get the procesPV as it is not available from interface xml
			String statPV = mcaConfig.getSTATRATE().getPv();
			String statProcPv = statPV.substring(0,statPV.lastIndexOf(".") )  + ".PROC"; 
			statproc = channelManager.createChannel(statProcPv, false);
			logger.debug("the stat proc pv is " + statProcPv);
			readrate=channelManager.createChannel(mcaConfig.getREADRATE().getPv(), false);
			//hack to get the procesPV as it is not available from interface xml
			String readPV = mcaConfig.getREADRATE().getPv();
			String readProcPv = readPV.substring(0,readPV.lastIndexOf("."))  + ".PROC"; 
			readproc = channelManager.createChannel(readProcPv, false);
			logger.debug("the read proc pv is " + readProcPv);
			//"Real time since start of acquisition (currently not working!)"
			treal=channelManager.createChannel(mcaConfig.getTREAL().getPv() ,rtimelistener, false);
			//Set total acquisition time (currently not working!)
			tacq=channelManager.createChannel(mcaConfig.getTACQ().getPv(), acqlistener,false);
			 mcaArray = new EpicsMCASimple[getNumberOfMca()];
			if (mcaConfig.getSIG0()!=null) {
				mcaArray[0] = new EpicsMCASimple();
				mcaArray[0].setMCAPV(mcaConfig.getSIG0().getPv());
				mcaArray[0].configure();
			}
			if (mcaConfig.getSIG1()!=null) {
				mcaArray[1] = new EpicsMCASimple();
				mcaArray[1].setMCAPV(mcaConfig.getSIG1().getPv());
				mcaArray[1].configure();
			}
			if (mcaConfig.getSIG2()!=null) {
				mcaArray[2] = new EpicsMCASimple();
				mcaArray[2].setMCAPV(mcaConfig.getSIG2().getPv());
				mcaArray[2].configure();
			}
			if (mcaConfig.getSIG3()!=null) {
				mcaArray[3] = new EpicsMCASimple();
				mcaArray[3].setMCAPV(mcaConfig.getSIG3().getPv());
				mcaArray[3].configure();
			}
			if (mcaConfig.getSIG4()!=null) {
				mcaArray[4] = new EpicsMCASimple();
				mcaArray[4].setMCAPV(mcaConfig.getSIG4().getPv());
				mcaArray[4].configure();
			}
			if (mcaConfig.getSIG5()!=null) {
				mcaArray[5] = new EpicsMCASimple();
				mcaArray[5].setMCAPV(mcaConfig.getSIG5().getPv());
				mcaArray[5].configure();
			}
			if (mcaConfig.getSIG6()!=null) {
				mcaArray[6] = new EpicsMCASimple();
				mcaArray[6].setMCAPV(mcaConfig.getSIG6().getPv());
				mcaArray[6].configure();
			}
			if (mcaConfig.getSIG7()!=null) {
				mcaArray[7] = new EpicsMCASimple();
				mcaArray[7].setMCAPV(mcaConfig.getSIG7().getPv());
				mcaArray[7].configure();
			}
			if (mcaConfig.getSIG8()!=null) {
				mcaArray[8] = new EpicsMCASimple();
				mcaArray[8].setMCAPV(mcaConfig.getSIG8().getPv());
				mcaArray[8].configure();
			}
			if (mcaConfig.getSIG9()!=null) {
				mcaArray[9] = new EpicsMCASimple();
				mcaArray[9].setMCAPV(mcaConfig.getSIG9().getPv());
				mcaArray[9].configure();
			}
			if (mcaConfig.getSIG10()!=null) {
				mcaArray[10] = new EpicsMCASimple();
				mcaArray[10].setMCAPV(mcaConfig.getSIG10().getPv());
				mcaArray[10].configure();
			}
			if (mcaConfig.getSIG11()!=null) {
				mcaArray[11] = new EpicsMCASimple();
				mcaArray[11].setMCAPV(mcaConfig.getSIG11().getPv());
				mcaArray[11].configure();
			}
			if (mcaConfig.getSIG12()!=null) {
				mcaArray[12] = new EpicsMCASimple();
				mcaArray[12].setMCAPV(mcaConfig.getSIG12().getPv());
				mcaArray[12].configure();
			}
			if (mcaConfig.getSIG13()!=null) {
				mcaArray[13] = new EpicsMCASimple();
				mcaArray[13].setMCAPV(mcaConfig.getSIG13().getPv());
				mcaArray[13].configure();
			}
			if (mcaConfig.getSIG14()!=null) {
				mcaArray[14] = new EpicsMCASimple();
				mcaArray[14].setMCAPV(mcaConfig.getSIG14().getPv());
				mcaArray[14].configure();
			}
			if (mcaConfig.getSIG15()!=null) {
				mcaArray[15] = new EpicsMCASimple();
				mcaArray[15].setMCAPV(mcaConfig.getSIG15().getPv());
				mcaArray[15].configure();
			}
			if (mcaConfig.getSIG16()!=null) {
				mcaArray[16] = new EpicsMCASimple();
				mcaArray[16].setMCAPV(mcaConfig.getSIG16().getPv());
				mcaArray[16].configure();
			}
			if (mcaConfig.getSIG17()!=null) {
				mcaArray[17] = new EpicsMCASimple();
				mcaArray[17].setMCAPV(mcaConfig.getSIG17().getPv());
				mcaArray[17].configure();
			}
			if (mcaConfig.getSIG18()!=null) {
				mcaArray[18] = new EpicsMCASimple();
				mcaArray[18].setMCAPV(mcaConfig.getSIG18().getPv());
				mcaArray[18].configure();
			}
			if (mcaConfig.getSIG19()!=null) {
				mcaArray[19] = new EpicsMCASimple();
				mcaArray[19].setMCAPV(mcaConfig.getSIG19().getPv());
				mcaArray[19].configure();
			}
			if (mcaConfig.getSIG20()!=null) {
				mcaArray[20] = new EpicsMCASimple();
				mcaArray[20].setMCAPV(mcaConfig.getSIG20().getPv());
				mcaArray[20].configure();
			}
			if (mcaConfig.getSIG21()!=null) {
				mcaArray[21] = new EpicsMCASimple();
				mcaArray[21].setMCAPV(mcaConfig.getSIG21().getPv());
				mcaArray[21].configure();
			}
			if (mcaConfig.getSIG22()!=null) {
				mcaArray[22] = new EpicsMCASimple();
				mcaArray[22].setMCAPV(mcaConfig.getSIG22().getPv());
				mcaArray[22].configure();
			}
			if (mcaConfig.getSIG23()!=null) {
				mcaArray[23] = new EpicsMCASimple();
				mcaArray[23].setMCAPV(mcaConfig.getSIG23().getPv());
				mcaArray[23].configure();
			}
			if (mcaConfig.getSIG24()!=null) {
				mcaArray[24] = new EpicsMCASimple();
				mcaArray[24].setMCAPV(mcaConfig.getSIG24().getPv());
				mcaArray[24].configure();
			}
			if (mcaConfig.getSIG25()!=null) {
				mcaArray[25] = new EpicsMCASimple();
				mcaArray[25].setMCAPV(mcaConfig.getSIG25().getPv());
				mcaArray[25].configure();
			}
			if (mcaConfig.getSIG26()!=null) {
				mcaArray[26] = new EpicsMCASimple();
				mcaArray[26].setMCAPV(mcaConfig.getSIG26().getPv());
				mcaArray[26].configure();
			}
			if (mcaConfig.getSIG27()!=null) {
				mcaArray[27] = new EpicsMCASimple();
				mcaArray[27].setMCAPV(mcaConfig.getSIG27().getPv());
				mcaArray[27].configure();
			}
			if (mcaConfig.getSIG28()!=null) {
				mcaArray[28] = new EpicsMCASimple();
				mcaArray[28].setMCAPV(mcaConfig.getSIG28().getPv());
				mcaArray[28].configure();
			}
			if (mcaConfig.getSIG29()!=null) {
				mcaArray[29] = new EpicsMCASimple();
				mcaArray[29].setMCAPV(mcaConfig.getSIG29().getPv());
				mcaArray[29].configure();
			}
			if (mcaConfig.getSIG30()!=null) {
				mcaArray[30] = new EpicsMCASimple();
				mcaArray[30].setMCAPV(mcaConfig.getSIG30().getPv());
				mcaArray[30].configure();
			}
			if (mcaConfig.getSIG31()!=null) {
				mcaArray[31] = new EpicsMCASimple();
				mcaArray[31].setMCAPV(mcaConfig.getSIG31().getPv());
				mcaArray[31].configure();
			}
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
			configured = true;
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
		
			}
		
		
	@Override
	public int getNumberOfMca() {
		return numberOfMca;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the device name
	 */
	public String getDeviceName() {
		return deviceName;
	}
	
	/**
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	/**
	 * Monitoring current acquire status of the hardware
	 */
	private class AcqStatusListener implements MonitorListener {
		



		@Override
		public void monitorChanged(MonitorEvent mev) {
			// at the moment the status is only a bit mask , hence the non standard
			// implementation of reading the status

			DBR dbr = mev.getDBR();
			if (dbr.isENUM()) {
				acquisitionDone = ((DBR_Enum) dbr).getEnumValue()[0] == 0;
			} else if(dbr.isDOUBLE()){
				acquisitionDone = ((DBR_Double)dbr).getDoubleValue()[0] == 0;
			}else {
				logger.error("expecting ENUM but got {} type.", dbr.getType());
			}
			try {
				notifyIObservers(this, getStatusObject());			
				logger.debug("acquisition status updated to {}", getStatus());
			} catch (DeviceException e) {
				logger.error("ln351 : AcqStatusListener , error ", e);
			}
			
		}

		
	}
	/**
	 * @return the status value
	 * @throws DeviceException
	 */
	
	@Override
	public int getStatus() throws DeviceException {
		try {
			//trigger the status read process , not sure this should be done here
			controller.caput(statproc, 1, 0);
		}  catch (Throwable th) {
			logger.error("ln371 : failed to get status from the XMAP", th);
			throw new DeviceException("ln371: failed to get status from the XMAP", th);
		}
		return getStatusObject();
	}

	private int getStatusObject() {
		return acquisitionDone ? Detector.IDLE : Detector.BUSY;
	}

	/**
	 * monitors elapses real time
	 */
	private class RealTimeListener implements MonitorListener {
		private double elapsedRealTimeValue;

		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				elapsedRealTimeValue = ((DBR_Double) dbr).getDoubleValue()[0];
			} else if (dbr.isFLOAT()) {
				elapsedRealTimeValue = ((DBR_Float) dbr).getFloatValue()[0];
			} else {
				logger.error("Expecting double or float but got {} type. ", dbr.getType());
			}
			//notifyIObservers(AcquisitionProperty.ELAPSEDTIME, elapsedRealTimeValue);
			logger.debug("Elapsed time updated to {}", elapsedRealTimeValue);
		}
	}
	/**
	 * @throws DeviceException
	 */
	@Override
	public void clearAndStart() throws DeviceException {
		try {
			controller.caput(clearStart, 1);
			acquisitionDone = false;
		
		} catch (Throwable th) {
			throw new DeviceException("failed to erase and start acquiring", th);
		}
	}
	/**
	 * @return the acqusition time
	 * @throws DeviceException
	 */
	@Override
	public double getAcquisitionTime() throws DeviceException {
		try {
			return controller.cagetDouble(tacq);
		} catch (Throwable th) {
			logger.error("failed to get number of bins {}.", getName());
			throw new DeviceException("failed get number of bins", th);
		}
		
	}
	
	
	@Override
	public int[][] getData() throws DeviceException {
		
		//should write data to a file
		//bespoke scan scripts write data at the moment
		int[][] data = new int[numberOfMca][numberOfBins];
		for (int i = 0; i < numberOfMca; i++) {
			data[i] = getData(i);
		}
		return data;
	}
	/**
	 * @param mcaNumber
	 * @return int array of data from the selected mca
	 * @throws DeviceException
	 */
	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		try {
			//trigger the read rate and scan rate process
			controller.caput(statproc, 1, 0);
			controller.caput(readproc, 1, 0);
			int[] returnArray = new int[numberOfBins];
			System.arraycopy(mcaArray[mcaNumber].getData(), 0, returnArray, 0, numberOfBins) ;
			return returnArray;
			
			
		} catch (Throwable th) {
			logger.error("failed to get the spectrum data on {} for channel {}.", getName(), mcaNumber);
			throw new DeviceException("Failed to get the spectrum data", th);
		}
	}
	/**
	 * @return number of MCA Bins
	 * @throws DeviceException
	 */
	@Override
	public int getNumberOfBins() throws DeviceException {
		try {
			if(configured)
				numberOfBins = controller.cagetInt(noOfBins);  
			 return numberOfBins;
		} catch (Throwable th) {
			logger.error("failed to get number of bins {}.", getName());
			throw new DeviceException("failed get number of bins", th);
		}
	}
	
	/**
	 * @return read rate
	 * @throws DeviceException
	 */
	@Override
	public double getReadRate() throws DeviceException {
		try {
			return controller.cagetEnum(readrate); 
		} catch (Throwable th) {
			logger.error("failed to get number of bins {}.", getName());
			throw new DeviceException("failed get number of bins", th);
		}
	}
	
	/**
	 * @return real time
	 * @throws DeviceException
	 */
	@Override
	public double getRealTime() throws DeviceException {
		try {
			return controller.cagetDouble(treal);
		} catch (Throwable th) {
			logger.error("failed to get number of bins {}.", getName());
			throw new DeviceException("failed get number of bins", th);
		}
	}
	/**
	 * @return status rate
	 * @throws DeviceException
	 */
	@Override
	public double getStatusRate() throws DeviceException {
		try {
			return controller.cagetEnum(statrate);
		} catch (Throwable th) {
			logger.error("failed to get number of bins {}.", getName());
			throw new DeviceException("failed get number of bins", th);
		}
	}
	/**
	 * @param time
	 * @throws DeviceException
	 */
	@Override
	public void setAcquisitionTime(double time) throws DeviceException {
		try {
			controller.caput(tacq, time);
			
		} catch (Throwable th) {
			throw new DeviceException("failed to set Acquisition time", th);
		}
	}
	/**
	 * @param numberOfBins
	 * @throws DeviceException
	 */
	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		try {
			if(configured)
				controller.caput(noOfBins, numberOfBins);
			this.numberOfBins = numberOfBins;

		} catch (Throwable th) {
			throw new DeviceException("failed to set number of bins", th);
		}
		
	}
	/**
	 * @param value
	 * @throws DeviceException
	 */
	@Override
	public void setStatusRate(String value) throws DeviceException {
		
		if (!statusUpdateRates.contains(value)) {
			throw new IllegalArgumentException("Input must be in range: " + statusUpdateRates.elements());
		}
		try {
			controller.caput(statrate, value);
		} catch (Throwable th) {
			logger.error("failed to set status update rate on {}.", getName());
			throw new DeviceException("failed to set status update rate", th);
		}
	}
	@Override
	public void initializationCompleted() {
		try {
			String[] position = getStatusRates();
			for (int i = 0; i < position.length; i++) {
				if (position[i] != null || position[i] != "") {
					statusUpdateRates.add(position[i]);
				}
			}
		} catch (DeviceException e) {
			logger.error("failed to initialise available Status Update Rates.");
		}
		try {
			String[] position = getReadRates();
			for (int i = 0; i < position.length; i++) {
				if (position[i] != null || position[i] != "") {
					readUpdateRates.add(position[i]);
				}
			}
		} catch (DeviceException e) {
			logger.error("failed to initialise available Read Update Rates.");
		}
		try {
			setNumberOfBins(this.numberOfBins);
			
		} catch (DeviceException e) {
			logger.error("failed to set number of bins after initialization.");
		}
		
		logger.info("{} is initialised.", getName());
	}

	/**
	 * @param value
	 * @throws DeviceException
	 */
	@Override
	public void setReadRate(String value) throws DeviceException {
		if (!readUpdateRates.contains(value)) {
			throw new IllegalArgumentException("Input must be in range: " + getReadRates());
		}
		try {
			controller.caput(readrate, value);
		} catch (Throwable th) {
			logger.error("failed to set read update rate on {}.", getName());
			throw new DeviceException("failed to set read update rate", th);
		}
	}
	/**
	 * gets all available read update rates from EPICS IOC
	 * 
	 * @return available read update rates
	 * @throws DeviceException
	 */
	public String[] getReadRates() throws DeviceException {
		
		String[] positionLabels = new String[readUpdateRates.size()];
		try {
			positionLabels = controller.cagetLabels(readrate);
		} catch (Throwable th) {
			logger.error("failed to get read update rates avalable on {}.", getName());
			throw new DeviceException("failed to get read update rates avalable", th);
		}
		return positionLabels;
	}
	/**
	 * gets all available status update rates from EPICS IOC
	 * 
	 * @return available status update rates
	 * @throws DeviceException
	 */
	public String[] getStatusRates() throws DeviceException {
		
		String[] positionLabels = new String[statusUpdateRates.size()];
		try {
			positionLabels = controller.cagetLabels(statrate);
		} catch (Throwable th) {
			logger.error("failed to get status update rates avalable on {}.", getName());
			throw new DeviceException("failed to set status update rates available.", th);
		}
		return positionLabels;
	}

	/**
	 * @param readRate
	 * @throws DeviceException
	 */
	@Override
	public void setReadRate(double readRate) throws DeviceException {
		try {
			controller.caput(readrate, readRate);
		
		} catch (Throwable th) {
			throw new DeviceException("failed to set read rate", th);
		}
	}

	/**
	 * @param statusRate
	 * @throws DeviceException
	 */
	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		try {
			controller.caput(statrate, statusRate);
		
		} catch (Throwable th) {
			throw new DeviceException("failed to set status rate", th);
		}
		
	}

	/**
	 * @throws DeviceException
	 */
	@Override
	public void start() throws DeviceException {
		try{
		controller.caput(start, 1);
		acquisitionDone = false;
		

	} catch (Throwable th) {
		throw new DeviceException("failed to start acquiring", th);
	}
		
	}

	/**
	 * @throws DeviceException
	 */
	@Override
	public void stop() throws DeviceException {
		try {
			controller.caput(stop, 1);
			acquisitionDone = true;

		} catch (Throwable th) {
			throw new DeviceException("failed to start acquiring", th);
		}
		
	}
	
	//This is an untested method , needs to be tested with real hardware.
	@Override
	public double[] getROIsSum()throws DeviceException
	{
		double[] roiSum = new double[actualNumberOfROIs];
		for (int j = 0; j < numberOfMca; j++) {
			double individualMCARois[] = this.getROIs(j);

			for (int i = 0; i < actualNumberOfROIs; i++) {

				roiSum[i] = roiSum[i] + individualMCARois[i];

			}
		}
		return roiSum;
	
	}
	
	/**
	 * @param roiIndex
	 * @return double array of roi count for all mca
	 * @throws DeviceException 
	 */
	@Override
	public double[] getROICounts(int roiIndex) throws DeviceException
	{
		double[] roiCounts = new double[numberOfMca];
		for (int j = 0; j < numberOfMca; j++) {
			double individualMCARois[] = this.getROIs(j);

			roiCounts[j] = individualMCARois[roiIndex];

		}
		return roiCounts;
	}
	/**
	 * @param mcaNumber
	 * @return double array of regions of interest
	 * @throws DeviceException
	 */
	@Override
	public double[] getROIs(int mcaNumber) throws DeviceException {
		try {
			controller.caput(statproc, 1, 0);
			controller.caput(readproc, 1, 0);
		} catch (Throwable th) {
			logger.error("ln682 : failed to get ROI from the XMAP", th);
			throw new DeviceException("ln371: failed to get ROI from the XMAP", th);
		}
		 double roi[][] =  mcaArray[mcaNumber].getRegionsOfInterestCount();
		 double roiCount[] = new double[roi.length];
		 for( int  i = 0 ; i < roiCount.length;i++)
		 {
			 roiCount[i] = roi[i][0];
		 }
		 return roiCount;
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		for (int i = 0; i < numberOfMca; i++) {
			setROI(rois, i);
		}

	}
	/**
	 * @param rois
	 * @param mcaIndex
	 * @throws DeviceException
	 */
	@Override
	public void setROI(double[][] rois, int mcaIndex) throws DeviceException {

		this.setRois(rois, mcaIndex);
		actualNumberOfROIs = rois.length;

	}
	/**
	 * @param rois
	 * @param mcaIndex
	 * @throws DeviceException
	 */
	private void setRois(double[][] rois, int mcaIndex) throws DeviceException {
		EpicsMCARegionOfInterest mcaRois[] = new EpicsMCARegionOfInterest[rois.length];
		for (int i = 0; i < rois.length; i++) {
			mcaRois[i] = new EpicsMCARegionOfInterest(i,
					(rois[i][0] <= rois[i][1]) ? rois[i][0] : rois[i][1],
					(rois[i][0] >= rois[i][1]) ? rois[i][0] : rois[i][1],
					"region" + i);

		}
		mcaArray[mcaIndex].setRegionsOfInterest(mcaRois);
	}
	
	/**
	 * Call to delete regions from an mca channel.
	 * @param mcaIndex
	 * @throws DeviceException 
	 */
	@Override
	public void deleteROIs(final int mcaIndex) throws DeviceException {
		final int regionCount = mcaArray[mcaIndex].getNumberOfRegions();
		for (int i = 0; i < regionCount; i++) {
			// If this throws an exception, may need to carry on loop.
			// Change after testing.
			mcaArray[mcaIndex].deleteRegionOfInterest(i);
		}
	}
	
	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {

		if (rois.length != numberOfMca) {
			logger.error("ROIs length does not match the Number of MCA");
			return;
		}
		for (int mcaIndex = 0; mcaIndex < numberOfMca; mcaIndex++) {
			this.setNthROI(rois[mcaIndex], roiIndex, mcaIndex);
		}
		actualNumberOfROIs = roiIndex + 1;

	}

	/**
	 * @param roi
	 * @param roiIndex
	 * @param mcaIndex
	 * @throws DeviceException
	 */
	private void setNthROI(double[] roi, int roiIndex, int mcaIndex) throws DeviceException {
		
		EpicsMCARegionOfInterest mcaRois[] = new EpicsMCARegionOfInterest[1];
		mcaRois[0] = new EpicsMCARegionOfInterest(roiIndex,
					(roi[0] <= roi[1]) ? roi[0] : roi[1],
					(roi[0] >= roi[1]) ? roi[0] : roi[1],
					"region" + roiIndex);

		mcaArray[mcaIndex].setRegionsOfInterest(mcaRois);
		
	}
	/**
	 * gets the current status update rate
	 * 
	 * @return the current status update rate
	 * @throws DeviceException
	 */
	
	 public String getStatusRate2() throws DeviceException {
		try {
			return controller.caget(statrate);
		} catch (Throwable th) {
			logger.error("failed to get status update rate on {}.", getName());
			throw new DeviceException("failed to get status update rate", th);
		}
	}
	 
		/**
		 * gets the current read update rate from DlsMcsSIS3820.
		 * @return read rate
		 * 
		 * @throws DeviceException
		 */
		
	 public String getReadRate2() throws DeviceException {
		try {
			return controller.caget(readrate);
		} catch (Throwable th) {
			logger.error("failed to get read update rate on {}.", getName());
			throw new DeviceException("failed to get read update rate", th);
		}
		
	 }

	 @Override
	 public int getEvents(int element) throws DeviceException {
		 throw new DeviceException(" getEvents(...) not supported!");
	 }

	@Override
	public double getICR(int element) throws DeviceException {
		throw new DeviceException("Input Count Rate not supported remotely!");
	}

	@Override
	public double getOCR(int element) throws DeviceException {
		throw new DeviceException("Output Count Rate not supported remotely!");
	}

	@Override
	public double[] getROIs(int mcaNumber, int[][] data) throws DeviceException {
		return getROIs(mcaNumber);
	}
	 
}
