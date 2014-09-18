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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.roi;

import org.dawb.common.ui.plot.roi.data.GridROIData;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.GridProfile;

/**
 * Class that extends a table viewer for linear regions of interests
 */
@Deprecated
public final class GridROITableViewer extends ROITableViewer {
	private GridProfile gridProfile;
	
	/**
	 * @param parent
	 * @param slistener
	 * @param clistener
	 */
	public GridROITableViewer(Composite parent, SelectionListener slistener,
			ICellEditorListener clistener, GridProfile gridProfile) {
		super(parent, slistener, clistener);
		this.gridProfile = gridProfile;
	}

	
	@Override
	public String content(Object element, int columnIndex) {
		String msg = null;

		GridROIData cROIData = (GridROIData) element;
		if (cROIData != null) {
			GridROI cROI = (GridROI)cROIData.getROI();
			switch (columnIndex) {
			case 1:
				msg = String.format("%.2f", gridProfile.getGridPrefs().getXMicronsFromPixelsCoord(cROI.getPointX()));
				break;
			case 2:
				msg = String.format("%.2f", gridProfile.getGridPrefs().getYMicronsFromPixelsCoord(cROI.getPointY()));
				break;
			case 3:
				msg = String.format("%.2f", gridProfile.getGridPrefs().getXMicronsFromPixelsLen(cROI.getLength(0)));
				break;
			case 4:
				msg = String.format("%.2f", gridProfile.getGridPrefs().getYMicronsFromPixelsLen(cROI.getLength(1)));
				break;
			case 5:
				msg = String.format("%.2f", gridProfile.getGridPrefs().getXMicronsFromPixelsLen(cROI.getxSpacing()));
				break;
			case 6:
				msg = String.format("%.2f", gridProfile.getGridPrefs().getYMicronsFromPixelsLen(cROI.getySpacing()));
				break;
			case 7:
				msg = cROI.isGridLineOn() ? "Y" : "N";
				break;
			case 8:
				msg = cROI.isMidPointOn() ? "Y" : "N";
				break;
			case 9:
				msg = String.format("%.2f", cROI.getAngleDegrees());
				break;
			case 10:
				if (cROI.isClippingCompensation())
					msg = "Y";
				else
					msg = "N";
				break;
			case 11:
				msg = String.format("%.2f", cROIData.getProfileSum());
				break;
			}
		}
		return msg;
	}

	@Override
	public String[] getTitles() {
		return new String[] { "Plot", "x_s", "y_s", "w", "h", "x_sp", "y_sp", "grid", "mid", "phi", "Clip", "Sum" };
	}

	@Override
	public int[] getWidths() {
		return new int[] { 40, 50, 50, 70, 70, 50, 50, 40, 40, 70, 40, 80 };
	}

	@Override
	public String[] getTipTexts() {
		return new String[] { "Plot", "Start x", "Start y", "Width", "Height", "X Spacing", "Y Spacing", "Grid Lines", "Mid Point Marks", "Angle", "Clipping compensation", "Sum of profile" };
	}
}
