/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.xmap.controller;

import gda.device.detector.nxdetector.xmap.controller.XmapModes.CollectionModeEnum;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.PresetMode;

import java.io.IOException;
import java.util.Random;

public class DummyXmapAcquisitionBaseEpicsLayer implements XmapAcquisitionBaseEpicsLayer {


	// This class defines the acquisition mode: step scan uses MCA SPECTRA while raster scan uses MCA_MAPPING
	private CollectionMode collectionMode;
	private String basePVName;
	private boolean acquiring;


	// Map <AcqControlPVname,PV<Boolean>> acqControlPV = new EnumMap<AcqControlPVname,PV<Boolean>>(AcqControlPVname.class);

	public DummyXmapAcquisitionBaseEpicsLayer(String basePVname, CollectionMode collectMode) throws IOException {
		this.basePVName = basePVname;
		this.collectionMode = collectMode;
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		if (collectMode == null) {
			throw new NullPointerException("Collection Mode needs to be declared");
		}
		createAcquisitionControlLazyPVs();
		createAcquisitionConfigurationLazyPVs();
		acquiring = false;
	}

	private void createAcquisitionConfigurationLazyPVs() {

	}

	private void createAcquisitionControlLazyPVs() {

	}

	@Override
	public String fullPVname(String PVsuffix) {
		return basePVName + PVsuffix;

	}

	@Override
	public String getBasePVName() {
		return basePVName;
	}

	@Override
	public void setStart() throws Exception {
		acquiring = true;
	}

	@Override
	public void setStop() throws Exception {
		acquiring = false;
	}

	@Override
	public void setErase() throws Exception {

	}

	@Override
	public void setEraseStart() throws Exception {
		acquiring = true;
	}

	@Override
	public boolean getAcquiring() throws Exception {
		return acquiring;
	}

	// Add this method as it is called in CollectionStrategyPlugin
	@Override
	public int getStatus() throws Exception {
		if (getAcquiring())
			return 1;
		else
			return 0;
	}

	@Override
	public void setCollectMode(CollectionModeEnum collectMode) throws Exception {
		if (collectMode.equals(CollectionModeEnum.MCA_MAPPING))
			isXmapMappingModeInstance("MCA_MAPPING mode");

	}

	@Override
	public CollectionModeEnum getCollectMode() throws Exception {
		return CollectionModeEnum.MCA_MAPPING;
	}

	@Override
	public void setNbins(int nbins) throws Exception {

	}

	@Override
	public int getNbins() throws Exception {
		return 2048;
	}

	@Override
	public void setPresetMode(PresetMode presetMode) throws Exception {
		if (presetMode.equals(PresetMode.NO_PRESET))
			isXmapMappingModeInstance("NO_PRESET type");

	}

	@Override
	public PresetMode getPresetMode() throws Exception {
		return PresetMode.NO_PRESET;
	}

	@Override
	public void setPresetRealTime(double realTime) throws Exception {

	}

	@Override
	public double getPresetRealTime() throws Exception {
		return 1.0;
	}

	@Override
	public void setPresetLiveTime(double liveTime) throws Exception {

	}

	@Override
	public double getPresetLiveTime() throws Exception {
		return 1.0;
	}

	@Override
	public void setPresetEvents(int event) throws Exception {

	}

	@Override
	public int getPresetEvents() throws Exception {
		return 100;
	}

	@Override
	public void setPresetTriggers(int triggers) throws Exception {

	}

	@Override
	public int getPresetTriggers() throws Exception {
		return 10;
	}

	@Override
	public void setAquisitionTime(double presetValue) throws Exception {

	}

	@Override
	public double getAquisitionTime() throws Exception {
		return 1.0;
	}

	@Override
	public CollectionMode getCollectionMode() {
		return collectionMode;
	}

	// For now include the getData for Xmap subdetector here! Just used for experimental setup otherwise
	// data written in HDF5 file!

	@Override
	public double[] getDataPerElement(int subdetector) throws Exception {
		double[] dummyData = new double[getNbins()];
		Random generator = new Random();
		for (int i = 0; i < getNbins(); i++)
			dummyData[i] = generator.nextGaussian();
		return dummyData;
	}

	@Override
	public boolean isXmapMappingModeInstance(String message) {
		if (!(collectionMode instanceof DummyXmapMappingModeEpicsLayer))
			throw new ClassCastException("For " + message + " CollectionMode object should be of type " + "XmapMappingModeEpicsLayer.");
		else
			return true;
	}
}

