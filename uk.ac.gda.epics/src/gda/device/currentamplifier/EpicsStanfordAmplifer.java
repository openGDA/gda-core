/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.ImmutableMap;

import gda.device.AmplifierAutoGain;
import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.util.functions.ThrowingConsumer;
import gda.util.functions.ThrowingFunction;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * this class is for controlling s Stanford current amplifier via EPICS.
 * <p>
 * The Stanford amplifies the input current usually in pA, nA, uA, or mA and outputs a voltage as determined by the sensitivity setting. i.e a <i>1x10^-6 A</i>
 * current with a <i>10^6</i> gain will output <i>1 V</i>
 * <p>
 * The output of Stanford usually feeds into ADC which offers the ability to average the voltage over time to improve the signal to noise ratio.
 * <p>
 * The EPICS EDM for these devices are: <br>
 * The Standford <img src="S_EDM.png"> <br>
 *
 * @see{@link EpicsBekhoffAdc}
 * @author Fajin Yuan
 */
public class EpicsStanfordAmplifer extends CurrentAmplifierBase implements AmplifierAutoGain, StanfordAmplifier, InitializingBean {

	private static final String ERROR_SETTING_TO_EPICS = "Error setting to EPICS: ";
	private static final String INTERRUPTED_WHILE_SETTING_TO_EPICS = "Interrupted while setting to EPICS: ";
	private static final String INTERRUPTED_WHILE_GETTING_FROM_EPICS = "Interrupted while getting from EPICS: ";
	private static final String ERROR_GET_FROM_EPICS = "Error get from EPICS: ";
	private static final Logger logger = LoggerFactory.getLogger(EpicsStanfordAmplifer.class);
	private final EpicsController epicsController = EpicsController.getInstance();
	private String basePVName = null;
	private Map<String, Channel> channelMap = new HashMap<>();

	private static final String[] positionLabels = { "1", "2", "5", "10", "20", "50", "100", "200", "500" };
	private static final String[] gainUnitLabels = { "pA/V", "nA/V", "uA/V", "mA/V" };
	private static final String[] gainModeLabels = { "Low Noise", "High Bandwidth", "Low Drift" };
	private static final String[] offsetUnitLabels = { "pA", "nA", "uA" };
	private Map<String, Double> unitScale = ImmutableMap.of("pA/V", 1.00e-12, "nA/V", 1.00e-9, "uA/V", 1.00e-6, "mA/V", 1.00e-3);
	private static final String UNSUPPORTED_OPERATION_MESSAGE = "Stanford Amplifier does not support this operation";

	protected List<String> currentOffsetUnits = new ArrayList<>();
	private Map<String, Double> gainStringToGainMap;

	// Stanford PV end point strings
	private static final String SENS_PV = "SENS:SEL1";
	private static final String SENS_UNIT_PV = "SENS:SEL2";
	private static final String OFFSET_PV = "IOLV:SEL1";
	private static final String OFFSET_UNIT_PV = "IOLV:SEL2";
	private static final String OFFSET_CURRENT_ON_PV = "IOON";
	private static final String MODE_PV = "GNMD";
	private static final String BIAS_PV = "BSLV";
	private static final String BIAS_ON_PV = "BSON";
	private static final String FILTER_TYPE_PV = "FLTT";
	private static final String FILTER_HIGHPASS_FREQUENCY_PV = "HFRQ";
	private static final String FILTER_LOWPASS_FREQUENCY_PV = "LFRQ";
	private static final String SIGNAL_INVERT_PV = "INVT";
	private static final String DEFAULT_SETTING_RESTORE = "RESET:SEQ";
	private static final String INPUT_FILTER_OVERLOAD_CLEAR = "ROLD";

	// PV for the instantaneous voltage reading
	private String instantaneousPV;

	private double lowerVoltageBound = 0; // V. If ADC input is below this increase gain
	private double upperVoltageBound = 10; // V. If ADC input is below this decrease gain

	private long settleTime = 500; // ms The time to wait after a gain change for stability
	private long startTime;

	public EpicsStanfordAmplifer() {
		gainPositions.addAll(Arrays.asList(positionLabels));
		gainUnits.addAll(Arrays.asList(gainUnitLabels));
		modePositions.addAll(Arrays.asList(gainModeLabels));
		currentOffsetUnits.addAll(Arrays.asList(offsetUnitLabels));
	}

	/**
	 * check all essential bean properties have been set. This must be used to specify a custom init-method in an XML bean definition.
	 */
	@Override
	public void afterPropertiesSet() {
		if (basePVName == null) {
			logger.error("No basePVName is set. Check spring configuration!");
			throw new IllegalStateException("No basePVName is set. Check spring configuration!");
		}
	}

	@Override
	public void configure() throws FactoryException {

		logger.trace("configure called");

		// Check if we are already configured
		if (isConfigured()) {
			logger.trace("Already configured");
			return;
		}

		// Check the basePv ends with : if not add it
		if (!basePVName.endsWith(":")) {
			logger.debug("basePv didn't end with : adding one");
			basePVName += ":";
		}

		gainStringToGainMap = gainMap();

		setInputNames(new String[] { getName() });
		setExtraNames(new String[] {});

		setConfigured(true);
		logger.info("{}: Finished configuring Stanford Amplifier ", getName());
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		String sensitivity = String.valueOf(position);
		String value = sensitivity.substring(0, sensitivity.indexOf(" "));
		String unit = sensitivity.substring(sensitivity.indexOf(" ") + 1);
		ThrowingConsumer<String> setGain = this::setGain;
		ThrowingConsumer<String> setGainUnit = this::setGainUnit;
		Async.execute(() -> setGain.accept((value)));
		Async.execute(() -> setGainUnit.accept((unit)));
		startTime = System.currentTimeMillis();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return getGain() + " " + getGainUnit();
	}

	@Override
	public boolean isBusy() {
		return System.currentTimeMillis() - startTime < settleTime;
	}

	@Override
	public boolean increaseOrDecreaseGain(int i) throws DeviceException {
		if (i != 1 && i != -1) {
			throw new IllegalArgumentException("The index increment must be 1 or -1");
		}

		final String gainString = getGain() + " " + getGainUnit();
		List<String> collect = gainStringToGainMap.keySet().stream().collect(Collectors.toList());

		final int indexOfCurrentGain = collect.indexOf(gainString);

		try {
			final String newGain = collect.get(indexOfCurrentGain + i);
			asynchronousMoveTo(newGain);
			waitWhileBusy();
			return true;
		} catch (IndexOutOfBoundsException e) {
			if (i > 0) {
				logger.debug("No higher gain avaliable ", e);
			} else if (i < 0) {
				logger.debug("No lower gain avaliable ", e);
			}
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.debug("Interrupted when increment or decrement the gain", e);
			return false;
		}
	}

	@Override
	public void setGain(String gain) throws DeviceException {
		if (!gainPositions.contains(gain))
			throw new IllegalArgumentException("The requested gain '" + gain + "' is not available in supported gains " + positionLabels);

		try {
			epicsController.caput(getChannel(SENS_PV), gain);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + SENS_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + SENS_PV, e);
		}
	}

	@Override
	public String getGain() throws DeviceException {
		try {
			return epicsController.caget(getChannel(SENS_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + SENS_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_GETTING_FROM_EPICS + basePVName + SENS_PV, e);
		}
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		if (!gainUnits.contains(unit))
			throw new IllegalArgumentException("The requested unit '" + unit + "' is not availabe in supported units " + gainUnitLabels);

		try {
			epicsController.caput(getChannel(SENS_UNIT_PV), unit);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + SENS_UNIT_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + SENS_UNIT_PV, e);
		}
	}

	@Override
	public String getGainUnit() throws DeviceException {
		try {
			return epicsController.caget(getChannel(SENS_UNIT_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + SENS_UNIT_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_GETTING_FROM_EPICS + basePVName + SENS_UNIT_PV, e);
		}
	}

	@Override
	public double getCurrent() throws DeviceException {
		double value = Double.parseDouble(getGain()); // sensitivity
		Double scale = unitScale.get(getGainUnit()); // sensitivity unit scale
		double gain = 1 / (value * scale);
		return getInstantaneousVoltage() / gain;
	}

	@Override
	public void setMode(String mode) throws DeviceException {
		if (!modePositions.contains(mode))
			throw new IllegalArgumentException("The requested mode '" + mode + "' is not available in supported gain modes " + gainModeLabels);

		try {
			epicsController.caput(getChannel(MODE_PV), mode);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + MODE_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + MODE_PV, e);
		}
	}

	@Override
	public String getMode() throws DeviceException {
		try {
			return epicsController.caget(getChannel(MODE_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + MODE_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_GETTING_FROM_EPICS + basePVName + MODE_PV, e);
		}
	}

	@Override
	public Status getStatus() throws DeviceException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	private Map<String, Double> gainMap() {
		Map<String, Double> map = new HashMap<>();
		for (String unit : gainUnits) {
			for (String position : gainPositions) {
				if (unit.equalsIgnoreCase("mA/V") && !position.equalsIgnoreCase("1")) {
					continue;
				}
				String key = position + " " + unit;
				double value = 1 / (Double.parseDouble(position) * unitScale.get(unit));
				map.put(key, value);
			}
		}
		return map;
	}

	@Override
	public void listGains() throws DeviceException {
		gainStringToGainMap.keySet().stream().forEach(InterfaceProvider.getTerminalPrinter()::print);
	}

	@Override
	public String getCouplingMode() throws DeviceException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public boolean isSupportsCoupling() {
		return false;
	}

	@Override
	public boolean hasMultipleModes() {
		return modePositions.size() > 1;
	}

	@Override
	public double getInstantaneousVoltage() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(instantaneousPV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + instantaneousPV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_GETTING_FROM_EPICS + instantaneousPV, e);
		}
	}

	@Override
	public int getSensitivity() throws DeviceException {
		return gainPositions.indexOf(getGain());
	}

	@Override
	public int getSensitivityUnit() throws DeviceException {
		return gainUnits.indexOf(getGainUnit());
	}

	@Override
	public boolean isOffsetCurrentOn() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(OFFSET_CURRENT_ON_PV)) == 1;
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + OFFSET_CURRENT_ON_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + OFFSET_CURRENT_ON_PV, e);
		}
	}

	@Override
	public void setOffsetCurrentOn(boolean switchOn) throws DeviceException {
		try {
			epicsController.caput(getChannel(OFFSET_CURRENT_ON_PV), switchOn ? 1 : 0);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + OFFSET_CURRENT_ON_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + OFFSET_CURRENT_ON_PV, e);
		}
	}

	@Override
	public int getOffset() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(OFFSET_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + OFFSET_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + OFFSET_PV, e);
		}
	}

	@Override
	public int getOffsetUnit() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(OFFSET_UNIT_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + OFFSET_UNIT_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + OFFSET_UNIT_PV, e);
		}
	}

	@Override
	public void setSensitivity(int sensitivity) throws DeviceException {
		setGain(gainPositions.get(sensitivity));
	}

	@Override
	public void setSensitivityUnit(int unit) throws DeviceException {
		setGainUnit(gainUnits.get(unit));
	}

	@Override
	public void setOffset(int offset) throws DeviceException {
		try {
			epicsController.caput(getChannel(OFFSET_PV), offset);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + OFFSET_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + OFFSET_PV, e);
		}
	}

	@Override
	public void setOffsetUnit(int unit) throws DeviceException {
		try {
			epicsController.caput(getChannel(OFFSET_UNIT_PV), unit);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + OFFSET_UNIT_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + OFFSET_UNIT_PV, e);
		}
	}

	/**
	 * check if bias voltage switched on or not
	 *
	 * @return true when on, false when off
	 * @throws DeviceException
	 */
	public boolean isBiasVoltageOn() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(BIAS_ON_PV)) == 1;
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + BIAS_ON_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + BIAS_ON_PV, e);
		}
	}

	/**
	 * switch bias voltage on (true) or off (false)
	 */
	public void setBiasVoltageOn(boolean switchOn) throws DeviceException {
		try {
			epicsController.caput(getChannel(BIAS_ON_PV), switchOn ? 1 : 0);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + BIAS_ON_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + BIAS_ON_PV, e);
		}
	}

	/**
	 * set bias voltage
	 *
	 * @param bias
	 * @throws DeviceException
	 */
	public void setBiasVoltage(double bias) throws DeviceException {
		if (bias < -5 || bias > 5)
			throw new IllegalArgumentException("Bias voltage must be between -5 and 5 Volts inclusively!");
		try {
			epicsController.caput(getChannel(BIAS_PV), bias);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + BIAS_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + BIAS_PV, e);
		}
	}

	/**
	 * get Bias voltage
	 *
	 * @return the bias voltage
	 * @throws DeviceException
	 */
	public double getBiasVoltage() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(BIAS_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + BIAS_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + BIAS_PV, e);
		}
	}

	/**
	 * set filter type using index position 0 = 6dB High Pass 1 = 12dB High Pass 2 = 6dB Band Pass 3 = 6dB Low Pass 4 = 12dB Low Pass 5 = None
	 *
	 * @param index
	 * @throws DeviceException
	 */
	public void setFilterType(int index) throws DeviceException {
		if (index < 0 || index > 5)
			throw new IllegalArgumentException("Filter type index must be between 0 and 5 inclusively!");
		try {
			epicsController.caput(getChannel(FILTER_TYPE_PV), index);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + FILTER_TYPE_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + FILTER_TYPE_PV, e);
		}
	}

	/**
	 * get the filter type value 0 = 6dB High Pass 1 = 12dB High Pass 2 = 6dB Band Pass 3 = 6dB Low Pass 4 = 12dB Low Pass 5 = None
	 *
	 * @return filter type index
	 * @throws DeviceException
	 */
	public int getFilterType() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(FILTER_TYPE_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + FILTER_TYPE_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + FILTER_TYPE_PV, e);
		}
	}

	/**
	 * set the filter high pass frequency using index position 0 = 0.03 Hz 1 = 0.1 Hz 2 = 0.3 Hz 3 = 1 Hz 4 = 3 Hz 5 = 10 Hz 6 = 30 Hz 7 = 100 Hz 8 = 300 Hz 9 =
	 * 1 kHz 10 = 3 kHz 11 = 10 KHz
	 *
	 * @param index
	 * @throws DeviceException
	 */
	public void setFilterHighpassFrequency(int index) throws DeviceException {
		if (index < 0 || index > 11)
			throw new IllegalArgumentException("Filter high pass frequency index must be between 0 and 11 inclusively!");
		try {
			epicsController.caput(getChannel(FILTER_HIGHPASS_FREQUENCY_PV), index);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + FILTER_HIGHPASS_FREQUENCY_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + FILTER_HIGHPASS_FREQUENCY_PV, e);
		}
	}

	/**
	 * get filter high pass frequency 0 = 0.03 Hz 1 = 0.1 Hz 2 = 0.3 Hz 3 = 1 Hz 4 = 3 Hz 5 = 10 Hz 6 = 30 Hz 7 = 100 Hz 8 = 300 Hz 9 = 1 kHz 10 = 3 kHz 11 = 10
	 * KHz
	 *
	 * @return the index position of the filter high pass frequency
	 * @throws DeviceException
	 */
	public int getFilterHighpassFrequency() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(FILTER_HIGHPASS_FREQUENCY_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + FILTER_HIGHPASS_FREQUENCY_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + FILTER_HIGHPASS_FREQUENCY_PV, e);
		}
	}

	/**
	 * set the filter low pass frequency using index position 0 = 0.03 Hz 1 = 0.1 Hz 2 = 0.3 Hz 3 = 1 Hz 4 = 3 Hz 5 = 10 Hz 6 = 30 Hz 7 = 100 Hz 8 = 300 Hz 9 =
	 * 1 kHz 10 = 3 kHz 11 = 10 KHz 12 = 30 kHz 13 = 100 kHz 14 = 300 kHz 15 = 1 MHz
	 *
	 * @param index
	 * @throws DeviceException
	 */
	public void setFilterLowpassFrequency(int index) throws DeviceException {
		if (index < 0 || index > 15)
			throw new IllegalArgumentException("Filter low pass frequency index must be between 0 and 15 inclusively!");
		try {
			epicsController.caput(getChannel(FILTER_LOWPASS_FREQUENCY_PV), index);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + FILTER_LOWPASS_FREQUENCY_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + FILTER_LOWPASS_FREQUENCY_PV, e);
		}
	}

	/**
	 * return the index of filter low pass frequency 0 = 0.03 Hz 1 = 0.1 Hz 2 = 0.3 Hz 3 = 1 Hz 4 = 3 Hz 5 = 10 Hz 6 = 30 Hz 7 = 100 Hz 8 = 300 Hz 9 = 1 kHz 10
	 * = 3 kHz 11 = 10 KHz 12 = 30 kHz 13 = 100 kHz 14 = 300 kHz 15 = 1 MHz
	 *
	 * @return the index position
	 * @throws DeviceException
	 */
	public int getFilterLowpassFrequency() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(FILTER_LOWPASS_FREQUENCY_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + FILTER_LOWPASS_FREQUENCY_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + FILTER_LOWPASS_FREQUENCY_PV, e);
		}
	}

	/**
	 * set the signal invert using index, 0 = Non-Inverted, 1 = Inverted
	 *
	 * @param index
	 * @throws DeviceException
	 */
	public void setSignalInvert(int index) throws DeviceException {
		if (index < 0 || index > 1)
			throw new IllegalArgumentException("Signal invert index must be either 0 or 1!");
		try {
			epicsController.caput(getChannel(SIGNAL_INVERT_PV), index);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + SIGNAL_INVERT_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + SIGNAL_INVERT_PV, e);
		}
	}

	/**
	 * return the index of the signal invert, 0 = Non-Inverted, 1 = Inverted
	 *
	 * @return the index of signal invert
	 * @throws DeviceException
	 */
	public int getSignalInvert() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(SIGNAL_INVERT_PV));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + SIGNAL_INVERT_PV, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(ERROR_GET_FROM_EPICS + basePVName + SIGNAL_INVERT_PV, e);
		}
	}

	/**
	 * restore default settings
	 *
	 * @throws DeviceException
	 */
	public void restore() throws DeviceException {
		try {
			epicsController.caput(getChannel(DEFAULT_SETTING_RESTORE), 1);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + DEFAULT_SETTING_RESTORE, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + DEFAULT_SETTING_RESTORE, e);
		}
	}

	/**
	 * clear input filter overload
	 *
	 * @throws DeviceException
	 */
	public void clear() throws DeviceException {
		try {
			epicsController.caput(getChannel(INPUT_FILTER_OVERLOAD_CLEAR), 1);
		} catch (CAException e) {
			throw new DeviceException(ERROR_SETTING_TO_EPICS + basePVName + INPUT_FILTER_OVERLOAD_CLEAR, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(INTERRUPTED_WHILE_SETTING_TO_EPICS + basePVName + INPUT_FILTER_OVERLOAD_CLEAR, e);
		}
	}

	@Override
	public String[] getOffsetUnits() {
		return offsetUnitLabels;
	}

	@Override
	public String[] getAllowedPositions() {
		return positionLabels;
	}

	@Override
	public String[] getGainUnits() {
		return gainUnitLabels;
	}

	/**
	 * Lazy initialise channels and store them in a map for retrieval later
	 *
	 * @param pvPostFix
	 * @return channel
	 */
	private Channel getChannel(String pvPostFix) {
		String fullPvName = getBasePVName() + pvPostFix;
		ThrowingFunction<String, Channel> f = epicsController::createChannel;
		Channel channel = channelMap.computeIfAbsent(fullPvName, f);
		logger.trace("Created channel for PV: {}", fullPvName);
		return channel;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public long getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(long settleTime) {
		this.settleTime = settleTime;
	}

	public String getInstantaneousPV() {
		return instantaneousPV;
	}

	public void setInstantaneousPV(String instantaneousPV) {
		this.instantaneousPV = instantaneousPV;
	}

	@Override
	public double getLowerVoltageBound() {
		return lowerVoltageBound;
	}

	public void setLowerVoltageBound(double lowerVoltageBound) {
		this.lowerVoltageBound = lowerVoltageBound;
	}

	@Override
	public double getUpperVoltageBound() {
		return upperVoltageBound;
	}

	public void setUpperVoltageBound(double upperVoltageBound) {
		this.upperVoltageBound = upperVoltageBound;
	}
}
