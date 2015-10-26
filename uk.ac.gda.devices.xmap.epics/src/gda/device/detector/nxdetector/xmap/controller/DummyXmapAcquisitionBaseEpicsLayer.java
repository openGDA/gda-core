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

import java.io.IOException;
import java.util.Random;

public class DummyXmapAcquisitionBaseEpicsLayer {
	public enum CollectionModeEnum {
		/* MCA spectra used in step scan: acquire only one spectrum */
		MCA_SPECTRA,
		/* MCA mapping used in raster/continuous scan: acquire multiple spectra */
		MCA_MAPPING, SCA_MAPPING, LIST_MAPPING
	}

	// This class defines the acquisition mode: step scan uses MCA SPECTRA while raster scan uses MCA_MAPPING
	private CollectionMode collectionMode;
	private String basePVName;

	public enum PresetMode {
		/* Option used for hardware trigger */
		NO_PRESET, REAL_TIME, LIVE_TIME, EVENTS, TRIGGERS
	}


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
	}

	public String fullPVname(String PVsuffix) {
		return basePVName + PVsuffix;

	}

	public String getBasePVName() {
		return basePVName;
	}

	private void createAcquisitionControlLazyPVs() throws IOException {

	}

	private void createAcquisitionConfigurationLazyPVs() throws IOException {

	}

	public void setStart() throws Exception {

	}

	public void setStop() throws Exception {

	}

	public void setErase() throws Exception {

	}

	public void setEraseStart() throws Exception {

	}

	public boolean getAcquiring() throws Exception {
		return false;
	}

	// Add this method as it is called in CollectionStrategyPlugin
	public int getStatus() throws Exception {
		if (getAcquiring())
			return 1;
		else
			return 0;
	}

	public void setCollectMode(CollectionModeEnum collectMode) throws Exception {
		if (collectMode.equals(CollectionModeEnum.MCA_MAPPING))
			isXmapMappingModeInstance("MCA_MAPPING mode");

	}

	public CollectionModeEnum getCollectMode() throws Exception {
		return CollectionModeEnum.MCA_MAPPING;
	}

	public void setNbins(int nbins) throws Exception {

	}

	public int getNbins() throws Exception {
		return 2048;
	}

	public void setPresetMode(PresetMode presetMode) throws Exception {
		if (presetMode.equals(PresetMode.NO_PRESET))
			isXmapMappingModeInstance("NO_PRESET type");

	}

	public PresetMode getPresetMode() throws Exception {
		return PresetMode.NO_PRESET;
	}

	public void setPresetRealTime(double realTime) throws Exception {

	}

	public double getPresetRealTime() throws Exception {
		return 1.0;
	}

	public void setPresetLiveTime(double liveTime) throws Exception {

	}

	public double getPresetLiveTime() throws Exception {
		return 1.0;
	}

	public void setPresetEvents(int event) throws Exception {

	}

	public int getPresetEvents() throws Exception {
		return 100;
	}

	public void setPresetTriggers(int triggers) throws Exception {

	}

	public int getPresetTriggers() throws Exception {
		return 10;
	}

	public void setAquisitionTime(double presetValue) throws Exception {

	}

	public double getAquisitionTime() throws Exception {
		return 1.0;
	}

	public CollectionMode getCollectionMode() {
		return collectionMode;
	}

	// For now include the getData for Xmap subdetector here! Just used for experimental setup otherwise
	// data written in HDF5 file!

	public double[] getDataPerElement(int subdetector) throws Exception {
		double[] dummyData = new double[getNbins()];
		Random generator = new Random();
		for (int i = 0; i < getNbins(); i++)
			dummyData[i] = generator.nextGaussian();
		return dummyData;
	}

	public boolean isXmapMappingModeInstance(String message) {
		if (!(collectionMode instanceof DummyXmapMappingModeEpicsLayer))
			throw new ClassCastException("For " + message + " CollectionMode object should be of type " + "XmapMappingModeEpicsLayer.");
		else
			return true;
	}
}

