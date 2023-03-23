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

import java.util.ArrayList;
import java.util.List;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

public abstract class ScanPlotSettingsUtils {
	/**
	 * Returns a ScanPlotSettings objects. If any of XaxisIndex, YAxesShownIndices or YAxesNotShownIndices are Null, the
	 * corresponding value in the resulting ScanPlotSettings will also be null.
	 *
	 * @param fieldsToChange
	 *            Input fields (only) of scannables to be scanned over
	 * @param fields
	 *            Input and Extra fields of all scannables (scanned over, moved, or read)
	 * @param xAxisIndex
	 * @param yAxesShownIndices
	 * @param yAxesNotShownIndices
	 * @return ScanPlotSettings
	 * @throws Exception
	 */
	public static ScanPlotSettings createSettings(List<String> fieldsToChange, List<String> fields, Integer xAxisIndex,
			List<Integer> yAxesShownIndices, List<Integer> yAxesNotShownIndices) throws Exception {
		try {
			String xAxisName = null;
			if (xAxisIndex != null) {
				int index = (xAxisIndex >= 0) ? xAxisIndex : fieldsToChange.size() + xAxisIndex;
				if (index < 0 || index >= fieldsToChange.size()) {
					throw new IllegalArgumentException("XaxisIndex is invalid - " + Integer.toString(xAxisIndex));
				}
				xAxisName = fieldsToChange.get(index);
			}

			List<String> yAxesShownNames = null;
			if (yAxesShownIndices != null) {
				yAxesShownNames = new ArrayList<>();
				for (int yindex : yAxesShownIndices) {
					int index = (yindex >= 0) ? yindex : fields.size() + yindex;
					if (index < 0 || index >= fields.size()) {
						throw new IllegalArgumentException("yindex is invalid - " + Integer.toString(yindex));
					}
					yAxesShownNames.add(fields.get(index));
				}
			}

			List<String> yAxesNotShownNames = null;
			if (yAxesNotShownIndices != null) {
				yAxesNotShownNames = new ArrayList<>();
				for (int yindex : yAxesNotShownIndices) {
					int index = (yindex >= 0) ? yindex : fields.size() + yindex;
					if (index < 0 || index >= fields.size()) {
						throw new IllegalArgumentException("yindex is invalid - " + Integer.toString(yindex));
					}
					String field = fields.get(index);
					if (yAxesShownNames != null && !yAxesShownNames.contains(field))
						yAxesNotShownNames.add(field);
				}
			}


			ScanPlotSettings settings = new ScanPlotSettings();
			if (xAxisName != null) {
				settings.setXAxisName(xAxisName);
			}
			if (yAxesShownNames != null) {
				settings.setYAxesShown(yAxesShownNames.toArray(new String[0]));
			}
			if (yAxesNotShownNames != null) {
				settings.setYAxesNotShown(yAxesNotShownNames.toArray(new String[0]));
			}
			return settings;
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder("posToChange = ");
			for (String s : fieldsToChange) {
				msg.append(s);
				msg.append(",");
			}
			msg.append("\nposNotToChange = ");
			for (String s : fields) {
				msg.append(s);
				msg.append(",");
			}
			msg.append("\nXaxisIndex = ");
			msg.append(xAxisIndex);
			msg.append("\nYAxesShownIndices = ");
			if (yAxesShownIndices != null) {
				for (Integer i : yAxesShownIndices) {
					msg.append(i);
					msg.append(",");
				}
			} else {
				msg.append("null");
			}
			msg.append("\nYAxesNotShownIndices = ");
			if (yAxesNotShownIndices != null) {
				for (Integer i : yAxesNotShownIndices) {
					msg.append(i);
					msg.append(",");
				}
			} else {
				msg.append("null");
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

		List<String> fieldsToChange = new ArrayList<>();
		fieldsToChange.addAll(ScannableUtils.getScannableInputFieldNames(allScannables.subList(0, numScannablesToSet)));

		List<String> fields = new ArrayList<>();
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
	 * @param xAxisIndex
	 * @param yAxesShownIndices
	 * @param yAxesNotShownIndices
	 * @return ScanPlotSettings
	 * @throws Exception
	 */
	public static ScanPlotSettings createSettings(List<Scannable> userListedScannablesToScan,
			List<Scannable> userListedScannablesToRead, @SuppressWarnings("unused") int numberOfChildScans,
			Integer xAxisIndex, List<Integer> yAxesShownIndices, List<Integer> yAxesNotShownIndices) throws Exception {

		final List<String> fieldsToChange = new ArrayList<>();
		fieldsToChange.addAll(ScannableUtils.getScannableInputFieldNames(userListedScannablesToScan));

		final List<String> fields = new ArrayList<>();
		fields.addAll(ScannableUtils.getScannableFieldNames(userListedScannablesToRead));

		return createSettings(fieldsToChange, fields, xAxisIndex, yAxesShownIndices, yAxesNotShownIndices);
	}

}
