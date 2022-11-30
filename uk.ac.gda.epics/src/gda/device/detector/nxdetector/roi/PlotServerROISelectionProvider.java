/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.roi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROIList;
import org.python.core.PyString;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

public class PlotServerROISelectionProvider implements IndexedRectangularROIProvider<Integer>{

	private int maximumActiveRois;
	private String viewName;
	private static final String ROIViewNameSuffix = " Array";

	public static String getGuiName(String detectorName){
		return detectorName + ROIViewNameSuffix;
	}

	public PlotServerROISelectionProvider() {
	}

	public PlotServerROISelectionProvider(String detectorName, int maximumActiveRois) {
		viewName = getGuiName(detectorName);
		this.maximumActiveRois = maximumActiveRois;
	}

	/**
	 * Returns a list of active Rois. This will have no more than maximumActiveRois elements.
	 * @return a list of active rois.
	 * @throws Exception
	 */
	public List<RectangularROI<Integer>> getActiveRoiList() throws Exception {
		List<org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI> scisoftRoiList = getScisoftRoiListFromSDAPlotter();
		ArrayList<RectangularROI<Integer>> roiList = new ArrayList<RectangularROI<Integer>>();
		for (org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI scisoftRoi : scisoftRoiList) {
			if (scisoftRoi.isPlot()) {
				roiList.add(ImutableRectangularIntegerROI.valueOf(scisoftRoi));
			}
		}
		return roiList;
	}

	public List<org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI> getScisoftRoiListFromSDAPlotter() throws Exception {
		// GuiBean is created if it does not exist
		GuiBean guiBean = SDAPlotter.getGuiBean(viewName);
		Serializable serializable = guiBean.get(GuiParameters.ROIDATALIST);

		List<org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI> scisoftRoiList;
		if (serializable instanceof PerimeterBoxROIList) {
			scisoftRoiList = new Vector<org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI>((PerimeterBoxROIList) guiBean.get(GuiParameters.ROIDATALIST));
		} else {
			scisoftRoiList = (RectangularROIList) guiBean.get(GuiParameters.ROIDATALIST);
		}
		if (scisoftRoiList == null) {
			scisoftRoiList = new Vector<org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI>();
		}
		// Sort the ROI list so we get the same order every time
		// Otherwise the currently selected ROI is always first
		// TODO: replace with scisoftRoiList.sort((r1, r2) -> r1.getName().compareTo(r2.getName())); for Java 8
		scisoftRoiList.sort(new Comparator<org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI>() {
			@Override
			public int compare(org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI o1,
					org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI o2) {
				String s1 = o1.getName();
				String s2 = o2.getName();
				return s1.compareTo(s2);
			}
		});
		return scisoftRoiList;
	}

	@Override
	public RectangularROI<Integer> getRoi(int index) throws IndexOutOfBoundsException, Exception {
		if (index >= maximumActiveRois) {
			throw new IndexOutOfBoundsException("Maximum index is: " + maximumActiveRois);
		}
		List<RectangularROI<Integer>> scisoftRoiList = getActiveRoiList();
		if (index >= scisoftRoiList.size()) {
			return null;
		}
		return scisoftRoiList.get(index);
	}

	public PyString __str__() {
		String str = "Regions from: '" + viewName +"'";
		str += "\n";
		List<RectangularROI<Integer>> roiList;
		try {
			roiList = getActiveRoiList();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < maximumActiveRois; i++) {
			String roiRepr;
			if ((i >= roiList.size()) || (roiList.get(i) == null)) {
				roiRepr = "---";
			} else {
				roiRepr = roiList.get(i).toString();
			}
			str += (i + 1) + ". " + roiRepr + "\n";
		}
		return new PyString(str);
	}

	public int getMaximumActiveRois() {
		return maximumActiveRois;
	}

	public void setMaximumActiveRois(int maximumActiveRois) {
		this.maximumActiveRois = maximumActiveRois;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
}
