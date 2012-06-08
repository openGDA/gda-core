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

package gda.scan;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

import java.util.List;
import java.util.Vector;

public abstract class ScanPlotSettingsUtils {
	/**
	 * Returns a ScanPlotSettings objects. If any of XaxisIndex, YAxesShownIndices or YAxesNotShownIndices are Null, the
	 * corresponding value in the resulting ScanPlotSettings will also be null.
	 * 
	 * @param fieldsToChange
	 *            Input fields (only) of scannables to be scanned over
	 * @param fields
	 *            Input and Extra fields of all scannables (scanned over, moved, or read)
	 * @param XaxisIndex
	 * @param YAxesShownIndices
	 * @param YAxesNotShownIndices
	 * @return ScanPlotSettings
	 * @throws Exception
	 */
	public static ScanPlotSettings createSettings(List<String> fieldsToChange, List<String> fields, Integer XaxisIndex,
			List<Integer> YAxesShownIndices, List<Integer> YAxesNotShownIndices) throws Exception {
		try {
			String xAxisName = null;
			{
				if (XaxisIndex != null) {
					int index = (XaxisIndex >= 0) ? XaxisIndex : fieldsToChange.size() + XaxisIndex;
					if (index < 0 || index >= fieldsToChange.size()) {
						throw new IllegalArgumentException("XaxisIndex is invalid - " + Integer.toString(XaxisIndex));
					}
					xAxisName = fieldsToChange.get(index);
				}
			}

			List<String> YAxesShownNames = null;
			{
				if (YAxesShownIndices != null) {
					YAxesShownNames = new Vector<String>();
					for (int yindex : YAxesShownIndices) {
						int index = (yindex >= 0) ? yindex : fields.size() + yindex;
						if (index < 0 || index >= fields.size()) {
							throw new IllegalArgumentException("yindex is invalid - " + Integer.toString(yindex));
						}
						YAxesShownNames.add(fields.get(index));
					}
				}
			}

			List<String> YAxesNotShownNames = null;
			{
				if (YAxesNotShownIndices != null) {
					YAxesNotShownNames = new Vector<String>();
					for (int yindex : YAxesNotShownIndices) {
						int index = (yindex >= 0) ? yindex : fields.size() + yindex;
						if (index < 0 || index >= fields.size()) {
							throw new IllegalArgumentException("yindex is invalid - " + Integer.toString(yindex));
						}
						String field = fields.get(index);
						if (YAxesShownNames != null && !YAxesShownNames.contains(field))
							YAxesNotShownNames.add(field);
					}
				}
			}

			ScanPlotSettings settings = new ScanPlotSettings();
			if (xAxisName != null) {
				settings.setXAxisName(xAxisName);
			}
			if (YAxesShownNames != null) {
				settings.setYAxesShown(YAxesShownNames.toArray(new String[0]));
			}
			if (YAxesNotShownNames != null) {
				settings.setYAxesNotShown(YAxesNotShownNames.toArray(new String[0]));
			}
			return settings;
		} catch (Exception e) {
			String msg = "posToChange = ";
			for (String s : fieldsToChange) {
				msg += s + ",";
			}
			msg += "\nposNotToChange = ";
			for (String s : fields) {
				msg += s + ",";
			}
			msg += "\nXaxisIndex = " + XaxisIndex;
			msg += "\nYAxesShownIndices = ";
			if (YAxesShownIndices != null) {
				for (Integer i : YAxesShownIndices) {
					msg += i + ",";
				}
			} else {
				msg += "null";
			}
			msg += "\nYAxesNotShownIndices = ";
			if (YAxesNotShownIndices != null) {
				for (Integer i : YAxesNotShownIndices) {
					msg += i + ",";
				}
			} else {
				msg += "null";
			}
			throw new Exception("Error in createSettings for " + msg, e);
		}
	}

	/**
	 * Method to set the xaxis Name and y axis names 2 mode - 1. Based on order they type them 2. Based on order
	 * executed
	 * 
	 * @param allScannables
	 * @param allDetectors
	 * @param numberOfChildScans
	 * @param XaxisIndex
	 * @param YAxesShownIndices
	 * @param YAxesNotShownIndices
	 * @return ScanPlotSettings
	 * @throws Exception
	 * @throws DeviceException
	 */
	public static ScanPlotSettings createSettingsWithDetector(List<Scannable> allScannables,
			List<Detector> allDetectors, int numberOfChildScans, Integer XaxisIndex, List<Integer> YAxesShownIndices,
			List<Integer> YAxesNotShownIndices) throws Exception {
		int numScannablesToSet = numberOfChildScans + 1;

		List<String> fieldsToChange = new Vector<String>();
		fieldsToChange.addAll(ScannableUtils.getScannableInputFieldNames(allScannables.subList(0, numScannablesToSet)));

		List<String> fields = new Vector<String>();
		fields.addAll(ScannableUtils.getScannableFieldNames(allScannables));
		fields.addAll(ScannableUtils.getDetectorFieldNames(allDetectors));
		return createSettings(fieldsToChange, fields, XaxisIndex, YAxesShownIndices, YAxesNotShownIndices);
	}

	/**
	 * Method to set the xaxis Name and y axis names 2 mode - 1. Based on order they type them 2. Based on order
	 * executed
	 * 
	 * @param userListedScannablesToScan
	 * @param userListedScannablesToRead
	 * @param numberOfChildScans
	 * @param XaxisIndex
	 * @param YAxesShownIndices
	 * @param YAxesNotShownIndices
	 * @return ScanPlotSettings
	 * @throws Exception
	 */
	public static ScanPlotSettings createSettings(List<Scannable> userListedScannablesToScan,
			List<Scannable> userListedScannablesToRead, @SuppressWarnings("unused") int numberOfChildScans,
			Integer XaxisIndex, List<Integer> YAxesShownIndices, List<Integer> YAxesNotShownIndices) throws Exception {

		List<String> fieldsToChange = new Vector<String>();
		fieldsToChange.addAll(ScannableUtils.getScannableInputFieldNames(userListedScannablesToScan));

		List<String> fields = new Vector<String>();
		fields.addAll(ScannableUtils.getScannableFieldNames(userListedScannablesToRead));

		return createSettings(fieldsToChange, fields, XaxisIndex, YAxesShownIndices, YAxesNotShownIndices);
	}

}
