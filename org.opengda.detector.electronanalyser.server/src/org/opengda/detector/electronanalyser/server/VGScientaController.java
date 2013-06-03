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

package org.opengda.detector.electronanalyser.server;

import gda.device.DeviceException;
import gda.device.detector.areadetector.IPVProvider;
import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gov.aps.jca.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VGScientaController implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaController.class);

	// Values internal to the object for Channel Access
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String basePVName = null;
	private IPVProvider pvProvider;
	public static final String LENSMODE = "LENS_MODE";
	public static final String LENSMODE_RBV = "LENS_MODE_RBV";
	public static final String ACQMODE = "ACQ_MODE";
	public static final String ACQMODE_RBV = "ACQ_MODE_RBV";
	public static final String ENERGYMODE = "ENERGY_MODE";
	public static final String ENERGYMODE_RBV = "ENERGY_MODE_RBV";
	public static final String DETECTORMODE = "DETECTOR_MODE";
	public static final String DETECTORMODE_RBV = "DETECTOR_MODE_RBV";
	public static final String ELEMENTSET = "ELEMENT_SET";
	public static final String ELEMENTSET_RBV = "ELEMENT_SET_RBV";
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
	public static final String FRAMES_RBV = "FRAMES_RBV";
	public static final String STEPTIME = "STEP_TIME";
	public static final String SLICE = "SLICES";
	public static final String SLICE_RBV = "SLICES_RBV";
	public static final String INTERATIONS = "NumExposures";
	public static final String ZERO_SUPPLIES = "ZERO_SUPPLIES";
	public static final String TOTALPOINTS = "TOTAL_POINTS_RBV";
	public static final String TOTALLEADPOINTS="TOTAL_LEAD_POINTS_RBV";
	public static final String TOTALDATAPOINTS="TOTAL_DATA_POINTS_RBV";
	public static final String CURRENTPOINT = "CURRENT_CHANNEL_RBV";
	public static final String SPECTRUMDATA = "INT_SPECTRUM";
	public static final String IMAGEDATA = "IMAGE";
	public static final String EXTIODATA = "EXTIO";
	public static final String EXCITATIONENERGY = "EXCITATION_ENERGY";
	public static final String EXCITATIONENERGY_RBV = "EXCITATION_ENERGY_RBV";
	public static final String XUNITS_RBV = "X_UNITS_RBV";
	public static final String YUNITS_RBV = "Y_UNITS_RBV";

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	private Vector<Integer> passenergies = new Vector<Integer>(7);

	public Channel getChannel(String pvPostFix) throws Exception {
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
		EPICS_CONTROLLER.caput(getChannel(LENSMODE), value);
	}
	public void setLensMode(String value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(LENSMODE), value, timeout);
	}

	public String getLensMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(LENSMODE_RBV));
	}

	public void setAcquisitionMode(String value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ACQMODE), value);
	}

	public void setAcquisitionMode(String value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(ACQMODE), value, timeout);
	}
	public String getAcquisitionMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ACQMODE_RBV));
	}

	public void setEnergyMode(String value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ENERGYMODE), value);
	}
	public void setEnergyMode(String value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(ENERGYMODE), value, timeout);
	}

	public String getEnergyMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ENERGYMODE_RBV));
	}

	public void setDetectorMode(String value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DETECTORMODE), value);
	}
	public void setDetectorMode(String value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(DETECTORMODE), value, timeout);
	}

	public String getDetectorMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(DETECTORMODE_RBV));
	}

	public void setElement(String value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ELEMENTSET), value);
	}

	public String getElement() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ELEMENTSET_RBV));
	}

	public void setPassEnergy(Integer value) throws Exception {
		int i = 0;
		for (Integer pes : passenergies) {
			if (value.equals(pes)) {
				EPICS_CONTROLLER.caput(getChannel(PASSENERGY), i);
				return;
			}
			i = i + 1;
		}

		throw new Exception("unknown pass energy");
	}
	public void setPassEnergy(Integer value, double timeout) throws Exception {
		int i = 0;
		for (Integer pes : passenergies) {
			if (value.equals(pes)) {
				EPICS_CONTROLLER.caputWait(getChannel(PASSENERGY), i, timeout);
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
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new DeviceException(" exception in getPositions", e);
		}
		return positionLabels;
	}
	public String[] getLensModes() throws DeviceException {
		String[] positionLabels = new String[0];
		try {
			positionLabels = EPICS_CONTROLLER.cagetLabels(getChannel(LENSMODE));
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new DeviceException(" exception in getPositions", e);
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
	public void setStartEnergy(Double value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(STARTENERGY), value, timeout);
	}

	public Double getStartEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(STARTENERGY_RBV));
	}

	public void setCentreEnergy(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(CENTREENERGY), value);
	}
	public void setCentreEnergy(Double value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(CENTREENERGY), value, timeout);
	}

	public Double getCentreEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(CENTREENERGY_RBV));
	}

	public void setEndEnergy(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ENDENERGY), value);
	}
	public void setEndEnergy(Double value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(ENDENERGY), value, timeout);
	}

	public Double getEndEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(ENDENERGY_RBV));
	}

	public void setEnergyStep(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ENERGYSTEP), value);
	}
	public void setEnergyStep(Double value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(ENERGYSTEP), value, timeout);
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

	public void setStepTime(Double value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(STEPTIME), value);
	}
	public void setStepTime(Double value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(STEPTIME), value, timeout);
	}

	public void setSlice(Integer value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(SLICE), value);
	}

	public void setSlice(Integer value, double timeout) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(SLICE), value, timeout);
	}
 	public Integer getSlice() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SLICE_RBV));
	}

	public Integer getTotalSteps() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTALPOINTS));
	}
	public Integer getTotalLeadPoints() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTALLEADPOINTS));
	}
	public Integer getTotalDataPoints() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTALDATAPOINTS));
	}

	public Integer getCurrentPoint() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(CURRENTPOINT));
	}

	public void zeroSupplies() throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ZERO_SUPPLIES), 1);
	}

	public double[] getSpectrum() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(SPECTRUMDATA));
	}

	public double[] getImage() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(IMAGEDATA));
	}

	public double[] getExtIO() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(EXTIODATA));
	}

	public void setExcitationEnergy(Integer value) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(EXCITATIONENERGY), value);
	}

	public double getExcitationEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(EXCITATIONENERGY_RBV));
	}

	public String getXUnits() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(XUNITS_RBV));
	}

	public String getYUnits() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(YUNITS_RBV));
	}
}