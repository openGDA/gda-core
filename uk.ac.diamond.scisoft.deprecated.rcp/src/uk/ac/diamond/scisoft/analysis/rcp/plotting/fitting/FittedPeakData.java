/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.fitting;

import java.awt.Color;

import org.dawb.common.ui.plot.roi.data.IRowData;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.swt.graphics.RGB;

@Deprecated
public class FittedPeakData implements IRowData {

	private IPeak fittedPeak;
	private Color peakColour;
	private boolean plot = true;
	
	public FittedPeakData(IPeak peak, Color colour) {
		fittedPeak = peak;
		peakColour = colour;
	}
	
	@Override
	public boolean isPlot() {
		return plot;
	}

	@Override
	public void setPlot(boolean require) {
		plot = require;
	}

	public Color getPeakColour() {
		return peakColour;
	}

	public void setPeakColour(Color peakColour) {
		this.peakColour = peakColour;
	}

	public IPeak getFittedPeak() {
		return fittedPeak;
	}

	@Override
	public RGB getPlotColourRGB() {
		return new RGB(peakColour.getRed(), peakColour.getGreen(), peakColour.getBlue());
	}
}
