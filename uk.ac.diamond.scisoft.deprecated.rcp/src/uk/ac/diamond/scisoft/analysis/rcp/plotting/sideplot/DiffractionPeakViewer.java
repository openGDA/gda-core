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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;


import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
@Deprecated
public class DiffractionPeakViewer extends Composite {
	
	private DataSetPlotter plotter;
	private HistogramUpdate update;
	
	public DiffractionPeakViewer(Composite parent, int style) {
		super(parent,style);
		setLayout(new FillLayout(SWT.VERTICAL));
		plotter = new DataSetPlotter(PlottingMode.SURF2D, this,false);
		plotter.setAxisModes(AxisMode.LINEAR_WITH_OFFSET, AxisMode.LINEAR_WITH_OFFSET, AxisMode.LINEAR);
		plotter.setZAxisLabel("Intensity");
	}
	
	public void processROI(IDataset data, RectangularROI rectROI) {
		if (rectROI.getLengths()[0] <= 1 || rectROI.getLengths()[1] <= 1 || data.getSize() <= 1)
			return;

		int [] startPoint = rectROI.getIntPoint();
		int [] stopPoint = rectROI.getIntPoint(1, 1);
		IDataset ROIdata = data.getSlice(new int[]{startPoint[1],startPoint[0]}, new int []{stopPoint[1],stopPoint[0]},new int[]{1,1});
		plotter.setAxisOffset(rectROI.getPointX(), rectROI.getPointY(), 0.0);

		try {
			plotter.replaceCurrentPlot(ROIdata);
		} catch (PlotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (update !=null){
			plotter.applyColourCast(update.getRedMapFunction(), update.getGreenMapFunction(), update
					.getBlueMapFunction(), update.getAlphaMapFunction(), update.inverseRed(), update.inverseGreen(),
					update.inverseBlue(), update.inverseAlpha(), update.getMinValue(), update.getMaxValue());
		}
		plotter.refresh(true);
	}
	
	@Override
	public void dispose() {
		if(plotter != null) plotter.cleanUp();
	}

	public void sendHistogramUpdate(HistogramUpdate update) {
		this.update = update;
		if(plotter == null)
			return;
		plotter.applyColourCast(update.getRedMapFunction(), update.getGreenMapFunction(), update
				.getBlueMapFunction(), update.getAlphaMapFunction(), update.inverseRed(), update.inverseGreen(),
				update.inverseBlue(), update.inverseAlpha(), update.getMinValue(), update.getMaxValue());
		plotter.refresh(true);
	}
}
