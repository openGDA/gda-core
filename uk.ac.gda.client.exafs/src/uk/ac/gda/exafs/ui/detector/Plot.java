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

package uk.ac.gda.exafs.ui.detector;

import gda.device.detector.xspress.ResGrades;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.richbeans.components.wrappers.ComboAndNumberWrapper;

public class Plot {
	private SashFormPlotComposite sashPlotFormComposite;
	private Counts counts;
	
	public Plot(SashFormPlotComposite sashPlotFormComposite, Counts counts) {
		this.sashPlotFormComposite = sashPlotFormComposite;
		this.counts = counts;
	}

	public void plot(int ielement, boolean updateTitle, int[][][] detectorData, DetectorElementComposite detectorElementComposite, int currentSelectedElementIndex, boolean isAdditiveResolutionGradeMode, ComboAndNumberWrapper resolutionGradeCombo) {
		final List<AbstractDataset> data = unpackDataSets(ielement, detectorData, isAdditiveResolutionGradeMode, resolutionGradeCombo);
		String plotTitle = null;
		if (updateTitle) {
			Date now = new Date();
			SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
			plotTitle = "Acquire at " + dt.format(now);
		}
		for (int i = 0; i < data.size(); i++) {
			String name = "" + ielement;
			if (data.size() > 1)
				name += " " + i;
			name += " " + plotTitle;
			data.get(i).setName(name);
		}
		sashPlotFormComposite.setDataSets(data.toArray(new AbstractDataset[data.size()]));
		sashPlotFormComposite.getPlottingSystem().setRescale(updateTitle);
		sashPlotFormComposite.plotData();
		sashPlotFormComposite.getPlottingSystem().setTitle(plotTitle);
		counts.calculateAndPlotCountTotals(true, true, detectorData, detectorElementComposite, currentSelectedElementIndex);
		sashPlotFormComposite.getPlottingSystem().setRescale(false);
	}
	
	protected List<AbstractDataset> unpackDataSets(int ielement, int[][][] detectorData, boolean isAdditiveResolutionGradeMode, ComboAndNumberWrapper resolutionGradeCombo) {
		List<AbstractDataset> ret = new ArrayList<AbstractDataset>(7);
		if(resolutionGradeCombo!=null)
			if (ielement < 0 || detectorData == null || !isAdditiveResolutionGradeMode || !resolutionGradeCombo.getValue().equals(ResGrades.ALLGRADES)){
				if (ielement < 0 || detectorData == null) {
					IntegerDataset ds = new IntegerDataset(new int[]{});
					ret.add(ds);
					return ret;
				}
				int[][] data = detectorData[ielement];
				for (int i = 0; i < data.length; i++) {
					IntegerDataset ds = new IntegerDataset(data[i], data[i].length);
					ret.add(ds);
				}
				return ret;
			}
		return unpackDataSets(ielement, detectorData);
	}
	
	protected List<AbstractDataset> unpackDataSets(int ielement, int[][][] detectorData) {
		List<AbstractDataset> ret = new ArrayList<AbstractDataset>(7);
		int[][] elementData = detectorData[ielement];
		for (int resGrade = 0; resGrade < elementData.length; resGrade++) {
			AbstractDataset d = new DoubleDataset(Arrays.copyOf(elementData[resGrade],elementData[resGrade].length));
			if (!ret.isEmpty()) {
				AbstractDataset p = ret.get(resGrade - 1);
				d.iadd(p);
			}
			ret.add(d);
		}
		return ret;
	}
	
}