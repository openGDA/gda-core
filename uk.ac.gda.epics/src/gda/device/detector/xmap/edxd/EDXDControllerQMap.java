/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.detector.xmap.edxd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.epicsdevice.ReturnType;
import gda.factory.FactoryException;

/**
 * Version of EDXDController including QMap data.
 * <p>
 * Originally developed for i12: see JEEP-239 or search Jira for "edxd1"
 */
public class EDXDControllerQMap extends EDXDController implements NexusDetector {
	private static final Logger logger = LoggerFactory.getLogger(EDXDControllerQMap.class);

	private static final String MEAN_DEAD_TIME = "DeadTime";

	private static final String COUNTS = "counts";
	private static final String COUNTS_PER_SECOND = "counts/second";
	private static final String KEV = "keV";
	private static final String PERCENT = "percent";
	private static final String UNITS = "seconds";
	private static final String SECONDS = "seconds";

	private Set<String> extraNamesSet;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		inputNames = new String[] {};
		extraNamesSet = new HashSet<>(Arrays.asList(extraNames));
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		// If there was a problem when acquiring the data
		verifyData();

		final NXDetectorData data = new NXDetectorData(this);

		// Quick integrator to calculate the total counts and values for the dead_time stats
		int totalCounts = 0;
		double deadTimeMax = -Double.MAX_VALUE;
		double deadTimeMin = Double.MAX_VALUE;
		double deadTimeMean = 0.0;
		int deadTimeMeanElements = 0;
		double liveTimeMean = 0.0;

		final int nsubdets = subDetectors.size();
		final double[][] allData = new double[nsubdets][];
		final double[][] allEnergy = new double[nsubdets][];
		final double[][] allQ = new double[nsubdets][];
		final double[] allEliveTime = new double[nsubdets];
		final double[] allTliveTime = new double[nsubdets];
		final double[] allRealTime = new double[nsubdets];
		final int[] allEvents = new int[nsubdets];
		final double[] allInputCountRate = new double[nsubdets];
		final double[] allOutputCountRate = new double[nsubdets];
		final double[] allDeadTime = new double[nsubdets];
		final double[] allDeadTimePercent = new double[nsubdets];

		// populate the data item from the elements
		for (int i = 0; i < subDetectors.size(); i++) {
			final IEDXDElement det = subDetectors.get(i);

			allData[i] = det.readoutDoubles();
			totalCounts += Arrays.stream(allData[i]).sum();

			// add the energy Axis
			allEnergy[i] = det.getEnergyBins();

			// add the q Axis
			allQ[i] = det.getQMapping();

			allEliveTime[i] = det.getEnergyLiveTime();

			final double tliveTime = det.getTriggerLiveTime();
			allTliveTime[i] = tliveTime;

			final double realTime = det.getRealTime();
			allRealTime[i] = realTime;

			allEvents[i] = det.getEvents();

			final double inputCountRate = det.getInputCountRate();
			allInputCountRate[i] = inputCountRate;

			final double outputCountRate = det.getOutputCountRate();
			allOutputCountRate[i] = outputCountRate;

			// simple deadtime calculation for now, which is simply based on the 2 rates
			final double deadTime = (1.0 - (outputCountRate / inputCountRate)) * realTime;
			allDeadTime[i] = deadTime;

			// simple deadtime calculation for now, which is simply based on the 2 rates
			final double deadTimePercent = (1.0 - (outputCountRate / inputCountRate)) * 100.0;
			allDeadTimePercent[i] = deadTimePercent;

			// now calculate the deadtime statistics
			if (deadTime > deadTimeMax) {
				deadTimeMax = deadTime;
			}
			if (deadTime < deadTimeMin) {
				deadTimeMin = deadTime;
			}
			deadTimeMean += deadTime;
			liveTimeMean += tliveTime;
			deadTimeMeanElements++;
		}

		final String name = "EDXD_elements";
		data.addData(name, new NexusGroupData(allData), COUNTS, 1);
		data.addAxis(name, "edxd_energy_approx", new NexusGroupData(allEnergy), 2, 2, KEV, false);
		data.addAxis(name, "edxd_q", new NexusGroupData(allQ), 2, 1, UNITS, false);
		data.addElement(name, "edxd_energy_live_time", new NexusGroupData(allEliveTime), SECONDS, true);
		data.addElement(name, "edxd_trigger_live_time", new NexusGroupData(allTliveTime), SECONDS, true);
		data.addElement(name, "edxd_real_time", new NexusGroupData(allRealTime), SECONDS, true);
		data.addElement(name, "edxd_events", new NexusGroupData(allEvents), COUNTS, true);
		data.addElement(name, "edxd_input_count_rate", new NexusGroupData(allInputCountRate), COUNTS_PER_SECOND, true);
		data.addElement(name, "edxd_output_count_rate", new NexusGroupData(allOutputCountRate), COUNTS_PER_SECOND, true);
		data.addElement(name, "edxd_dead_time", new NexusGroupData(allDeadTime), SECONDS, true);
		data.addElement(name, "edxd_dead_time_percent", new NexusGroupData(allDeadTimePercent), PERCENT, true);

		if (deadTimeMeanElements <= 0) {
			// This can probably never happen, but check to keep the compiler happy
			logger.warn("deadTimeMeanElements is {}: setting to 1 to avoid divide by zero", deadTimeMeanElements);
			deadTimeMeanElements = 1;
		}

		if (extraNamesSet.isEmpty()) {
			data.setPlottableValue(getName(), (double) totalCounts); // default "extra name" is detector name
		} else {
			setPlottableIfNamed(data, "edxd_mean_live_time", liveTimeMean / deadTimeMeanElements);
			setPlottableIfNamed(data, "edxd_max_dead_time", deadTimeMax);
			setPlottableIfNamed(data, "edxd_min_dead_time", deadTimeMin);
			double edxdMeanDeadTime;
			try {
				edxdMeanDeadTime = getMeanDeadTime();
			} catch (DeviceException e) {
				logger.error("Failed to get edxd_mean_dead_time, using estimated value", e);
				edxdMeanDeadTime = deadTimeMean / deadTimeMeanElements;
			}
			setPlottableIfNamed(data, "edxd_mean_dead_time", edxdMeanDeadTime);
			setPlottableIfNamed(data, "edxd_total_counts", (double) totalCounts);
		}

		return data;
	}

	// NXDetectorData complains if you set a plottable value that is not defined as an "extra name"
	private void setPlottableIfNamed(NXDetectorData data, String plottableName, Double value) {
		if (extraNamesSet.contains(plottableName)) {
			data.setPlottableValue(plottableName, value);
		}
	}

	private double getMeanDeadTime() throws DeviceException {
		return (double) xmap.getValue(ReturnType.DBR_NATIVE, MEAN_DEAD_TIME, "");
	}
}
