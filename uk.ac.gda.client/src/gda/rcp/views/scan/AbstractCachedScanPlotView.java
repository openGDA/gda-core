/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp.views.scan;

import gda.rcp.util.ScanDataPointEvent;
import gda.scan.IScanDataPoint;

import java.util.ArrayList;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;


/**
 * Extend to deal with plots which are simple functions.
 */
public abstract class AbstractCachedScanPlotView extends AbstractScanPlotView {

	protected List<Double>  cachedX,cachedY;
	protected String        xAxisTitle;

	@Override
	public void scanStopped() {
		super.scanStopped();
        if (cachedX==null)   cachedX     = new ArrayList<Double>(89);
        if (cachedY==null)   cachedY     = new ArrayList<Double>(89);
        cachedX.clear();
        cachedY.clear();
	}
	
	@Override
	public void scanStarted() {
		super.scanStarted();
        if (cachedX==null)    cachedX    = new ArrayList<Double>(89);
        if (cachedY==null)    cachedY    = new ArrayList<Double>(89);
    }

	@Override
	public void scanDataPointChanged(ScanDataPointEvent e) {
        if (cachedX==null)    cachedX    = new ArrayList<Double>(89);
        if (cachedY==null)    cachedY    = new ArrayList<Double>(89);
		super.scanDataPointChanged(e);
	}
	@Override
	protected IPlotData getX(IScanDataPoint... points) {
        
		if (cachedX==null)    cachedX    = new ArrayList<Double>(89);
       
		for (int i = 0; i < points.length; i++) {
			final IScanDataPoint point   = points[i];
			final Double[]      data    = point.getAllValuesAsDoubles();
		    cachedX.add(data[0]);
		    xAxisTitle = point.getPositionHeader().get(0);
		}
		return new PlotData(xAxisTitle, cachedX);
	}

	@Override
	protected String getXAxis() {
		return xAxisTitle;
	}

	@Override
	protected void plotPointsFromService() throws Exception {
	    
        if (cachedX==null)   cachedX     = new ArrayList<Double>(89);
        if (cachedY==null)   cachedY     = new ArrayList<Double>(89);
        
        cachedX.clear();
        cachedY.clear();

        super.plotPointsFromService();
	}

	public List<Double> testGetCachedX() {
		return this.cachedX;
	}
	
	public List<Double> testGetCachedY() {
		return this.cachedY;
	}
}