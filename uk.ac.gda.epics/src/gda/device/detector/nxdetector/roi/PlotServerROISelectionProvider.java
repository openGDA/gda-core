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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROIList;
//import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * 
 */
public class PlotServerROISelectionProvider implements RectangularROIProvider<Integer>{
	
	final private int maximumActiveRois;
	
	private String viewName;

	public PlotServerROISelectionProvider(String detectorName, int maximumActiveRois) {
		viewName = detectorName + " Array View";  // WARNING: Duplicated in TwoDArray and TwoDArrayView
		this.maximumActiveRois = maximumActiveRois;
	}

	/**
	 * Returns a list of active Rois. This will have no more than maximumActiveRois elements.
	 * @return a list of active rois.
	 * @throws Exception 
	 */
	public List<RectangularROI<Integer>> getActiveRoiList() throws Exception {
		List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> scisoftRoiList = getScisoftRoiListFromSDAPlotter();
		ArrayList<RectangularROI<Integer>> roiList = new ArrayList<RectangularROI<Integer>>();
		for (uk.ac.diamond.scisoft.analysis.roi.RectangularROI scisoftRoi : scisoftRoiList) {
			roiList.add(ImutableRectangularIntegerROI.valueOf(scisoftRoi));
		}
		return roiList;
	}

	private List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> getScisoftRoiListFromSDAPlotter() throws Exception {
		GuiBean guiBean;
		try {
			guiBean = SDAPlotter.getGuiBean(viewName);
		} catch (Exception eOriginal) {
			String[] guiNames;
			try {
				guiNames = SDAPlotter.getGuiNames();
			} catch (Exception e1) {
				throw new Exception("Problem communicating with SDAPlotter", e1);
			}
			throw new Exception(MessageFormat.format(
					"Problem getting gui bean for view named ''{0}''. Available: {1}",
					viewName, Arrays.toString(guiNames)), eOriginal);
		}
		List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> scisoftRoiList = (RectangularROIList) guiBean.get(GuiParameters.ROIDATALIST);
		return scisoftRoiList;
	}

	@Override
	public RectangularROI<Integer> getROI(int index) throws IndexOutOfBoundsException, Exception {
		if (index >= maximumActiveRois) {
			throw new IndexOutOfBoundsException("Maximum index is: " + maximumActiveRois);
		}
		List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> scisoftRoiList = getScisoftRoiListFromSDAPlotter();
		if (index >= scisoftRoiList.size()) {
			return null;
		}
		return ImutableRectangularIntegerROI.valueOf(scisoftRoiList.get(index));
	}

}
