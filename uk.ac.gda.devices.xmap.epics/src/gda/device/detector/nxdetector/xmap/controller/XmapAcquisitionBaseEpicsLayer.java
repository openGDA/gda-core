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

public interface XmapAcquisitionBaseEpicsLayer {

	String fullPVname(String PVsuffix);

	String getBasePVName();

	void setStart() throws Exception;

	void setStop() throws Exception;

	void setErase() throws Exception;

	void setEraseStart() throws Exception;

	boolean getAcquiring() throws Exception;

	// Add this method as it is called in CollectionStrategyPlugin
	int getStatus() throws Exception;

	void setCollectMode(CollectionModeEnum collectMode) throws Exception;

	CollectionModeEnum getCollectMode() throws Exception;

	void setNbins(int nbins) throws Exception;

	int getNbins() throws Exception;

	void setPresetMode(PresetMode presetMode) throws Exception;

	PresetMode getPresetMode() throws Exception;

	void setPresetRealTime(double realTime) throws Exception;

	double getPresetRealTime() throws Exception;

	void setPresetLiveTime(double liveTime) throws Exception;

	double getPresetLiveTime() throws Exception;

	void setPresetEvents(int event) throws Exception;

	int getPresetEvents() throws Exception;

	void setPresetTriggers(int triggers) throws Exception;

	int getPresetTriggers() throws Exception;

	void setAquisitionTime(double presetValue) throws Exception;

	double getAquisitionTime() throws Exception;

	CollectionMode getCollectionMode();

	boolean isXmapMappingModeInstance(String message);

	double[] getDataPerElement(int i) throws IOException, Exception;
}
