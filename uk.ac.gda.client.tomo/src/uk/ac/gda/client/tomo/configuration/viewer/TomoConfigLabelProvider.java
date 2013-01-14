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
package uk.ac.gda.client.tomo.configuration.viewer;

import java.util.Calendar;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoClientActivator;

public class TomoConfigLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		boolean isChecked = false;
		if (columnIndex == TomoConfigTableConstants.COL_SHOULD_DISPLAY
				|| columnIndex == TomoConfigTableConstants.COL_SELECTION) {
			if (element instanceof TomoConfigContent) {
				TomoConfigContent tc = (TomoConfigContent) element;
				switch (columnIndex) {
				case TomoConfigTableConstants.COL_SHOULD_DISPLAY:
					isChecked = tc.isShouldDisplay();
					break;
				case TomoConfigTableConstants.COL_SELECTION:
					isChecked = tc.isSelectedToRun();
					break;
				}
			}

			if (isChecked) {
				return TomoClientActivator.getDefault().getImageRegistry().get(ImageConstants.ICON_CHECKED_STATE);
			}
			return TomoClientActivator.getDefault().getImageRegistry().get(ImageConstants.ICON_UNCHECKED_STATE);
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof TomoConfigContent) {
			TomoConfigContent configContent = (TomoConfigContent) element;
			if (TomoConfigTableConstants.COL_DRAG == columnIndex) {
				return "::";
			} else if (TomoConfigTableConstants.COL_PROPOSAL == columnIndex) {
				return configContent.getProposalId();
			} else if (TomoConfigTableConstants.COL_SAMPLE_DESCRIPTION == columnIndex) {
				return configContent.getSampleDescription();
			} else if (TomoConfigTableConstants.COL_MODULE == columnIndex) {
				return Double.toString(configContent.getObjectPixelSize());
			} else if (TomoConfigTableConstants.COL_ACQUISITION_TIME == columnIndex) {
				return Double.toString(configContent.getSampleExposureTime());
			} else if (TomoConfigTableConstants.COL_FLAT_ACQ_TIME == columnIndex) {
				return Double.toString(configContent.getFlatExposureTime());
			} else if (TomoConfigTableConstants.COL_DETECTOR_DISTANCE == columnIndex) {
				return Double.toString(configContent.getSampleDetectorDistance());
			} else if (TomoConfigTableConstants.COL_ENERGY == columnIndex) {
				return Double.toString(configContent.getEnergy());
			} else if (TomoConfigTableConstants.COL_SAMPLE_WEIGHT == columnIndex) {
				return configContent.getSampleWeight();
			} else if (TomoConfigTableConstants.COL_RESOLUTION == columnIndex) {
				return configContent.getResolution();
			} else if (TomoConfigTableConstants.COL_FRAMES_PER_PROJECTION == columnIndex) {
				return Integer.toString(configContent.getFramesPerProjection());
			} else if (TomoConfigTableConstants.COL_CONTINUOUS_STEP == columnIndex) {
				return configContent.getScanMode();
			} else if (TomoConfigTableConstants.COL_RUN_TIME == columnIndex) {
				double runTime = configContent.getRunTime();
				int hours = (int) (runTime / 3600); // since both are ints, you get an int
				int minutes = (int) ((runTime / 60) % 60);
				int seconds = (int) (runTime % 60);
				return String.format("%d:%02d:%02d", hours, minutes, seconds);
			} else if (TomoConfigTableConstants.COL_EST_END_TIME == columnIndex) {
				double estEndTime = configContent.getEstEndTime();
				if (estEndTime == 0) {
					return "";
				}
				Calendar now = Calendar.getInstance();
				now.add(Calendar.SECOND, (int) estEndTime);
				return String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
			} else if (TomoConfigTableConstants.COL_TIME_DIVIDER == columnIndex) {
				return Double.toString(configContent.getTimeDivider());
			} else if (TomoConfigTableConstants.COL_PROGRESS == columnIndex) {
				return Double.toString(configContent.getProgress()) + " %";
			} else if (TomoConfigTableConstants.COL_ADDITIONAL == columnIndex) {
				return "Click...";
			}
		}

		if (element instanceof String) {
			if (columnIndex == 1)
				return (String) element;
		}

		return "";
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}

	private static final Color COLOR_COMPLETE = ColorConstants.cyan;
	private static final Color COLOR_RUNNING = ColorConstants.green;
	private static final Color COLOR_NONE = null;
	private static final Color COLOR_FAIL = ColorConstants.red;
	private static final Color COLOR_STARTING = ColorConstants.lightGreen;

	@Override
	public Color getBackground(Object element, int columnIndex) {
		if (columnIndex == TomoConfigTableConstants.COL_PROGRESS) {
			if (element instanceof TomoConfigContent) {
				switch (((TomoConfigContent) element).getStatus()) {
				case STARTING:
					return COLOR_STARTING;
				case COMPLETE:
					return COLOR_COMPLETE;
				case RUNNING:
					return COLOR_RUNNING;
				case NONE:
					return COLOR_NONE;
				case FAIL:
					return COLOR_FAIL;
				}
			}

		}
		return null;
	}

}
