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

import gda.device.detector.nxdetector.xmap.controller.DummyXmapAcquisitionBaseEpicsLayer.CollectionModeEnum;

import java.io.IOException;

public class DummyXmapMappingModeEpicsLayer extends CollectionMode {
	public enum ListMode {
		EAndGate, EAndSync, EAndClock
	}

	public enum PixelAdvanceMode {
		Gate, Sync
	}


	/* Here the CollectMode will be not used as the PV is already created in the XmapAcquisitionBase class */

	private enum MappingSettingsPVname {
		ListMode_RBV, PixelAdvanceMode, PixelAdvanceMode_RBV, SyncCount, SyncCount_RBV, IgnoreGate, IgnoreGate_RBV, InputLogicPolarity, PixelsPerRun, PixelsPerRun_RBV
	}

	private enum PixelsPerBufferPVname {
		AutoPixelsPerBuffer, AutoPixelsPerBuffer_RBV, PixelsPerBuffer, PixelsPerBuffer_RBV, BufferSize
	}

	private enum RunTimePV {
		NextPixel("NextPixel"), CurrentPixel("DXP1:CurrentPixel");
		private final String PVName;

		RunTimePV(String pvname) {
			this.PVName = pvname;
		}

		@Override
		public String toString() {
			return PVName;
		}
	}

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

	public String fullPVname(String PVsuffix) {
		return basePVName + PVsuffix;
	}

	private void createMappingSettingsLazyPVs() {
	}

	private void createPixelsPerBufferLazyPVs() {
	}

	private void createRuntimeLazyPVs() {
	}

	public ListMode getListMode_RBV() throws IOException {
		return ListMode.EAndGate;
	}

	public void setPixelAdvanceMode(PixelAdvanceMode pixelAdvanceMode) throws IOException {
	}

	public PixelAdvanceMode getPixelAdvanceMode() throws IOException {
		return PixelAdvanceMode.Gate;
	}

	public void setSyncCount(int syncCount) throws IOException {
	}

	public int getSyncCount() throws IOException {
		return 1;
	}

	public void setIgnoreGate(boolean ignoreGate) throws IOException {

	}

	public boolean getIgnoreGate() throws IOException {
		return false;
	}

	public void setInputLogicPolarity(boolean inputLogicParity) throws IOException {

	}

	public boolean getInputLogicPolarity() throws IOException {
		return true;
	}

	public void setPixelsPerRun(int pixelsPerRun) throws IOException {

	}

	public int getPixelsPerRun() throws IOException {
		return 2;
	}

	public void setAutoPixelsPerBuffer(boolean autoPixelsPerBuffer) throws IOException {

	}

	public boolean getAutoPixelsPerBuffer() throws IOException {
		return false;
	}

	public void setPixelsPerBuffer(int pixelsPerBuffer) throws IOException {

	}

	public int getPixelsPerBuffer() throws IOException {
		return 10;
	}

	public void setNextPixel() throws IOException {

	}

	public int getCurrentPixel() throws IOException {
		return 20;
	}

}
