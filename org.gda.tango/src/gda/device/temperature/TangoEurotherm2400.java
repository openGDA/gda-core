/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.temperature;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceException;
import gda.device.Temperature;
import gda.device.TangoDeviceProxy;
import gda.device.TemperatureRamp;
import gda.device.TemperatureStatus;
import gda.factory.FactoryException;
import gda.util.PollerEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class TangoEurotherm2400 extends TemperatureBase implements Temperature, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(TangoEurotherm2400.class);
	private final static double MAXTEMP = 900.0;
	private final static double MINTEMP = -35.0;
	private TangoDeviceProxy tangoDeviceProxy;
	private double startTime = 0;
	private int scale;
	private short maxSegments;
	private short maxPrograms;
	protected volatile boolean activeProgram = false;	

	private static short CURRENT_SEGMENT_NUM = 56;
	private static short CURRENT_SEGMENT_TYPE = 29;
	private static short CURRENT_PROGRAM = 22;
	private static short PROGRAM_STATUS = 23;
	private static short SETPOINT_MAXIMUM = 111;	
	private static short SETPOINT_MINIMUM = 112;
	private static short INSTRUMENT_IDENTITY = 122;
	private static short SCALE_FACTOR = 525;
	private static short MAXIMUM_SEGMENTS = 211;
	private static short MAXIMUM_PROGRAMS = 517;
	private static short STATUS = 75;
	private static short SETPOINT = 2;

	private short HOLD = 4;
	private short RUN = 2;
	private short RESET = 1;
			
	public TangoEurotherm2400() {
		// These will be overwritten by the values specified in the XML
		// but are given here as defaults.
		lowerTemp = MINTEMP;
		upperTemp = MAXTEMP;
	}

	@Override
	public void configure() throws FactoryException {
//		if (!configured) {
		super.configure();
			try {
				getInstrumentIdentity();
				getScaleFactor();
				getMaximumSegments();
				getMaximumPrograms();
				lowerTemp = getSetPointMinimum();
				upperTemp = getSetPointMaximum();
				getCurrentTemperature();
				startPoller();
				configured = true;
			} catch (DeviceException e) {
				logger.error(e.getMessage());
			}
//		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!configured)
			configure();
	}

	@Override
	public void close() throws DeviceException {
		stopPoller();
		poller = null;
		probeNameList.clear();
		configured = false;
	}

	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy dev) {
		this.tangoDeviceProxy = dev;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoDeviceProxy == null) {
			throw new IllegalArgumentException("tango device proxy needs to be set");
		}
	}

	/**
	 * Get the Eurotherm Controller identifier.
	 * 
	 * @returns the identifier in format >ABCD (hex),
	 *     A = 2 (series 2000) B = Range number  C = Size       D = Type
	 *                             2: 2200           3: 1/32 din    0: PID/on-off
	 *                             4: 2400           6: 1/16 din    2: VP
	 *                                               8: 1/8 din
	 *                                               4: ¼ din
	 */
	private void getInstrumentIdentity() throws DeviceException {
		try {
			short ident = getModbusValue(INSTRUMENT_IDENTITY);
			System.out.println("Eurotherm identity as short is " + ident);
			String identString = String.format("%x", ident);
			logger.info("Eurotherm identity is " + identString);
			if (identString.charAt(0) != '2' & identString.charAt(1) != '4') {
				DeviceException ex = new DeviceException("This is not an Eurotherm 2000 series.");
				logger.error("This is not an Eurotherm 2000 series.");
				throw ex;
			}
			probeNameList.add(identString);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to read eurotherm identity: " + ex.getMessage());
			throw ex;
		}
	}

	private void getScaleFactor() throws DeviceException {
		try {
			scale = (int) Math.pow(10, getModbusValue(SCALE_FACTOR));
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to read eurotherm identity: " + ex.getMessage());
			throw ex;
		}
	}

	private void getMaximumSegments() throws DeviceException {
		try {
			maxSegments = getModbusValue(MAXIMUM_SEGMENTS);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get maximum segments: " + ex.getMessage());
			throw ex;
		}
	}

	private void getMaximumPrograms() throws DeviceException {
		try {
			maxPrograms = getModbusValue(MAXIMUM_PROGRAMS);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get maximum programs: " + ex.getMessage());
			throw ex;
		}
	}

	@Override
	public double getCurrentTemperature() throws DeviceException {
		try {
			currentTemp = tangoDeviceProxy.getAttributeAsDouble("Temperature");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to read eurotherm temperature: " + ex.getMessage());
			throw ex;
		}
		return currentTemp;
	}

	/**
	 * @return the current status
	 * @throws DeviceException
	 */
	public short getStatus() throws DeviceException {
		short status;
		try {
			status = getModbusValue(STATUS);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to read eurotherm temperature: " + ex.getMessage());
			throw ex;
		}
		logger.debug("getStatus status is " + status);
		return status;
	}

	@Override
	public void pollDone(PollerEvent pe) {
		String stateString = null;
		String dataString = null;

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		logger.debug("Eurotherm pollDone called");

		try {
			currentTemp = getCurrentTemperature();
			if (busy && currentRamp == -1 && isAtTargetTemperature()) {
				busy = false;
//				startHoldTimer();
			} else if (busy && activeProgram && getProgramStatus() == RESET) {
				busy = false;
				activeProgram = false;
			}
			if (busy) {
				if (getProgramStatus() == RUN) {
					stateString = "Run: Program " + getCurrentProgram() + " Segment " + getCurrentSegment();
				} else {
					stateString = (targetTemp > currentTemp) ? "Heating" : "Cooling";
				}
//			} else if (currentRamp > -1) {
//				stateString = "At temperature";
			} else {
				stateString = "Idle";
			}
		} catch (DeviceException de) {
			logger.error("Exception " + de.getMessage());
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}
		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;
		TemperatureStatus ts = new TemperatureStatus(currentTemp, currentRamp, stateString, dataString);
		logger.debug("Eurotherm notifying IObservers with " + ts);

		double data[] = new double[2];
		data[0] = currentTemp;
		data[1] = timeSinceStart / 1000.0;
		if (data[1] >= 0.0) {
			System.out.println("current temp " + data[0] + " time since start " + data[1]);
			bufferedData.add(data);
		}

		if (isBeingObserved()) {
			notifyIObservers(this, ts);
		}
	}

	@Override
	public boolean isAtTargetTemperature() throws DeviceException {
		// Wait for the controller to reach its setPoint.
		// It requires 5 consecutive readings to be +/- accuracy to minimise errors
		// caused be overheat or overcool.

		currentTemp = getCurrentTemperature();
		double diff = targetTemp - currentTemp;

		if (Math.abs(diff) <= getAccuracy() && busy)
			count++;
		else
			count = 0;

		return (count >= 5);
	}

	@Override
	protected void sendRamp(int ramp) throws DeviceException {
		logger.debug("Not used in this implementation but required by abstract class");
	}

	@Override
	public void hold() throws DeviceException {
		try {
			setModbusValue(PROGRAM_STATUS, HOLD);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to hold eurotherm: " + ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Tells the hardware to start heating or cooling
	 * 
	 * @throws DeviceException
	 */
	private void sendStart() throws DeviceException {

		if (busy) {
			throw new DeviceException("Eurotherm is already ramping to temperature");
		}
		try {
			setModbusValue(PROGRAM_STATUS, RUN);
			busy = true;
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to start eurotherm: " + ex.getMessage());
			throw ex;
		}
	}

	@Override
	protected void doStart() throws DeviceException {
		Date d = new Date();
		startTime = d.getTime();
		currentRamp = -1;
		sendStart();
	}

	@Override
	protected void startNextRamp() throws DeviceException {
		activeProgram = true;
		sendStart();
		//		currentRamp++;
//		logger.debug("startNextRamp called currentRamp now " + currentRamp);
//		if (currentRamp < rampList.size()) {
//			sendRamp(currentRamp);
//			sendStart();
//		} else {
//			stop();
//		}
	}

	@Override
	protected void doStop() throws DeviceException {
		short[] args = new short[2];
		DeviceData argin;
		try {
			args[0] = 15; // Select setpoint
			args[1] = 0;
			argin = new DeviceData();
			argin.insert(args);
			tangoDeviceProxy.command_inout("PresetSingleRegister", argin);

			setModbusValue(PROGRAM_STATUS, RESET);
			busy = false;

		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to stop eurotherm: " + ex.getMessage());
			throw ex;
		}
	}

	@Override
	public Object getAttribute(String name) {
		if (name.equalsIgnoreCase("NeedsCooler")) {
			return Boolean.FALSE;
		} else if (name.equalsIgnoreCase("NeedsCoolerSpeedSetting")){
			return Boolean.FALSE;
		} else {
			return null;
		}
	}

	@Override
	protected void startTowardsTarget() throws DeviceException {
		try {
			setModbusValue(SETPOINT, (short)(targetTemp*scale));
			busy = true;
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to stop eurotherm: " + ex.getMessage());
			throw ex;
		}
	}

	public void startTowardsTarget(double targetTemp) throws DeviceException {
		this.targetTemp = targetTemp;
		startTowardsTarget();
	}
	
	@Override
	protected void setHWLowerTemp(double lowerTemp) throws DeviceException {		
	}

	@Override
	protected void setHWUpperTemp(double upperTemp) throws DeviceException {
	}

	@Override
	public void runRamp() throws DeviceException {
	}

	public ArrayList<double[]> getBufferedData() {
		return bufferedData;
	}

	@Override
	public Object readout() {
		return bufferedData;
	}

	/**
	 * Sets the array of ramps.
	 * 
	 * @param ramps an ArrayList<TemperatureRamp> of ramps to be set
	 */
	@Override
	public void setRamps(ArrayList<TemperatureRamp> ramps) {
		rampList = ramps;
		short[] modbusBuffer = new short[17*8];
		
		modbusBuffer[0] = 0; // no hold back
		modbusBuffer[2] = 1; // ramp unit to minutes
		modbusBuffer[3] = 1; // dwell units to minutes
		modbusBuffer[4] = 1; // program cycles
		int segment = 1;
		int j;
//		TemperatureRamp lastRamp=null;
//		double startTemp;
		for (TemperatureRamp ramp : ramps) {
			j = segment * 8;
			// rate segment
			modbusBuffer[j] = 1;
			modbusBuffer[j+1] = (short) (ramp.getEndTemperature()*scale);
			modbusBuffer[j+2] = (short) (ramp.getRate()*scale);
			// time segment
//			modbusBuffer[j] = 2;
//			modbusBuffer[j+1] = (short) (ramp.getEndTemperature()*scale);
//			startTemp = (segment == 1) ? currentTemp : lastRamp.getEndTemperature();
//			modbusBuffer[j+2] = (short) (Math.abs(ramp.getEndTemperature() - startTemp)*60*scale/(ramp.getRate()));
			
			segment++;
			short dwell = (short)(ramp.getDwellTime()*1);
			if (dwell > 0) {
				j = segment * 8;
				// dwell segment
				modbusBuffer[j] = 3;
				modbusBuffer[j+2] = dwell;
				segment++;
			}
//			lastRamp = ramp;
		}
		// end segment
		j = segment * 8;
		modbusBuffer[j] = 0;
		modbusBuffer[j+3] = 1;
		try {
			writeProgram(modbusBuffer, 1, segment);
		} catch (DeviceException e) {
			logger.error("Unable to write eurotherm program", e);
		}
	}

	/**
	 * Address calculation is base address 8192 plus number of program (skip 0) times 17*8 writing a progwriting a
	 * program has to be done in a selective manner. One can`t just write a block of modbus addresses. Each segment type
	 * has arguments, which can be written over.
	 * 
	 * @param modbusBuffer
	 * @param programNumber
	 * @param numSegments
	 * @throws DeviceException
	 */
	public void writeProgram(short[] modbusBuffer, int programNumber, int numSegments) throws DeviceException {
		short[] args = new short[2];
		DeviceData argin;
		short programBase = 8192;
		if (programNumber > maxPrograms) {
			logger.error("Invalid program number:");
			return;
		}
			
		short baseOffset = (short) (programBase + (programNumber * 17 * 8));
		try {
			numSegments = (numSegments > maxSegments) ? maxSegments : numSegments;

			args[0] = baseOffset; 
			args[1] = modbusBuffer[0]; // Holdback type (0:OFF, 1:Low, 2:High, 3:Band)
			System.out.println("Segment 0" + " address " + args[0] + " value " + args[1]);
			argin = new DeviceData();
			argin.insert(args);
			tangoDeviceProxy.command_inout("PresetSingleRegister", argin);
			if (modbusBuffer[0] != 0) {
				args[0] = (short) (baseOffset + 1); 
				args[1] = modbusBuffer[1]; // Holdback value
				System.out.println("Segment 0" + " address " + args[0] + " value " + args[1]);
				argin = new DeviceData();
				argin.insert(args);
				tangoDeviceProxy.command_inout("PresetSingleRegister", argin);
			}
			args[0] = (short) (baseOffset + 2);
			args[1] = modbusBuffer[2]; // Ramp Time units  (0:secs, 1:mins, 2:hours)
			System.out.println("Segment 0" + " address " + args[0] + " value " + args[1]);
			argin = new DeviceData();
			argin.insert(args);
			tangoDeviceProxy.command_inout("PresetSingleRegister", argin);

			args[0] = (short) (baseOffset + 3);
			args[1] = modbusBuffer[3]; // Dwell Time units  (0:secs, 1:mins, 2:hours)
			System.out.println("Segment 0" + " address " + args[0] + " value " + args[1]);
			argin = new DeviceData();
			argin.insert(args);
			tangoDeviceProxy.command_inout("PresetSingleRegister", argin);

			args[0] = (short) (baseOffset + 4);
			args[1] = modbusBuffer[4]; // Program Cycles
			System.out.println("Segment 0" + " address " + args[0] + " value " + args[1]);
			argin = new DeviceData();
			argin.insert(args);
			tangoDeviceProxy.command_inout("PresetSingleRegister", argin);

			for (int i=1 ; i<=numSegments; i++ ) {
				int segtype = modbusBuffer[(i * 8)];
				logger.debug("segment type", segtype);
				args[0] = (short) (baseOffset + (i * 8));
				args[1] = modbusBuffer[(i * 8)]; // Segment Type 0:End, 1:Ramp(rate) 2:Ramp(time) 3:Dwell 4:Step 5:Call program"
				System.out.println("Segment 0" + " address " + args[0] + " value " + args[1]);
				argin = new DeviceData();
				argin.insert(args);
				tangoDeviceProxy.command_inout("PresetSingleRegister", argin);
				if (segtype == 0 ) {	// End
					args[0] = (short) (baseOffset + (i * 8) + 3);
					args[1] = modbusBuffer[(i * 8) + 3]; // End action (0:Reset, 1:Indefinite_Dwell, 2:SetOutput)
					System.out.println("Segment " + segtype + " address " + args[0] + " value " + args[1]);
					argin = new DeviceData();
					argin.insert(args);
					tangoDeviceProxy.command_inout("PresetSingleRegister", argin);
				} else if (segtype == 1 ) {	// ramp (rate)
					logger.debug("target setpoint {} ramp rate {}", modbusBuffer[(i * 8) + 1], modbusBuffer[(i * 8) + 2]);
					setModbusValue((short)(baseOffset + (i * 8) + 1), modbusBuffer[(i * 8) + 1]); //target setpoint
					setModbusValue((short)(baseOffset + (i * 8) + 2), modbusBuffer[(i * 8) + 2]); // rate
				} else if (segtype == 2 ) {	// ramp (time)
					logger.debug("target setpoint {} time value {}", modbusBuffer[(i * 8) + 1], modbusBuffer[(i * 8) + 2]);
					setModbusValue((short)(baseOffset + (i * 8) + 1), modbusBuffer[(i * 8) + 1]); //target setpoint
					setModbusValue((short)(baseOffset + (i * 8) + 2), modbusBuffer[(i * 8) + 2]); // duration
				} else if (segtype == 3 ) {	// dwell
					logger.debug("dwell duration {}", modbusBuffer[(i * 8) + 2]);
					setModbusValue((short)(baseOffset + (i * 8) + 2), modbusBuffer[(i * 8) + 2]); //duration
				} else if (segtype == 4 ) {	// Step
					logger.debug("target setpoint {}", modbusBuffer[(i * 8) + 1]);
					setModbusValue((short)(baseOffset + (i * 8) + 1), modbusBuffer[(i * 8) + 1]); //target setpoint
				} else if (segtype == 5 ) {	// Call
					logger.debug("program number {} call cycle {}", modbusBuffer[(i * 8) + 3], modbusBuffer[(i * 8) + 4]);
					setModbusValue((short)(baseOffset + (i * 8) + 3), modbusBuffer[(i * 8) + 3]); // program number
					setModbusValue((short)(baseOffset + (i * 8) + 4), modbusBuffer[(i * 8) + 4]); // call cycles
				}
			}
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to write " + programNumber + " to the eurotherm: " + ex.getMessage());
			throw ex;
		}
	}

	public String getCurrentSegmentType() throws DeviceException {
		try {
			switch (getModbusValue(CURRENT_SEGMENT_TYPE)) {
			case 0x0:
				return "end";
			case 0x1:
				return "ramp (rate)";
			case 0x2:
				return "ramp (time to target)";
			case 0x3:
				return "dwell";
			case 0x4:
				return "step";
			case 0x5:
				return "call";
			default:
				return "unknown";
			}
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get current segment type: " + ex.getMessage());
			throw ex;
		}
	}
	
	public short getCurrentSegment() throws DeviceException {
		try {
			return getModbusValue(CURRENT_SEGMENT_NUM);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get current segment number: " + ex.getMessage());
			throw ex;
		}
	}

	public short getCurrentProgram() throws DeviceException {
		try {
			return getModbusValue(CURRENT_PROGRAM);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get current program: " + ex.getMessage());
			throw ex;
		}
	}

	public short getProgramStatus() throws DeviceException {
		try {
			return getModbusValue(PROGRAM_STATUS);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get program status: " + ex.getMessage());
			throw ex;
		}
	}

	public short getSetPointMaximum() throws DeviceException {
		try {
			return (short)(getModbusValue(SETPOINT_MAXIMUM)/scale);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get setpoint maximum: " + ex.getMessage());
			throw ex;
		}
	}

	public short getSetPointMinimum() throws DeviceException {
		try {
			return (short)(getModbusValue(SETPOINT_MINIMUM)/scale);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error("Failed to get setpoint minimum: " + ex.getMessage());
			throw ex;
		}
	}

	private short getModbusValue(short address) throws DevFailed {
		short[] args = new short[2];
		DeviceData argin;
		DeviceData argout;
		args[0] = address;
		args[1] = 1;
		argin = new DeviceData();
		argin.insert(args);
		argout = tangoDeviceProxy.command_inout("ReadHoldingRegisters", argin);
		System.out.println("modbus address " + address + "return value " + argout.extractShortArray()[0]);
		return argout.extractShortArray()[0];
	}

	private void setModbusValue(short address, short value) throws DevFailed {
		short[] args = new short[2];
		DeviceData argin;
		args[0] = address;
		args[1] = value;
		argin = new DeviceData();
		argin.insert(args);
		System.out.println("modbus address " + address + "writing value " + value);
		tangoDeviceProxy.command_inout("PresetSingleRegister", argin);
	}
}