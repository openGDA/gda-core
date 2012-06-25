/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo;

import java.util.ArrayList;

/**
 *
 */
public class DoublePointList {

	private ArrayList<DoublePoint> doublePointList;

	public DoublePointList() {
		doublePointList = new ArrayList<DoublePointList.DoublePoint>();
	}

	public void addPoint(double x, double y) {
		doublePointList.add(new DoublePoint(x, y));
	}

	@Override
	public String toString() {
		StringBuilder strBldr = new StringBuilder();
		for (DoublePoint dp : doublePointList) {
			strBldr.append(dp.toString());
		}
		return strBldr.toString();
	}

	public class DoublePoint {
		private double x;
		private double y;

		public DoublePoint(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public void setX(double x) {
			this.x = x;
		}

		public double getY() {
			return y;
		}

		public void setY(double y) {
			this.y = y;
		}

		@Override
		public String toString() {
			return "[" + Double.toString(x) + "," + Double.toString(y) + "]";
		}

	}

	public ArrayList<DoublePoint> getDoublePointList() {
		return doublePointList;
	}

	public double[] getXDoubleArray() {
		double[] doublearr = new double[doublePointList.size()];
		int count = 0;
		for (DoublePoint d : doublePointList) {
			doublearr[count++] = d.x;
		}
		return doublearr;
	}

	public double[] getYDoubleArray() {
		double[] doublearr = new double[doublePointList.size()];
		int count = 0;
		for (DoublePoint d : doublePointList) {
			doublearr[count++] = d.y;
		}
		return doublearr;
	}
}
