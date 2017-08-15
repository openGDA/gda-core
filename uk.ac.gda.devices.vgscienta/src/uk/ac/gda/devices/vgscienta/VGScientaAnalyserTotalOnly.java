/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.areadetector.v17.ADBase;
import gda.factory.FactoryException;

public class VGScientaAnalyserTotalOnly extends DetectorBase implements NexusDetector {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaAnalyserTotalOnly.class);

	private ADBase adBase;
	private VGScientaController controller;

	private int spectrumSize;
	private boolean firstReadoutInScan;
	private EntranceSlitInformationProvider entranceSlitInformationProvider;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		// No input names its read-only
		setInputNames(new String[] {});
		setExtraNames(new String[] { getName() });
	}

	@Override
	public void collectData() throws DeviceException {
		try {
			adBase.startAcquiring();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		// Cache the size of the spectrum to save a call to EPICS on each readout
		try {
			spectrumSize = controller.getEnergyChannels();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		firstReadoutInScan = true;
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		zeroSuppliesIgnoreErrors();
	}

	@Override
	public int getStatus() throws DeviceException {
		return adBase.getStatus();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		adBase.waitWhileStatusBusy();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		try {
			// Used cached spectrumSize to improve performance
			final double[] spectrum = controller.getSpectrum(spectrumSize);
			// Sum all the elements in the spectrum
			final double total =  Arrays.stream(spectrum).sum();

			// Build the NXDetectorData pass in this to setup input/extra names and output format
			NXDetectorData data = new NXDetectorData(this);
			// Just plot the total during the scan
			data.setPlottableValue(getName(), total);

			// Add the data to be written to the file
			data.addData(getName(), getName(), new NexusGroupData(total), "counts");

			if (firstReadoutInScan) { // place in entry1/instrument/analyser_total(NXdetector) group.
				appendMetadata(data);
				firstReadoutInScan = false;
			}

			return data;
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	private void appendMetadata(NXDetectorData data) throws Exception {
		data.addData(getName(), "kinetic_energy_start", new NexusGroupData(controller.getStartEnergy()), "eV", null);
		data.addData(getName(), "kinetic_energy_center", new NexusGroupData(controller.getCentreEnergy()), "eV", null);
		data.addData(getName(), "kinetic_energy_end", new NexusGroupData(controller.getEndEnergy()), "eV", null);
		data.addData(getName(), "time_per_channel", new NexusGroupData(controller.getExposureTime()), "s", null, null, true);
		data.addData(getName(), "lens_mode", new NexusGroupData(controller.getLensMode()), null, null);
		data.addData(getName(), "acquisition_mode", new NexusGroupData(controller.getAcquisitionMode()), null, null);
		data.addData(getName(), "pass_energy", new NexusGroupData(controller.getPassEnergy()), "eV", null);
		data.addData(getName(), "psu_mode", new NexusGroupData(controller.getPsuMode()), null, null);
		data.addData(getName(), "number_of_frames", new NexusGroupData(controller.getFrames()), null, null);
		data.addData(getName(), "time_for_frames", new NexusGroupData(getAdBase().getAcquireTime_RBV()), "s", null);
		data.addData(getName(), "sensor_size", new NexusGroupData(getAdBase().getMaxSizeX_RBV(), getAdBase().getMaxSizeY_RBV()), null, null);
		data.addData(getName(), "region_origin", new NexusGroupData(getAdBase().getMinX_RBV(), getAdBase().getMinY_RBV()), null, null);
		data.addData(getName(), "region_size", new NexusGroupData(getAdBase().getSizeX_RBV(), getAdBase().getSizeY_RBV()), null, null);

		if (entranceSlitInformationProvider != null) {
			data.addData(getName(), "entrance_slit_size", new NexusGroupData(entranceSlitInformationProvider.getSizeInMM()), "mm", null);
			data.addData(getName(), "entrance_slit_setting",
					new NexusGroupData(String.format("%03d", entranceSlitInformationProvider.getRawValue().intValue())), null, null);
			data.addData(getName(), "entrance_slit_shape", new NexusGroupData(entranceSlitInformationProvider.getShape().toLowerCase()), null, null);
			data.addData(getName(), "entrance_slit_direction", new NexusGroupData(entranceSlitInformationProvider.getDirection().toLowerCase()), null, null);
		}
	}

	public void zeroSuppliesIgnoreErrors() {
		try {
			controller.zeroSupplies();
		} catch (Exception e) {
			logger.error("error zeroing power supplies", e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false; // It doesn't
	}

	public ADBase getAdBase() {
		return adBase;
	}

	public void setAdBase(ADBase adBase) {
		this.adBase = adBase;
	}

	public VGScientaController getController() {
		return controller;
	}

	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	public EntranceSlitInformationProvider getEntranceSlitInformationProvider() {
		return entranceSlitInformationProvider;
	}

	public void setEntranceSlitInformationProvider(EntranceSlitInformationProvider entranceSlitInformationProvider) {
		this.entranceSlitInformationProvider = entranceSlitInformationProvider;
	}

}
