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

package gda.device.currentamplifier;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.CurrentAmplifier;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.CurrAmpSingleType;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;
import gov.aps.jca.Channel;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EPICS class for Stanford SR570 Current Amplifier device.
 */
public class EpicsStanfordCurrentAmp extends CurrentAmplifierBase implements InitializationListener, Scannable,
		CurrentAmplifier {

	private static final Logger logger = LoggerFactory.getLogger(EpicsStanfordCurrentAmp.class);

	private static final String[] positionLabels = {"1","2","5","10","20","50","100","200","500"};
	private static final String[] unitLabels = {"pA/V","nA/V","\u03BCA/V","mA/V"};
	
	private String deviceName = null;
	private EpicsChannelManager channelManager;
	private EpicsController controller;
	
	private volatile String gain = "";
	private volatile String gainUnit = "";

	private Channel setGain = null;
	private Channel setGainUnit = null;

	/**
	 * Constructor
	 */
	public EpicsStanfordCurrentAmp() {

		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		gainPositions.addAll(Arrays.asList(positionLabels));
		gainUnits.addAll(Arrays.asList(unitLabels));
	}

	/**
	 * Configures the class with the PV information from the gda-interface.xml file. Vendor and model are available
	 * through EPICS but are currently not supported in GDA.
	 * 
	 * @see gda.device.DeviceBase#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getDeviceName() != null) {
				CurrAmpSingleType currAmpConfig;
				try {
					currAmpConfig = Configurator.getConfiguration(getDeviceName(),
							gda.epics.interfaces.CurrAmpSingleType.class);
					createChannelAccess(currAmpConfig);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for current amplifier " + getDeviceName(), e);
				}
			} else {
				logger.error("Missing EPICS interface configuration for the current amplifier " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the current amplifier "
						+ getName());
			}
			
			configured = true;
		}// end of if (!configured)
	}

	@Override
	public String[] getGainPositions() throws DeviceException {
		return positionLabels;
	}

	@Override
	public String[] getGainUnits() throws DeviceException {
		return unitLabels;
	}

	/**
	 * returns a parsed list of gains available for this amplifier.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void listGains() throws DeviceException {
		for (String gain : positionLabels) {
			JythonServerFacade f = JythonServerFacade.getInstance();
			f.print(gain);
		}
	}

	@Override
	public void setGain(String position) throws DeviceException {
		int index = 0;
		for (String gainLabel : gainPositions) {
			if (gainLabel.equals(position)) {
				try {
					controller.caput(setGain, index, 2);
					gain = position;
				} catch (Throwable th) {
					throw new DeviceException(setGain.getName() + " failed to moveTo " + position, th);
				}
				return;
			}
			index++;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position + " not found.");
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		int index = 0;
		
		// Some kind of unicode problem with the unit being sent over corba.
		if ("¼A/V".equals(unit)) unit = "\u03BCA/V";
		for (String unitLabel : gainUnits) {
			if (unitLabel.equals(unit)) {
				try {
					controller.caput(setGainUnit, index, 2);
					gainUnit = unit;
				} catch (Throwable th) {
					throw new DeviceException(setGainUnit.getName() + " failed to moveTo " + unit, th);
				}
				return;
			}
			index++;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("SetUnit called: " + unit + " not found.");
	}
	
	@Override
	public String getGain() throws DeviceException {
		return gain;
	}

	@Override
	public String getGainUnit() {
		return gainUnit;
	}


	@Override
	public String toFormattedString() {
		return getName() + " : " + gain + " " + gainUnit;
	}

	/**
	 * gets device name.
	 * 
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets device name.
	 * 
	 * @param name
	 */
	public void setDeviceName(String name) {
		this.deviceName = name;
	}

	/**
	 * creates channel access to amplifier
	 * 
	 * @param currAmpConfig
	 * @throws FactoryException
	 */
	private void createChannelAccess(CurrAmpSingleType currAmpConfig) throws FactoryException {
		try {
			setGain = channelManager.createChannel(currAmpConfig.getSETGAIN().getPv());
			setGainUnit = channelManager.createChannel(currAmpConfig.getSETGAINUNIT().getPv());
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public double getCurrent() throws DeviceException {
		logger.debug("Stanford SR570 does not support this operation");
		return 0;
	}

	@Override
	public String getMode() throws DeviceException {
		logger.debug("Stanford SR570 does not support this operation");
		return null;
	}

	@Override
	public Status getStatus() throws DeviceException {
		logger.debug("Stanford SR570 does not support this operation");
		return null;
	}

	@Override
	public void setMode(String mode) throws DeviceException {
		logger.debug("Stanford SR570 does not support this operation");
	}

	@Override
	public void initializationCompleted() {		
	}

}