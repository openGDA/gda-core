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

package uk.ac.gda.devices.cirrus;

import gda.device.DeviceException;
import gda.device.scannable.ScannableStatus;
import gda.factory.FactoryException;
import gda.util.Version;
import mksAsciiComms.IEvents;
import mksAsciiComms.JAnalogInput;
import mksAsciiComms.JCirrus;
import mksAsciiComms.JDegas;
import mksAsciiComms.JDetectorSettings;
import mksAsciiComms.JDiagnostics;
import mksAsciiComms.JDigitalPort;
import mksAsciiComms.JError;
import mksAsciiComms.JFilaments;
import mksAsciiComms.JInlet;
import mksAsciiComms.JInletCollection;
import mksAsciiComms.JMeasurement;
import mksAsciiComms.JMultiplier;
import mksAsciiComms.JPeakJumpMeasurement;
import mksAsciiComms.JRF;
import mksAsciiComms.JRGAConnection;
import mksAsciiComms.JRGASensor;
import mksAsciiComms.JRVC;
import mksAsciiComms.JScan;
import mksAsciiComms.JSourceSettings;
import mksAsciiComms.JSourceSettingsCollection;
import mksAsciiComms.JTotalPressure;
import mksAsciiComms.rgaFilterModes;

import org.slf4j.LoggerFactory;

/**
 * Uses the SDK directly and holds the current state returned through the event-driven methods.
 * 
 * @author rjw82
 */
public class CirrusController implements IEvents {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CirrusController.class);

	protected JRGAConnection rgaConnection;
	private volatile CirrusState currentState;
	// flag to tell the OnMeasurementCreated method to start data collection
	private volatile Boolean startWhenMeasurementCreated = false;
	private volatile Integer[] masses = new Integer[] {};
	private String name = "Cirrus";

	public CirrusController() {
		setCurrentState(new CirrusState());
	}

	public void connect(String host) throws FactoryException {
		currentState.setControlResponseRecieved(false);
		currentState.setHasControl(null);
		rgaConnection = new JRGAConnection(this);
		rgaConnection.Connect(host);

		long timeout = 10000; // 10s timeout in milliseconds
		long timeWaiting = 0;

		try {
			while (currentState.getControlResponseRecieved() == false && timeWaiting < timeout) {
				Thread.sleep(50);
				timeWaiting += 50;
			}
		} catch (InterruptedException e) {
			// ignore as state handled after this
		}

		if (timeWaiting >= timeout || currentState.getControlResponseRecieved() == false) {
			throw new FactoryException("Timeout while waiting for Cirrus connection");
		}

		if (!currentState.getHasControl()) {
			throw new FactoryException("Control of Cirrus denied");
		}

	}

	public void disconnect() {
		if (rgaConnection != null && getCurrentState().getHasControl()){
			rgaConnection.getSelectedSensor().setInControl(false);
			rgaConnection = null;
		}
	}

	public void createAndRunScan(Integer[] masses) throws DeviceException {

		checkCanControlHardware();

		currentState.setStatus(new ScannableStatus(name, ScannableStatus.BUSY));
		rgaConnection.getSelectedSensor().getMeasurements().RemoveAll();
		startWhenMeasurementCreated = true;
		createMeasurement(masses);
	}

	private void checkCanControlHardware() throws DeviceException {

		if (rgaConnection == null || !getCurrentState().getIsConnected()) {
			throw new DeviceException("Connection has not been established with Cirrus hardware. Try to reconnect.");
		}
		if (!getCurrentState().getHasControl()) {
			throw new DeviceException(
					"Connection with Cirrus hardware does not have control of the hardware. Try to reconnect.");
		}

	}

	public void createMeasurement(Integer[] masses) throws DeviceException {

		checkCanControlHardware();

		JRGASensor pSensor = rgaConnection.getSelectedSensor();

		if (pSensor.getFilaments().isFilamentOn() == false) {
			logger.info("Switching " + name + " filaments on....");
			pSensor.getFilaments().setFilamentOn(true);
		}

		this.masses = masses;
		logger.info("new measurement defined in " + name);
		pSensor.getMeasurements().AddPeakJump("GDA_measurement", rgaFilterModes.PeakAverage, 0, 0, 0, 0);
	}

	public void stop() throws DeviceException {

		checkCanControlHardware();

		logger.info("Stop requested for current scan in " + name);
		rgaConnection.getSelectedSensor().getScan().Stop();
	}

	@Override
	public void OnAnalogInputReading(JAnalogInput arg0) {
		// messages from Cirrus constantly being sent to this method
		currentState.setLastAnalogInputReading(arg0);
	}

	@Override
	public void OnCirrusState(JCirrus arg0) {
		// messages from Cirrus constantly being sent to this method
		currentState.setLastState(arg0);
	}

	@Override
	public void OnConnected(boolean arg0) {
		currentState.setControlResponseRecieved(true);
		currentState.setIsConnected(arg0);
		if (arg0) {
			JRGASensor sensor = rgaConnection.getSelectedSensor();
			if (sensor == null) {
				// If we connect to the 'server' and SelectedSensor is NULL it
				// means we have connected to a windows server that can
				// provide access to many old RS232 sensors. In this case we
				// must select one and we will get a second Connected event
				// fired. This code was developed with a windows server so in
				// order to access the sensor i assume i want the first
				// available head. Production code should decide what unit it
				// wants to select by looking through the
				// SensorSerialNo[] and SensorName[] collections where valid
				// indexes are 0 to SensorCount.
				if (rgaConnection.getSensorCount() > 0) {
					rgaConnection.Select(0);
				} else {
					currentState.setStatus(new ScannableStatus(name, ScannableStatus.FAULT));
					currentState.setStatusString("No available sensor on Cirrus server");
				}
			} else {
				logger.info("The sensor serial#=" + sensor.getSerialNumber() + " and name= " + sensor.getName());
				logger.info("Version: " + sensor.getVersion());
				logger.info("ConfigurableIonSource: " + (sensor.isConfigurableIonSource() ? "Yes" : "No"));
				logger.info("DetectorType: " + sensor.getDetectorType());
				logger.info("Number of Electronic Gains: " + sensor.getEGainCount());

				for (int n = 0; n < sensor.getEGainCount(); n++) {
					logger.info("\tEGain[" + n + "] = " + sensor.getEGain(n));
				}

				logger.info("FilamentType: " + sensor.getFilamentType());
				logger.info("MaxMass: " + sensor.getMaximumMass());
				logger.info("PeakResolution: " + sensor.getPeakResolution());

				JSourceSettingsCollection SrcColl = sensor.getSourceSettings();

				logger.info("Number of Source Settings: " + SrcColl.getCount());

				for (int n = 0; n < SrcColl.getCount(); n++) {
					// ISourceSettingsPtr pItem = pSources->Item[n];
					JSourceSettings pItem = SrcColl.getItem(n);

					logger.info("\tSourceSettings->Item[" + n + "]: '" + pItem.getName() + "'");
					logger.info("\tElectronEmision: " + pItem.getElectronEmission());
					logger.info("\tElectronEnergy: " + pItem.getElectronEnergy());
					logger.info("\tExtractVolts: " + pItem.getExtractVolts());
					logger.info("\tIonEnergy: " + pItem.getIonEnergy());
					logger.info("\tLowMassAlignment: " + pItem.getLowMassAlignment());
					logger.info("\tHighMassAlignment: " + pItem.getHighMassAlignment());
					logger.info("\tLowMassResolution: " + pItem.getLowMassResolution());
					logger.info("\tHighMassResolution: " + pItem.getHighMassResolution());
					logger.info("\tMaxRecommendedPressure: " + pItem.getMaxRecommendedPressure() + " Pascal");
					logger.info("\tHas " + pItem.getDetectorCount() + " Detector Settings");

					for (short d = 0; d < pItem.getDetectorCount(); d++) {
						// IDetectorSettingsPtr pDetector =
						// pItem->DetectorSettings[d];
						JDetectorSettings pDetector = pItem.getDetectorSettings().getItem(d);

						logger.info("\t\tDetectorSettings[" + d + "]: '" + pDetector.getName() + "'");
						logger.info("\t\t\tDefaultFactor: " + pDetector.getDefaultFactor() + " Amps/Pascal");

						logger.info("\t\t\tCurrent Factors: [Filament1] " + pDetector.getFactor(1) + ", [Filament2] "
								+ pDetector.getFactor(2) + " Amps/Pascal");

						if (d != 0) {
							// Multiplier settings (if fitted)
							logger.info("\t\t\tDefaultVoltage: " + pDetector.getDefaultVoltage() + " V");
							logger.info("\t\t\tCurrent Voltages: [Filament1] " + pDetector.getVoltage(1)
									+ ", [Filament2] " + pDetector.getVoltage(2) + " V");
						}

						for (short e = 0; e < sensor.getEGainCount(); e++) {
							if (pDetector.EGainUsable(e)) {
								logger.info("\t\t\tEGainIndex " + e + " OK up to MaxPressure of "
										+ pDetector.getMaxPressure(e, -1) + " Pascal on the current inlet");
							} else {
								logger.info("\t\t\tEGainIndex " + e + " not available for this source/detector");
							}
						}
					}
				}

				JInletCollection pInlets = sensor.getInlets();
				logger.info("Number of Inlets: " + pInlets.getCount());

				for (int n = 0; n < pInlets.getCount(); n++) {
					JInlet pInlet = pInlets.getItem(n);
					logger.info("\tInlets->Item[" + n + "]: '" + pInlet.getTypeName() + "'");
					logger.info("\t\tFixed: " + (pInlet.isFixed() ? "Yes" : "No"));
					logger.info("\t\tCanCalibrate: " + (pInlet.isCanCalibrate() ? "Yes" : "No"));
					logger.info("\t\tDefaultFactor: " + pInlet.getDefaultFactor());
					logger.info("\t\tFactor: " + pInlet.getFactor());
				}

				// Take control of the sensor - only one app can control a
				// sensor at any one time. If an app is already
				// controlling it then this request will fail. The result will
				// be fired in the Controlling() event

				// this will be updated by a call to OnControlling
				currentState.setHasControl(false);

				sensor.Control("GDA", Version.getReleaseVersion());

			}

		}

	}

	@Override
	public void OnControlling(boolean arg0) {
		currentState.setHasControl(arg0);
	}

	@Override
	public void OnDegasReading(JDegas arg0) {
		currentState.setLastDegasReading(arg0);
	}

	@Override
	public void OnDiagnosticsComplete(JDiagnostics arg0) {
		currentState.setLastDiagnosticsReading(arg0);
	}

	@Override
	public void OnDigitalInputChange(JDigitalPort arg0) {
		currentState.setLastDigitalPortState(arg0);
	}

	@Override
	public void OnEndOfScan(JScan arg0) {
		currentState.setLastMeasurement(arg0.getMeasurements().getItem(0));
		currentState.setRunningJScan(null);
		currentState.setScanStarted(false);
		arg0.Stop(); // must do this or OnStartingScan events continue to be sent
		currentState.setStatus(new ScannableStatus(name, ScannableStatus.IDLE));
	}

	@Override
	public void OnError(JError arg0) {
		currentState.setStatus(new ScannableStatus(name, ScannableStatus.FAULT));
		currentState.setStatusString("Error in " + name + ". Reason: " + arg0.getErrorDescription());
	}

	@Override
	public void OnFilamentChange(JFilaments arg0) {
		currentState.setLastFilamentState(arg0);
	}

	@Override
	public void OnFilamentOnTime(JFilaments arg0) {
		currentState.setLastFilamentState(arg0);
	}

	@Override
	public void OnInletChange(JInlet arg0) {
		currentState.setLastInletState(arg0);
	}

	@Override
	public void OnLinkDown(int arg0) {
		currentState.setHasControl(false);
		currentState.setIsConnected(false);
		currentState.setStatus(new ScannableStatus(name, ScannableStatus.FAULT));
		currentState.setStatusString("Link to " + name + " lost. Reason number " + arg0);
	}

	@Override
	public void OnMeasurementCreated(boolean success, JMeasurement arg1) {
		if (!success) {
			currentState.setStatus(new ScannableStatus(name, ScannableStatus.FAULT));
			currentState.setStatusString("Last attempt to define a measurement failed");
		}

		if (success && startWhenMeasurementCreated) {

			// assume that the created measurement is what we just sent

			JPeakJumpMeasurement measurement = (JPeakJumpMeasurement) arg1;

			for (Integer mass : masses) {
				measurement.getMasses().AddMass(mass);
			}

			// Now add it to the scan and start the thing off

			JRGASensor sensor = this.rgaConnection.getSelectedSensor();

			sensor.getScan().getMeasurements().Add(measurement);

			currentState.setStatus(new ScannableStatus(name, ScannableStatus.BUSY));
			currentState.setStatusString("");

			logger.info("scan started in " + name);
			sensor.getScan().Start(1);
		}
	}

	@Override
	public void OnMultiplierState(JMultiplier arg0) {
		currentState.setLastMultiplierState(arg0);
	}

	@Override
	public void OnRFTripState(JRF arg0) {
		currentState.setLastRFTripState(arg0);
	}

	@Override
	public void OnRVCDigitalInputState(JRVC arg0) {
		currentState.setLastRVCState(arg0);
	}

	@Override
	public void OnRVCHeaterState(JRVC arg0) {
		currentState.setLastRVCState(arg0);
	}

	@Override
	public void OnRVCInterlocksState(JRVC arg0) {
		currentState.setLastRVCState(arg0);
	}

	@Override
	public void OnRVCPumpState(JRVC arg0) {
		currentState.setLastRVCState(arg0);
	}

	@Override
	public void OnRVCStatus(JRVC arg0) {
		currentState.setLastRVCState(arg0);
	}

	@Override
	public void OnRVCValveState(JRVC arg0) {
		currentState.setLastRVCState(arg0);
	}

	@Override
	public void OnStartingScan(JScan arg0) {
		if (startWhenMeasurementCreated) {
			startWhenMeasurementCreated = false;

			currentState.setLastMeasurement(null);
			currentState.setScanStarted(true);
			currentState.setRunningJScan(arg0);
			arg0.Resume(1); // must do this to give permission for scan to start (at a guess there is a hardware trigger
							// which could replicate this?)
		}
	}

	@Override
	public void OnTotalPressureReading(JTotalPressure arg0) {
		currentState.setLastTotalPressureReading(arg0);
	}

	public void setCurrentState(CirrusState currentState) {
		this.currentState = currentState;
	}

	public CirrusState getCurrentState() {
		return currentState;
	}
}
