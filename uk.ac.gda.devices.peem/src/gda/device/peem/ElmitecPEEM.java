/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.peem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.PEEM;
import gda.device.detector.uview.corba.impl.CorbaBridgeConnection;
import gda.device.peem.MicroscopeControl.Microscope;
import gda.device.peem.MicroscopeControl.MicroscopePackage.MicroscopeException;
import gda.factory.Finder;

/**
 * Elmitec PhotoEmission Electron Microscope (PEEM) class
 */
public class ElmitecPEEM extends DeviceBase implements PEEM {
	
	private static final Logger logger = LoggerFactory.getLogger(ElmitecPEEM.class);

	String corbaBridgeName;

	boolean isConnected = false;

	CorbaBridgeConnection bridge = null;

	// PEEMClient mc = new PEEMClient();
	Microscope msImpl = null;

	// gda.device.peem.MicroscopeControl.MicroscopePackage.UNBOUNDED_SHORTSEQUENCEHolder
	// data=new UNBOUNDED_SHORTSEQUENCEHolder(imageData);

	/**
	 * Constructor
	 */
	public ElmitecPEEM() {
		// connect();
	}

	// PEEM interface implementation

	@Override
	public boolean connect() {
		Finder finder = Finder.getInstance();
		bridge = (CorbaBridgeConnection) finder.find(corbaBridgeName);
		if (bridge != null) {
			logger.info("Find PEEM Corba Bridge!");
			msImpl = bridge.connect();
			isConnected = true;
			logger.info("PEEM Corba Bridge found. ElimtecPEEM enabled");
		} else {
			isConnected = false;
			logger.error("Can not find PEEM Corba Bridge.");
		}
		return isConnected;
	}

	@Override
	public boolean disconnect() {
		
		this.isConnected = false;
		this.msImpl = null;
		this.bridge = null;
		
		return true;
	}

	@Override
	public void configure() {
		connect();
	}

	public boolean isConnected(){
		
		return isConnected;
		
	}
	/**
	 * Get Corba bridge name
	 * 
	 * @return String Corba bridge name
	 */
	public String getCorbaBridgeName() {
		return corbaBridgeName;
	}

	/**
	 * Set Corba bridge name
	 * 
	 * @param corbaBridgeName
	 */
	public void setCorbaBridgeName(String corbaBridgeName) {
		this.corbaBridgeName = corbaBridgeName;
	}

	@Override
	public double[] getMicrometerValue() throws DeviceException {
		double[] coord = new double[2];
		org.omg.CORBA.FloatHolder xcoord = new org.omg.CORBA.FloatHolder();
		org.omg.CORBA.FloatHolder ycoord = new org.omg.CORBA.FloatHolder();

		msImpl.GetMicrometerValue(xcoord, ycoord);

		coord[0] = xcoord.value;
		coord[1] = ycoord.value;

		return coord;
	}

	@Override
	public int getModuleIndex(String moduleName) throws DeviceException {
		int index = -1;
		int numModules = 0;
		String psName = null;

		numModules = msImpl.GetNrModules();

		for (int i = 0; i < numModules; i++) {
			psName = msImpl.GetPSName((short) i);
			if (psName.compareTo(moduleName) == 0) {
				index = i;
				break;
			}
		}

		if (index < 0) {
			logger.error("No modules with name \"" + moduleName + " \" is found! Please check the module name.");
		}

		return index;
	}

	@Override
	public String getPSName(int index) throws DeviceException {
		String psName = msImpl.GetPSName((short) index);
		if (psName.compareTo("disabled") == 0) {// the module does not exists
			logger.error("Module with index " + index + " does NOT exist.");
			psName = null;
		}
		return psName;
	}

	@Override
	public double getPSValue(int index) throws DeviceException {
		float value = 0;
		String psName = msImpl.GetPSName((short) index);
		if (psName.compareTo("disabled") != 0) {// the module exists
			value = msImpl.GetPSValue((short) index);
		}
		return value;
	}

	@Override
	public String getPreset() throws DeviceException {
		String strName = msImpl.GetPresetLabel();
		return strName;
	}

	@Override
	public String getVacuumGaugeLabel() throws DeviceException {
		String str = msImpl.ReadGaugeLabel((short) 1);
		return str;
	}

	@Override
	public double getVacuumGaugeValue() throws DeviceException {
		double ret = msImpl.ReadGauge((short) 1);
		return ret;
	}

	@Override
	public boolean isInitDone() throws DeviceException {
		return msImpl.GetInitDone();
	}

	@Override
	public String modules() throws DeviceException {
		StringBuffer info = new StringBuffer(500);

		int numModules = 0, numAct = 0;
		numModules = msImpl.GetNrModules();

		String psName, psUnit;
		float psValue, psLimitLow, psLimitHigh;

		String format = "%20s %5d %10.3f %5s %10.3f  %10.3f \n";
		info.append("         Module Name   Index  Value     Unit  LowLimit    HighLimit \n");
		for (short i = 0; i < numModules; i++) {
			psName = msImpl.GetPSName(i);
			if (psName.compareTo("disabled") != 0) {
				numAct++;
				psValue = msImpl.GetPSValue(i);
				psLimitHigh = msImpl.GetPSLimitHigh(i);
				psLimitLow = msImpl.GetPSLimitLow(i);
				psUnit = msImpl.GetPSUnit(i);

				info.append(String.format(format, psName, i, psValue, psUnit, psLimitLow, psLimitHigh));
				System.out.printf("%20s %5d %10.3f %5s %10.3f  %10.3f \n", psName, i, psValue, psUnit, psLimitLow,
						psLimitHigh);
			}
		}

		info.append("The total number of LEEM2000 modules is: " + numModules + ". The active module number is: "
				+ numAct + ".");
		logger.debug("The total number of LEEM2000 modules is: " + numModules + ". The active module number is: "
				+ numAct + ".");
		return info.toString();
	}

	@Override
	public int getModuleNumber() throws DeviceException {
		int numModules = 0;
		numModules = msImpl.GetNrModules();
		// Message.debug("The total number of LEEM2000 modules is: " +
		// numModules
		// +".");
		return numModules;
	}

	@Override
	public boolean setPSValue(int index, double value) throws DeviceException {
		boolean result = false;
		String psName, psUnit;
		float psLimitLow, psLimitHigh;

		psName = msImpl.GetPSName((short) index);

		if (psName.compareTo("disabled") != 0) {// the module exists
			psLimitHigh = msImpl.GetPSLimitHigh((short) index);
			psLimitLow = msImpl.GetPSLimitLow((short) index);
			psUnit = msImpl.GetPSUnit((short) index);
			if (value >= msImpl.GetPSLimitLow((short) index) && value <= msImpl.GetPSLimitHigh((short) index)) {
				try {
					msImpl.SetPSValue((short) index, (float) value);
					result = true;
				} catch (MicroscopeException e) {
					logger.error("Error when try to set value for Module " + psName);
					e.toString();
					result = false;
				}
			} else {// Out of range
				logger.error("Error! New value " + value + " for " + psName + " module is out of limits. ("
						+ psLimitLow + ", " + psLimitHigh + " " + psUnit + ")");
			}
		}
		return result;
	}

	@Override
	public int setPhi(double angle) throws DeviceException {
		int ret = msImpl.SetPhi((float) angle);
		return ret;
	}

	/*
	 * public void update(java.lang.Object theObserved, java.lang.Object changeCode){ if (theObserved != null &&
	 * theObserved instanceof Detector && changeCode instanceof String){ String str = (String) changeCode; if
	 * (str.equals("turretchange")) notifyIObservers(this, changeCode); } }
	 */

}
