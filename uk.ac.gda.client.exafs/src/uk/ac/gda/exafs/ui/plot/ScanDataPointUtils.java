/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.plot;

import gda.scan.IScanDataPoint;

import java.util.List;

public class ScanDataPointUtils {
	
	public static double getFF(final IScanDataPoint point) {
		if (point == null)
			return 0;
		final Double[] data = point.getDetectorDataAsDoubles();
		final List<String> names = point.getDetectorHeader();
		double ff = Double.NaN;
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).toLowerCase().equals("ff")) {
				ff = data[i];
			}
		}
		return ff;
	}
	
	public static double getFFI0(final IScanDataPoint point) {
		if (point == null)
			return 0;
		final Double[] data = point.getDetectorDataAsDoubles();
		final List<String> names = point.getDetectorHeader();
		double ff = Double.NaN;
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).toLowerCase().equals("ffi0") || names.get(i).toLowerCase().equals("ffi1")) {
				ff = data[i];
			}
		}
		return ff;
	}

	
	public static double getI0(final IScanDataPoint point) {
		if (point == null)
			return 0;
		final Double[] data = point.getDetectorDataAsDoubles();
		final List<String> names = point.getDetectorHeader();
		double i0 = Double.NaN;
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).toLowerCase().equals("i0") || names.get(i).toLowerCase().equals("i1")) {
				i0 = data[i];
			}
		}
		return i0;
	}
	
	public static double getIt(final IScanDataPoint point) {
		if (point == null)
			return 0;
		final Double[] data = point.getDetectorDataAsDoubles();
		final List<String> names = point.getDetectorHeader();
		double it = Double.NaN;
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).toLowerCase().equals("it")) {
				it = data[i];
			}
		}
		return it;
	}
	
}
