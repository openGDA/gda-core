/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.plots;

import java.awt.Color;
import java.io.IOException;

import org.eclipse.january.dataset.DoubleDataset;

import gda.scan.AxisSpec;

public interface XYDataHandler {
	void setXAxisLabel(String label);
	void setYAxisLabel(String label);
	void archive(boolean all, String archiveFolder) throws IOException;
	void addPointToLine(int which, double x, double y) ;
	int getNextAvailableLine();
	void setLineVisibility(int which, boolean visibility) ;
	Color getLineColor(int which);
	Marker getLineMarker(int which);
	public static int LEFTYAXIS = 0; //taken from SimplePlot

	void dispose();
	void deleteAllLines();

	//needed by PlotTreeLegend
	void setLineColor(int which, Color color);
	void setLineMarker(int which, Marker marker);
	void initializeLine(int which, int axis, String name, String xLabel, String yLabel, String dataFileName, AxisSpec yAxisSpec);
	void deleteLine(int which);

	void onUpdate(boolean force);
	void setsPointsForLine(int which, DoubleDataset xData, DoubleDataset yData);
}
