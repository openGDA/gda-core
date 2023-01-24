package gda.device.detector.nxdetector.xmap.controller;

import gda.device.detector.nxdetector.xmap.controller.XmapModes.ListMode;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.PixelAdvanceMode;

import java.io.IOException;

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

public interface XmapMappingModeEpicsLayer {

	String fullPVname(String PVsuffix);

	ListMode getListMode_RBV() throws IOException;

	void setPixelAdvanceMode(PixelAdvanceMode pixelAdvanceMode) throws IOException;

	PixelAdvanceMode getPixelAdvanceMode() throws IOException;

	void setSyncCount(int syncCount) throws IOException;

	int getSyncCount() throws IOException;

	void setIgnoreGate(boolean ignoreGate) throws IOException;

	boolean getIgnoreGate() throws IOException;

	void setInputLogicPolarity(boolean inputLogicParity) throws IOException;

	boolean getInputLogicPolarity() throws IOException;

	void setPixelsPerRun(int pixelsPerRun) throws IOException;

	int getPixelsPerRun() throws IOException;

	void setAutoPixelsPerBuffer(boolean autoPixelsPerBuffer) throws IOException;

	boolean getAutoPixelsPerBuffer() throws IOException;

	void setPixelsPerBuffer(int pixelsPerBuffer) throws IOException;

	int getPixelsPerBuffer() throws IOException;

	void setNextPixel() throws IOException;

	int getCurrentPixel() throws IOException;

}
