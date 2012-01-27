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
import mksAsciiComms.JReadings;
import mksAsciiComms.JScan;
import mksAsciiComms.JSourceSettings;
import mksAsciiComms.JSourceSettingsCollection;
import mksAsciiComms.JTotalPressure;
import mksAsciiComms.rgaFilamentSummary;
import mksAsciiComms.rgaFilterModes;
import mksAsciiComms.rgaMeasurementTypes;

/**
 * The example from the SDK slightly modified to produce meaningful output from
 * a Cirrus simulation running on the same machine.
 * 
 * @author rjw82
 * 
 */
public class CirrusExample implements IEvents {

	private JRGAConnection RGA = null;

	/**
	 * Initialise the connection
	 * 
	 */
	public void Init() {
		RGA = new JRGAConnection(this);
		RGA.Connect("172.23.5.147");
	}

	public static void main(String[] args) {
		// Create an instance of the class
		CirrusExample Connection = new CirrusExample();

		// Now lets Initialise and start using the Sensor.
		Connection.Init();

		// Loop round for ever without hogging the CPU
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

		}
	}

	/**
	 * Fires when a TCP/IP connection is made or fails to the sensor.
	 * 
	 * @param bSuccess
	 */
	@Override
	public void OnConnected(boolean bSuccess) {
		if (bSuccess) {
			JRGASensor Sensor = RGA.getSelectedSensor();
			if (Sensor == null) {
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
				if (RGA.getSensorCount() > 0) {
					RGA.Select(0);
				} else {
					System.out
							.println("There are no sensors to select on the server\n");
				}
			} else {
				System.out.println("The sensor serial#="
						+ Sensor.getSerialNumber() + " and name= "
						+ Sensor.getName());
				System.out.println("Version: " + Sensor.getVersion());
				System.out.println("ConfigurableIonSource: "
						+ (Sensor.isConfigurableIonSource() ? "Yes" : "No"));
				System.out.println("DetectorType: " + Sensor.getDetectorType());
				System.out.println("Number of Electronic Gains: "
						+ Sensor.getEGainCount());

				for (int n = 0; n < Sensor.getEGainCount(); n++) {
					System.out.println("\tEGain[" + n + "] = "
							+ Sensor.getEGain(n));
				}

				System.out.println("FilamentType: " + Sensor.getFilamentType());
				System.out.println("MaxMass: " + Sensor.getMaximumMass());
				System.out.println("PeakResolution: "
						+ Sensor.getPeakResolution());

				JSourceSettingsCollection SrcColl = Sensor.getSourceSettings();

				System.out.println("Number of Source Settings: "
						+ SrcColl.getCount());

				for (int n = 0; n < SrcColl.getCount(); n++) {
					// ISourceSettingsPtr pItem = pSources->Item[n];
					JSourceSettings pItem = SrcColl.getItem(n);

					System.out.println("\tSourceSettings->Item[" + n + "]: '"
							+ pItem.getName() + "'");
					System.out.println("\tElectronEmision: "
							+ pItem.getElectronEmission());
					System.out.println("\tElectronEnergy: "
							+ pItem.getElectronEnergy());
					System.out.println("\tExtractVolts: "
							+ pItem.getExtractVolts());
					System.out.println("\tIonEnergy: " + pItem.getIonEnergy());
					System.out.println("\tLowMassAlignment: "
							+ pItem.getLowMassAlignment());
					System.out.println("\tHighMassAlignment: "
							+ pItem.getHighMassAlignment());
					System.out.println("\tLowMassResolution: "
							+ pItem.getLowMassResolution());
					System.out.println("\tHighMassResolution: "
							+ pItem.getHighMassResolution());
					System.out.println("\tMaxRecommendedPressure: "
							+ pItem.getMaxRecommendedPressure() + " Pascal");
					System.out.println("\tHas " + pItem.getDetectorCount()
							+ " Detector Settings");

					for (short d = 0; d < pItem.getDetectorCount(); d++) {
						// IDetectorSettingsPtr pDetector =
						// pItem->DetectorSettings[d];
						JDetectorSettings pDetector = pItem
								.getDetectorSettings().getItem(d);

						System.out.println("\t\tDetectorSettings[" + d + "]: '"
								+ pDetector.getName() + "'");
						System.out
								.println("\t\t\tDefaultFactor: "
										+ pDetector.getDefaultFactor()
										+ " Amps/Pascal");

						System.out
								.println("\t\t\tCurrent Factors: [Filament1] "
										+ pDetector.getFactor(1)
										+ ", [Filament2] "
										+ pDetector.getFactor(2)
										+ " Amps/Pascal");

						if (d != 0) {
							// Multiplier settings (if fitted)
							System.out.println("\t\t\tDefaultVoltage: "
									+ pDetector.getDefaultVoltage() + " V");
							System.out
									.println("\t\t\tCurrent Voltages: [Filament1] "
											+ pDetector.getVoltage(1)
											+ ", [Filament2] "
											+ pDetector.getVoltage(2) + " V");
						}

						for (short e = 0; e < Sensor.getEGainCount(); e++) {
							if (pDetector.EGainUsable(e)) {
								System.out.println("\t\t\tEGainIndex " + e
										+ " OK up to MaxPressure of "
										+ pDetector.getMaxPressure(e, -1)
										+ " Pascal on the current inlet");
							} else {
								System.out
										.println("\t\t\tEGainIndex "
												+ e
												+ " not available for this source/detector");
							}
						}
					}
				}

				JInletCollection pInlets = Sensor.getInlets();
				System.out.println("Number of Inlets: " + pInlets.getCount());

				for (int n = 0; n < pInlets.getCount(); n++) {
					JInlet pInlet = pInlets.getItem(n);
					System.out.println("\tInlets->Item[" + n + "]: '"
							+ pInlet.getTypeName() + "'");
					System.out.println("\t\tFixed: "
							+ (pInlet.isFixed() ? "Yes" : "No"));
					System.out.println("\t\tCanCalibrate: "
							+ (pInlet.isCanCalibrate() ? "Yes" : "No"));
					System.out.println("\t\tDefaultFactor: "
							+ pInlet.getDefaultFactor());
					System.out.println("\t\tFactor: " + pInlet.getFactor());
				}

				System.out
						.println("--------------------------------------------------\n");

				// Take control of the sensor - only one app can control a
				// sensor at any one time. If an app is already
				// controlling it then this request will fail. The result will
				// be fired in the Controlling() event
				Sensor.Control("Demo Console App", "1.0");
			}

		}

	}

	/**
	 * Fires when the controlling state of the connection changes.
	 * 
	 * @param bInControl
	 */
	@Override
	public void OnControlling(boolean bInControl) {
		System.out.println("Controlling( " + ((bInControl) ? "True" : "False")
				+ ")");

		if (bInControl) {
			// We successfully took control of the unit so now we can create our
			// measurement(s)
			JRGASensor pSensor = RGA.getSelectedSensor();

			// Check the current filament state and turn on if they're not on.
			// ***** ENSURE IT IS SAFE TO TURN THEM ON *****
			System.out.println("The sensor filaments are "
					+ (pSensor.getFilaments().isFilamentOn() ? "On" : "Off")
					+ " and filament "
					+ pSensor.getFilaments().getSelectedFilament()
					+ " is selected");
			if (pSensor.getFilaments().isFilamentOn() == false) {
				System.out.println("Switching filaments on....");
				pSensor.getFilaments().setFilamentOn(true);
			}

			// Create a PeakJump measurement named 'MyMeasurement1', it will use
			// PeakCenter acquisition mode, accuracy level 5.
			// Electronic gain index 1, source settings index 0 and detector
			// settings index 0 (faraday)
			// When the measurement is
			System.out
					.println("Adding a peak jump measurement named 'MyMeasurement1'...");
			pSensor.getMeasurements().AddPeakJump("MyMeasurement1",
					rgaFilterModes.PeakAverage, 5, 1, 0, 0);
		}
	}

	/**
	 * Fires when measurement is fully created or fails to be created
	 * 
	 * @param bSuccess
	 * @param theMeasurement
	 */
	@Override
	public void OnMeasurementCreated(boolean bSuccess,
			JMeasurement theMeasurement) {
		if (bSuccess
				&& (theMeasurement.getName().compareTo("MyMeasurement1") == 0)
				&& theMeasurement.getType() == rgaMeasurementTypes.measurementPeakJump) {
			// The measurement we asked for has been created so now we can add
			// our masses to it
			System.out
					.println("Measurement created OK. Adding masses 18, 28 and 40. Then starting scan...\n");

			JPeakJumpMeasurement pPeakJump = (JPeakJumpMeasurement) theMeasurement;
//			JPeakJumpPeaks peaks = pPeakJump.getMasses();
			pPeakJump.getMasses().AddMass(18);
			pPeakJump.getMasses().AddMass(28);
			pPeakJump.getMasses().AddMass(40);

			// Now add it to the scan and start the thing off

			JRGASensor pSensor = RGA.getSelectedSensor();

			pSensor.getScan().getMeasurements().Add(theMeasurement);

			pSensor.getScan().Start(1); // Asking for just 1 scan. We can ask
										// for more scans to be done
										// automatically by changing the 1 to
										// more

		}
	}

	/**
	 * Fires when a scan is starting
	 * 
	 * @param theScan
	 */
	@Override
	public void OnStartingScan(JScan theScan) {
		System.out.println("Starting scan " + theScan.getNumber()
				+ ", number of scans remaining " + theScan.getRemaining());
		if (theScan.getNumber() < 10 && theScan.getRemaining() == 0) {
			theScan.Resume(1);
		}
	}

	/**
	 * Fires when the end of a scan is reached
	 * 
	 * @param theScan
	 */
	@Override
	public void OnEndOfScan(JScan theScan) {
		System.out.println("OnEndOfScan:");
		System.out.println("End of scan " + theScan.getNumber());

		JReadings pReadings = theScan.getMeasurements().getItem(0).getData();
		System.out
				.println("\tMass 18 = " + pReadings.getReading(0) + " Pascal");
		System.out
				.println("\tMass 28 = " + pReadings.getReading(1) + " Pascal");
		System.out
				.println("\tMass 40 = " + pReadings.getReading(2) + " Pascal");

		if (theScan.getNumber() == 1) {
			System.out.println("Stopping scan. Done!");
			theScan.Stop();
			RGA.getSelectedSensor().setInControl(false);
			System.exit(0);
		}
	}

	/**
	 * Fires when filament settings change
	 * 
	 * @param theFilaments
	 */
	@Override
	public void OnFilamentChange(JFilaments theFilaments) {
		System.out.println("**Filament State Change**");
		System.out.print("\tFilament " + theFilaments.getSelectedFilament()
				+ " is ");
		int filstate = theFilaments.getFilamentSummary();
		switch (filstate) {
		case rgaFilamentSummary.filamentOff:
			System.out.println("Off");
			break;
		case rgaFilamentSummary.filamentWarmUp:
			System.out.println("Warming up");
			break;
		case rgaFilamentSummary.filamentOn:
			System.out.println("Off");
			break;
		case rgaFilamentSummary.filamentCoolDown:
			System.out.println("Cooling down");
			break;
		case rgaFilamentSummary.filamentBadEmission:
			System.out.println("Bad Emission");

		}
	}

	@Override
	public void OnError(JError err) {
		System.out.println("An Error occured - Description is "
				+ err.getErrorDescription());
	}

	@Override
	public void OnFilamentOnTime(JFilaments theFilaments) {
		System.out.println("OnFilamentOnTime");
	}

	@Override
	public void OnDigitalInputChange(JDigitalPort thePort) {
		System.out.println("OnDigitalInputChange");
	}

	@Override
	public void OnInletChange(JInlet ActiveInlet) {
		System.out.println("OnInletChange");
	}

	@Override
	public void OnLinkDown(int Reason) {
		System.out.println("OnLinkDown");
	}

	@Override
	public void OnAnalogInputReading(JAnalogInput theInput) {
		System.out.println("OnAnalogInputReading");
	}

	@Override
	public void OnTotalPressureReading(JTotalPressure theTotalPressure) {
		System.out.println("OnTotalPressureReading");
	}

	@Override
	public void OnRFTripState(JRF theRF) {
		System.out.println("OnRFTripState");
	}

	@Override
	public void OnMultiplierState(JMultiplier theMultiplier) {
		System.out.println("OnMultiplierState");
	}

	@Override
	public void OnCirrusState(JCirrus theCirrus) {
		System.out.println("OnCirrusState");
		// System.out.println(theCirrus.toString());
	}

	@Override
	public void OnRVCPumpState(JRVC theRVC) {
		System.out.println("OnRVCPumpState");
	}

	@Override
	public void OnRVCHeaterState(JRVC theRVC) {
		System.out.println("OnRVCHeaterState");
	}

	@Override
	public void OnRVCValveState(JRVC theRVC) {
		System.out.println("OnRVCValveState");
	}

	@Override
	public void OnRVCInterlocksState(JRVC theRVC) {
		System.out.println("OnRVCValveState");
	}

	@Override
	public void OnRVCStatus(JRVC theRVC) {
		System.out.println("OnRVCStatus");
	}

	@Override
	public void OnRVCDigitalInputState(JRVC theRVC) {
		System.out.println("OnRVCDigitalInputState");
	}

	@Override
	public void OnDiagnosticsComplete(JDiagnostics diags) {
		System.out.println("OnDiagnosticsComplete");
	}

	@Override
	public void OnDegasReading(JDegas degas) {
		System.out.println("OnDegasReading");
	}
}
