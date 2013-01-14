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

package uk.ac.gda.exafs.util;

import gda.device.CurrentAmplifier;
import gda.device.Detector;
import gda.device.Scannable;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import uk.ac.gda.util.number.DoubleUtils;

/**
 * A class to automatically estimate the gain of the detector.
 */
public class GainCalculation {

	private static final List<String> NOTCHES;
	private static final List<String> OFFSET_NOTCHES;
	private final static double MAX_INTENSITY = 1000000;

	static {
		NOTCHES = new ArrayList<String>(13);
		addGainSettings("pA/V", NOTCHES);
		addGainSettings("nA/V", NOTCHES);
		addGainSettings("\u03BCA/V", NOTCHES);
		NOTCHES.add("1 mA/V");
		
		
		OFFSET_NOTCHES = new ArrayList<String>(13);
		addOffsetSettings("pA/V", OFFSET_NOTCHES);
		addOffsetSettings("nA/V", OFFSET_NOTCHES);
		addOffsetSettings("\u03BCA/V", OFFSET_NOTCHES);
		OFFSET_NOTCHES.add("1 mA/V");
	}

	private final static void addGainSettings(String unit, final List<String> items) {
		items.add("1 " + unit);
		items.add("2 " + unit);
		items.add("5 " + unit);
		items.add("10 " + unit);
		items.add("20 " + unit);
		items.add("50 " + unit);
		items.add("100 " + unit);
		items.add("200 " + unit);
		items.add("500 " + unit);
	}
	
	private final static void addOffsetSettings(String unit, final List<String> items) {
		items.add("1 " + unit);
		items.add("2 " + unit);
		items.add("5 " + unit);
		items.add("10 " + unit);
		items.add("20 " + unit);
		items.add("50 " + unit);
		items.add("100 " + unit);
		items.add("200 " + unit);
		items.add("500 " + unit);
	}


	/**
	 * @param bean
	 * @return counts / specified time array of chambers
	 * @throws Exception
	 */
	public static double getIntensity(final GainBean bean) throws Exception {

		final Scannable monocromator = (Scannable) Finder.getInstance().find(bean.getScannableName());
		while (monocromator.isBusy() && !bean.isCancelled()) {
			bean.setMonitorMessage("Waiting for monochromator to be available");
			Thread.sleep(1000);
		}
		bean.setMonitorMessage("Moving monochromator to '" + bean.getEnergy() + "' eV");
		monocromator.asynchronousMoveTo(bean.getEnergy());

		while (monocromator.isBusy() && !bean.isCancelled())
			Thread.sleep(1000);
		if (bean.isCancelled()) {
			final IntensityException e = new CancelledException();
			bean.log("Calculation cancelled.");
			e.setType(-1); // No message
			throw e;
		}

		if (!DoubleUtils.equalsWithinTolerance((Double) monocromator.getPosition(), bean.getEnergy(), 0.1)) {
			throw new Exception("The mononchromator scannable is value '" + monocromator.getPosition()
					+ "' and should be " + bean.getEnergy());
		}

		bean.setMonitorMessage("Collecting intensity at '" + bean.getEnergy() + "' eV");
		final Detector timer = (Detector) Finder.getInstance().find(bean.getCounterTimerName());
		timer.setCollectionTime(bean.getCollectionTime() / 1000.0);
		timer.readout();

		while (timer.getStatus() == Detector.BUSY) {
			Thread.sleep(bean.getCollectionTime()); // Throws interrupted exception if the thread executing this method.
		}

		final double[] counts = (double[]) timer.readout();
		final double time = bean.getCollectionTime() / 1000;
		final int dataIndex = bean.getChannel();

		bean.worked();

		if (bean.isCancelled()) {
			final IntensityException e = new CancelledException();
			bean.log("Calculation cancelled.");
			e.setType(-1); // No message
			throw e;
		}
		return counts[dataIndex] / time;
	}

	/**
	 * @param bean
	 * @return string for the amplifier setting.
	 * @throws Exception
	 */
	public static String getSuggestedGain(final GainBean bean) throws Exception {

		bean.log("\nCalculating suggested sensitivity for chamber '" + bean.getIonChamberName() + "'.");
		bean.log("Chamber\t\tAmplifier\t\tGain\tEnergy (eV)\tIntensity (Hz)");

		String gain = "N/A";
		if (bean.getIonChamberName().equalsIgnoreCase("I0")) {
			bean.setEnergy(bean.getFinalEnergy());
			gain = GainCalculation.getOptimalGain(bean);

		} else if (bean.getIonChamberName().equalsIgnoreCase("It")) {
			bean.setEnergy(bean.getFinalEnergy());
			final String e1Gain = GainCalculation.getOptimalGain(bean);

			bean.setEnergy(bean.getSampleEdgeEnergy());
			final String e2Gain = GainCalculation.getOptimalGain(bean);
			gain = GainCalculation.getLowestGain(e1Gain, e2Gain);

		} else if (bean.getIonChamberName().equalsIgnoreCase("Iref")) {
			bean.setEnergy(bean.getFinalEnergy());
			final String e1Gain = GainCalculation.getOptimalGain(bean);

			bean.setEnergy(bean.getSampleEdgeEnergy());
			final String e2Gain = GainCalculation.getOptimalGain(bean);

			bean.setEnergy(bean.getReferenceEdgeEnergy());
			final String e3Gain = GainCalculation.getOptimalGain(bean);

			gain = GainCalculation.getLowestGain(e1Gain, e2Gain, e3Gain);
		} else {
			throw new Exception("Ion Chamber not found '" + bean.getIonChamberName() + "'");
		}
		bean.log("Suggested gain is " + gain);
		return gain;
	}
	
	public static List<String> getGainNotches() {
		return NOTCHES;
	}
	
	public static List<String> getOffsetNotches() {
		return OFFSET_NOTCHES;
	}

	private static String getOptimalGain(final GainBean bean) throws Exception {

		final double intensity = GainCalculation.getIntensityAtGain(bean, bean.getCurrentGain());

		if (intensity > (MAX_INTENSITY * (bean.getTolerance() / 100))) {
			return GainCalculation.getOptimalGainByReducing(bean, bean.getCurrentGain());

		} else if (intensity < (MAX_INTENSITY * (bean.getTolerance() / 100))) {
			return GainCalculation.getOptimalGainByIncreasing(bean, bean.getCurrentGain());
		}

		return bean.getCurrentGain();
	}

	private static String getOptimalGainByReducing(final GainBean bean, final String curGain) throws Exception {

		final int index = NOTCHES.indexOf(curGain);
		if (index == 0) {
			final IntensityException e = new SaturationException(
					"The intensity is saturating at minimum gain '"
							+ NOTCHES.get(0)
							+ "' for chamber '"
							+ bean.getIonChamberName()
							+ "'.\nThis means there is a problem with the experimental set up.\n\nPlease contact a member of beam-line staff or try a different amplifier.");
			bean.log("Error - The intensity is saturating at minimum gain '" + NOTCHES.get(0) + "' for chamber '"
					+ bean.getIonChamberName() + "'.");
			e.setSuggestedGain(NOTCHES.get(0));
			throw e;
		}

		final String gain = NOTCHES.get(index - 1);
		final double intensity = GainCalculation.getIntensityAtGain(bean, gain);

		if (intensity > (MAX_INTENSITY * (bean.getTolerance() / 100))) {
			return GainCalculation.getOptimalGainByReducing(bean, gain);
		}
		return gain;
	}

	private static String getOptimalGainByIncreasing(final GainBean bean, final String curGain) throws Exception {

		final int index = NOTCHES.indexOf(curGain);
		String gain;
		try {
			gain = NOTCHES.get(index + 1);
		} catch (Exception e) {
			final IntensityException e1 = new SmallIntensityException(
					"The intensity is small at the maximum gain setting of '" + NOTCHES.get(NOTCHES.size() - 1)
							+ "' for chamber '" + bean.getIonChamberName()
							+ "'.\n\nThe maximum value will be set for this chamber.");
			bean.log("Warning - The intensity is small at the maximum gain setting of '"
					+ NOTCHES.get(NOTCHES.size() - 1) + "' for chamber '" + bean.getIonChamberName() + "'.");
			e1.setType(MessageDialog.WARNING);
			e1.setSuggestedGain(NOTCHES.get(NOTCHES.size() - 1));
			throw e1;
		}
		final double intensity = GainCalculation.getIntensityAtGain(bean, gain);

		if (intensity >= (MAX_INTENSITY * (bean.getTolerance() / 100))) {
			return curGain;

		} else if (intensity < (MAX_INTENSITY * (bean.getTolerance() / 100))) {
			return GainCalculation.getOptimalGainByIncreasing(bean, gain);
		}
		return gain;
	}

	private static double getIntensityAtGain(final GainBean bean, final String gain) throws Exception {

		final CurrentAmplifier amp = (CurrentAmplifier) Finder.getInstance().find(bean.getCurrentAmplifierName());
		amp.setGain(getGainValue(gain));
		amp.setGainUnit(getGainUnit(gain));

		final double intensity = GainCalculation.getIntensity(bean);

		bean.log(bean.getIonChamberName() + "\t\t\t" + bean.getCurrentAmplifierName() + "\t" + gain + "\t"
				+ bean.getEnergy() + "\t\t" + intensity);

		return intensity;
	}

	private static String getGainValue(String currentAmpSetting) {
		return currentAmpSetting.substring(0, currentAmpSetting.indexOf(' '));
	}

	private static String getGainUnit(String currentAmpSetting) {
		return currentAmpSetting.substring(currentAmpSetting.indexOf(' ') + 1);
	}

	private static String getLowestGain(String... gains) {

		int lowest = Integer.MAX_VALUE;
		for (int i = 0; i < gains.length; i++) {
			lowest = Math.min(lowest, NOTCHES.indexOf(gains[i]));
		}
		return NOTCHES.get(lowest);
	}


}
