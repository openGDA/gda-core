/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.util;

import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.scan.ScanBase;
import gda.scan.ScanPositionProvider;

public class ScanPositionsTwoWay implements ScanPositionProvider {
	
	private double start;
	private double stop;
	private double step;
	private double[] points;
	private boolean forward;

	public ScanPositionsTwoWay(Scannable firstScannable, double start, double stop, double step) throws Exception
	{
		this.start = start;
		this.stop = stop;
		this.step = (Double) ScanBase.sortArguments(start, stop, step);
		int numberSteps = ScannableUtils.getNumberSteps(firstScannable, this.start, this.stop, this.step);
		this.points = new double[numberSteps + 1];
		this.points[0]=start;
		double previousPoint = start;
		double nextPoint = start;
		for (int i =1; i<= numberSteps;i++){
			            nextPoint = (Double) ScannableUtils.calculateNextPoint(previousPoint, this.step);
			            this.points[i] = nextPoint;
			            previousPoint = nextPoint;
		}
		this.forward=false;
	}

	@Override
	public Object get(int index) {
		int  max_index = this.size()-1;
		Object val = null;
		if (index > max_index)
			throw new IndexOutOfBoundsException("Position "+ index+" is outside possible range : "+max_index);
		if (this.forward){
			val = this.points[index];
			if (index == max_index)
				this.forward = false;
		}
		else
		{
			val = this.points[max_index-index];
			if (index == max_index)
			this.forward = true;
		}
		return val;
	}

	@Override
	public int size() {
		return points.length;
	}
}
