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

package uk.ac.gda.devices.vgscienta;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.areadetector.IPVProvider;
import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gov.aps.jca.Channel;

public class VGScientaController implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaController.class);

	// Values internal to the object for Channel Access
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String basePVName = null;
	private IPVProvider pvProvider;
	public static final String PSUMODE_RBV = "ELEMENT_SET_RBV";
	public static final String LENSMODE = "LENS_MODE";
	public static final String LENSMODE_RBV = "LENS_MODE_RBV";
	public static final String ACQMODE = "ACQ_MODE";
	public static final String ACQMODE_RBV = "ACQ_MODE_RBV";
	public static final String PASSENERGY = "PASS_ENERGY";
	public static final String PASSENERGY_RBV = "PASS_ENERGY_RBV";
	public static final String STARTENERGY = "LOW_ENERGY";
	public static final String STARTENERGY_RBV = "LOW_ENERGY_RBV";
	public static final String CENTREENERGY = "CENTRE_ENERGY";
	public static final String CENTREENERGY_RBV = "CENTRE_ENERGY_RBV";
	public static final String ENDENERGY = "HIGH_ENERGY";
	public static final String ENDENERGY_RBV = "HIGH_ENERGY_RBV";
	public static final String ENERGYSTEP = "STEP_SIZE";
	public static final String ENERGYSTEP_RBV = "STEP_SIZE_RBV";
	public static final String FRAMES = "FRAMES";
	public static final String FRAMES_RBV = "FRAMES";
	public static final String SLICE = "SLICES";
	public static final String SLICE_RBV = "SLICES_RBV";
	public static final String ZERO_SUPPLIES = "ZERO_SUPPLIES";
	public static final String TOTAL_DATA_POINTS = "TOTAL_DATA_POINTS_RBV";
	public static final String TOTAL_POINTS_ITERATION_RBV = "TOTAL_POINTS_ITERATION_RBV";
	public static final String TOTAL_LEAD_POINTS_RBV = "TOTAL_LEAD_POINTS_RBV";
	public static final String ENERGY_UNITS_RBV = "X_UNITS_RBV";
	public static final String Y_UNITS_RBV = "Y_UNITS_RBV";
	public static final String INTENSITY_UNITS_RBV = "I_UNITS_RBV";
	public static final String ENERGY_SCALE_RBV = "X_SCALE_RBV";
	public static final String Y_SCALE_RBV = "Y_SCALE_RBV";

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	private Vector<Integer> passenergies = new Vector<Integer>(7);

	private Channel getChannel(String pvPostFix) throws Exception {
		String fullPvName;
		if (pvProvider != null) {
			fullPvName = pvProvider.getPV(pvPostFix);
		} else {
			fullPvName = basePVName + pvPostFix;
		}
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public void setLensMode(String value) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(LENSMODE), value);
	}

	public String getLensMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(LENSMODE_RBV));
	}
	public String getPsuMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(PSUMODE_RBV));
	}

	public void setAcquisitionMode(String value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ACQMODE), value);
	}

	public String getAcquisitionMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ACQMODE_RBV));
	}

	public void setPassEnergy(Integer value) throws Exception {
		int i = 0;
		for(Integer pes: passenergies) {
			if (value.equals(pes)) {
				EPICS_CONTROLLER.caputWait(getChannel(PASSENERGY), i);
				return;
			}
			i = i + 1;
		}

		throw new Exception("unknown pass energy");
	}

	public Integer getPassEnergy() throws Exception {
			return passenergies.get(EPICS_CONTROLLER.cagetInt(getChannel(PASSENERGY_RBV)));
	}

	/**
	 * gets the available positions from this device.
	 *
	 * @return available positions
	 * @throws DeviceException
	 */
	public String[] getPassEnergies() throws DeviceException {
		String[] positionLabels = new String[0];
		try {
			positionLabels = EPICS_CONTROLLER.cagetLabels(getChannel(PASSENERGY));
		} catch (Exception e) {
			if( e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new DeviceException(" exception in getPositions",e);
		}
		return positionLabels;
	}

	@Override
	public void configure() {
		try {
		String[] position = getPassEnergies();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null || position[i] != "") {
				passenergies.add(Integer.valueOf(position[i]));
			}
		}
		} catch (Exception e) {
			logger.error("cannot fill passenergy array, setting and getting passenergy will fail");
		}
	}

	public void setStartEnergy(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(STARTENERGY), value);
	}

	public Double getStartEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(STARTENERGY_RBV));
	}

	public void setCentreEnergy(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(CENTREENERGY), value);
	}

	public Double getCentreEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(CENTREENERGY_RBV));
	}

	public void setEndEnergy(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ENDENERGY), value);
	}

	public Double getEndEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(ENDENERGY_RBV));
	}

	public void setEnergyStep(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ENERGYSTEP), value);
	}

	public Double getEnergyStep() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(ENERGYSTEP_RBV));
	}

	public void setFrames(Integer value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(FRAMES), value);
	}

	public Integer getFrames() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAMES_RBV));
	}

	public void setSlice(Integer value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(SLICE), value);
	}

	public Integer getSlice() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SLICE_RBV));
	}

	public void zeroSupplies() throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(ZERO_SUPPLIES), 1);
			logger.debug("looks like we successfully set the volates to zero");
		} catch (Exception e) {
			logger.error("exception received while zeroing voltages: ", e);
			throw e;
		}
	}

	public int getSweepSteps() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTAL_POINTS_ITERATION_RBV))-EPICS_CONTROLLER.cagetInt(getChannel(TOTAL_LEAD_POINTS_RBV));
	}

	public double[] getEnergyAxis() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ENERGY_SCALE_RBV), getEnergyChannels());
	}

	public String getEnergyUnits() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ENERGY_UNITS_RBV));
	}

	public double[] getYAxis() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(Y_SCALE_RBV), getSlice());
	}

	public String getYUnits() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(Y_UNITS_RBV));
	}

	public String getIntensityUnits() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(INTENSITY_UNITS_RBV));
	}

	public Integer getEnergyChannels() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTAL_DATA_POINTS));
	}
}
