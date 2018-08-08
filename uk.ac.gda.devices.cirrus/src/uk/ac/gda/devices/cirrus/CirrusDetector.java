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

import gda.data.nexus.extractor.NexusGroupData;
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

/**
 * Operates the Cirrus mass spectrometer in scans as a detector.
 */
public class CirrusDetector extends DetectorBase implements NexusDetector, Cirrus {

	private CirrusController controller = new CirrusController();
	private String cirrusHost;
	private Integer[] masses = new Integer[] {};

	public static void main(String[] args) {
		try {
			CirrusDetector det = new CirrusDetector();
			det.setCirrusHost("192.168.0.250");
			Integer[] masses = new Integer[]{18,28,32};
			det.setMasses(masses);
			det.setFilamentToUse(2);
			det.setCapillaryHeaterOn(true);
//			det.setCirrusHeaterOn(true);
			det.setCirrusHeaterOn(false);
			System.out.println("Configuring system...");
			det.configure();

			det.collectData();

			while(det.isBusy()){
				System.out.println("Detector is busy...");
				Thread.sleep(50);
			}
			System.out.println("Capillary heater on?" + det.isCapillaryHeaterOn());
			System.out.println("System heater on?" + det.isCirrusHeaterOn());
			System.out.println("Chamber pressure:" + det.getChamberPressure());
			System.out.println("Filament:" + det.getFilamentToUse());

			System.out.println("Measured values:");
			Double[] results = ((NXDetectorData)det.readout()).getDoubleVals();
			for (int i = 0; i < results.length; i++){
				System.out.println("Mass: " + masses[i] + " -> " + String.format("%.2f",results[i]) + " mbar");
			}

			System.out.println("Test of Cirrus complete!");
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
	public void atCommandFailure() throws DeviceException {
		getController().getCurrentState().setStatus(ScannableStatus.BUSY);
	}

	@Override
	public int getStatus() throws DeviceException {
		if (getController() == null || getController().rgaConnection == null || getController().rgaConnection.getSensorCount() == 0) {
			return Detector.STANDBY;
		}
		ScannableStatus currentStatus = getController().getCurrentState().getStatus();
		if (currentStatus == ScannableStatus.BUSY) {
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
		double[][] dblFullResults = new double[pReadings.getNumReadingsAvailable()][2];

		for (int i = 0; i < pReadings.getNumReadingsAvailable(); i++) {
			double mass = this.getMasses()[i];
			double counts = pReadings.getReading(i);
			counts /= 100;  // to convert from Pa to mbar (1Pa = 10E-5bar = 0.01mbar)
			nxdata.setPlottableValue(getExtraNames()[i], counts);
			dblFullResults[i][1] = counts;
			dblFullResults[i][0] = mass;
		}
		nxdata.addData(getName(), "masses", new NexusGroupData(dblFullResults), "mbar", 1);

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

	@Override
	public String[] getOutputFormat() {
		String[] extranames = new String[masses.length];

		for (int i = 0 ; i < masses.length; i++){
			extranames[i] = "%.4f";
		}

		return extranames;
	}

	public float getChamberPressure(){
		return getController().getPressure();
	}

	public void turnOffHardware() throws DeviceException{
		controller.turnOffHardware();
	}

	public CirrusController getController() {
		return controller;
	}

	public String getCirrusHost() {
		return cirrusHost;
	}

	public void setCirrusHost(String cirrusHost) {
		this.cirrusHost = cirrusHost;
	}

	@Override
	public Integer[] getMasses() {
		return masses;
	}

	@Override
	public void setMasses(Integer[] masses) {
		this.masses = masses;
	}

	public int getFilamentToUse() {
		return controller.getFilamentToUse();
	}

	public void setFilamentToUse(int filamentToUse) {
		controller.setFilamentToUse(filamentToUse);
	}

	public boolean isCapillaryHeaterOn() {
		return controller.isCapillaryHeaterOn();
	}

	public void setCapillaryHeaterOn(boolean capillaryHeaterOn) {
		controller.setCapillaryHeaterOn(capillaryHeaterOn);
	}

	public boolean isCirrusHeaterOn() {
		return controller.isCirrusHeaterOn();
	}

	public void setCirrusHeaterOn(boolean cirrusHeaterOn) {
		controller.setCirrusHeaterOn(cirrusHeaterOn);
	}

}
