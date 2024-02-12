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

package uk.ac.diamond.daq.devices.mbs;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

public class MbsNXDetectorDataAppender implements NXDetectorDataAppender {

	private MbsAnalyserCompletedRegion region;
	private boolean isPointDependent=false;

	private static final Logger logger = LoggerFactory.getLogger(MbsNXDetectorDataAppender.class);

	public MbsNXDetectorDataAppender(MbsAnalyserCompletedRegion region) {
		this.region = region;
		this.isPointDependent = checkCentreEnergyIsVaried(); // I05-605
		logger.info("isPointDependentServerObject : {}", isPointDependent);
	}

	private boolean checkCentreEnergyIsVaried() {
		// Get scan info from GDA server
		ScanInformation info =InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		// first check centre_energy scannable explicit change
		if (Arrays.asList(info.getScannableNames()).contains("centre_energy")) {
			return true;
		}
		// otherwise check scannable groups from Jython namespace and see if they are called and if they contain centre_energy scannable
		// thus we don't rely on a "scan_group" name in a scan_creator.py script!
		for (String scanGroupName:InterfaceProvider.getJythonNamespace().getAllNamesForType(ScannableGroup.class)) {
			if (Arrays.asList(info.getScannableNames()).contains(scanGroupName)) {
				ScannableGroup sm = (ScannableGroup) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scanGroupName);
				if (Arrays.asList(sm.getGroupMemberNames()).contains("centre_energy")){
					return true;
					}
				}
			}
		return false;
		}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		NexusGroupData imageData = new NexusGroupData(region.getImage());
		imageData.isDetectorEntryData = true;
		data.addData(detectorName, "data", imageData, null, 1);
		double[] xAxis = region.getEnergyAxis();
		data.addAxis(detectorName, "energies", new NexusGroupData(xAxis), 2, 1, "eV", isPointDependent);

		double[] yAxis = region.getLensAxis();
		String yAxisName = region.isTransmissionLensMode() ? "location" : "angles";
		String yAxisUnits = region.isTransmissionLensMode() ? "mm" : "degree";
		data.addAxis(detectorName, yAxisName, new NexusGroupData(yAxis), 1, 1, yAxisUnits, false);

		data.addData(detectorName, "time_for_frames", new NexusGroupData(region.getCollectionTime()), "sec", null);
		data.addData(detectorName, "acquire_time", new NexusGroupData(region.getAcquireTime()), "sec", null);
		data.addData(detectorName, "acquire_period", new NexusGroupData(region.getAcquirePeriod()), "sec", null);
		data.addData(detectorName, "number_of_iterations", new NexusGroupData(region.getIterations()), null, null);
		data.addData(detectorName, "pass_energy", new NexusGroupData(region.getPassEnergy()), "eV", null);
		data.addData(detectorName, "lens_mode", new NexusGroupData(region.getLensMode()), null, null);
		data.addData(detectorName, "acquisition_mode", new NexusGroupData(region.getAcquisitionMode()), null, null);
		data.addData(detectorName, "kinetic_energy_start", new NexusGroupData(region.getStartEnergy()), "eV", null);
		data.addData(detectorName, "kinetic_energy_end", new NexusGroupData(region.getEndEnergy()), "eV", null);
		data.addData(detectorName, "kinetic_energy_center", new NexusGroupData(region.getCentreEnergy()), "eV", null);
		data.addData(detectorName, "energy width", new NexusGroupData(region.getEnergyWidth()), "eV", null);
		data.addData(detectorName, "deflector_x", new NexusGroupData(region.getDeflectorX()), null, null);
		data.addData(detectorName, "detector_y", new NexusGroupData(region.getDeflectorY()), null, null);
		data.addData(detectorName, "slices", new NexusGroupData(region.getNumberOfSlices()), null, null);
		data.addData(detectorName, "steps", new NexusGroupData(region.getNumberfSteps()), null, null);
		data.addData(detectorName, "dither_steps", new NexusGroupData(region.getNumberOfDitherSteps()), null, null);
		data.addData(detectorName, "spin_offset", new NexusGroupData(region.getSpinOffset()), null, null);
		data.addData(detectorName, "kinetic_energy_step", new NexusGroupData(region.getStepSize()), "eV", null);
		data.addData(detectorName, "region_origin", new NexusGroupData(region.getRegionStartX(), region.getRegionStartY()), null, null);
		data.addData(detectorName, "region_size", new NexusGroupData(region.getRegionSizeX(), region.getRegionSizeY()), null, null);
		data.addData(detectorName, "sensor_size", new NexusGroupData(region.getSensorSizeX(), region.getSensorSizeY()), null, null);
		data.addData(detectorName, "psu_mode", new NexusGroupData(region.getPsuMode()), null, null);
		data.addData(detectorName, "entrance_slit_direction", new NexusGroupData(region.getAnalyserSlitDirection()),null, null);
		data.addData(detectorName, "entrance_slit_setting", new NexusGroupData(region.getAnalyserSlitSetting()),null, null);
		data.addData(detectorName, "entrance_slit_size", new NexusGroupData(region.getAnalyserSlitSize()),null, null);
		data.addData(detectorName, "entrance_slit_shape", new NexusGroupData(region.getAnalyserSlitShape()),null, null);

		data.addData(detectorName, "cps", new NexusGroupData(region.getCountPerSecond()), "Hz", null, null, true);
		data.setPlottableValue("cps", region.getCountPerSecond());
	}
}
