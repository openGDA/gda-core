/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.cirrus;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.scannable.ScannableStatus;
import gda.factory.FactoryException;
import mksAsciiComms.JMeasurement;
import mksAsciiComms.JReadings;

import org.nexusformat.NexusFile;

/**
 * Operates the Cirrus mass spectrometer in scans as a detector.
 */
public class CirrusDetector extends DetectorBase implements NexusDetector {

	private CirrusController controller;
	private String cirrusHost;
	private Integer[] masses = new Integer[] {};
	
	public static void main(String[] args) {
		try {
			CirrusDetector det = new CirrusDetector();
			det.setCirrusHost("172.23.5.147");
			det.setMasses(new Integer[]{18,28,40});
			det.configure();

			det.collectData();
			
			while(det.isBusy()){
				System.out.println("Detector is busy...");
				Thread.sleep(50);
			}
			
			System.out.println(det.readout().toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
		
	}
	
	public CirrusDetector() {
		setInputNames(null);
	}
	
	@Override
	public void configure() throws FactoryException {
		getController().connect(cirrusHost);
	}
	
	@Override
	public void reconfigure() throws FactoryException {
		getController().disconnect();
		getController().connect(cirrusHost);
	}

	@Override
	public void collectData() throws DeviceException {
		// create a measurement and set the masses
		if (masses.length == 0) {
			throw new DeviceException("No masses defined - cannot take a measurement.");
		}

		// add the measurement to the scan and start it
		getController().createAndRunScan(masses);
	}

	@Override
	public int getStatus() throws DeviceException {
		if (getController() == null || getController().rgaConnection == null || getController().rgaConnection.getSensorCount() == 0) {
			return Detector.STANDBY;
		}
		int currentState = getController().getCurrentState().getStatus().status;
		if (currentState == ScannableStatus.BUSY) {
			return Detector.BUSY;
		}
		return Detector.IDLE;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return getController().rgaConnection.getSelectedSensor().getName();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getController().rgaConnection.getSelectedSensor().getSerialNumber();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Mass spectrometer";
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {

		JMeasurement measurements = getController().getCurrentState().getLastMeasurement();

		if (measurements == null) {
			// scan failed or not finished yet
			return null;
		}

		JReadings pReadings = measurements.getData();

		NXDetectorData nxdata = new NXDetectorData(this);
		INexusTree detTree = nxdata.getDetTree(getName());
		double[] results = new double[pReadings.getNumReadingsAvailable()];
		double[][] dblFullResults = new double[pReadings.getNumReadingsAvailable()][2];

		for (int i = 0; i < pReadings.getNumReadingsAvailable(); i++) {
			double mass = this.getMasses()[i];
			double counts = pReadings.getReading(i);
			nxdata.setPlottableValue(getExtraNames()[i], counts);
			dblFullResults[i][1] = counts;
			dblFullResults[i][0] = mass;
		}
		nxdata.addData(detTree, "masses", new int[] { results.length, 2 }, NexusFile.NX_FLOAT64, dblFullResults,
				"counts", 1);
		
		return nxdata;

	}
	
	@Override
	public String[] getExtraNames() {
		
		// dynamic, based on number of masses
		String[] extranames = new String[masses.length];
		
		for (int i = 0 ; i < masses.length; i++){
			extranames[i] = getName() + "_" + Integer.toString(masses[i]);
		}
		
		return extranames;
	}

	public CirrusController getController() {
		if (controller == null){
			controller = new CirrusController();
		}
		return controller;
	}

	public String getCirrusHost() {
		return cirrusHost;
	}

	public void setCirrusHost(String cirrusHost) {
		this.cirrusHost = cirrusHost;
	}

	public Integer[] getMasses() {
		return masses;
	}

	public void setMasses(Integer[] masses) {
		this.masses = masses;
	}

}
