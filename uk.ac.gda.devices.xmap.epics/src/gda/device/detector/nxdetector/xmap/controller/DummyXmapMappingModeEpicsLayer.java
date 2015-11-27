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
import gda.device.detector.nxdetector.xmap.controller.XmapModes.ListMode;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.PixelAdvanceMode;

import java.io.IOException;

public class DummyXmapMappingModeEpicsLayer extends CollectionMode implements XmapMappingModeEpicsLayer {

	private String basePVName;
	CollectionModeEnum collectMode;

	public DummyXmapMappingModeEpicsLayer(String basePVname) {
		this.collectMode = CollectionModeEnum.MCA_MAPPING;
		this.basePVName = basePVname;
		if (basePVname == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		createMappingSettingsLazyPVs();
		createPixelsPerBufferLazyPVs();
		createRuntimeLazyPVs();
	}

	@Override
	public String fullPVname(String PVsuffix) {
		return basePVName + PVsuffix;
	}

	private void createMappingSettingsLazyPVs() {
	}

	private void createPixelsPerBufferLazyPVs() {
	}

	private void createRuntimeLazyPVs() {
	}

	@Override
	public ListMode getListMode_RBV() throws IOException {
		return ListMode.EAndGate;
	}

	@Override
	public void setPixelAdvanceMode(PixelAdvanceMode pixelAdvanceMode) throws IOException {
	}

	@Override
	public PixelAdvanceMode getPixelAdvanceMode() throws IOException {
		return PixelAdvanceMode.Gate;
	}

	@Override
	public void setSyncCount(int syncCount) throws IOException {
	}

	@Override
	public int getSyncCount() throws IOException {
		return 1;
	}

	@Override
	public void setIgnoreGate(boolean ignoreGate) throws IOException {

	}

	@Override
	public boolean getIgnoreGate() throws IOException {
		return false;
	}

	@Override
	public void setInputLogicPolarity(boolean inputLogicParity) throws IOException {

	}

	@Override
	public boolean getInputLogicPolarity() throws IOException {
		return true;
	}

	@Override
	public void setPixelsPerRun(int pixelsPerRun) throws IOException {

	}

	@Override
	public int getPixelsPerRun() throws IOException {
		return 2;
	}

	@Override
	public void setAutoPixelsPerBuffer(boolean autoPixelsPerBuffer) throws IOException {

	}

	@Override
	public boolean getAutoPixelsPerBuffer() throws IOException {
		return false;
	}

	@Override
	public void setPixelsPerBuffer(int pixelsPerBuffer) throws IOException {

	}

	@Override
	public int getPixelsPerBuffer() throws IOException {
		return 10;
	}

	@Override
	public void setNextPixel() throws IOException {

	}

	@Override
	public int getCurrentPixel() throws IOException {
		return 20;
	}

}
