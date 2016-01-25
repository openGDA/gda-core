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

import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROITableViewer;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;

@Deprecated
public final class FittedPeakTableViewer extends ROITableViewer {
	
	
	
	public FittedPeakTableViewer(Composite parent, SelectionListener slistener,
			ICellEditorListener clistener, ISelectionChangedListener scListener) {
		super(parent, slistener, clistener);
		this.addLeftClickListener(scListener);
	}
	
	@Override
	public
	String content(Object element, int columnIndex) {
		String msg = null;

		FittedPeakData peakData = (FittedPeakData) element;
		if (peakData != null) {
			IPeak apeak = peakData.getFittedPeak();
			switch (columnIndex) {
			case 1:
				msg = formatCellToCorrectDP(apeak.getPosition());
				break;
			case 2 :
				msg = formatCellToCorrectDP(apeak.getFWHM());
				break;
			case 3:
				msg = formatCellToCorrectDP(apeak.getArea());
				break;
			case 4:
				msg = apeak.getClass().getSimpleName();
				break;
			}
		}
		return msg;
	}

	@Override
	public
	String[] getTitles() {
		return new String[] { "Visible", "Peak Position", "FWHM", "Area", "Name"};
	}

	@Override
	public
	int[] getWidths() {
		return new int[] { 60, 100, 100, 100, 150 };
	}

	@Override
	public
	String[] getTipTexts() {
		return new String[] { "Visible", "Peak position", "Full width at half maximum", "Area under peak",
				"Name of distribution"};
	}
	
	private String formatCellToCorrectDP(double tableValue){
		int decimalPlaces = AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.FITTING_1D_DECIMAL_PLACES);
		return String.format("%."+decimalPlaces+"f", tableValue);
		
	}
	
}
