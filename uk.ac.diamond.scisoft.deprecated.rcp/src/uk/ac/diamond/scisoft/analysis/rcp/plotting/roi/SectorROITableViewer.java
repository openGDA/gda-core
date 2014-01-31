/*
 * Copyright 2012 Diamond Light Source Ltd.
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

import org.dawb.common.ui.plot.roi.data.SectorROIData;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * Class that extends a table viewer for linear regions of interests
 */
public final class SectorROITableViewer extends ROITableViewer {
	/**
	 * @param parent
	 * @param slistener
	 * @param clistener
	 */
	public SectorROITableViewer(Composite parent, SelectionListener slistener,
			ICellEditorListener clistener) {
		super(parent, slistener, clistener);
	}

	@Override
	public String content(Object element, int columnIndex) {
		String msg = null;
		
		SectorROIData cROIData = (SectorROIData) element;
		if (cROIData != null) {
			SectorROI cROI = cROIData.getROI();
			switch (columnIndex) {
			case 1:
				msg = Integer.toString((int) cROI.getPointX());
				break;
			case 2:
				msg = Integer.toString((int) cROI.getPointY());
				break;
			case 3:
				msg = String.format("%.2f", cROI.getRadius(0));
				break;
			case 4:
				msg = String.format("%.2f", cROI.getRadius(1));
				break;
			case 5:
				msg = String.format("%.1f", cROI.getAngleDegrees(0));
				break;
			case 6:
				msg = String.format("%.1f", cROI.getAngleDegrees(1));
				break;
			case 7:
				if (cROI.isClippingCompensation())
					msg = "Y";
				else
					msg = "N";
				break;
			case 8:
				msg = cROI.getSymmetryText();
				break;
			case 9:
				if (cROI.isCombineSymmetry())
					msg = "Y";
				else
					msg = "N";
				break;

			case 10:
				msg = String.format("%.2f", cROIData.getProfileSum());
				break;
			}
		}
		return msg;
	}

	@Override
	public String[] getTitles() {
		return new String[] { "Plot", "x_c", "y_c", "r_i", "r_o", "phi_s", "phi_e", "Clip", "Sym", "Combine", "Sum" };
	}

	@Override
	public int[] getWidths() {
		return new int[] { 40, 50, 50, 70, 70, 70, 70, 40, 40, 40, 80 };
	}

	@Override
	public
	String[] getTipTexts() {
		return new String[] { "Plot", "Centre x", "Centre y", "Inner radius", "Outer radius", "Start angle", "End angle",
				"Clipping compensation", "Symmetry for ROI", "Combine symmetric sector", "Sum of profile" };
	}
}
