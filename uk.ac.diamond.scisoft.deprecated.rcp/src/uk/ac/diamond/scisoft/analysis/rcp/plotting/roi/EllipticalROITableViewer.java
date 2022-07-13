/*-
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

import org.dawb.common.ui.plot.roi.data.EllipticalROIData;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

/**
 * Class that extends a table viewer for linear regions of interests
 */
public final class EllipticalROITableViewer extends ROITableViewer {
	/**
	 * @param parent
	 * @param slistener
	 * @param clistener
	 */
	public EllipticalROITableViewer(Composite parent, SelectionListener slistener,
			ICellEditorListener clistener) {
		super(parent, slistener, clistener);
	}

	@Override
	public String content(Object element, int columnIndex) {
		String msg = null;
		
		EllipticalROIData cROIData = (EllipticalROIData) element;
		if (cROIData != null) {
			EllipticalROI cROI = (EllipticalROI) cROIData.getROI();
			switch (columnIndex) {
			case 1:
				msg = Double.toString(cROI.getPointX());
				break;
			case 2:
				msg = Double.toString(cROI.getPointY());
				break;
			case 3:
				msg = Double.toString(cROI.getSemiAxis(0));
				break;
			case 4:
				msg = Double.toString(cROI.getSemiAxis(1));
				break;
			case 5:
				msg = Double.toString(cROI.getAngleDegrees());
				break;
			}
		}
		return msg;
	}

	@Override
	public String[] getTitles() {
		return new String[] { "Plot", "x_c", "y_c", "maj", "min", "ang" };
	}

	@Override
	public int[] getWidths() {
		return new int[] { 40, 50, 50 };
	}

	@Override
	public String[] getTipTexts() {
		return new String[] { "Plot", "Centre x", "Centre y", "Major radius", "Minor radius", "Major axis angle" };
	}
}
