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

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;

public class MbsNXDetectorDataAppender implements NXDetectorDataAppender {

	private MbsAnalyserCompletedRegion region;

	public MbsNXDetectorDataAppender(MbsAnalyserCompletedRegion region) {
		this.region = region;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		NexusGroupData imageData = new NexusGroupData(region.getImage());
		imageData.isDetectorEntryData = true;
		data.addData(detectorName, "data", imageData, null, 1);
		double[] xAxis = region.getEnergyAxis();
		data.addAxis(detectorName, "energies", new NexusGroupData(xAxis), 2, 1, "eV", true);

		double[] yAxis = region.getLensAxis();
		String yAxisName = region.isTransmissionLensMode() ? "location" : "angles";
		String yAxisUnits = region.isTransmissionLensMode() ? "mm" : "degree";
		data.addAxis(detectorName, yAxisName, new NexusGroupData(yAxis), 1, 1, yAxisUnits, false);

		data.addData(detectorName, "time_for_frame", new NexusGroupData(region.getCollectionTime()), "sec", null);
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

		data.addData(detectorName, "cps", new NexusGroupData(region.getCountPerSecond()), "Hz", null, null, true);
		data.setPlottableValue("cps", region.getCountPerSecond());
	}
}
