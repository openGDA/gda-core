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

package gda.device.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.enumpositioner.EpicsQuadraCurrentAmplifier;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * EpicsCurrAmpQuadController Class: A monitor type to get the beam intensity, beam positions and four currents
 * 
 * @since 9.3.3
 * @fy65 @Deprecated please use {@link gda.device.qbpm.EpicsQbpm} instead
 */
@Deprecated
public class EpicsQuadrantBeamPositionMointor extends MonitorBase implements Monitor{

	private static final Logger logger = LoggerFactory.getLogger(EpicsQuadrantBeamPositionMointor.class);

	private String bpmName = null;

	private String qcaName = null;

	private EpicsQuadraCurrentAmplifier qca;

	private EpicsBeamPositionMonitor bpm;

	private Finder finder = Finder.getInstance();

	/**
	 * Constructor
	 */
	public EpicsQuadrantBeamPositionMointor(){
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getCurrAmpQuadName() != null) {
				qca = (EpicsQuadraCurrentAmplifier) finder.find(getCurrAmpQuadName());
			} else {
				logger.error("Missing EPICS interface configuration for the current amplifier " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the current amplifier "
						+ getName());
			}
			if (getBpmName() != null) {
				bpm = (EpicsBeamPositionMonitor) finder.find(getBpmName());
			} else {
				logger.error("Missing EPICS interface configuration for the BPM device " + getBpmName());
				throw new FactoryException("Missing EPICS interface configuration for the BPM device " + getBpmName());
			}
			this.modifyExtraNames();
			configured = true;
		}
	}

	private void modifyExtraNames(){
		setInputNames(new String[0]);
		String preName=this.getName()+"_";
		setExtraNames(new String[]{ preName+"intensity", preName+"x", preName+"y", preName+"current1", preName+"current2", preName+"current3", preName+"current4"});

		outputFormat = new String[inputNames.length + extraNames.length];

		for (int i = 0; i < outputFormat.length; i++) {
			outputFormat[i] = "%4.10f";
		}
		setOutputFormat(outputFormat);
	}


	/**
	 * @return String bpm name
	 *
	 */
	public String getBpmName() {
		return bpmName;
	}

	/**
	 * @param name bpm
	 *
	 *
	 */
	public void setBpmName(String name) {
		this.bpmName = name;
	}

	/**
	 * @return qca name
	 */
	public String getCurrAmpQuadName() {
		return qcaName;
	}

	/**
	 * @param name
	 */
	public void setCurrAmpQuadName(String name) {
		this.qcaName = name;
	}

	@Override
	public Object getPosition() throws DeviceException {
		double[] value = new double[7];
		value[0]=bpm.getIntensity();
		value[1]=bpm.getXPosition();
		value[2]=bpm.getYPosition();
		value[3]=qca.getCurrents()[0];
		value[4]=qca.getCurrents()[1];
		value[5]=qca.getCurrents()[2];
		value[6]=qca.getCurrents()[3];

		return value;
	}

}